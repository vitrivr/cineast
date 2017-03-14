package org.vitrivr.cineast.core.data.segments;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.data.frames.AudioFrame;
import org.vitrivr.cineast.core.data.frames.VideoFrame;
import org.vitrivr.cineast.core.data.MultiImage;
import org.vitrivr.cineast.core.data.Pair;
import org.vitrivr.cineast.core.decode.subtitle.SubtitleItem;
import org.vitrivr.cineast.core.descriptor.AvgImg;
import org.vitrivr.cineast.core.descriptor.MedianImg;
import org.vitrivr.cineast.core.descriptor.MostRepresentative;
import org.vitrivr.cineast.core.descriptor.PathList;

import boofcv.struct.geo.AssociatedPair;
import georegression.struct.point.Point2D_F32;
import org.vitrivr.cineast.core.util.fft.STFT;
import org.vitrivr.cineast.core.util.fft.windows.WindowFunction;

public class VideoSegment implements SegmentContainer {

	private static final Logger LOGGER = LogManager.getLogger();
	
	private LinkedList<VideoFrame> videoFrames = new LinkedList<>();
	private LinkedList<AudioFrame> audioFrames = new LinkedList<>();
    private final LinkedList<SubtitleItem> subItems = new LinkedList<>();
    private MultiImage avgImg = null, medianImg = null;
	private VideoFrame mostRepresentative = null;
	private List<Pair<Integer, LinkedList<Point2D_F32>>> paths = null;
	private List<Pair<Integer, LinkedList<Point2D_F32>>> bgPaths = null;
	private LinkedList<Pair<Integer,ArrayList<AssociatedPair>>> allPaths = null;
	private ArrayList<String> tags = new ArrayList<>(1);
	private String movieId;
	private String shotId;

	private final int movieFrameCount;

    /** Total number of samples in the AudioSegment. */
    private int totalSamples;

    /** Total duration of the AudioSegment in seconds. */
    private float totalDuration;

    /** Sample rate of the AudioSegment. Determined by the sample rate of the first AudioFrame. */
    private Float samplerate;

    /** Number of channels in the AudioSegment. Determined by the number of channels in the first AudioFrame. */
    private Integer channels;

	/**
	 *
	 * @param movieFrameCount
	 */
	public VideoSegment(int movieFrameCount) {
		this.movieFrameCount = movieFrameCount;
	}

	/**
	 *
	 * @param movieId
	 * @param movieFrameCount
	 */
	public VideoSegment(String movieId, int movieFrameCount){
		this.movieId = movieId;
		this.movieFrameCount = movieFrameCount;
	}
	
	public int getNumberOfFrames(){
		return this.videoFrames.size();
	}


    /**
     *
     * @return
     */
	public List<VideoFrame> getVideoFrames() {
	    return Collections.unmodifiableList(this.videoFrames);
	}

    /**
     *
     * @return
     */
	public List<AudioFrame> getAudioFrames() {
	    return Collections.unmodifiableList(this.audioFrames);
    }

    /**
     * Adds a VideoFrame to the current VideoSegment. If the VideoFrame contains
     * audio, that audio is added too.
     *
     * @param f VideoFrame to add to the container.
     */
	public void addFrame(VideoFrame f){
        this.videoFrames.add(f);
        if (f.hasAudio()) this.addAudioFrame(f.getAudio());
	}

    /**
     * Adds an AudioFrame to the collection of frames and thereby increases both
     * the number of frames and the duration of the segment.
     *
     * @param frame AudioFrame to add.
     * @return boolean True if frame was added, false otherwise.
     */
    public boolean addAudioFrame(AudioFrame frame) {
        if (frame == null) return false;
        if (this.channels == null) this.channels = frame.getChannels();
        if (this.samplerate == null) this.samplerate = frame.getSampleRate();

        if (this.channels != frame.getChannels() || this.samplerate != frame.getSampleRate()) {
            return false;
        }

        this.totalSamples += frame.numberOfSamples();
        this.totalDuration += frame.getDuration();
        this.audioFrames.add(frame);

        return true;
    }

    /**
     * Getter for the total number of samples in the AudioSegment.
     *
     * @return
     */
    public int getNumberOfSamples() {
        return this.totalSamples;
    }

    /**
     * Getter for the total duration of the AudioSegment.
     *
     * @return
     */
    public float getAudioDuration() {
        return totalDuration;
    }

    /**
     *
     * @return
     */
    public float getSampleRate() {
        return this.samplerate;
    }

    /**
     *
     * @return
     */
    public int getChannels() {
        return this.channels;
    }


