package org.vitrivr.cineast.core.run.filehandler;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.vitrivr.cineast.core.config.Config;
import org.vitrivr.cineast.core.config.IdConfig;
import org.vitrivr.cineast.core.data.MediaType;
import org.vitrivr.cineast.core.data.SegmentContainer;
import org.vitrivr.cineast.core.data.entities.MultimediaMetadataDescriptor;
import org.vitrivr.cineast.core.data.entities.MultimediaObjectDescriptor;
import org.vitrivr.cineast.core.data.entities.SegmentDescriptor;
import org.vitrivr.cineast.core.db.DBSelectorSupplier;
import org.vitrivr.cineast.core.db.dao.reader.MultimediaObjectLookup;
import org.vitrivr.cineast.core.db.PersistencyWriterSupplier;
import org.vitrivr.cineast.core.db.dao.reader.SegmentLookup;
import org.vitrivr.cineast.core.db.dao.writer.MultimediaMetadataWriter;
import org.vitrivr.cineast.core.db.dao.writer.MultimediaObjectWriter;
import org.vitrivr.cineast.core.db.dao.writer.SegmentWriter;
import org.vitrivr.cineast.core.decode.general.Decoder;
import org.vitrivr.cineast.core.features.extractor.DefaultExtractorInitializer;
import org.vitrivr.cineast.core.idgenerator.ObjectIdGenerator;
import org.vitrivr.cineast.core.metadata.MetadataExtractor;
import org.vitrivr.cineast.core.run.ExtractionContextProvider;
import org.vitrivr.cineast.core.runtime.ExtractionPipeline;
import org.vitrivr.cineast.core.segmenter.general.Segmenter;

