package org.vitrivr.cineast.core.segmenter;

import org.vitrivr.cineast.core.data.Frame;
import org.vitrivr.cineast.core.data.Histogram;
import org.vitrivr.cineast.core.data.MediaType;
import org.vitrivr.cineast.core.data.Shot;
import org.vitrivr.cineast.core.data.providers.ShotProvider;
import org.vitrivr.cineast.core.db.PersistencyWriter;
import org.vitrivr.cineast.core.db.PersistentTuple;
import org.vitrivr.cineast.core.db.SegmentLookup.SegmentDescriptor;
import org.vitrivr.cineast.core.decode.subtitle.SubTitle;
import org.vitrivr.cineast.core.decode.subtitle.SubtitleItem;
import org.vitrivr.cineast.core.decode.video.VideoDecoder;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class ShotSegmenter implements ShotProvider{
	
	private static final double THRESHOLD = 0.05;
	private static final int PRE_QUEUE_LEN = 10;
	private static final int MAX_SHOT_LENGTH = 720;

	private VideoDecoder vdecoder;
	private final String movieId;
	private LinkedList<Frame> frameQueue = new LinkedList<>();
	private LinkedList<DoublePair<Frame>> preShotQueue = new LinkedList<>();
	private ArrayList<SubTitle> subtitles = new ArrayList<SubTitle>();
	@SuppressWarnings("rawtypes")
	private PersistencyWriter pwriter;
	private List<SegmentDescriptor> knownShotBoundaries;
	
	public ShotSegmenter(VideoDecoder vdecoder, String movieId, @SuppressWarnings("rawtypes") PersistencyWriter pwriter, List<SegmentDescriptor> knownShotBoundaries){
		this.vdecoder = vdecoder;
		this.movieId = movieId;
		this.pwriter = pwriter;
		this.pwriter.setFieldNames("id", "multimediaobject", "sequencenumber", "segmentstart", "segmentend");
		this.pwriter.open("cineast_segment");
		this.knownShotBoundaries = ((knownShotBoundaries == null) ? new LinkedList<SegmentDescriptor>() : knownShotBoundaries);
	}
	
	public void addSubTitle(SubTitle st) {
		this.subtitles.add(st);
	}
	
	private boolean queueFrames(){
		return queueFrames(20);
	}
	
	private boolean queueFrames(int number){
		Frame f;
		for(int i = 0; i < number; ++i){
			f = this.vdecoder.getFrame();
			if(f == null){ //no more frames
				return false;
			}else{
				this.frameQueue.offer(f);
			}
		}
		return true;
	}
	
	
	
	public Shot getNextShot(){
		if(this.frameQueue.isEmpty()){
			queueFrames();
		}
		
		Shot _return = null;
		
		if (!preShotQueue.isEmpty()){
			_return = new Shot(this.movieId, this.vdecoder.getTotalFrameCount());
			while (!preShotQueue.isEmpty()) {
				_return.addFrame(preShotQueue.removeFirst().first);
			}
		}
		if(this.frameQueue.isEmpty()){
			return finishShot(_return); //no more shots to segment
		}
		
		if(_return == null){
			_return = new Shot(this.movieId, this.vdecoder.getTotalFrameCount());
		}
		
		
		Frame frame = this.frameQueue.poll();
		
		SegmentDescriptor bounds = this.knownShotBoundaries.size() > 0 ? this.knownShotBoundaries.remove(0) : null;
		
		if (bounds != null && frame.getId() >= bounds.getStartFrame() && frame.getId() <= bounds.getEndFrame()){
			
			_return.addFrame(frame);
			queueFrames(bounds.getEndFrame() - bounds.getStartFrame());
			do{
				frame = this.frameQueue.poll();
				if(frame != null){
					_return.addFrame(frame);
				}else{
					break;
				}
				
			}while(frame.getId() < bounds.getEndFrame());
			
			_return.setShotId(bounds.getSegmentId());
			addSubtitleItems(_return);
			
			idCounter.incrementAndGet();
			
			return _return;
			
		}else{
			Histogram hPrev, h = getHistogram(frame);
			_return.addFrame(frame);
			while (true) {
				if ((frame = this.frameQueue.poll()) == null) {
					queueFrames();
					if ((frame = this.frameQueue.poll()) == null) {
						return finishShot(_return);
					}
				}
				hPrev = h;
				h = getHistogram(frame);
				double distance = hPrev.getDistance(h);

				preShotQueue.offer(new DoublePair<Frame>(frame, distance));

				if (preShotQueue.size() > PRE_QUEUE_LEN) {
					double max = 0;
					int index = -1, i = 0;
					for (DoublePair<Frame> pair : preShotQueue) {
						if (pair.second > max) {
							index = i;
							max = pair.second;
						}
						i++;
					}
					if (max <= THRESHOLD && _return.getNumberOfFrames() < MAX_SHOT_LENGTH) { //no cut
						for (DoublePair<Frame> pair : preShotQueue) {
							_return.addFrame(pair.first);
						}
						preShotQueue.clear();
					} else {
						for (i = 0; i < index; ++i) {
							_return.addFrame(preShotQueue.removeFirst().first);
						}
						break;
					}
				}
			}
			return finishShot(_return);
		}
	}
	
	private static Histogram getHistogram(Frame f){
		return FuzzyColorHistogramCalculator.getSubdividedHistogramNormalized(f.getImage().getThumbnailImage(), 3);
	}

	private AtomicInteger idCounter = new AtomicInteger(0);
	
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Shot finishShot(Shot shot){
		
		if(shot == null){
			return null;
		}
		
		int shotNumber = idCounter.incrementAndGet();
		String shotId = MediaType.generateId(MediaType.VIDEO, movieId, shotNumber);
		
		shot.setShotId(shotId);
		addSubtitleItems(shot);
		
		
		PersistentTuple tuple = this.pwriter.generateTuple(shotId, movieId, shotNumber, shot.getStart(), shot.getEnd());
		this.pwriter.persist(tuple);
		
		return shot;
	}
	
	private void addSubtitleItems(Shot shot){
		int start = shot.getStart();
		int end = shot.getEnd();
		for(SubTitle st : this.subtitles){
			for(int i = 1; i <= st.getNumerOfItems(); ++i){
				SubtitleItem si = st.getItem(i);
				if(si == null || start > si.getEndFrame() || end < si.getStartFrame()){
					continue;
				}
				shot.addSubtitleItem(si);
			}
		}
	}

	@Override
	public void close() {
		this.pwriter.close();
		this.vdecoder.close();
	}
	
}

class DoublePair<K>{
	K first;
	double second;
	
	DoublePair(K first, double second){
		this.first = first;
		this.second = second;
	}
}