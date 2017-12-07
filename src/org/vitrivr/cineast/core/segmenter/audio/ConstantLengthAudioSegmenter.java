package org.vitrivr.cineast.core.segmenter.audio;

import java.util.ArrayDeque;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.vitrivr.cineast.core.data.entities.MultimediaObjectDescriptor;
import org.vitrivr.cineast.core.data.entities.SegmentDescriptor;
import org.vitrivr.cineast.core.data.frames.AudioFrame;
import org.vitrivr.cineast.core.data.segments.AudioSegment;
import org.vitrivr.cineast.core.data.segments.SegmentContainer;
import org.vitrivr.cineast.core.db.dao.reader.SegmentLookup;
import org.vitrivr.cineast.core.decode.general.Decoder;
import org.vitrivr.cineast.core.run.ExtractionContextProvider;
import org.vitrivr.cineast.core.segmenter.general.Segmenter;

/**
 * Merges multiple AudioFrames into a single AudioSegment using a constant number of frames per AudioSegment. The length
 * of an AudioSegment in frames AND the overlapfactor between two subsequent AudioSegments can be defined upon onstruction of the
 * ConstantLengthAudioSegmenter.
 *
 * @see AudioSegment
 * @see AudioFrame
 *
 * @author rgasser
 * @version 1.0
 * @created 31.01.17
 */
public class ConstantLengthAudioSegmenter implements Segmenter<AudioFrame> {

    /** Key in the configuration map used to configure the length setting. */
    private static final String PROPERTY_LENGTH_KEY = "length";

    /** Key in the configuration map used to configure the overlap setting. */
    private static final String PROPERTY_OVERLAP_KEY = "overlap";

    /** Key in the configuration map used to configure the length setting. */
    private static final Float PROPERTY_LENGTH_DEFAULT = 10.0f;

    /** Key in the configuration map used to configure the overlap setting. */
    private static final Float PROPERTY_OVERLAP_DEFAULT = 1.0f;

    private static final int SEGMENT_QUEUE_LENGTH = 10;
    private static final int SEGMENT_POLLING_TIMEOUT = 1000;

    /** Decoder<AudioFrame> used for file decoding. */
    private Decoder<AudioFrame> decoder;

    /** A LinkedBlockingQueue used that holds the resulting AudioSegments. */
    private final LinkedBlockingQueue<SegmentContainer> outputQueue = new LinkedBlockingQueue<SegmentContainer>(SEGMENT_QUEUE_LENGTH);

    /** The length in AudioFrames of a resulting AudioSegment. */
    private final float length;

    /** Length of the overlap between two segments in seconds. */
    private final float overlap;

    /** AudioSegment that is currently filled with AudioFrames. */
    private AudioSegment currentSegment;

    /** ArrayDeque holding AudioFrames that have been queued for overlap. */
    private ArrayDeque<AudioFrame> overlapQueue = new ArrayDeque<>();

    /** A flag indicating whether or not the segmenter has completed its work. */
    private AtomicBoolean complete = new AtomicBoolean(false);

    /**
     * Constructor for {@link ConstantLengthAudioSegmenter}.
     *
     * @param length Length of an individual segment in seconds.
     * @param overlap Overlap between to subsequent AudioSegments
     */
    public ConstantLengthAudioSegmenter(float length, float overlap) {
        this.length = length;
        this.overlap = overlap;
        if (overlap >= 0.9f * length) {
            throw new IllegalArgumentException("Overlap must be smaller than total segment length.");
        }
    }

    /**
     * Constructor for {@link ConstantLengthAudioSegmenter required for instantiation through {@link org.vitrivr.cineast.core.util.ReflectionHelper}.
     *
     * @param context The {@link ExtractionContextProvider} for the extraction context this {@link ConstantLengthAudioSegmenter} is created in.
     */
    public ConstantLengthAudioSegmenter(ExtractionContextProvider context) {
        this(PROPERTY_LENGTH_DEFAULT, PROPERTY_OVERLAP_DEFAULT);
    }

