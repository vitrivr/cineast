package org.vitrivr.cineast.core.extraction.segmenter.video;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.data.entities.MediaObjectDescriptor;
import org.vitrivr.cineast.core.data.entities.MediaSegmentDescriptor;
import org.vitrivr.cineast.core.data.frames.VideoFrame;
import org.vitrivr.cineast.core.data.segments.SegmentContainer;
import org.vitrivr.cineast.core.data.segments.VideoSegment;
import org.vitrivr.cineast.core.extraction.ExtractionContextProvider;
import org.vitrivr.cineast.core.extraction.decode.general.Decoder;
import org.vitrivr.cineast.core.extraction.segmenter.general.Segmenter;
import org.vitrivr.cineast.core.extraction.segmenter.image.ImageSegmenter;
import org.vitrivr.cineast.core.util.ReflectionHelper;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class TRECVidMSRSegmenter implements Segmenter<VideoFrame> {

    /** */
    private static final Logger LOGGER = LogManager.getLogger();

    /** Maximum length of the output queue. */
    private static final int SEGMENT_QUEUE_LENGTH = 10;

    /** The timeout when polling the output queue. Defaults to 5s. */
    private static final int SEGMENT_POLLING_TIMEOUT = 5000;

    /** Key in the configuration map used to configure the MSR folder setting. */
    private static final String PROPERTY_FOLDER_KEY = "folder";

    /** Queue of shot boundaries (pair of start and end frame). The entries are supposed to be sorted in ascending order. */
    private final Queue<Pair<Long,Long>> boundaries = new ArrayDeque<>();

    /** Queue for resulting {@link SegmentContainer}s waiting pick up by some consumer. */
    private final LinkedBlockingQueue<SegmentContainer> outputQueue = new LinkedBlockingQueue<>(SEGMENT_QUEUE_LENGTH);

    /** Path to the folder relative to which MSR files will be looked up. */
    private final Path msrFolderPath;

    /** The {@link Decoder} instance used by the current instance of {@link TRECVidMSRSegmenter}*/
    private Decoder<VideoFrame> decoder;

    /** Internal flag that indicates whether the {@link TRECVidMSRSegmenter} is still running (= true). */
    private volatile boolean running = false;

    /** A flag indicating whether more {@link SegmentContainer}s are to be expected from this {@link TRECVidMSRSegmenter}. */
    private volatile boolean complete = false;

    /**
     * Constructor for {@link TRECVidMSRSegmenter}.
     *
     * @param path Path to the folder relative to which MSR files will be resolved (based on the name of the input video file).
     */
    public TRECVidMSRSegmenter(Path path) {
        this.msrFolderPath = path;
        if (!Files.isDirectory(path)) {
            throw new IllegalArgumentException("The MSR path must point to a directory.");
        }
    }

    /**
     * Constructor for {@link ImageSegmenter required for instantiation through {@link ReflectionHelper }.
     *
     * @param context The {@link ExtractionContextProvider } for the extraction context this {@link ImageSegmenter} is created in.
     */
    public TRECVidMSRSegmenter(ExtractionContextProvider context) {
        this(context.inputPath().orElse(null));
    }

    /**
     * Constructor for {@link ImageSegmenter required for instantiation through {@link ReflectionHelper }.
     *
     * @param context The {@link ExtractionContextProvider} for the extraction context this {@link ImageSegmenter} is created in.
     * @param properties A HashMap containing the configuration properties for {@link ImageSegmenter}
     */
    public TRECVidMSRSegmenter(ExtractionContextProvider context, Map<String,String> properties) {
        this(properties.containsKey(PROPERTY_FOLDER_KEY) ? Paths.get(properties.get(PROPERTY_FOLDER_KEY)) : context.inputPath().orElse(null));
    }

    /**
     *
     * @param decoder Decoder used for media-decoding.
     * @param object Media object that is about to be segmented.
     */
    @Override
    public synchronized void init(Decoder<VideoFrame> decoder, MediaObjectDescriptor object) {
        if (!this.running) {
            this.decoder = decoder;
            this.complete = false;
            this.boundaries.clear();

            /* Loads the MSR file relative to the video. */
            final String suffix = object.getPath().substring(object.getPath().lastIndexOf("."), object.getPath().length());
            final String msrFilename = object.getPath().replace(suffix, ".msb");
            final Path path = this.msrFolderPath.resolve(msrFilename);
            this.boundaries.addAll(decode(path));
        } else {
            throw new IllegalArgumentException("You cannot call init() while the TRECVidMSRSegmenter is running.");
        }
    }

    /**
     * Returns the next {@link SegmentContainer} or null, if there is currently no {@link SegmentContainer} available.
     *
     * @return {@link SegmentContainer} or null
     * @throws InterruptedException If thread is interrupted while waiting fro the {@link SegmentContainer}
     */
    @Override
    public SegmentContainer getNext() throws InterruptedException {
        SegmentContainer segment = this.outputQueue.poll(SEGMENT_POLLING_TIMEOUT, TimeUnit.MILLISECONDS);
        synchronized (this) {
            if (segment == null && !this.running) {
                this.complete = true;
            }
        }
        return segment;
    }

    /**
     * Checks the current state of the {@link TRECVidMSRSegmenter}. The {@link TRECVidMSRSegmenter} is considered to
     * be complete if the segmenting process has stopped AND the segments queue has been drained completely.
     *
     * @return Current state of the
     */
    @Override
    public synchronized boolean complete() {
        return this.complete;
    }

    /**
     * Closes the {@link TRECVidMSRSegmenter} and all associated resources. This includes decoders.
     */
    @Override
    public synchronized void close() {
        if (!this.running) {
            this.decoder.close();
        } else {
            throw new IllegalStateException("You cannot close the TRECVidMSRSegmenter while it is running.");
        }
    }

    /**
     * This method takes care of the actual segmenting based on existing TRECVid master shot record boundaries.
     */
    @Override
    public void run() {
        /* Sets the running flag to true. */
        synchronized (this) {
            this.running = true;
        }

        try {
            VideoFrame currentFrame = null;
            while (!this.boundaries.isEmpty()) {
                final Pair<Long, Long> boundary = this.boundaries.poll();
                if (boundary == null) break;

                final VideoSegment segment = new VideoSegment();

                /* Append frames to the segment until the VideoFrame's (sequential) is beyond the boundaries. */
                while (!this.decoder.complete()) {
                    if (currentFrame == null) currentFrame = this.decoder.getNext();
                    if (currentFrame.getId() >= boundary.getLeft() && currentFrame.getId() <= boundary.getRight()) {
                        segment.addVideoFrame(currentFrame);
                        currentFrame = null;
                    } else {
                        break;
                    }
                }

                /* Add video segment to the segments queue. */
                this.outputQueue.put(segment);
            }

            /* Create final segment. */
            final VideoSegment finalSegment = new VideoSegment();
            while (!this.decoder.complete()) {
                finalSegment.addVideoFrame(this.decoder.getNext());
            }
            if (finalSegment.getNumberOfFrames() > 0) this.outputQueue.put(finalSegment);
        } catch (InterruptedException e) {
            LOGGER.log(Level.ERROR, "The thread that runs the TRECVidMSRSegmenter was interrupted: {}", e);
        }

        /* Sets the running flag to false. */
        synchronized (this) {
            this.running = false;
        }
    }

    /**
     * Decodes shot boundaries in the format used for TRECVID and creates {@link MediaSegmentDescriptor}s accordingly.
     *
     * @param msr The file containing the master shot references.
     */
    public static List<Pair<Long,Long>> decode(Path msr) {
        final List<Pair<Long,Long>> _return = new ArrayList<>();
        try (final BufferedReader reader = Files.newBufferedReader(msr, StandardCharsets.ISO_8859_1)) {
            String line = null;
            int shotCounter = 0;
            while((line = reader.readLine()) != null){
                line = line.trim();

                if(line.isEmpty()){ //skip empty lines
                    continue;
                }

                if(!Character.isDigit(line.charAt(0))){//line does not start with a number
                    continue;
                }

                String[] split = line.split(" ");
                if(split.length < 2){//there are not two blocks on this line
                    continue;
                }

                long start, end;
                try{
                    start = 1 + Long.parseLong(split[0]); //TRECVID msr starts with 0
                    end = 1 + Long.parseLong(split[1]);
                } catch(NumberFormatException e){
                    continue;
                }

                ++shotCounter;

				/* TODO: Derive absolute start and end position of MediaSegmentDescriptor. */
                _return.add(new ImmutablePair<>(start, end));
            }
        } catch (FileNotFoundException e) {
            LOGGER.error("TRECVid MSR file '{}' was not found.", msr.toString());
        } catch (IOException e) {
            LOGGER.error("Error while reading RECVid MSR file '{}': {}", msr.toString(), e.getMessage());
        }

        return _return;
    }
}
