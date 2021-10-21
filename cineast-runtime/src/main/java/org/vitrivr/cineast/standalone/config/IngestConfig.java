package org.vitrivr.cineast.standalone.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.vitrivr.cineast.core.config.DatabaseConfig;
import org.vitrivr.cineast.core.config.IdConfig;
import org.vitrivr.cineast.core.config.CacheConfig;
import org.vitrivr.cineast.core.config.SegmenterConfig;
import org.vitrivr.cineast.core.data.MediaType;
import org.vitrivr.cineast.core.db.DBSelectorSupplier;
import org.vitrivr.cineast.core.db.PersistencyWriterSupplier;
import org.vitrivr.cineast.core.extraction.ExtractionContextProvider;
import org.vitrivr.cineast.core.extraction.idgenerator.ObjectIdGenerator;
import org.vitrivr.cineast.core.extraction.segmenter.general.Segmenter;
import org.vitrivr.cineast.core.features.extractor.Extractor;
import org.vitrivr.cineast.core.extraction.metadata.MetadataExtractor;
import org.vitrivr.cineast.standalone.run.ExtractionContainerProvider;
import org.vitrivr.cineast.standalone.run.path.SessionContainerProvider;
import org.vitrivr.cineast.standalone.run.path.SingletonContainerProvider;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Configures a data-ingest or extraction run, acts as an ExtractionContextProvider.
 *
 * A concrete instance can be obtained by deserializing a JSON file that is compatible with the
 * structure defined by this classes and its fields.
 *
 */
public class IngestConfig implements ExtractionContextProvider {
    /** MediaType for the Extraction run. */
    private final MediaType type;

    /** Input configuration for the Extraction run*/
    private final InputConfig input;

    /** List of {@link ExtractorConfig}s, one for every {@link Extractor} that should be used during the extraction run. */
    private final List<ExtractorConfig> extractors;

    /** List of {@link ExtractorConfig}s, one for every exporter that should be used during the extraction run. */
    private final List<ExtractorConfig> exporters;

    /** List of {@link MetadataConfig} entries, one for each {@link MetadataExtractor} that should be used during an extraction run. */
    private final List<MetadataConfig> metadata;

    /** Database-setting to use for import. Defaults to application settings. */
    private final DatabaseConfig database;

    /** Configuration for extraction-pipeline. Defaults to global configuration. */
    private final ExtractionPipelineConfig pipeline;

    /** Configuration for extraction-pipeline. Defaults to global configuration. */
    private final SegmenterConfig segmenter;

    /** Configuration for extraction-pipeline. Defaults to global configuration. */
    private final CacheConfig cacheConfig;

