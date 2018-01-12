package org.vitrivr.cineast.core.run.filehandler;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.config.Config;
import org.vitrivr.cineast.core.config.IdConfig;
import org.vitrivr.cineast.core.data.MediaType;
import org.vitrivr.cineast.core.data.entities.MultimediaMetadataDescriptor;
import org.vitrivr.cineast.core.data.entities.MultimediaObjectDescriptor;
import org.vitrivr.cineast.core.data.entities.SegmentDescriptor;
import org.vitrivr.cineast.core.data.segments.SegmentContainer;
import org.vitrivr.cineast.core.db.DBSelectorSupplier;
import org.vitrivr.cineast.core.db.PersistencyWriterSupplier;
import org.vitrivr.cineast.core.db.dao.reader.MultimediaObjectLookup;
import org.vitrivr.cineast.core.db.dao.reader.SegmentLookup;
import org.vitrivr.cineast.core.db.dao.writer.MultimediaMetadataWriter;
import org.vitrivr.cineast.core.db.dao.writer.MultimediaObjectWriter;
import org.vitrivr.cineast.core.db.dao.writer.SegmentWriter;
import org.vitrivr.cineast.core.decode.general.Decoder;
import org.vitrivr.cineast.core.features.abstracts.MetadataFeatureModule;
import org.vitrivr.cineast.core.features.extractor.DefaultExtractorInitializer;
import org.vitrivr.cineast.core.idgenerator.ObjectIdGenerator;
import org.vitrivr.cineast.core.metadata.MetadataExtractor;
import org.vitrivr.cineast.core.run.ExtractionCompleteListener;
import org.vitrivr.cineast.core.run.ExtractionContextProvider;
import org.vitrivr.cineast.core.runtime.ExtractionPipeline;
import org.vitrivr.cineast.core.segmenter.general.Segmenter;
import org.vitrivr.cineast.core.util.LogHelper;
import org.vitrivr.cineast.core.util.MimeTypeHelper;

/**
 * Abstract implementation of ExtractionFileHandler. This class should fit most media-types. However,
 * a concrete implementation must provide the correct decoder and segmenter classes.
 * *
 *
 * @author rgasser
 * @version 1.0
 * @created 14.01.17
 * @see ExtractionFileHandler
 * @see org.vitrivr.cineast.core.run.ExtractionDispatcher
 */
public abstract class AbstractExtractionFileHandler<T> implements ExtractionFileHandler<T> {

    private static final Logger LOGGER = LogManager.getLogger();

    /**
     * MultimediaObjectWriter used to persist MultimediaObjectDescriptors created during the extraction.
     */
    private final MultimediaObjectWriter objectWriter;

    /**
     * SegmentWriter used to persist SegmentDescriptors created during the extraction.
     */
    private final SegmentWriter segmentWriter;

    /**
     * SegmentWriter used to persist SegmentDescriptors created during the extraction.
     */
    private final MultimediaMetadataWriter metadataWriter;

    /**
     * MultimediaObjectLookup used to lookup existing MultimediaObjectDescriptors during the extraction.
     */
    protected final MultimediaObjectLookup objectReader;

    /**
     * SegmentLookup used to lookup existing SegmentDescriptors during the extraction.
     */
    protected final SegmentLookup segmentReader;

    /**
     * ExtractionContextProvider that is used to configure the extraction.
     */
    protected final ExtractionContextProvider context;

    /**
     * Iterator off all paths that are due for extraction.
     */
    private final Iterator<Path> files;

    /**
     * ExecutorService used to run the ExtractionPipeline and the Segmenter.
     */
    private final ExecutorService executorService = Executors.newFixedThreadPool(2, r -> {
        Thread thread = new Thread(r);
        if (r instanceof ExtractionPipeline) {
            thread.setName("extraction-pipeline-thread");
        } else if (r instanceof Segmenter) {
            thread.setName("extraction-segmenter-thread");
        }
        return thread;
    });

