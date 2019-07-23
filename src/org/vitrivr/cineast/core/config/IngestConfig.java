package org.vitrivr.cineast.core.config;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.data.MediaType;
import org.vitrivr.cineast.core.db.DBSelectorSupplier;
import org.vitrivr.cineast.core.db.PersistencyWriterSupplier;
import org.vitrivr.cineast.core.features.extractor.Extractor;
import org.vitrivr.cineast.core.idgenerator.ObjectIdGenerator;
import org.vitrivr.cineast.core.metadata.MetadataExtractor;
import org.vitrivr.cineast.core.run.ExtractionContextProvider;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.vitrivr.cineast.core.run.ExtractionContainerProvider;
import org.vitrivr.cineast.core.run.path.SessionContainerProvider;
import org.vitrivr.cineast.core.run.path.SingletonContainerProvider;
import org.vitrivr.cineast.core.segmenter.general.Segmenter;

/**
 * Configures a data-ingest or extraction run, acts as an ExtractionContextProvider.
 *
 * A concrete instance can be obtained by deserializing a JSON file that is compatible with the
 * structure defined by this classes and its fields.
 *
 * @author rgasser
 * @version 1.0
 * @created 13.01.17
 */
public final class IngestConfig implements ExtractionContextProvider {

    private static final Logger LOGGER = LogManager.getLogger();

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
                        @JsonProperty(value = "segmenter") SegmenterConfig segmenter) {

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
    }

    @JsonProperty(required = true)
    public MediaType getType() {
        return type;
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
    public MediaType sourceType() {
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
     * Offset into the list of files that are being distracted.
     *
     * @return A positive number or zero
     */
    @Override
    @Deprecated
    public int skip() {
        return this.input.getSkip();
    }

    /**
     * Limits the number of files that should be extracted. This a predicate is applied
     * before extraction starts. If extraction fails for some fails the effective number
     * of extracted files may be lower.
     *
     * @return A number greater than zero.
     */
    @Override
    @Deprecated
    public int limit() {
        return this.input.getLimit();
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

    /**
     *
     * @return
     */
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
     * Returns the DBSelectorSupplier that can be used during the extraction run to obtain
     * a DBSelector instance.
     *
     * @return DBSelectorSupplier instance used obtain a DBSelector.
     */
    @Override
    public DBSelectorSupplier persistencyReader() {
        return this.database.getSelectorSupplier();
    }

    /**
     *
     * @return
     */
    @Override
    public File outputLocation() {
        return this.pipeline.getOutputLocation();
    }

    /**
     *
     * @return
     */
    @Override
    public int threadPoolSize() {
        return this.pipeline.getThreadPoolSize();
    }

    /**
     *
     * @return
     */
    @Override
    public Integer taskQueueSize() {
        return this.pipeline.getTaskQueueSize();
    }

    /**
     *
     * @return
     */
    @Override
    public Integer segmentQueueSize() {
        return this.pipeline.getShotQueueSize();
    }

    /**
     * Returns the size of a batch. A batch is used when persisting data. Entities will be kept in
     * memory until the batchsize limit is hit at which point they will be persisted.
     *
     * @return Batch size.
     */
    @Override
    public Integer getBatchsize() {
        return this.database.getBatchsize();
    }
}
