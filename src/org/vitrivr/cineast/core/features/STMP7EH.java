package org.vitrivr.cineast.core.features;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.config.ReadableQueryConfig;
import org.vitrivr.cineast.core.data.FloatVectorImpl;
import org.vitrivr.cineast.core.data.MultiImage;
import org.vitrivr.cineast.core.data.StatElement;
import org.vitrivr.cineast.core.data.StringDoublePair;
import org.vitrivr.cineast.core.data.frames.VideoFrame;
import org.vitrivr.cineast.core.data.segments.SegmentContainer;
import org.vitrivr.cineast.core.db.PersistencyWriterSupplier;

public class STMP7EH extends EHD {

	private static final Logger LOGGER = LogManager.getLogger();

	@Override
	public void init(PersistencyWriterSupplier supply) {
		this.phandler = supply.get();
		this.phandler.open("features_STMP7EH");
	}
	
	@Override
	public void processShot(SegmentContainer shot) {
		LOGGER.traceEntry();
		if (!phandler.idExists(shot.getId())) {
			List<VideoFrame> videoFrames = shot.getVideoFrames();
			float[] hist = new float[80];
			StatElement[] stats = new StatElement[80];
			for(int i = 0; i < 80; ++i){
				stats[i] = new StatElement();
			}
			for(VideoFrame f : videoFrames){
				MultiImage img = f.getImage();
				
				hist = process(img, hist);
				for(int i = 0; i < 80; ++i){
					stats[i].add(hist[i]);
					hist[i] = 0f;
				}
			}
			float[] vec = new float[160];
			for(int i = 0; i < 80; ++i){
				vec[i] = stats[i].getAvg();
				vec[i + 80] = (float) Math.sqrt(stats[i].getVariance());
			}
			persist(shot.getId(), new FloatVectorImpl(vec));
		}
		LOGGER.traceExit();
	}

	@Override
	public List<StringDoublePair> getSimilar(SegmentContainer sc, ReadableQueryConfig qc) {
		// TODO Auto-generated method stub
		return super.getSimilar(sc, qc);
	}

	@Override
	public List<StringDoublePair> getSimilar(String shotId, ReadableQueryConfig qc) {
		// TODO Auto-generated method stub
		return super.getSimilar(shotId, qc);
	}
	

}
