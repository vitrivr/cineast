package org.vitrivr.cineast.core.extraction.segmenter.video;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.data.Histogram;
import org.vitrivr.cineast.core.data.Pair;
import org.vitrivr.cineast.core.data.entities.MediaObjectDescriptor;
import org.vitrivr.cineast.core.data.entities.MediaSegmentDescriptor;
import org.vitrivr.cineast.core.data.frames.VideoFrame;
import org.vitrivr.cineast.core.data.segments.SegmentContainer;
import org.vitrivr.cineast.core.data.segments.VideoSegment;
import org.vitrivr.cineast.core.db.dao.reader.MediaSegmentReader;
import org.vitrivr.cineast.core.extraction.ExtractionContextProvider;
import org.vitrivr.cineast.core.extraction.decode.general.Decoder;
import org.vitrivr.cineast.core.extraction.segmenter.FuzzyColorHistogramCalculator;
import org.vitrivr.cineast.core.extraction.segmenter.general.Segmenter;
import org.vitrivr.cineast.core.util.ReflectionHelper;

import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;


public class VideoHistogramSegmenter implements Segmenter<VideoFrame> {
    /** */
    private static final Logger LOGGER = LogManager.getLogger();

    private static final double DEFAULT_THRESHOLD = 0.05;

    private static final int SEGMENT_QUEUE_LENGTH = 10;

    private static final int PRESHOT_QUEUE_LENGTH = 10;

    private static final int DEFAULT_MAX_SHOT_LENGTH = 720;

    private static final int SEGMENT_POLLING_TIMEOUT = 1000;

    private final double threshold;

    private final int maxShotLength;

    private Decoder<VideoFrame> decoder;

    private final LinkedList<VideoFrame> videoFrameList = new LinkedList<>();

    private final LinkedList<Pair<VideoFrame,Double>> preShotList = new LinkedList<>();

    private final LinkedBlockingQueue<SegmentContainer> segments = new LinkedBlockingQueue<>(SEGMENT_QUEUE_LENGTH);

    private final List<MediaSegmentDescriptor> knownShotBoundaries = new LinkedList<>();

    private volatile boolean complete = false;

    private volatile boolean isrunning = false;

    /** MediaSegmentReader used to lookup existing SegmentDescriptors during the extraction. */
    private final MediaSegmentReader segmentReader;


    /**
     * Constructor required for instantiates through {@link ReflectionHelper}.
     */
    public VideoHistogramSegmenter(ExtractionContextProvider context) {
        this(context, new HashMap<>(0));
    }

    /**
     * Constructor required for instantiates through {@link ReflectionHelper}.
     */
    public VideoHistogramSegmenter(ExtractionContextProvider context, Map<String,String> parameters) {
        if (parameters.containsKey("threshold")) {
            this.threshold = Double.parseDouble(parameters.get("threshold"));
        } else {
            this.threshold = DEFAULT_THRESHOLD;
        }
        if (parameters.containsKey("maxShotLength")) {
            this.maxShotLength = Integer.parseInt(parameters.get("maxShotLength"));
        } else {
            this.maxShotLength = DEFAULT_MAX_SHOT_LENGTH;
        }
        this.segmentReader = new MediaSegmentReader(context.persistencyReader().get());
    }

    private static Histogram getHistogram(VideoFrame f){
        return FuzzyColorHistogramCalculator.getSubdividedHistogramNormalized(f.getImage().getThumbnailImage(), 3);
    }

    /**
     * Method used to initialize the Segmenter - assigns the new Decoder instance and clears
     * all the queues.
     *
     * @param object Media object that is about to be segmented.
     */
    @Override
    public synchronized void init(Decoder<VideoFrame> decoder, MediaObjectDescriptor object) {
        if (!this.isrunning) {
            this.decoder = decoder;
            this.complete = false;
            this.preShotList.clear();
            this.segments.clear();
            this.videoFrameList.clear();
            this.knownShotBoundaries.clear();
            this.knownShotBoundaries.addAll(this.segmentReader.lookUpSegmentsOfObject(object.getObjectId()));
            this.knownShotBoundaries.sort(Comparator.comparingInt(MediaSegmentDescriptor::getSequenceNumber));
        }
    }

    @Override
    public SegmentContainer getNext() throws InterruptedException {
        SegmentContainer nextContainer = this.segments.poll(SEGMENT_POLLING_TIMEOUT, TimeUnit.MILLISECONDS);
        if (nextContainer == null) {
            synchronized (this) {
                this.complete = !this.isrunning && this.decoder.complete();
            }
        }
        return nextContainer;
    }

