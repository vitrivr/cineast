package org.vitrivr.cineast.standalone.run;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.config.IdConfig;
import org.vitrivr.cineast.core.data.MediaType;
import org.vitrivr.cineast.core.data.entities.MediaObjectDescriptor;
import org.vitrivr.cineast.core.data.entities.MediaObjectMetadataDescriptor;
import org.vitrivr.cineast.core.data.entities.MediaSegmentDescriptor;
import org.vitrivr.cineast.core.data.m3d.Mesh;
import org.vitrivr.cineast.core.data.segments.Model3DSegment;
import org.vitrivr.cineast.core.data.segments.SegmentContainer;
import org.vitrivr.cineast.core.db.DBSelectorSupplier;
import org.vitrivr.cineast.core.db.PersistencyWriterSupplier;
import org.vitrivr.cineast.core.db.dao.reader.MediaObjectReader;
import org.vitrivr.cineast.core.db.dao.reader.MediaSegmentReader;
import org.vitrivr.cineast.core.db.dao.writer.MediaObjectMetadataWriter;
import org.vitrivr.cineast.core.db.dao.writer.MediaObjectWriter;
import org.vitrivr.cineast.core.db.dao.writer.MediaSegmentWriter;
import org.vitrivr.cineast.core.extraction.ExtractionContextProvider;
import org.vitrivr.cineast.core.extraction.decode.audio.FFMpegAudioDecoder;
import org.vitrivr.cineast.core.extraction.decode.general.Decoder;
import org.vitrivr.cineast.core.extraction.decode.image.DefaultImageDecoder;
import org.vitrivr.cineast.core.extraction.decode.image.ImageSequenceDecoder;
import org.vitrivr.cineast.core.extraction.decode.m3d.ModularMeshDecoder;
import org.vitrivr.cineast.core.extraction.decode.video.FFMpegVideoDecoder;
import org.vitrivr.cineast.core.extraction.idgenerator.ObjectIdGenerator;
import org.vitrivr.cineast.core.extraction.metadata.MetadataExtractor;
import org.vitrivr.cineast.core.extraction.segmenter.audio.ConstantLengthAudioSegmenter;
import org.vitrivr.cineast.core.extraction.segmenter.general.PassthroughSegmenter;
import org.vitrivr.cineast.core.extraction.segmenter.general.Segmenter;
import org.vitrivr.cineast.core.extraction.segmenter.image.ImageSegmenter;
import org.vitrivr.cineast.core.extraction.segmenter.image.ImageSequenceSegmenter;
import org.vitrivr.cineast.core.extraction.segmenter.video.VideoHistogramSegmenter;
import org.vitrivr.cineast.core.features.abstracts.MetadataFeatureModule;
import org.vitrivr.cineast.core.util.LogHelper;
import org.vitrivr.cineast.core.util.MimeTypeHelper;
import org.vitrivr.cineast.core.util.ReflectionHelper;
import org.vitrivr.cineast.standalone.config.Config;
import org.vitrivr.cineast.standalone.config.IngestConfig;
import org.vitrivr.cineast.standalone.runtime.ExtractionPipeline;

/**
 * This class is used to extract a continuous list of {@link ExtractionItemContainer}s.
 *
 * Additionally, has support to extract only specific media types by providing the desired {@link MediaType} in the constructor.
 *
 * @author silvan on 16.04.18.
 * @version 1.1
 */
public class GenericExtractionItemHandler implements Runnable, ExtractionItemProcessor {


  private static final Logger LOGGER = LogManager.getLogger();

  private final MediaObjectWriter objectWriter;
  private final MediaSegmentWriter mediaSegmentWriter;
  private final MediaObjectMetadataWriter metadataWriter;
  private final MediaObjectReader objectReader;
  private final MediaSegmentReader segmentReader;
  private final ExtractionContextProvider context;
  private final ExtractionContainerProvider pathProvider;
  private final MediaType mediaType;

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

  private final ExtractionPipeline pipeline;
  private long count_processed = 0;

  private Map<MediaType, Pair<Supplier<Decoder>, Supplier<Segmenter>>> handlers = new HashMap<>();

  private Map<MediaType, Pair<Decoder, Segmenter>> handlerCache = new HashMap<>();