    /** List of {@link MetadataExtractor}s that should be executed as part of the extraction. */
    private final List<MetadataExtractor> metadataExtractors;

    /** List of {@link ExtractionCompleteListener}s that should be notified when extraction is complete. */
    private final List<ExtractionCompleteListener> completeListeners = new ArrayList<>();

    /** Reference to {@link ExtractionPipeline{ that extracts features from the segments. */
    private final ExtractionPipeline pipeline;

    /** Used to measure the duration of an extraction run. */
    private long start_timestamp;

    /** Total number of files that were effectively processed. */
    private long count_processed = 0;


    /**
     * Default constructor used to initialize the class.
     *
     * @param files   List of files that should be extracted.
     * @param context ExtractionContextProvider that holds extraction specific configurations.
     */
    public AbstractExtractionFileHandler(Iterator<Path> files, ExtractionContextProvider context) throws IOException {
        this.files = files;

        /* Setup the required persistence-writer classes. */
        final PersistencyWriterSupplier writerSupplier = context.persistencyWriter();
        this.objectWriter = new MultimediaObjectWriter(writerSupplier.get(), context.getBatchsize());
        this.segmentWriter = new SegmentWriter(writerSupplier.get(), context.getBatchsize());
        this.metadataWriter = new MultimediaMetadataWriter(writerSupplier.get(), context.getBatchsize());

        /* Setup the required persistence-reader classes. */
        final DBSelectorSupplier readerSupplier = context.persistencyReader();
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

        LOGGER.info("Starting extraction.");

        /* Submit the ExtractionPipeline to the executor-service. */
        this.executorService.execute(pipeline);

        /* Instantiates some of the helper classes required by this class. */
        final ObjectIdGenerator generator = this.context.objectIdGenerator();
        Path path = null;
        
        /* Initialise MetadataFeatureModules. */
        for (MetadataExtractor extractor : this.metadataExtractors) {
            if (extractor instanceof MetadataFeatureModule) {
                this.pipeline.getInitializer().initialize((MetadataFeatureModule<?>) extractor);
            } else {
                extractor.init();
            }
        }

        /* Process every file in the list. */
        while ((path = this.nextPath(decoder)) != null) {
            LOGGER.info("Processing file {}.", path);

            /* Pass file to decoder and decoder to segmenter. */
            if (decoder.init(path, Config.sharedConfig().getDecoders().get(this.context.sourceType()))) {
                /* Create / lookup MultimediaObjectDescriptor for new file. */
                final MultimediaObjectDescriptor descriptor = this.fetchOrCreateMultimediaObjectDescriptor(generator, path, context.sourceType());
                if (!this.checkAndPersistMultimediaObject(descriptor)) {
                    continue;
                }

                /* Store objectId for further reference and initialize a new segment number. */
                final String objectId = descriptor.getObjectId();
                int segmentNumber = 1;

                /* Initialize segmenter and pass to executor service. */
                segmenter.init(decoder, descriptor);
                this.executorService.execute(segmenter);

                /* Poll for output from the segmenter until that segmenter reports that no more output
                 * is going to be generated.
                 *
                 * For every segment: Increase the segment-number, persist a segment descriptor and emit the segment
                 * to the ExtractionPipeline!
                 */
                while (!segmenter.complete()) {
                    try {
                        final SegmentContainer container = segmenter.getNext();
                        if (container != null) {
                            /* Create segment-descriptor and try to persist it. */
                            final SegmentDescriptor segmentDescriptor = this.fetchOrCreateSegmentDescriptor(objectId, segmentNumber, container.getStart(), container.getEnd(), container.getAbsoluteStart(), container.getAbsoluteEnd());
                            if (!this.checkAndPersistSegment(segmentDescriptor)) {
                                continue;
                            }

                            /* Update container ID's. */
                            container.setId(segmentDescriptor.getSegmentId());
                            container.setSuperId(segmentDescriptor.getObjectId());

                            /* Timeout in ms used when emitting segments into the ExtractionPipeline. */
                            int emissionTimout = 1000;

                            /* Emit container to extraction pipeline. */
                            while (!this.pipeline.emit(container, emissionTimout)) {
                                LOGGER.warn("ExtractionPipeline is full - deferring emission of segment. Consider increasing the thread-pool count for the extraction pipeline.");
                                Thread.sleep(emissionTimout);
                                emissionTimout += 500;
                            }

                            /* Increase the segment number. */
                            segmentNumber += 1;
                        }
                    } catch (InterruptedException e) {
                        LOGGER.log(Level.ERROR, "Thread was interrupted while the extraction process was running. Aborting...");
                        break;
                    }
                }

                /* Extract metadata. */
                this.extractAndPersistMetadata(path, objectId);

                /* Force flush the segment, object and metadata information. */
                this.segmentWriter.flush();
                this.objectWriter.flush();
                this.metadataWriter.flush();
            } else {
                LOGGER.error("Failed to initialize decoder. File is being skipped...");
            }


            /* Increment the files counter. */
            this.count_processed += 1;

            /*  Create new decoder pair for a new file if the decoder reports that it cannot be reused.*/
            if (!decoder.canBeReused()) {
                decoder.close();
                decoder = this.newDecoder();
            }

            /* notify all listeners about the completed extraction */
            for (int i = 0; i < this.completeListeners.size(); ++i) {
                this.completeListeners.get(i).onCompleted(path);
            }

            /*
             * Trigger garbage collection once in a while. This is specially relevant when many small files are processed, since unused allocated memory could accumulate and trigger swapping.
             */
            if (this.count_processed % 50 == 0) {
                System.gc();
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
            /* Close all the MetadataExtracto classes. */
            for (MetadataExtractor extractor : this.metadataExtractors) {
                extractor.finish();
            }

            /* Close all the writers and readers. */
            this.segmentWriter.close();
            this.objectWriter.close();
            this.metadataWriter.close();
            this.objectReader.close();
            this.segmentReader.close();

            /* Measure duration. */
            Duration duration = Duration.ofMillis(System.currentTimeMillis() - this.start_timestamp);
            LOGGER.info("File extraction complete! It took {} to extract {} out files.", duration.toString(), this.count_processed);
        }
    }


    /**
     * returns the next file which can be decoded by the decoder or <code>null</code> if there are no more files
     */
    protected Path nextPath(final Decoder<T> decoder) {
        while (this.files != null && this.files.hasNext()) {
            Path path = files.next();
            String type = MimeTypeHelper.getContentType(path.toString());
            if (decoder.supportedFiles().contains(type)) {
                return path;
            }
        }
        return null;
    }

    /**
     * Checks if the MultimediaObjectDescriptor already exists and decides whether extraction should continue for
     * that object or not (based on the ingest settings). If it does not exist, the MultimediaObjectDescriptor is
     * persisted.
     *
     * @param descriptor MultimediaObjectDescriptor that should be persisted.
     * @return true if object should be processed further or false if it should be skipped.
     */
    protected boolean checkAndPersistMultimediaObject(MultimediaObjectDescriptor descriptor) {
        if (descriptor.exists() && this.context.existenceCheck() == IdConfig.ExistenceCheck.CHECK_SKIP) {//this is true when a descriptor is used which has previously been retrieved from the database
            LOGGER.info("MultimediaObject {} (name: {}) already exists. This object will be skipped.", descriptor.getObjectId(), descriptor.getName());
            return false;
        } else if (descriptor.exists() && this.context.existenceCheck() == IdConfig.ExistenceCheck.CHECK_PROCEED) {
            LOGGER.info("MultimediaObject {} (name: {}) already exists. Proceeding anyway...", descriptor.getObjectId(), descriptor.getName());
            return true;
        } else if (descriptor.getObjectId() == null) {
            LOGGER.warn("The objectId that was generated for {} is empty. This object cannot be persisted and will be skipped.", descriptor.getPath());
            return false;
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
    protected boolean checkAndPersistSegment(SegmentDescriptor descriptor) {
        if (descriptor.exists() && this.context.existenceCheck() == IdConfig.ExistenceCheck.CHECK_SKIP) {
            LOGGER.info("Segment {} already exists. This segment will be skipped.", descriptor.getSegmentId());
            return false;
        } else if (descriptor.exists() && this.context.existenceCheck() == IdConfig.ExistenceCheck.CHECK_PROCEED) {
            LOGGER.info("Segment {} already exists. Proceeding anyway...", descriptor.getSegmentId());
            return true;
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
     * @param path      The Path that points to the file for which a new MultimediaObjectDescriptor should be created.
     * @param type      MediaType of the new MultimediaObjectDescriptor
     * @return the existing or a new MultimediaObjectDescriptor
     */
    protected MultimediaObjectDescriptor fetchOrCreateMultimediaObjectDescriptor(ObjectIdGenerator generator, Path path, MediaType type) {
        /*
         * Two cases:
         * - For single-file extraction, the file-name is stored as file-path.
         * - For extraction of a folder, the path relative to the input folder is stored as file-path.
         */
        if (Files.isRegularFile(this.context.inputPath())) {
            path = path.getFileName();
        } else {
            path = this.context.inputPath().toAbsolutePath().relativize(path.toAbsolutePath());
        }

        /* Lookup multimedia-object and persist if necessary. */
        MultimediaObjectDescriptor descriptor = this.objectReader.lookUpObjectByPath(path.toString());
        if (descriptor.exists() && descriptor.getMediatype() == this.context.sourceType()) {
            return descriptor;
        }
        return MultimediaObjectDescriptor.newMultimediaObjectDescriptor(generator, path, type, this.objectReader);
    }

    /**
     * Convenience method to lookup a SegmentDescriptor for a given path and type or create a new one if needed.
     * If a new descriptor is required, SegmentDescriptor.newSegmentDescriptor() is used.
     *
     * @param objectId
     * @param segmentNumber
     * @param start
     * @param end
     * @param startabs
     * @param endabs
     * @return
     */
    protected SegmentDescriptor fetchOrCreateSegmentDescriptor(String objectId, int segmentNumber, int start, int end, float startabs, float endabs) {
        String segmentId = MediaType.generateSegmentId(objectId, segmentNumber);
        return this.segmentReader.lookUpSegment(segmentId).orElse(SegmentDescriptor.newSegmentDescriptor(objectId, segmentNumber, start, end, startabs, endabs));
    }

    /**
     * Extracts metadata from a file by handing it down the list of MetadataExtractor objects.
     *
     * @param path     Path to the file for which metadata must be extracted.
     * @param objectId ObjectId of the MediaObjectDescriptor associated with the path.
     */
    protected void extractAndPersistMetadata(Path path, String objectId) {
        for (MetadataExtractor extractor : this.metadataExtractors) {
            try {
                List<MultimediaMetadataDescriptor> metadata = extractor.extract(objectId, path);
                if (!metadata.isEmpty()) {
                    this.metadataWriter.write(metadata);
                }
            } catch (Exception e) {
                LOGGER.error("exception during metadata extraction: {}", LogHelper.getStackTrace(e));
            }
        }
    }

    /**
     * @param object
     * @return
     */
    protected List<SegmentDescriptor> retrieveExistingSegments(MultimediaObjectDescriptor object) {
        return this.segmentReader.lookUpSegmentsOfObject(object.getObjectId());
    }

    /**
     * Adds a {@link ExtractionCompleteListener} to be notified about every object for which the extraction completes.
     *
     * @param listener {@link ExtractionCompleteListener}
     */
    @Override
    public void addExtractionCompleteListener(ExtractionCompleteListener listener) {
        if(listener != null && !this.completeListeners.contains(listener)){
            completeListeners.add(listener);
        }
    }
}
