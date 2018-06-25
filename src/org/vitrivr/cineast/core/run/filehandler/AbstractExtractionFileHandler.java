package org.vitrivr.cineast.core.run.filehandler;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
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
import org.vitrivr.cineast.core.run.ExtractionContainerProvider;
import org.vitrivr.cineast.core.run.ExtractionContextProvider;
import org.vitrivr.cineast.core.run.ExtractionItemContainer;
import org.vitrivr.cineast.core.run.ExtractionItemProcessor;
import org.vitrivr.cineast.core.runtime.ExtractionPipeline;
import org.vitrivr.cineast.core.segmenter.general.Segmenter;
import org.vitrivr.cineast.core.util.LogHelper;
import org.vitrivr.cineast.core.util.MimeTypeHelper;

/**
 * This class should fit most media-types.
 * However, a concrete implementation must provide the correct {@link Decoder} and {@link Segmenter}
 * classes.
 *
 * @author rgasser
 * @created 14.01.17
 * @see org.vitrivr.cineast.core.run.ExtractionDispatcher
 */
public abstract class AbstractExtractionFileHandler<T> implements Runnable,
    ExtractionItemProcessor {

  private static final Logger LOGGER = LogManager.getLogger();

  private final MultimediaObjectWriter objectWriter;
  private final SegmentWriter segmentWriter;
  private final MultimediaMetadataWriter metadataWriter;
  private final MultimediaObjectLookup objectReader;
  private final SegmentLookup segmentReader;
  protected final ExtractionContextProvider context;
  private final ExtractionContainerProvider itemProvider;

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

  private final List<MetadataExtractor> metadataExtractors;
  private final List<ExtractionCompleteListener> completeListeners = new ArrayList<>();

  /**
   * Reference to {@link ExtractionPipeline { that extracts features from the segments.
   */
  private final ExtractionPipeline pipeline;
  private long start_timestamp;
  private long count_processed = 0;


  /**
   * Default constructor used to initialize the class.
   *
   * @param context ExtractionContextProvider that holds extraction specific configurations.
   */
  public AbstractExtractionFileHandler(ExtractionContainerProvider itemProvider,
      ExtractionContextProvider context) throws IOException {
    this.itemProvider = itemProvider;

        /* Setup the required persistence-writer classes. */
    final PersistencyWriterSupplier writerSupplier = context.persistencyWriter();
    this.objectWriter = new MultimediaObjectWriter(writerSupplier.get());
    this.segmentWriter = new SegmentWriter(writerSupplier.get(), context.getBatchsize());
    this.metadataWriter = new MultimediaMetadataWriter(writerSupplier.get(),
        context.getBatchsize());

        /* Setup the required persistence-reader classes. */
    final DBSelectorSupplier readerSupplier = context.persistencyReader();
    this.objectReader = new MultimediaObjectLookup(readerSupplier.get());
    this.segmentReader = new SegmentLookup(readerSupplier.get());

        /* Setup the ExtractionPipeline and the metadata extractors. */
    this.pipeline = new ExtractionPipeline(context,
        new DefaultExtractorInitializer(writerSupplier));
    this.metadataExtractors = context.metadataExtractors();

        /* Store the context. */
    this.context = context;
  }

  @Override
  public void run() {
    this.start_timestamp = System.currentTimeMillis();

    Decoder<T> decoder = this.newDecoder();
    Segmenter<T> segmenter = this.newSegmenter();

    LOGGER.info("Starting extraction.");

    this.executorService.execute(pipeline);

    final ObjectIdGenerator generator = this.context.objectIdGenerator();
    ExtractionItemContainer item = null;

    for (MetadataExtractor extractor : this.metadataExtractors) {
      if (extractor instanceof MetadataFeatureModule) {
        this.pipeline.getInitializer().initialize((MetadataFeatureModule<?>) extractor);
      } else {
        extractor.init();
      }
    }

    while ((item = this.nextItem(decoder)) != null) {
      try {
        LOGGER.info("Processing file {}.", item);
        if (decoder.init(item.getPathForExtraction(),
            Config.sharedConfig().getDecoders().get(this.context.sourceType()))) {
                /* Create / lookup MultimediaObjectDescriptor for new file. */
          final MultimediaObjectDescriptor descriptor = this
              .fetchOrCreateMultimediaObjectDescriptor(generator, item, context.sourceType());
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
                final SegmentDescriptor segmentDescriptor = this
                    .fetchOrCreateSegmentDescriptor(objectId, segmentNumber,
                        container.getStart(), container.getEnd(),
                        container.getAbsoluteStart(), container.getAbsoluteEnd());
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
                  LOGGER.warn(
                      "ExtractionPipeline is full - deferring emission of segment. Consider increasing the thread-pool count for the extraction pipeline.");
                  Thread.sleep(emissionTimout);
                  emissionTimout += 500;
                }

                            /* Increase the segment number. */
                segmentNumber += 1;
              }
            } catch (InterruptedException e) {
              LOGGER.log(Level.ERROR,
                  "Thread was interrupted while the extraction process was running. Aborting...");
              break;
            }
          }

          List<MultimediaMetadataDescriptor> metadata = Arrays.stream(item.getMetadata())
              .map(el -> MultimediaMetadataDescriptor.fromExisting(el, objectId))
              .collect(Collectors.toList());
          this.metadataWriter.write(metadata);

                /* Extract metadata. */
          this.extractAndPersistMetadata(item, objectId);

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
          this.completeListeners.get(i).onCompleted(item);
        }

            /*
             * Trigger garbage collection once in a while. This is specially relevant when many small files are processed, since unused allocated memory could accumulate and trigger swapping.
             */
        if (this.count_processed % 50 == 0) {
          System.gc();
        }
      } catch (Throwable t) {
        LOGGER.error("Exception while processing path {}, {}", item, t.getMessage());
        LOGGER.error(t);
      }
    }

        /* Shutdown the FileHandler. */
    this.shutdown();
  }

  /**
   * Stops the ExtractionPipeline, relinquishing all resources.
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
      LOGGER.info("File extraction complete! It took {} to extract {} out files.",
          duration.toString(), this.count_processed);
    }
  }

  /**
   * returns the next file which can be decoded by the decoder or <code>null</code> if there are no
   * more files
   */
  protected ExtractionItemContainer nextItem(final Decoder<T> decoder) {
    while (this.itemProvider != null && this.itemProvider.isOpen()) {
      if (this.itemProvider.hasNextAvailable()) {
        Optional<ExtractionItemContainer> providerResult = itemProvider.next();
        if (!providerResult.isPresent()) {
          //No Path currently in Queue, wait
          try {
            Thread.sleep(1);
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
          continue;
        }
        ExtractionItemContainer item = providerResult.get();
        String type = MimeTypeHelper.getContentType(item.getPathForExtraction().toString());
        if (decoder.supportedFiles().contains(type)) {
          return item;
        }
        //TODO here we should possibly check if we need to cache external paths (e.g. ambry, HTTP) for decoders
        if (item.getPathForExtraction().toUri().getScheme().equals("ambry")) {
          return item;    //This is somewhat of a workaround. Currently, a Pathprovider does not provide the mediatype of a path and thus we just assume that ambry is playing nice
        }
      } else {
        try {
          Thread.sleep(5);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
    }
    if (this.itemProvider == null) {
      LOGGER.error("Path provider was null");
    }
    if (this.itemProvider != null && !this.itemProvider.isOpen()) {
      LOGGER.error("Pathprovider closed, quitting");
    }
    return null;
  }

  /**
   * Checks if the MultimediaObjectDescriptor already exists and decides whether extraction should
   * continue for that object or not (based on the ingest settings). If it does not exist, the
   * MultimediaObjectDescriptor is persisted.
   *
   * @param descriptor MultimediaObjectDescriptor that should be persisted.
   * @return true if object should be processed further or false if it should be skipped.
   */
  protected boolean checkAndPersistMultimediaObject(MultimediaObjectDescriptor descriptor) {
    if (descriptor.exists() && this.context.existenceCheck()
        == IdConfig.ExistenceCheck.CHECK_SKIP) {//this is true when a descriptor is used which has previously been retrieved from the database
      LOGGER.info("MultimediaObject {} (name: {}) already exists. This object will be skipped.",
          descriptor.getObjectId(), descriptor.getName());
      return false;
    } else if (descriptor.exists()
        && this.context.existenceCheck() == IdConfig.ExistenceCheck.CHECK_PROCEED) {
      LOGGER.info("MultimediaObject {} (name: {}) already exists. Proceeding anyway...",
          descriptor.getObjectId(), descriptor.getName());
      return true;
    } else if (descriptor.getObjectId() == null) {
      LOGGER.warn(
          "The objectId that was generated for {} is empty. This object cannot be persisted and will be skipped.",
          descriptor.getPath());
      return false;
    } else {
      this.objectWriter.write(descriptor);
      return true;
    }
  }

  /**
   * Persists a SegmentDescriptor and performs an existence check before, if so configured. Based on
   * the outcome of that persistence check and the settings in the ExtractionContext this method
   * returns true if segment should be processed further or false otherwise.
   *
   * @param descriptor SegmentDescriptor that should be persisted.
   * @return true if segment should be processed further or false if it should be skipped.
   */
  protected boolean checkAndPersistSegment(SegmentDescriptor descriptor) {
    if (descriptor.exists()
        && this.context.existenceCheck() == IdConfig.ExistenceCheck.CHECK_SKIP) {
      LOGGER.info("Segment {} already exists. This segment will be skipped.",
          descriptor.getSegmentId());
      return false;
    } else if (descriptor.exists()
        && this.context.existenceCheck() == IdConfig.ExistenceCheck.CHECK_PROCEED) {
      LOGGER.info("Segment {} already exists. Proceeding anyway...", descriptor.getSegmentId());
      return true;
    } else {
      this.segmentWriter.write(descriptor);
      return true;
    }
  }

  protected MultimediaObjectDescriptor fetchOrCreateMultimediaObjectDescriptor(
      ObjectIdGenerator generator, ExtractionItemContainer item, MediaType type) {

    MultimediaObjectDescriptor fetchedDescriptor = this.objectReader.lookUpObjectByPath(item.getObject().getPath());
    if (fetchedDescriptor.exists() && fetchedDescriptor.getMediatype() == this.context.sourceType()) {
      return fetchedDescriptor;
    }
    return MultimediaObjectDescriptor
        .mergeItem(fetchedDescriptor, generator, item, type);
  }

  /**
   * Convenience method to lookup a SegmentDescriptor for a given path and type or create a new one
   * if needed. If a new descriptor is required, SegmentDescriptor.newSegmentDescriptor() is used.
   */
  protected SegmentDescriptor fetchOrCreateSegmentDescriptor(String objectId, int segmentNumber,
      int start, int end, float startabs, float endabs) {
    String segmentId = MediaType.generateSegmentId(objectId, segmentNumber);
    return this.segmentReader.lookUpSegment(segmentId).orElse(SegmentDescriptor
        .newSegmentDescriptor(objectId, segmentNumber, start, end, startabs, endabs));
  }

  protected void extractAndPersistMetadata(ExtractionItemContainer item, String objectId) {
    for (MetadataExtractor extractor : this.metadataExtractors) {
      LOGGER.debug( "Extracting metadata for {}", extractor.getClass().getSimpleName() );
      try {
        List<MultimediaMetadataDescriptor> metadata = extractor
            .extract(objectId, item.getPathForExtraction());
        if (!metadata.isEmpty()) {
          this.metadataWriter.write(metadata);
        }
      } catch (Exception e) {
        LOGGER.error("exception during metadata extraction: {}", LogHelper.getStackTrace(e));
      }
    }
  }

  protected List<SegmentDescriptor> retrieveExistingSegments(MultimediaObjectDescriptor object) {
    return this.segmentReader.lookUpSegmentsOfObject(object.getObjectId());
  }

  /**
   * Returns a new instance of {@link Decoder} which will be used by the core-logic.
   */
  abstract Decoder<T> newDecoder();

  /**
   * Returns a new instance of Segmenter<T> that should be used with a concrete implementation
   * of this interface.
   *
   * @return Segmenter<T>
   */
  abstract Segmenter<T> newSegmenter();

  /**
   * Adds a {@link ExtractionCompleteListener} to be notified about every object for which the
   * extraction completes.
   *
   * @param listener {@link ExtractionCompleteListener}
   */
  @Override
  public void addExtractionCompleteListener(ExtractionCompleteListener listener) {
    if (listener != null && !this.completeListeners.contains(listener)) {
      LOGGER.debug("Adding Listener {}", listener.getClass().getSimpleName());
      completeListeners.add(listener);
    }
  }
}