  /**
   * @param pathProvider where the {@link ExtractionItemContainer}s will be coming from
   * @param context context for this extraction run
   * @param mediaType can be null. if provided, will be used for all given items
   */
  public GenericExtractionItemHandler(ExtractionContainerProvider pathProvider, IngestConfig context, MediaType mediaType) {
    this.context = context;

    this.pathProvider = pathProvider;
    this.mediaType = mediaType;

    final PersistencyWriterSupplier writerSupplier = context.persistencyWriter();
    this.objectWriter = new MediaObjectWriter(writerSupplier.get());
    this.mediaSegmentWriter = new MediaSegmentWriter(writerSupplier.get(), context.batchSize());
    this.metadataWriter = new MediaObjectMetadataWriter(writerSupplier.get(), context.batchSize());

    final DBSelectorSupplier readerSupplier = context.persistencyReader();
    this.objectReader = new MediaObjectReader(readerSupplier.get());
    this.segmentReader = new MediaSegmentReader(readerSupplier.get());

    this.pipeline = new ExtractionPipeline(context);
    this.metadataExtractors = context.metadataExtractors();

    //Reasonable Defaults
    handlers.put(MediaType.IMAGE, new ImmutablePair<>(DefaultImageDecoder::new, () -> new ImageSegmenter(context)));
    handlers.put(MediaType.IMAGE_SEQUENCE, new ImmutablePair<>(ImageSequenceDecoder::new, () -> new ImageSequenceSegmenter(context)));
    handlers.put(MediaType.AUDIO, new ImmutablePair<>(FFMpegAudioDecoder::new, () -> new ConstantLengthAudioSegmenter(context)));
    handlers.put(MediaType.VIDEO, new ImmutablePair<>(FFMpegVideoDecoder::new, () -> new VideoHistogramSegmenter(context)));
    handlers.put(MediaType.MODEL3D, new ImmutablePair<>(ModularMeshDecoder::new, () -> new PassthroughSegmenter<Mesh>() {
      @Override
      protected SegmentContainer getSegmentFromContent(Mesh content) {
        return new Model3DSegment(content);
      }
    }));
    //Config overwrite
    Config.sharedConfig().getDecoders().forEach((type, decoderConfig) -> {
      handlers.put(type, new ImmutablePair<>(ReflectionHelper.newDecoder(decoderConfig.getDecoder(), type), handlers.getOrDefault(type, null)).getRight());
    });
    //TODO Config should allow for multiple segmenters

    this.handlers.forEach((key, value) -> handlerCache.put(key, ImmutablePair.of(value.getLeft().get(), value.getRight().get())));

  }