    /**
     * Indicates whether the Segmenter is complete i.e. no new segments
     * are to be expected.
     */
    @Override
    public synchronized boolean complete() {
        return this.complete;
    }

    /**
     *
     */
    @Override
    public synchronized void close() {
        if (!this.isrunning) {
            if(this.decoder != null){
                this.decoder.close();
            }
        }
    }

    /**
     * When an object implementing interface <code>Runnable</code> is used
     * to create a thread, starting the thread causes the object's
     * <code>run</code> method to be called in that separately executing
     * thread.
     * <p>
     * The general contract of the method <code>run</code> is that it may
     * take any action whatsoever.
     *
     * @see Thread#run()
     */
    @Override
    public void run() {
        /* Begin: Set running to false. */
        synchronized(this) {
            this.isrunning = true;
        }

        try {
            while (!this.decoder.complete()) {
                if (this.videoFrameList.isEmpty()) {
                  queueFrames();
                }

                VideoSegment _return = null;

                if (!preShotList.isEmpty()) {
                    _return = new VideoSegment();
                    while (!preShotList.isEmpty()) {
                        _return.addVideoFrame(preShotList.removeFirst().first);
                    }
                }

                if (this.videoFrameList.isEmpty()) {
                    this.segments.put(_return);
                    continue; //no more shots to segment
                }

                if (_return == null) {
                    _return = new VideoSegment();
                }


                VideoFrame videoFrame = this.videoFrameList.poll();

                MediaSegmentDescriptor bounds = this.knownShotBoundaries.size() > 0 ? this.knownShotBoundaries.remove(0) : null;

                if (bounds != null && videoFrame.getId() >= bounds.getStart() && videoFrame.getId() <= bounds.getEnd()) {

                    _return.addVideoFrame(videoFrame);
                    queueFrames(bounds.getEnd() - bounds.getStart());
                    do {
                        videoFrame = this.videoFrameList.poll();
                        if (videoFrame != null) {
                            _return.addVideoFrame(videoFrame);
                        } else {
                            break;
                        }
                    } while (videoFrame.getId() < bounds.getEnd());

                    this.segments.put(_return);
                    continue;

                } else {
                    Histogram hPrev, h = getHistogram(videoFrame);
                    _return.addVideoFrame(videoFrame);
                    while (true) {
                        if ((videoFrame = this.videoFrameList.poll()) == null) {
                            queueFrames();
                            if ((videoFrame = this.videoFrameList.poll()) == null) {
                                this.segments.put(_return);
                                _return = null;
                                break;
                            }
                        }
                        hPrev = h;
                        h = getHistogram(videoFrame);
                        double distance = hPrev.getDistance(h);

                        preShotList.offer(new Pair<>(videoFrame, distance));

                        if (preShotList.size() > PRESHOT_QUEUE_LENGTH) {
                            double max = 0;
                            int index = -1, i = 0;
                            for (Pair<VideoFrame, Double> pair : preShotList) {
                                if (pair.second > max) {
                                    index = i;
                                    max = pair.second;
                                }
                                i++;
                            }
                            if (max <= this.threshold && _return.getNumberOfFrames() < this.maxShotLength) { //no cut
                                for (Pair<VideoFrame, Double> pair : preShotList) {
                                    _return.addVideoFrame(pair.first);
                                }
                                preShotList.clear();
                            } else {
                                for (i = 0; i < index; ++i) {
                                    _return.addVideoFrame(preShotList.removeFirst().first);
                                }
                                break;
                            }
                        }
                    }
                    if(_return != null){
                        this.segments.put(_return);
                    }
                }
            }
        } catch (InterruptedException e) {
            LOGGER.log(Level.ERROR, "The thread that runs the VideoHistogramSegmenter was interrupted: {}", e);
        }

        /* End: Reset running to false. */
        synchronized(this) {
            this.isrunning = false;
        }
    }

    private boolean queueFrames(){
        return queueFrames(20);
    }

    private  boolean queueFrames(int number) {
        for(int i = 0; i < number; ++i){
            VideoFrame f = this.decoder.getNext();
            if (f == null) { //no more frames
                return false;
            } else {
                synchronized (this) {
                    this.videoFrameList.offer(f);
                }
            }
        }
        return true;
    }
}
