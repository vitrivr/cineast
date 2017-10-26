package org.vitrivr.cineast.core.config;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.vitrivr.cineast.core.data.MediaType;
import org.vitrivr.cineast.core.db.DBSelectorSupplier;
import org.vitrivr.cineast.core.db.PersistencyWriterSupplier;
import org.vitrivr.cineast.core.features.extractor.Extractor;
import org.vitrivr.cineast.core.idgenerator.ObjectIdGenerator;
import org.vitrivr.cineast.core.metadata.MetadataExtractor;
import org.vitrivr.cineast.core.run.ExtractionContextProvider;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Configures a data-ingest or extraction run, acts as an ExtractionContextProvider.
 *
 * A concrete instance be obtained by deserializing a JSON file that is compatible with the
 * structure defined by this classes and its fields.
 *
 * @author rgasser
 * @version 1.0
 * @created 13.01.17
 */
public class IngestConfig implements ExtractionContextProvider {

    /** MediaType for the Extraction run. */
    private MediaType type;

    /** Input configuration for the Extraction run*/
    private InputConfig input;

    /** List of ExtractorConfigs, one for every extractor that should be used during the Extraction run. */
    private List<ExtractorConfig> extractors = new ArrayList<>();

    /** List of ExtractorConfigs, one for every exporter that should be used during the Extraction run. */
    private List<ExtractorConfig> exporters = new ArrayList<>();

    /** List of MetadataConfig entries, one for each MetadataExtractor that should be used during an Extraction run. */
    private List<MetadataConfig> metadata = new ArrayList<>();

    /** Database-setting to use for import. Defaults to application settings. */
    private DatabaseConfig database = Config.sharedConfig().getDatabase();

    /** Configuration for extraction-pipeline. Defaults to global configuration. */
    private ExtractionPipelineConfig pipeline = Config.sharedConfig().getExtractor();

    @JsonCreator
    public IngestConfig() {
    }

    @JsonProperty(required = true)
    public MediaType getType() {
        return type;
    }
    public void setType(MediaType type) {
        this.type = type;
    }

    @JsonProperty(required = true)
    public InputConfig getInput() {
        return input;
    }
    public void setInput(InputConfig input) {
        this.input = input;
    }

    @JsonProperty
    public List<ExtractorConfig> getExtractors() {
        return extractors;
    }
    public void setExtractors(ArrayList<ExtractorConfig> extractors) {
        this.extractors = extractors;
    }

    @JsonProperty
    public List<ExtractorConfig> getExporters() {
        return exporters;
    }
    public void setExporters(ArrayList<ExtractorConfig> exporters) {
        this.exporters = exporters;
    }

    @JsonProperty
    public List<MetadataConfig> getMetadata() {
        return metadata;
    }
    public void setMetadata(List<MetadataConfig> metadata) {
        this.metadata = metadata;
    }

    @JsonProperty
    public DatabaseConfig getDatabase() {
        return database;
    }
    public void setDatabase(DatabaseConfig database) {
        /* Merge with global settings if not set. */
        DatabaseConfig global = Config.sharedConfig().getDatabase();
        if (this.database.getSelector() == null) {
          this.database.setSelector(global.getSelector());
        }
        if (this.database.getWriter() == null) {
          this.database.setWriter(global.getWriter());
        }
        if (this.database.getBatchsize() == DatabaseConfig.DEFAULT_BATCH_SIZE) {
          this.database.setBatchsize(global.getBatchsize());
        }
        if (this.database.getHost() == DatabaseConfig.DEFAULT_HOST) {
          this.database.setHost(global.getHost());
        }
        if (this.database.getPort() == DatabaseConfig.DEFAULT_PORT) {
          this.database.setPort(global.getPort());
        }
        if (this.database.getPlaintext() == DatabaseConfig.DEFAULT_PLAINTEXT) {
          this.database.setPlaintext(global.getPlaintext());
        }
        
        /* Apply. */
        this.database = database;
    }

    @JsonProperty
    public ExtractionPipelineConfig getPipeline() {
        return pipeline;
    }
    public void setPipeline(ExtractionPipelineConfig pipeline) {
        /* Merge with global settings if not set. */
        ExtractionPipelineConfig global = Config.sharedConfig().getExtractor();
        if (this.pipeline.getTaskQueueSize() == ExtractionPipelineConfig.DEFAULT_TASKQUEUE_SIZE) {
          this.pipeline.setTaskQueueSize(global.getTaskQueueSize());
        }
        if (this.pipeline.getThreadPoolSize() == ExtractionPipelineConfig.DEFAULT_THREADPOOL_SIZE) {
          this.pipeline.setThreadPoolSize(global.getThreadPoolSize());
        }
        if (this.pipeline.getShotQueueSize() == ExtractionPipelineConfig.DEFAULT_SEGMENTQUEUE_SIZE) {
          this.pipeline.setShotQueueSize(global.getShotQueueSize());
        }

        this.pipeline = pipeline;
    }

    @Override
    public Path inputPath() {
        if (this.input != null) {
            return Paths.get(this.input.getPath());
        } else {
            return null;
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