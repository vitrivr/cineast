package org.vitrivr.cineast.core.config;

import java.io.File;
import java.util.HashMap;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.data.MediaType;
import org.vitrivr.cineast.core.util.json.JacksonJsonProvider;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Config {
    private static final Logger LOGGER = LogManager.getLogger();

    public static final UUID UNIQUE_ID = UUID.randomUUID();

    /** Global, shared instance of the Config object. Gets loading during application startup. */
    private volatile static Config sharedConfig;

    private APIConfig api;
    private DatabaseConfig database;
    private RetrieverConfig retriever;
    private ExtractionPipelineConfig extractor;
    private ImageCacheConfig imagecache;
    private QueryConfig query;
    private HashMap<MediaType, DecoderConfig> decoders;
    private BenchmarkConfig benchmark = new BenchmarkConfig();
    private MonitoringConfig monitoring = new MonitoringConfig();


    /**
     * Accessor for shared (i.e. application wide) configuration.
     *
     * @return Currently shared instance of Config.
     */
    public synchronized static Config sharedConfig() {
        if (sharedConfig == null) {
            sharedConfig = loadConfig("cineast.json");
        }
        return sharedConfig;
    }

    /**
     * Loads a config file and thereby replaces the shared instance of the Config.
     *
     * @param name Name of the config file.
     */
  public static Config loadConfig(String name) {
    Config config = (new JacksonJsonProvider()).toObject(new File(name), Config.class);

    if (config == null) {
      LOGGER.warn("Could not read config file '{}'.", name);
      return null;
    } else {
      LOGGER.info("Config file loaded!");
      sharedConfig = config;
      return config;
    }
  }

    @JsonProperty
    public APIConfig getApi() {
        return api;
    }
    public void setApi(APIConfig api) {
        this.api = api;
    }

    @JsonProperty
    public DatabaseConfig getDatabase() {
        return database;
    }
    public void setDatabase(DatabaseConfig database) {
        this.database = database;
    }

    @JsonProperty
    public RetrieverConfig getRetriever() {
        return retriever;
    }
    public void setRetriever(RetrieverConfig retriever) {
        this.retriever = retriever;
    }

    @JsonProperty
    public ExtractionPipelineConfig getExtractor() {
        return extractor;
    }
    public void setExtractor(ExtractionPipelineConfig extractor) {
        this.extractor = extractor;
    }

    @JsonProperty
    public ImageCacheConfig getImagecache() {
        return imagecache;
    }
    public void setImagecache(ImageCacheConfig imagecache) {
        this.imagecache = imagecache;
    }


    @JsonProperty
    public QueryConfig getQuery() {
        return query;
    }
    public void setQuery(QueryConfig query) {
        this.query = query;
    }

    @JsonProperty
    public HashMap<MediaType, DecoderConfig> getDecoders() {
        return decoders;
    }
    public void setDecoders(HashMap<MediaType, DecoderConfig> decoders) {
        this.decoders = decoders;
    }

    @JsonProperty
    public BenchmarkConfig getBenchmark() {
        return benchmark;
    }
    public void setBenchmark(BenchmarkConfig benchmark) {
        this.benchmark = benchmark;
    }

    @JsonProperty
    public MonitoringConfig getMonitoring() {
      return monitoring;
    }

    public void setMonitoring(MonitoringConfig monitoring) {
      this.monitoring = monitoring;
    }
}
