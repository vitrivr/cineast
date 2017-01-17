package org.vitrivr.cineast.core.run.filehandler;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.data.Pair;
import org.vitrivr.cineast.core.data.SegmentContainer;
import org.vitrivr.cineast.core.data.entities.MultimediaObjectDescriptor;
import org.vitrivr.cineast.core.data.entities.SegmentDescriptor;
import org.vitrivr.cineast.core.db.dao.MultimediaObjectWriter;
import org.vitrivr.cineast.core.db.dao.SegmentWriter;
import org.vitrivr.cineast.core.decode.general.Decoder;
import org.vitrivr.cineast.core.run.ExtractionContextProvider;
import org.vitrivr.cineast.core.runtime.ExtractionPipeline;
import org.vitrivr.cineast.core.segmenter.general.Segmenter;

import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Abstract implementation of ExtractionFileHandler. This class should fit most media-types. However,
 * a concrete implementation must provide the correct decoder and segmenter classes.
 *
 * TODO: File and Segment processor for i.e. Thumbnail export, Metadata extraction etc. Should be configured by the ExtractionContextProvider.
 *
 * @see ExtractionFileHandler
 * @see org.vitrivr.cineast.core.run.ExtractionDispatcher
 *
 * @author rgasser
 * @version 1.0
 * @created 14.01.17
 */
public abstract class AbstractExtractionFileHandler<T> implements ExtractionFileHandler<T> {

    private static final Logger LOGGER = LogManager.getLogger();

    /** MultimediaObjectWriter used to persist MultimediaObjectDescriptors created during the extraction. */
    private final MultimediaObjectWriter objectWriter;

    /** SegmentWriter used to persist SegmentDescriptors created during the extraction. */
    private final SegmentWriter segmentWriter;

    /** List of files that are being extracted. */
    private final List<Path> files;

    /** ExtractionContextProvider that is used to configure the extraction. */
    private final ExtractionContextProvider context;

    /** ExecutorService used to run the ExtractionPipeline and the Segmenter. */
    private final ExecutorService executorService = Executors.newFixedThreadPool(2, r -> {
        Thread thread = new Thread(r);
        thread.setName(String.format("file-handler-thread-%s", r.getClass().getSimpleName().toLowerCase()));
        return thread;
    });

    /** ExtractionPipeline that extracts features from the segments. */
    private final ExtractionPipeline pipeline;

    /**
     * Default constructor used to initialize the class.
     *
     * @param files List of files that should be extracted.
     * @param context ExtractionContextProvider that holds extraction specific configurations.
     */
    public AbstractExtractionFileHandler(List<Path> files, ExtractionContextProvider context) {
        this.objectWriter = new MultimediaObjectWriter();
        this.segmentWriter = new SegmentWriter();
        this.pipeline = new ExtractionPipeline(context);
        this.files = files;
        this.context = context;
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
        /* Create new, initial decoder and segmenter. */
        Decoder<T> decoder = this.newDecoder();
        Segmenter<T> segmenter = this.newSegmenter();

        LOGGER.info("Starting image extraction with {} files.", this.files.size());

        /* Submit the ExtractionPipeline to the executor-service. */
        this.executorService.execute(pipeline);

        /* Pre-processes the files. */
        List<Pair<String, Path>> preprocessedFiles = this.preprocess(decoder);

        /*
         * Process every file in the list.
         */
        for (Pair<String, Path> file : preprocessedFiles) {
            LOGGER.info("Processing file under {} (id: {})", file.second.toString(), file.first);

            /* Pass file to decoder and decoder to segmenter. */
            decoder.init(file.second);
            segmenter.init(decoder);

            /* Initialize a new segment number. */
            int segmentNumber = 1;

            /* Pass segmenter (runnable) to executor service. */
            this.executorService.execute(segmenter);

            /* Poll for output from the segmenter until that segmenter reports that no more output
             * is going to be generated.
             *
             * For every segment: Increase the segment-number, persist a segment descriptor and emit the segment
             * to the ExtractionPipeline!
             */
            while (!segmenter.complete()) {
                try {
                    SegmentContainer container = segmenter.getNext();
                    if (container != null) {
                        SegmentDescriptor segmentDescriptor = SegmentDescriptor.newSegmentDescriptor(file.first, segmentNumber, container.getStart(), container.getEnd());
                        this.segmentWriter.write(segmentDescriptor);

                        /* Update container ID's. */
                        container.setId(segmentDescriptor.getSegmentId());
                        container.setSuperId(segmentDescriptor.getObjectId());

                        LOGGER.debug("Received and persisted new segment for object {} (segment #: {}, segmentId: {}). Emitting to extraction pipeline...",  segmentDescriptor.getObjectId(), segmentNumber, segmentDescriptor.getSegmentId());

                        /* Emit container to extraction pipeline. */
                        this.pipeline.emit(container);

                         /* Increase the segment number. */
                        segmentNumber+=1;
                    }
                } catch (InterruptedException e) {
                   LOGGER.log(Level.ERROR, "Thread was interrupted while the extraction process was running. Aborting...");
                   break;
                }
            }

            /*  Create new decoder pair for a new file if the decoder reports that it cannot be reused.*/
            if (!decoder.canBeReused()) {
                decoder.close();
                decoder = this.newDecoder();
            }
        }

        /* Shutdown the FileHandler. */
        this.shutdown();
    }
    /**
     * Stops the ExtractionPipeline and relinquishing all resources.
     */
    private void shutdown() {
        LOGGER.info("File extraction complete! Shutting down...");
        this.executorService.shutdown();
        try {
            this.executorService.awaitTermination(15, TimeUnit.MINUTES);
            this.pipeline.stop();
        } catch (InterruptedException e) {
            LOGGER.warn("Interrupted while waiting for Executor to shutdown!");
        } finally {
            this.segmentWriter.close();
            this.objectWriter.close();
        }
    }

    /**
     * Pre-processes the list of files by filtering unsupported types and create MultimediaObjectDescriptors
     * with the remaining entries.
     *
     * @return List of Pairs mapping the new objectId to the Path.
     */
    private List<Pair<String, Path>> preprocess(Decoder<T> decoder) {
        return this.files.parallelStream().filter( path -> {
            Set<String> supportedFiles = decoder.supportedFiles();
            if (supportedFiles != null) {
                String type = fileTypeMap.getContentType(path.toString());
                return decoder.supportedFiles().contains(type);
            } else {
                return true;
            }
        }).map( path -> {
            MultimediaObjectDescriptor descriptor = MultimediaObjectDescriptor.newMultimediaObjectDescriptor(path, context.sourceType());
            this.objectWriter.write(descriptor);
            return new Pair<String, Path>(descriptor.getObjectId(), path);
        }).collect(Collectors.toList());
    }
}