    /**
     * Constructor for {@link IngestConfig}. Used by Jackson for JSON deserialization.
     *
     */
    @JsonCreator
    public IngestConfig(@JsonProperty(value = "type") MediaType type,
                        @JsonProperty(value = "input", required = true) InputConfig input,
                        @JsonProperty(value = "extractors") List<ExtractorConfig> extractors,
                        @JsonProperty(value = "exporters") List<ExtractorConfig> exporters,
                        @JsonProperty(value = "metadata") List<MetadataConfig> metadata,
                        @JsonProperty(value = "database") DatabaseConfig database,
                        @JsonProperty(value = "pipeline") ExtractionPipelineConfig pipeline,
                        @JsonProperty(value = "segmenter") SegmenterConfig segmenter,
                        @JsonProperty(value = "imagecache") CacheConfig cacheConfig) {

        if (input == null) throw new IllegalArgumentException("You have not defined an 'type' or 'input' object in your ingest configuration file.");
        this.type = type;
        this.input = input;

        /* Initialize list of ExtractorConfig. */
        if (extractors == null) extractors = new ArrayList<>(0);
        this.extractors = extractors;

        /* Initialize list of ExtractorConfigs. */
        if (exporters == null) exporters = new ArrayList<>(0);
        this.exporters = exporters;

        /* Initialize list of MetadataExtractors. */
        if (metadata == null) metadata = new ArrayList<>(0);
        this.metadata = metadata;

        /* Initialize DatabaseConfig. */
        final DatabaseConfig globalDb = Config.sharedConfig().getDatabase();
        if (database == null) database = globalDb;
        this.database = database;
        if (this.database.getSelector() == null) {
            this.database.setSelector(globalDb.getSelector());
        }
        if (this.database.getWriter() == null) {
            this.database.setWriter(globalDb.getWriter());
        }
        if (this.database.getBatchsize() == DatabaseConfig.DEFAULT_BATCH_SIZE) {
            this.database.setBatchsize(globalDb.getBatchsize());
        }
        if (this.database.getHost().equals(DatabaseConfig.DEFAULT_HOST)) {
            this.database.setHost(globalDb.getHost());
        }
        if (this.database.getPort() == DatabaseConfig.DEFAULT_PORT) {
            this.database.setPort(globalDb.getPort());
        }
        if (this.database.getPlaintext() == DatabaseConfig.DEFAULT_PLAINTEXT) {
            this.database.setPlaintext(globalDb.getPlaintext());
        }

        /* Merge with global settings if not set. */
        final ExtractionPipelineConfig globalExt = Config.sharedConfig().getExtractor();
        if (pipeline == null) pipeline = globalExt;
        this.pipeline = pipeline;
        if (this.pipeline.getTaskQueueSize() == ExtractionPipelineConfig.DEFAULT_TASKQUEUE_SIZE) {
            this.pipeline.setTaskQueueSize(globalExt.getTaskQueueSize());
        }
        if (this.pipeline.getThreadPoolSize() == ExtractionPipelineConfig.DEFAULT_THREADPOOL_SIZE) {
            this.pipeline.setThreadPoolSize(globalExt.getThreadPoolSize());
        }
        if (this.pipeline.getShotQueueSize() == ExtractionPipelineConfig.DEFAULT_SEGMENTQUEUE_SIZE) {
            this.pipeline.setShotQueueSize(globalExt.getShotQueueSize());
        }

        /* Set SegmenterConfig. */
        if (segmenter == null) segmenter = new SegmenterConfig(this.type);
        this.segmenter = segmenter;

        /* Set ImageCacheConfig. */
        if (cacheConfig == null) cacheConfig = Config.sharedConfig().getCache();
        this.cacheConfig = cacheConfig;
    }

    @JsonProperty(required = true)
    public InputConfig getInput() {
        return input;
    }

    @JsonProperty
    public List<ExtractorConfig> getExtractors() {
        return extractors;
    }

    @JsonProperty
    public List<ExtractorConfig> getExporters() {
        return exporters;
    }

    @JsonProperty
    public List<MetadataConfig> getMetadata() {
        return metadata;
    }

    @JsonProperty
    public DatabaseConfig getDatabase() {
        return database;
    }

    @JsonProperty
    public ExtractionPipelineConfig getPipeline() {
        return pipeline;
    }

    @JsonProperty
    public SegmenterConfig getSegmenter() {
        return this.segmenter;
    }

    @Override
    public Optional<Path> inputPath() {
        if (this.input != null) {
            return Optional.of(Paths.get(this.input.getPath()));
        } else {
            return Optional.empty();
        }
    }

    @Override
    public Optional<Path> relPath() {
        if (this.input == null || this.input.getRelTo() == null) {
            return Optional.empty();
        }
        return Optional.of(Paths.get(this.input.getRelTo()));
    }

    public ExtractionContainerProvider pathProvider() {
        if (this.input != null) {
            return new SingletonContainerProvider(Paths.get(this.input.getPath()));
        } else {
            return new SessionContainerProvider();
        }
    }

    /**
     * Determines the MediaType of the source material. Only one media-type
     * can be specified per ExtractionContextProvider.
     *
     * @return Media-type of the source material.
     */
    @Override
    @JsonProperty
    public MediaType getType() {
        return this.type;
    }

