package org.vitrivr.cineast.standalone.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.config.*;
import org.vitrivr.cineast.core.data.MediaType;
import org.vitrivr.cineast.core.data.raw.CachedDataFactory;
import org.vitrivr.cineast.core.util.json.JacksonJsonProvider;

import java.io.File;
import java.util.HashMap;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Config {
    private static final Logger LOGGER = LogManager.getLogger();

    /** Global, shared instance of the Config object. Gets loading during application startup. */
    private volatile static Config sharedConfig;

    private APIConfig api;
    private DatabaseConfig database;
    private RetrievalRuntimeConfig retriever;
    private ExtractionPipelineConfig extractor;
    private CacheConfig cache;
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
            loadConfig("cineast.json");
        }
        return sharedConfig;
    }

    /**
     * Loads a config file and thereby replaces the shared instance of the Config.
     *
     * @param name Name of the config file.
     */
  public static Config loadConfig(String name) {
    final Config config = (new JacksonJsonProvider()).toObject(new File(name), Config.class);
    if (config == null) {
      LOGGER.warn("Could not read config file '{}'.", name);
      return null;
    } else {
      LOGGER.info("Config file loaded!");
      initSharedConfig(config);
      return config;
    }
  }

  public static void initSharedConfig(Config config) {
    sharedConfig = config;
    if (config.cache != null) {
      CachedDataFactory.configureDefault(config.cache);
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
    public RetrievalRuntimeConfig getRetriever() {
        return retriever;
    }
    public void setRetriever(RetrievalRuntimeConfig retriever) {
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
    public CacheConfig getCache() {
      if(cache == null){
          cache = new CacheConfig();
      }
        return cache;
    }
    public void setCache(CacheConfig cache) {
        this.cache = cache;
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