  @Override
  @SuppressWarnings("unchecked")
  public void run() {
    LOGGER.info("Starting extraction");

    this.executorService.execute(pipeline);

    final ObjectIdGenerator generator = this.context.objectIdGenerator();
    Pair<ExtractionItemContainer, MediaType> pair = null;

    /* Initalize all Metadata Extractors */
    for (MetadataExtractor extractor : this.metadataExtractors) {
      LOGGER.debug("Initializing metadata extractor {}", extractor.getClass().getSimpleName());
      if (extractor instanceof MetadataFeatureModule) {
        ((MetadataFeatureModule<?>) extractor).init(this.context.persistencyWriter(), this.context.batchSize());
      } else {
        extractor.init();
      }
    }

    /* Process until there's nothing left*/
    while ((pair = this.nextItem()) != null) {
      try {
        LOGGER.info("Processing path {} and mediatype {}", pair.getLeft(), pair.getRight());

        if (handlerCache.get(pair.getRight()) == null) {
          LOGGER.error("Unknown mediatype {}, exiting extraction", pair.getRight());
          break;
        }

        /* Clear non-reusable segmenter */
        handlerCache.compute(pair.getRight(), (mediaType, cache) -> {
          Decoder decoder = null;
          if (cache.getLeft() != null) {
            if (cache.getLeft().canBeReused()) {
              decoder = cache.getLeft();
            }
          }
          return ImmutablePair.of(decoder, null);
        });

        /* Put a new decoder in the cache if there's not one already there */
        if (handlerCache.get(pair.getRight()).getLeft() == null) {
          Decoder decoder = handlers.get(pair.getRight()).getLeft().get();
          handlerCache.compute(pair.getRight(), (mediaType, cache) -> new ImmutablePair<>(decoder, cache.getRight()));
        }
        /* Put a new segmenter in the cache if there's not one already there */
        if (handlerCache.get(pair.getRight()).getRight() == null) {
          Segmenter segmenter = handlers.get(pair.getRight()).getRight().get();
          handlerCache.compute(pair.getRight(), (mediaType, cache) -> new ImmutablePair<>(cache.getLeft(), segmenter));
        }

        Decoder decoder = handlerCache.get(pair.getRight()).getLeft();
        Segmenter segmenter = handlers.get(pair.getRight()).getRight().get();

        if (decoder.init(pair.getLeft().getPathForExtraction(), Config.sharedConfig().getDecoders().get(pair.getRight()), Config.sharedConfig().getCache())) {
          /* Create / lookup MediaObjectDescriptor for new file. */
          final MediaObjectDescriptor descriptor = this
              .fetchOrCreateMultimediaObjectDescriptor(generator, pair.getLeft(), pair.getRight());
          if (!this.checkAndPersistMultimediaObject(descriptor)) {
            continue;
          }

          final String objectId = descriptor.getObjectId();
          int segmentNumber = 1;

          segmenter.init(decoder, descriptor);
          this.executorService.execute(segmenter);

          while (!segmenter.complete()) {
            try {
              final SegmentContainer container = segmenter.getNext();
              if (container != null) {
                /* Create segment-descriptor and try to persist it. */
                MediaSegmentDescriptor mediaSegmentDescriptor;
                if (container.getId() != null) {
                  mediaSegmentDescriptor = this.fetchOrCreateSegmentDescriptor(objectId, container.getId(), segmentNumber, container.getStart(), container.getEnd(), container.getAbsoluteStart(), container.getAbsoluteEnd()); /* Special case; segment ID is determined by container (image sequences only) */
                } else {
                  mediaSegmentDescriptor = this.fetchOrCreateSegmentDescriptor(objectId, segmentNumber, container.getStart(), container.getEnd(), container.getAbsoluteStart(), container.getAbsoluteEnd());
                }
                container.setId(mediaSegmentDescriptor.getSegmentId());
                container.setSuperId(mediaSegmentDescriptor.getObjectId());
                if (!this.checkAndPersistSegment(mediaSegmentDescriptor)) {
                  continue;
                }

                int emissionTimeout = 1000;

                while (!this.pipeline.emit(container, emissionTimeout)) {
                  LOGGER.debug(
                      "ExtractionPipeline is full - deferring emission of segment. Consider increasing the thread-pool count for the extraction pipeline.");
                  Thread.sleep(emissionTimeout);
                  //emissionTimeout += 500;
                }

                segmentNumber += 1;
              }
            } catch (InterruptedException e) {
              LOGGER.log(Level.ERROR,
                  "Thread was interrupted while the extraction process was running. Aborting...");
              break;
            }
          }

          List<MediaObjectMetadataDescriptor> metadata = pair.getLeft().getMetadata().stream()
              .map(el -> MediaObjectMetadataDescriptor.fromExisting(el, objectId))
              .collect(Collectors.toList());
          this.metadataWriter.write(metadata);

          /* Extract metadata. */
          this.extractAndPersistMetadata(pair.getLeft(), objectId);

          /* Force flush the segment, object and metadata information. */
          this.mediaSegmentWriter.flush();
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
          final MediaType type = pair.getRight();
          handlerCache.compute(type, (mediaType, cache) -> ImmutablePair.of(handlers.get(type).getLeft().get(), cache.getRight()));
        }
        //We assume segmenters are reusable

        for (ExtractionCompleteListener completeListener : this.completeListeners) {
          completeListener.onCompleted(pair.getLeft());
        }

        /*
         * Trigger garbage collection once in a while. This is specially relevant when many small files are processed, since unused allocated memory could accumulate and trigger swapping.
         */
        if (this.count_processed % 50 == 0) {
          System.gc();
        }
      } catch (Throwable t) {
        LOGGER.error("Exception while processing path {}, {}", pair.getLeft(), t.getMessage());
        t.printStackTrace();
      }
    }
    shutdown();
  }

  /**
   * Stops the ExtractionPipeline, relinquishing all resources.
   */
  private void shutdown() {
    try {
      Thread.sleep(5000);

      LOGGER.info("File decoding and segmenting complete! Shutting down...");
      this.executorService.shutdown();
      this.pipeline.stop();

      LOGGER.info("Waiting for ExtractionPipeline to terminate! This could take a while.");
      this.executorService.awaitTermination(30, TimeUnit.MINUTES);
    } catch (InterruptedException e) {
      LOGGER.warn("Interrupted while waiting for ExtractionPipeline to shutdown!");
    } finally {
      for (MetadataExtractor extractor : this.metadataExtractors) {
        LOGGER.debug("Closing metadata extractor {}", extractor.getClass().getSimpleName());
        extractor.finish();
      }
      LOGGER.debug("Closing & flushing all writers");
      if (pathProvider != null) {
        pathProvider.close();
      }
      this.mediaSegmentWriter.close();
      this.objectWriter.close();
      this.metadataWriter.close();
      this.objectReader.close();
      this.segmentReader.close();
      this.handlerCache.values().forEach(el -> {
        if (el.getLeft() != null) {
          el.getLeft().close();
        }
        if (el.getRight() != null) {
          el.getRight().close();
        }
      });
      this.pathProvider.close();
      LOGGER.debug("Shutdown complete");
      this.completeListeners.forEach(ExtractionCompleteListener::extractionComplete);
    }
  }