    /**
     * Calculates and returns the Short-term Fourier Transform of the audio in the
     * current VideoSegment.
     *
     * @param windowsize Size of the window used during STFT. Must be a power of two.
     * @param overlap Overlap in samples between two subsequent windows.
     * @param function WindowFunction to apply before calculating the STFT.
     *
     * @return STFT of the audio in the current VideoSegment or null if the segment has no audio.
     */
    @Override
    public STFT getSTFT(int windowsize, int overlap, WindowFunction function) {
        if (this.getNumberOfSamples() >= windowsize) {
            STFT stft = new STFT(this.getMeanSamplesAsDouble(), this.samplerate);
            stft.forward(windowsize, overlap, function);
            return stft;
        } else {
            return null;
        }
    }

	public void addSubtitleItem(SubtitleItem si){
		this.subItems.add(si);
	}
	
	private Object getAvgLock = new Object();
	public MultiImage getAvgImg(){
		synchronized (getAvgLock) {
			if(avgImg == null){
				avgImg = AvgImg.getAvg(videoFrames);
			}
			return avgImg;
		}
	}
	
	private Object getMedianLock = new Object();
	public MultiImage getMedianImg(){
		synchronized (getMedianLock) {
			if(this.medianImg == null){
				this.medianImg = MedianImg.getMedian(videoFrames);
		}
		return this.medianImg;
		}
	}
	
	private Object getPathsLock = new Object();
	public List<Pair<Integer, LinkedList<Point2D_F32>>> getPaths() {
		synchronized (getPathsLock) {
			if(this.paths == null){
				this.allPaths = PathList.getDensePaths(videoFrames);
				this.paths = new ArrayList<Pair<Integer, LinkedList<Point2D_F32>>>();
				this.bgPaths = new ArrayList<Pair<Integer, LinkedList<Point2D_F32>>>();
				PathList.separateFgBgPaths(videoFrames, this.allPaths, this.paths, this.bgPaths);
			}
		}
		return this.paths;
	}
	
	public List<Pair<Integer, LinkedList<Point2D_F32>>> getBgPaths() {
		synchronized (getPathsLock) {
			if(this.bgPaths == null){
				this.allPaths = PathList.getDensePaths(videoFrames);
				this.paths = new ArrayList<Pair<Integer, LinkedList<Point2D_F32>>>();
				this.bgPaths = new ArrayList<Pair<Integer, LinkedList<Point2D_F32>>>();
				PathList.separateFgBgPaths(videoFrames, this.allPaths, this.paths, this.bgPaths);
			}
		}
		return this.bgPaths;
	}

    /**
     *
     */
	public void clear(){
		LOGGER.trace("clear shot {}", shotId);
		for(VideoFrame f : videoFrames){
			f.clear();
		}
		videoFrames.clear();
		subItems.clear();
		this.videoFrames = null;
		this.audioFrames = null;
		if(avgImg != null){
			this.avgImg.clear();
			this.avgImg = null;
		}
		if(medianImg != null){
			this.medianImg.clear();
			this.medianImg = null;
		}
		if(this.paths != null){
			this.paths.clear();
			this.paths = null;
		}
		
		this.mostRepresentative = null;
	}

	private Object getMostRepresentativeLock = new Object();
	public VideoFrame getMostRepresentativeFrame(){
		synchronized (getMostRepresentativeLock) {
			if(this.mostRepresentative == null){
				this.mostRepresentative = MostRepresentative.getMostRepresentative(this);
			}
			return this.mostRepresentative;
		}
	}

	public int getStart(){
		if (!this.videoFrames.isEmpty()) {
			return this.videoFrames.get(0).getId();
		} else {
			return 0;
		}
	}
	
	public int getEnd(){
		if (!this.videoFrames.isEmpty()) {
			return this.videoFrames.get(this.videoFrames.size()-1).getId();
		} else {
			return 0;
		}
	}
	
	public String getId(){
		return this.shotId;
	}
	
	public String getSuperId(){
		return this.movieId;
	}

	/**
	 * @param id
	 * @return a unique id of this
	 */
	@Override
	public void setId(String id) {
		this.shotId = id;
	}

	/**
	 * @param id
	 */
	@Override
	public void setSuperId(String id) {
		this.movieId = id;
	}

	@Override
	protected void finalize() throws Throwable {
		clear();
		super.finalize();
	}

	@Override
	public List<SubtitleItem> getSubtitleItems() {
		return this.subItems;
	}

	@Override
	public float getRelativeStart() {
		return (getStart() / (float)this.movieFrameCount);
	}

	@Override
	public float getRelativeEnd() {
		return (getEnd() / (float)this.movieFrameCount);
	}
	
	@Override
	public List<String> getTags() {
		return this.tags;
	}

    @Override
    public String toString() {
        return "Shot id: " + this.shotId;
    }
	
}