    /**
     * Constructor for {@link ConstantLengthAudioSegmenter required for instantiation through {@link org.vitrivr.cineast.core.util.ReflectionHelper}.
     *
     * @param context The {@link ExtractionContextProvider} for the extraction context this {@link ConstantLengthAudioSegmenter} is created in.
     * @param properties A HashMap containing the configuration properties for {@link ConstantLengthAudioSegmenter}
     */
    public ConstantLengthAudioSegmenter(ExtractionContextProvider context, Map<String,String> properties) {
        this.length = Float.parseFloat(properties.getOrDefault(PROPERTY_LENGTH_KEY, PROPERTY_LENGTH_DEFAULT.toString()));
        this.overlap = Float.parseFloat(properties.getOrDefault(PROPERTY_OVERLAP_KEY, PROPERTY_OVERLAP_DEFAULT.toString()));
        if (overlap >= 0.9f * length) {
            throw new IllegalArgumentException("Overlap must be smaller than total segment length.");
        }
    }

    /**
     * Method used to initialize the Segmenter. A class implementing the Decoder interface with the same type must be provided.
     *
     * @param decoder Decoder used for frames-decoding.
     * @param object Media object that is about to be segmented.
     */
    @Override
    public void init(Decoder<AudioFrame> decoder, MultimediaObjectDescriptor object) {
        this.decoder = decoder;
        this.complete.set(false);
    }

    /**
     * Returns the next SegmentContainer from the source OR null if there are no more segments in the outputQueue. As
     * generation of SegmentContainers can take some time (depending on the media-type), a null return does not
     * necessarily mean that the Segmenter is done segmenting. Use the complete() method to check this.
     *
     * Important: This method should be designed to block and wait for an appropriate amount of time if the Segmenter
     * is not yet ready to deliver another segment! It's up to the Segmenter how long that timeout should last.
     *
     * @return
     */
    @Override
    public SegmentContainer getNext() throws InterruptedException {
       SegmentContainer segment = this.outputQueue.poll(SEGMENT_POLLING_TIMEOUT, TimeUnit.MILLISECONDS);
       if (segment == null) {
           this.complete.set(this.decoder.complete());
       }
       return segment;
    }

    /**
     * Indicates whether the Segmenter is complete i.e. no new segments are to be expected.
     *
     * @return true if work is complete, false otherwise.
     */
    @Override
    public boolean complete() {
        return this.complete.get();
    }

    /**
     * Closes the Segmenter. This method should cleanup and relinquish all resources. Especially,
     * calling this method should also close the Decoder associated with this Segmenter instance.
     * <p>
     * Note: It is unsafe to re-use a Segmenter after it has been closed.
     */
    @Override
    public void close() {
        if (this.complete.get()) {
            this.decoder.close();
        }
    }

    /**
     * This methods pulls AudioFrames as they become available from the Decoder and adds them
     * the the Deque of AudioFrames. Once that queue holds enough AudioFrames it is drained and
     * a AudioSegment is emmitted.
     *
     * The overlapfactor defines how many frames re-enter the queue once they have been added to the
     * new Segment.
     *
     * @see Thread#run()
     */
    @Override
    public void run() {

        this.currentSegment = new AudioSegment();

        while (!this.decoder.complete()) {
            AudioFrame newFrame = this.decoder.getNext();
            if (newFrame != null) {
                this.currentSegment.addFrame(newFrame);
                if (this.currentSegment.getAudioDuration() >= (this.length - this.overlap)) {
                    this.overlapQueue.offerLast(newFrame);
                }
                if (this.currentSegment.getAudioDuration() >= this.length) {
                    this.nextCycle(false);
                }
            } else if (this.decoder.complete()) {
                this.nextCycle(true);
            }
        }
    }


    /**
     * Drains the Deque and emits a new AudioSegment.
     */
    private void nextCycle(boolean end) {
        try {
            this.outputQueue.put(this.currentSegment);
            if (!end) {
                this.currentSegment = new AudioSegment();
                AudioFrame frame;
                while ((frame = this.overlapQueue.poll()) != null) {
                    this.currentSegment.addFrame(frame);
                }
            } else {
                this.currentSegment = null;
                this.overlapQueue.clear();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