  /**
   * Provide the next {@link ExtractionItemContainer} and its corresponding media type. Waits with returning until the next item is available.
   *
   * @return can be null. If it is null, it does not make sense to call this method again since the underlying {@link ExtractionContainerProvider} is either not existing or closed
   */
  private Pair<ExtractionItemContainer, MediaType> nextItem() {
    if (this.pathProvider == null) {
      LOGGER.error("Path provider was null, returning null");
      return null;
    }
    if (!this.pathProvider.isOpen()) {
      LOGGER.error("Pathprovider closed upon entrance, returning null");
      return null;
    }

    /* If the provider is still open, poll it until it's closed*/
    while (this.pathProvider.isOpen()) {
      /* If there's no item available, sleep */
      if (!this.pathProvider.hasNextAvailable()) {
        try {
          Thread.sleep(100);
        } catch (InterruptedException e) {
          throw new RuntimeException(e);
        }
        continue;
      }
      Optional<ExtractionItemContainer> providerResult = pathProvider.next();
      /* Check if we've received something */
      if (!providerResult.isPresent()) {
        continue;
      }
      ExtractionItemContainer item = providerResult.get();

      /* Get content type */
      String type = MimeTypeHelper.getContentType(item.getPathForExtraction().toString());

      /* If a media type is specified, use it*/
      if (item.getObject() != null && item.getObject().getMediatype() != null && item.getObject().getMediatype() != MediaType.UNKNOWN) {
        return new ImmutablePair<>(item, item.getObject().getMediatype());
      }

      /* if we were given a default media type, try to use it*/
      if (mediaType != null) {
        /* if the given decoder supports the item type, use it*/
        if (handlerCache.get(mediaType).getKey().supportedFiles().contains(type)) {
          return new ImmutablePair<>(item, mediaType);
        }
        /* if not, log an  error and move on */
        LOGGER.error("Media Type {} does not support file type {}", mediaType, type);
        continue;
      }

      /* Get the appropriate handler for this item. We ignore image sequences if they're not specified because they support the same file types as images*/
      Optional<Map.Entry<MediaType, Pair<Decoder, Segmenter>>> match = handlerCache.entrySet().stream().filter(handler -> handler != null && handler.getValue().getKey() != null && handler.getValue().getKey().supportedFiles().contains(type) && handler.getKey() != MediaType.IMAGE_SEQUENCE).findFirst();
      if (match.isPresent()) {
        return new ImmutablePair<>(item, match.get().getKey());
      } else {
        if (Files.isDirectory(item.getPathForExtraction())) {
          LOGGER.debug("Not processing directory {}. Its content might be processed, but the directory itself is only processed for IMAGE_SEQUENCES. If you wish to use those, please specify the type explicitly in the extraction config.", item.getPathForExtraction());
        } else {
          /* If no appropriate handler is found, we log an error and continue with the next item */
          LOGGER.error("No matching handlers found for type {} and item {}", type, item);
          handlers.forEach((key, value) -> LOGGER.debug(key + " | " + value));
        }
      }
      //TODO Add support for separate filesystems.
    }
    /* Path provider has been closed */
    LOGGER.info("Pathprovider closed, returning null.");
    return null;
  }

  /**
   * Checks if the MediaObjectDescriptor already exists and decides whether extraction should continue for that object or not (based on the ingest settings). If it does not exist, the MediaObjectDescriptor is persisted.
   *
   * @param descriptor MediaObjectDescriptor that should be persisted.
   * @return true if object should be processed further or false if it should be skipped.
   */
  protected boolean checkAndPersistMultimediaObject(MediaObjectDescriptor descriptor) {
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
   * Persists a MediaSegmentDescriptor and performs an existence check before, if so configured. Based on the outcome of that persistence check and the settings in the ExtractionContext this method returns true if segment should be processed further or false otherwise.
   *
   * @param descriptor MediaSegmentDescriptor that should be persisted.
   * @return true if segment should be processed further or false if it should be skipped.
   */
  protected boolean checkAndPersistSegment(MediaSegmentDescriptor descriptor) {
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
      this.mediaSegmentWriter.write(descriptor);
      return true;
    }
  }