    /**
     * Returns a list of extractor classes that should be used for
     * the extraction run!
     *
     * @return List of named extractors.
     */
    @Override
    public List<Extractor> extractors() {
        return this.extractors.stream()
                .map(ExtractorConfig::getExtractor)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * Returns a list of exporter classes that should be invoked during extraction. Exporters
     * usually generate some representation and persistently store that information somewhere.
     *
     * @return List of named exporters.
     */
    @Override
    public List<Extractor> exporters() {
        return this.exporters.stream()
                .map(ExtractorConfig::getExporter)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * Returns a list of metadata extractor classes that should be invoked during extraction. MetadataExtractor's
     * usually read some metadata from a file.
     *
     * @return List of named exporters.
     */
    @Override
    public List<MetadataExtractor> metadataExtractors() {
        return this.metadata.stream()
                .map(MetadataConfig::getMetadataExtractor)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * Selects, configures and returns a new instance of the {@link Segmenter} that was configured in the
     * current instance of {@link ExtractionContextProvider}.
     *
     * @return {@link Segmenter} that was configured in the current instance of {@link ExtractionContextProvider}
     */
    @Override
    public <T> Segmenter<T> newSegmenter() {
        return this.segmenter.newSegmenter(this);
    }

    /**
     * Limits the depth of recursion when extraction folders of files.
     *
     * @return A number greater than zero.
     */
    @Override
    public int depth() {
        return this.input.getDepth();
    }

    /**
     * Returns an instance of ObjectIdGenerator that should be used to generated MultimediaObject ID's
     * during an extraction run.
     *
     * @return ObjectIdGenerator
     */
    @Override
    public ObjectIdGenerator objectIdGenerator() {
        return this.getInput().getId().getGenerator();
    }

    @Override
    public IdConfig.ExistenceCheck existenceCheck() {
        return this.input.getId().getExistenceCheckMode();
    }

    /**
     * Returns the PersistencyWriterSupplier that can be used during the extraction run to
     * obtain PersistencyWriter instance.
     *
     * @return PersistencyWriterSupplier instance used obtain a PersistencyWriter.
     */
    @Override
    public PersistencyWriterSupplier persistencyWriter() {
       return this.database.getWriterSupplier();
    }

    /**
     * Returns the size of a batch. A batch is used when persisting data. Entities will be kept in
     * memory until the batchsize limit is hit at which point they will be persisted.
     *
     * @return Batch size.
     */
    @Override
    public Integer batchSize() {
        return this.database.getBatchsize();
    }


    /**
     * Returns the DBSelectorSupplier that can be used during the extraction run to obtain
     * a DBSelector instance.
     *
     * @return DBSelectorSupplier instance used obtain a DBSelector.
     */
    @Override
    public DBSelectorSupplier persistencyReader() {
        return this.database.getSelectorSupplier();
    }

    @Override
    public File outputLocation() {
        return this.pipeline.getOutputLocation();
    }

    @Override
    public int threadPoolSize() {
        return this.pipeline.getThreadPoolSize();
    }

    /**
     * Returns the size of the extraction task queue. Limits how many extraction tasks can be dispatched and kept in memory.
     *
     * @return Size of extraction task queue.
     */
    @Override
    public Integer taskQueueSize() {
        return this.pipeline.getTaskQueueSize();
    }

    /**
     * Returns the size of the segment queue. Segments can be created and kept in memory until that queue is full.
     *
     * @return Size of segment queue.
     */
    @Override
    public Integer segmentQueueSize() {
        return this.pipeline.getShotQueueSize();
    }

    /**
     * Returns the instance of {@link CacheConfig}.
     *
     * @return {@link CacheConfig} reference.
     */
    @Override
    public CacheConfig cacheConfig() {
        return this.cacheConfig;
    }
}
