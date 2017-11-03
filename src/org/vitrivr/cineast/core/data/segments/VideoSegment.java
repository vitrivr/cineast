package org.vitrivr.cineast.core.data.segments;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.data.MultiImage;
import org.vitrivr.cineast.core.data.Pair;
import org.vitrivr.cineast.core.data.frames.AudioDescriptor;
import org.vitrivr.cineast.core.data.frames.AudioFrame;
import org.vitrivr.cineast.core.data.frames.VideoDescriptor;
import org.vitrivr.cineast.core.data.frames.VideoFrame;
import org.vitrivr.cineast.core.data.tag.Tag;
import org.vitrivr.cineast.core.decode.subtitle.SubtitleItem;
import org.vitrivr.cineast.core.descriptor.AvgImg;
import org.vitrivr.cineast.core.descriptor.MedianImg;
import org.vitrivr.cineast.core.descriptor.MostRepresentative;
import org.vitrivr.cineast.core.descriptor.PathList;
import org.vitrivr.cineast.core.util.dsp.fft.STFT;
import org.vitrivr.cineast.core.util.dsp.fft.windows.WindowFunction;

import boofcv.struct.geo.AssociatedPair;
import georegression.struct.point.Point2D_F32;

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
	private ArrayList<Tag> tags = new ArrayList<>(1);
	private String movieId;
	private String shotId;

    /** Total number of samples in the AudioSegment. */
    private int totalSamples;

    /** Total duration of the AudioSegment in seconds. */
    private float totalAudioDuration;

	/** AudioDescriptor for the audio stream in this VideoSegment. */
	private AudioDescriptor audioDescriptor = null;

	/** VideoDescriptor for the video stream in this VideoSegment. */
	private VideoDescriptor videoDescriptor = null;

	/**
	 *
	 */
	public VideoSegment() {

	}

	/**
	 *
	 * @param movieId
	 */
	public VideoSegment(String movieId){
		this.movieId = movieId;
	}

	/**
	 *
	 * @return
	 */
	public int getNumberOfFrames(){
		return this.videoFrames.size();
	}


    /**
     *
     * @return
     */
	@Override
  public List<VideoFrame> getVideoFrames() {
	    return Collections.unmodifiableList(this.videoFrames);
	}

    /**
     *
     * @return
     */
	@Override
  public List<AudioFrame> getAudioFrames() {
	    return Collections.unmodifiableList(this.audioFrames);
    }

    /**
     * Adds a VideoFrame to the current VideoSegment. If the VideoFrame contains
     * audio, that audio is added too.
     *
     * @param frame VideoFrame to add to the container.
     */
	public boolean addVideoFrame(VideoFrame frame){
		if (frame == null) {
      return false;
    }
		if (this.videoDescriptor == null) {
      this.videoDescriptor = frame.getDescriptor();
    }
		if (!this.videoDescriptor.equals(frame.getDescriptor())) {
      return false;
    }

        this.videoFrames.add(frame);
        if (frame.hasAudio()) {
          this.addAudioFrame(frame.getAudio());
        }
        return true;
	}

    /**
     * Adds an AudioFrame to the collection of frames and thereby increases both
     * the number of frames and the duration of the segment.
     *
     * @param frame AudioFrame to add.
     * @return boolean True if frame was added, false otherwise.
     */
    public boolean addAudioFrame(AudioFrame frame) {
        if (frame == null) {
          return false;
        }
        if (this.audioDescriptor == null) {
          this.audioDescriptor = frame.getDescriptor();
        }
        if (!this.audioDescriptor.equals(frame.getDescriptor())) {
          return false;
        }

        this.totalSamples += frame.numberOfSamples();
        this.totalAudioDuration += frame.getDuration();
        this.audioFrames.add(frame);

        return true;
    }

    /**
     * Getter for the total number of samples in the AudioSegment.
     *
     * @return
     */
    @Override
    public int getNumberOfSamples() {
        return this.totalSamples;
    }

    /**
     * Getter for the total duration of the AudioSegment.
     *
     * @return
     */
    @Override
    public float getAudioDuration() {
        return totalAudioDuration;
    }

    /**
     * Getter for samplingrate of the AudioSegment.
	 *
     * @return
     */
    @Override
    public float getSamplingrate() {
        return this.audioDescriptor.getSamplingrate();
    }

    /**
     *
     * @return
     */
    @Override
    public int getChannels() {
        return this.audioDescriptor.getChannels();
    }

    /**
     * Calculates and returns the Short-term Fourier Transform of the audio in the
     * current VideoSegment.
     *
     * @param windowsize Size of the window used during STFT. Must be a power of two.
     * @param overlap Overlap in samples between two subsequent windows.
	 * @param padding Zero-padding before and after the actual sample data. Causes the window to contain (windowsize-2*padding) data-points..
	 * @param function WindowFunction to apply before calculating the STFT.
     *
     * @return STFT of the audio in the current VideoSegment or null if the segment has no audio.
     */
    @Override
    public STFT getSTFT(int windowsize, int overlap, int padding, WindowFunction function) {
		if (2*padding >= windowsize) {
      throw new IllegalArgumentException("The combined padding must be smaller than the sample window.");
    }
		STFT stft = new STFT(windowsize, overlap, padding, function, this.audioDescriptor.getSamplingrate());
		stft.forward(this.getMeanSamplesAsDouble());
		return stft;
    }

	public void addSubtitleItem(SubtitleItem si){
		this.subItems.add(si);
	}
	
	private Object getAvgLock = new Object();
	@Override
  public MultiImage getAvgImg(){
		synchronized (getAvgLock) {
			if(avgImg == null){
				avgImg = AvgImg.getAvg(videoFrames);
			}
			return avgImg;
		}
	}
	
	private Object getMedianLock = new Object();
	@Override
  public MultiImage getMedianImg(){
		synchronized (getMedianLock) {
			if(this.medianImg == null){
				this.medianImg = MedianImg.getMedian(videoFrames);
		}
		return this.medianImg;
		}
	}
	
	private Object getPathsLock = new Object();
	@Override
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
	
	@Override
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
	@Override
  public VideoFrame getMostRepresentativeFrame(){
		synchronized (getMostRepresentativeLock) {
			if(this.mostRepresentative == null){
				this.mostRepresentative = MostRepresentative.getMostRepresentative(this);
			}
			return this.mostRepresentative;
		}
	}
	
	@Override
  public String getId(){
		return this.shotId;
	}
	
	@Override
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

	/**
	 * Returns the frame-number of the first frame in the segment (relative to the entire stream).
	 *
	 * @return
	 */
	@Override
	public int getStart(){
		if (!this.videoFrames.isEmpty()) {
			return this.videoFrames.get(0).getId();
		} else {
			return 0;
		}
	}

	/**
	 * Returns the frame-number of the last frame in the segment (relative to the entire stream).
	 *
	 * @return
	 */
	@Override
	public int getEnd(){
		if (!this.videoFrames.isEmpty()) {
			return this.videoFrames.get(this.videoFrames.size()-1).getId();
		} else {
			return 0;
		}
	}

	/**
	 * Returns the relative start of the VideoSegment in percent (relative to the entire stream).
	 *
	 * @return
	 */
	@Override
	public float getRelativeStart() {
		return (1000.0f * this.getStart()) / this.videoDescriptor.getDuration();
	}

	/**
	 * Returns the relative end of the VideoSegment in percent (relative to the entire stream).
	 *
	 * @return
	 */
	@Override
	public float getRelativeEnd() {
		return (1000.0f * this.getEnd()) / this.videoDescriptor.getDuration();
	}

	/**
	 * Returns the absolute start of the VideoSegment in seconds (relative to the entire stream).
	 *
	 * @return
	 */
	@Override
	public float getAbsoluteStart() {
		if (!this.videoFrames.isEmpty()) {
			return this.videoFrames.get(0).getTimestampSeconds();
		} else {
			return 0;
		}
	}

	/**
	 * Returns the absolute end of the VideoSegment in seconds (relative to the entire stream).
	 *
	 * @return
	 */
	@Override
	public float getAbsoluteEnd() {
		if (!this.videoFrames.isEmpty()) {
			return this.videoFrames.get(this.videoFrames.size()-1).getTimestampSeconds();
		} else {
			return 0;
		}
	}
	
	@Override
	public List<Tag> getTags() {
		return this.tags;
	}

    @Override
    public String toString() {
        return "Shot id: " + this.shotId;
    }
	
}