  protected MediaObjectDescriptor fetchOrCreateMultimediaObjectDescriptor(
      ObjectIdGenerator generator, ExtractionItemContainer item, MediaType type) {

    MediaObjectDescriptor fetchedDescriptor = this.objectReader
        .lookUpObjectByPath(item.getObject().getPath());
    if (fetchedDescriptor.exists() && fetchedDescriptor.getMediatype() == type) {
      return fetchedDescriptor;
    }
    return GenericExtractionItemHandler.mergeItem(fetchedDescriptor, generator, item, type);
  }

  /**
   * Convenience method to lookup a MediaSegmentDescriptor for a given properties and type or create a new one if needed. The {@link MediaSegmentDescriptor}'s ID is auto-generated.
   *
   * @return {@link MediaSegmentDescriptor}
   */
  protected MediaSegmentDescriptor fetchOrCreateSegmentDescriptor(String objectId, int segmentNumber, int start, int end, float startabs, float endabs) {
    String segmentId = MediaType.generateSegmentId(objectId, segmentNumber);
    return this.segmentReader.lookUpSegment(segmentId).orElse(new MediaSegmentDescriptor(objectId, segmentId, segmentNumber, start, end, startabs, endabs, false));
  }

  /**
   * Convenience method to lookup a MediaSegmentDescriptor for a given properties and type or create a new one if needed. The {@link MediaSegmentDescriptor}'s ID is provided.
   *
   * @return {@link MediaSegmentDescriptor}
   */
  protected MediaSegmentDescriptor fetchOrCreateSegmentDescriptor(String objectId, String segmentId, int segmentNumber, int start, int end, float startabs, float endabs) {
    return this.segmentReader.lookUpSegment(segmentId).orElse(new MediaSegmentDescriptor(objectId, segmentId, segmentNumber, start, end, startabs, endabs, false));
  }

  protected void extractAndPersistMetadata(ExtractionItemContainer item, String objectId) {
    for (MetadataExtractor extractor : this.metadataExtractors) {
      LOGGER.debug("Extracting metadata with {}", extractor.getClass().getSimpleName());
      try {
        List<MediaObjectMetadataDescriptor> metadata = extractor
            .extract(objectId, item.getPathForExtraction());
        if (!metadata.isEmpty()) {
          this.metadataWriter.write(metadata);
        }
      } catch (Exception e) {
        LOGGER.error("exception during metadata extraction: {}", LogHelper.getStackTrace(e));
      }
    }
  }

  /**
   * create a new descriptor based on the provided one.
   *
   * if an id is already given in the descriptor, it takes precedence over generating a new one. if a new path is provided as an argument, it takes precedence over one which might already be existing in the descriptor. if a new type is provided as an argument, it takes precedence over one which might already be existing in the descriptor.
   *
   * The exists variable is taken from the provided descriptor, since that is more current than the one provided in the item
   */
  public static MediaObjectDescriptor mergeItem(MediaObjectDescriptor descriptor,
      ObjectIdGenerator generator, ExtractionItemContainer item, MediaType type) {
    Path _path = item.getPathForExtraction() == null ? Paths.get(descriptor.getPath())
        : item.getPathForExtraction();
    String _name =
        StringUtils.isEmpty(item.getObject().getName()) ? StringUtils.isEmpty(descriptor.getName())
            ? MediaObjectDescriptor.cleanPath(_path.getFileName()) : descriptor.getName()
            : item.getObject().getName();
    boolean exists = descriptor.exists();
    MediaType _type = type == null ? descriptor.getMediatype() : type;
    String _id =
        StringUtils.isEmpty(item.getObject().getObjectId()) ?
            StringUtils.isEmpty(descriptor.getObjectId())
                ? generator.next(_path, _type) : descriptor.getObjectId()
            : item.getObject().getObjectId();
    String storagePath = StringUtils.isEmpty(item.getObject().getPath()) ? descriptor.getPath()
        : item.getObject().getPath();
    return new MediaObjectDescriptor(_id, _name, storagePath, _type, exists);
  }


  @Override
  public void addExtractionCompleteListener(ExtractionCompleteListener listener) {
    if (listener != null && !this.completeListeners.contains(listener)) {
      LOGGER.debug("Adding Listener {}", listener.getClass().getSimpleName());
      completeListeners.add(listener);
    }
  }
}