import javax.activation.MimetypesFileTypeMap;
import java.io.IOException;
import java.nio.file.Path;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Abstract implementation of ExtractionFileHandler. This class should fit most media-types. However,
 * a concrete implementation must provide the correct decoder and segmenter classes.
 **
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

    /** SegmentWriter used to persist SegmentDescriptors created during the extraction. */
    private final MultimediaMetadataWriter metadataWriter;

    /** MultimediaObjectLookup used to lookup existing MultimediaObjectDescriptors during the extraction. */
    private final MultimediaObjectLookup objectReader;

    /** SegmentLookup used to lookup existing SegmentDescriptors during the extraction. */
    private final SegmentLookup segmentReader;

    /** Deque of files that are being extracted. */
    private final Deque<Path> files = new ArrayDeque<>();

    /** ExtractionContextProvider that is used to configure the extraction. */
    private final ExtractionContextProvider context;

    /** ExecutorService used to run the ExtractionPipeline and the Segmenter. */
    private final ExecutorService executorService = Executors.newFixedThreadPool(2, r -> {
        Thread thread = new Thread(r);
        if (r instanceof ExtractionPipeline) {
            thread.setName("extraction-pipeline-thread");
        } else if (r instanceof Segmenter) {
            thread.setName("extraction-segmenter-thread");
        }
        return thread;
    });

    /** List of MetadataExtractors that should be executed as part of the Extraction. */
    private final List<MetadataExtractor> metadataExtractors;

    /** ExtractionPipeline that extracts features from the segments. */
    private final ExtractionPipeline pipeline;

    /** Used to measure the duration of an extraction run. */
    private long start_timestamp;

    /** Total number of files that were queued for processing. */
    private long count_files = 0;

    /** Total number of files that were effectively processed. */
    private long count_processed = 0;

    /**
     * Default constructor used to initialize the class.
     *
     * @param files List of files that should be extracted.
     * @param context ExtractionContextProvider that holds extraction specific configurations.
     */
    public AbstractExtractionFileHandler(List<Path> files, ExtractionContextProvider context) throws IOException {
         /* Loads the files into the Deque. */
        this.preprocess(files);

        /* Setup the required persistence-writer classes. */
        PersistencyWriterSupplier writerSupplier = context.persistencyWriter();
        this.objectWriter = new MultimediaObjectWriter(writerSupplier.get(),10);
        this.segmentWriter = new SegmentWriter(writerSupplier.get(),10);
        this.metadataWriter = new MultimediaMetadataWriter(writerSupplier.get(),10);

        /* Setup the required persistence-reader classes. */
        DBSelectorSupplier readerSupplier = context.persistencyReader();
        this.objectReader = new MultimediaObjectLookup(readerSupplier.get());
        this.segmentReader = new SegmentLookup(readerSupplier.get());

        /* Setup the ExtractionPipeline and the metadata extractors. */
        this.pipeline = new ExtractionPipeline(context, new DefaultExtractorInitializer(writerSupplier));
        this.metadataExtractors = context.metadataExtractors();

        /* Store the context. */
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
        /* Get start_timestamp-timestamp. */
        this.start_timestamp = System.currentTimeMillis();

        /* Create new, initial decoder and segmenter. */
        Decoder<T> decoder = this.newDecoder();
        Segmenter<T> segmenter = this.newSegmenter();

        LOGGER.info("Starting extraction with {} files.", this.files.size());

        /* Submit the ExtractionPipeline to the executor-service. */
        this.executorService.execute(pipeline);

        /* Instantiates some of the helper classes required by this class. */
        final ObjectIdGenerator generator = this.context.objectIdGenerator();
        Path path = null;

        /* Process every file in the list. */
        while ((path = this.files.poll()) != null) {
            LOGGER.info("Processing file {}.", path);

            /* Create / lookup MultimediaObjectDescriptor for new file. */
            MultimediaObjectDescriptor descriptor = this.getOrCreateMultimediaObjectDescriptor(generator, this.context.inputPath().relativize(path), context.sourceType());
            if (!this.checkAndPersistMultimediaObject(descriptor)) continue;

            /* Pass file to decoder and decoder to segmenter. */
            decoder.init(path, Config.sharedConfig().getDecoders().get(this.context.sourceType()));
            segmenter.init(decoder);

            /* Store objectId for further reference and initialize a new segment number. */
            String objectId = descriptor.getObjectId();
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
                        /* Create segment-descriptor and try to persist it. */
                        SegmentDescriptor segmentDescriptor = SegmentDescriptor.newSegmentDescriptor(objectId, segmentNumber, container.getStart(), container.getEnd(), container.getAbsoluteStart(), container.getAbsoluteEnd());
                        if (!this.checkAndPersistSegment(segmentDescriptor)) continue;

                        /* Update container ID's. */
                        container.setId(segmentDescriptor.getSegmentId());
                        container.setSuperId(segmentDescriptor.getObjectId());

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

            /* Extract metadata. */
            this.extractAndPersistMetadata(path, objectId);

            /* Increment the files counter. */
            this.count_processed += 1;

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
        try {
            /* Wait a few seconds for the ExtractionPipeline to submit remaining tasks to the queue. */
            Thread.sleep(5000);

            /* Now shutdown the ExecutorService and tell the pipeline to stop. */
            LOGGER.info("File decoding and segmenting complete! Shutting down...");
            this.executorService.shutdown();
            this.pipeline.stop();

            /* Wait for pipeline to complete. */
            LOGGER.info("Waiting for ExtractionPipeline to terminate! This could take a while.");
            this.executorService.awaitTermination(30, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            LOGGER.warn("Interrupted while waiting for ExtractionPipeline to shutdown!");
        } finally {
            this.segmentWriter.close();
            this.objectWriter.close();
            Duration duration = Duration.ofMillis(System.currentTimeMillis()-this.start_timestamp);
            LOGGER.info("File extraction complete! It took me {} to extract {} out of {} files.", duration.toString(), this.count_processed, this.count_files);
        }
    }

    /**
     * Pre-processes the list of files by filtering unsupported types. The remaining files are
     * added to the
     *
     * ID's are generated b the ObjectIdGenerator configured.
     *
     * @return List of Pairs mapping the new objectId to the Path.
     */
    private void preprocess(List<Path> files) throws IOException {
        final MimetypesFileTypeMap filetypes = new MimetypesFileTypeMap("mime.types");
        final Decoder<T> decoder = this.newDecoder();
        files.stream().filter( path -> {
            Set<String> supportedFiles = decoder.supportedFiles();
            if (supportedFiles != null) {
                String type = filetypes.getContentType(path.toString());
                return decoder.supportedFiles().contains(type);
            } else {
                return true;
            }
        }).forEach(this.files::push);
        this.count_files = this.files.size();
    }

    /**
     * Persists a MultimediaObjectDescriptor and performs an existence check before, if so configured. Based
     * on the outcome of that persistence check and the settings in the ExtractionContext this method
     * returns true if object should be processed further or false otherwise.
     *
     * @param descriptor MultimediaObjectDescriptor that should be persisted.
     * @return true if object should be processed further or false if it should be skipped.
     */
    private boolean checkAndPersistMultimediaObject(MultimediaObjectDescriptor descriptor) {
        if (descriptor.exists()) { //this is true when a descriptor is used which has previously been retrieved from the database
          return true;
        }
      
        if (descriptor.getObjectId() == null) {
            LOGGER.warn("The objectId that was generated for {} is empty. This object cannot be persisted and will be skipped.", descriptor.getPath());
            return false;
        }

        if (this.context.existenceCheck() != IdConfig.ExistenceCheck.NOCHECK) {
            if (!this.objectReader.lookUpObjectById(descriptor.getObjectId()).exists()) {
                this.objectWriter.write(descriptor);
                return true;
            } else if (this.context.existenceCheck() == IdConfig.ExistenceCheck.CHECK_SKIP) {
                LOGGER.warn("MultimediaObject {} (name: {}) already exists. This object will be skipped.", descriptor.getObjectId(), descriptor.getName());
                return false;
            } else {
                LOGGER.warn("MultimediaObject {} (name: {}) already exists. Proceeding anyway...", descriptor.getObjectId(), descriptor.getName());
                return true;
            }
        } else {
            this.objectWriter.write(descriptor);
            return true;
        }
    }

    /**
     * Persists a SegmentDescriptor and performs an existence check before, if so configured. Based
     * on the outcome of that persistence check and the settings in the ExtractionContext this method
     * returns true if segment should be processed further or false otherwise.
     *
     * @param descriptor SegmentDescriptor that should be persisted.
     * @return true if segment should be processed further or false if it should be skipped.
     */
    private boolean checkAndPersistSegment(SegmentDescriptor descriptor) {
        if (this.context.existenceCheck() != IdConfig.ExistenceCheck.NOCHECK) {
            
            if (!this.segmentReader.lookUpShot(descriptor.getSegmentId()).exists()) {
                this.segmentWriter.write(descriptor);
                return true;
            } else if (this.context.existenceCheck() == IdConfig.ExistenceCheck.CHECK_SKIP) {
                LOGGER.warn("Segment {} already exists. This segment will be skipped.", descriptor.getSegmentId());
                return false;
            } else {
                LOGGER.warn("Segment {} already exists. Proceeding anyway...", descriptor.getSegmentId());
                return true;
            }
        } else {
            this.segmentWriter.write(descriptor);
            return true;
        }
    }

    /**
     * Convenience method to lookup a MultimediaObjectDescriptor for a given path and type or create a new one if needed.
     * If a new descriptor is required, newMultimediaObjectDescriptor is used.
     *
     * @param generator ObjectIdGenerator used for ID generation.
     * @param path The Path that points to the file for which a new MultimediaObjectDescriptor should be created.
     * @param type MediaType of the new MultimediaObjectDescriptor
     * @return the existing or a new MultimediaObjectDescriptor
     */
    public MultimediaObjectDescriptor getOrCreateMultimediaObjectDescriptor(ObjectIdGenerator generator, Path path, MediaType type){
        MultimediaObjectDescriptor descriptor = this.objectReader.lookUpObjectByPath(path.getFileName().toString());
        if (descriptor.exists() && descriptor.getMediatype() == this.context.sourceType()){
            return descriptor;
        }
        return MultimediaObjectDescriptor.newMultimediaObjectDescriptor(generator, path, type);
    }

    /**
     * Extracts metadata from a file by handing it down the list of MetadataExtractor objects.
     *
     * @param path Path to the file for which metadata must be extracted.
     * @param objectId ObjectId of the MediaObjectDescriptor associated with the path.
     */
    private void extractAndPersistMetadata(Path path, String objectId) {
        for (MetadataExtractor extractor : this.metadataExtractors) {
            List<MultimediaMetadataDescriptor> metadata = extractor.extract(objectId, path);
            if (metadata.size() > 0) {
                this.metadataWriter.write(metadata);
            }
        }
    }
}
