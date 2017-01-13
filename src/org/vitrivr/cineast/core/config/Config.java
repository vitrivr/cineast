package org.vitrivr.cineast.core.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Config {
    private static final Logger LOGGER = LogManager.getLogger();

    public static final UUID UNIQUE_ID = UUID.randomUUID();

    private static Config sharedConfig;

    private APIConfig api;
    private DatabaseConfig database;
    private RetrieverConfig retriever;
    private DecoderConfig decoder;
    private ExtractorConfig extractor;
    private ImageCacheConfig imagecache;
    private VisualizationConfig visualization;
    private NeuralNetConfig neuralnet;
    private QueryConfig query = (new QueryConfig()).setDistance(QueryConfig.Distance.manhattan); //FIXME remove as soon as chisquared distance works again;

    /**
     * Accessor for shared (i.e. application wide) configuration.
     *
     * @return Currently shared instance of Config.
     */
    public synchronized static Config sharedConfig() {
        if (sharedConfig == null) loadConfig("cineast.json");
        return sharedConfig;
    }

    /**
     * Loads a config file and thereby replaces the shared instance of the Config.
     *
     * @param name Name of the config file.
     */
    public static synchronized void loadConfig(String name) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            LOGGER.info("Config file loaded!");
            sharedConfig = mapper.readValue(new File("cineast.json"), Config.class);
        } catch (FileNotFoundException e) {
            LOGGER.warn("Config file '" + name + "' not found.");
        } catch (IOException e) {
            LOGGER.warn("Could not read config file '" + name + "'.");
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
    public DecoderConfig getDecoder() {
        return decoder;
    }
    public void setDecoder(DecoderConfig decoder) {
        this.decoder = decoder;
    }

    @JsonProperty
    public ExtractorConfig getExtractor() {
        return extractor;
    }
    public void setExtractor(ExtractorConfig extractor) {
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
    public VisualizationConfig getVisualization() {
        return visualization;
    }
    public void setVisualization(VisualizationConfig visualization) {
        this.visualization = visualization;
    }

    @JsonProperty
    public NeuralNetConfig getNeuralnet() {
        return this.neuralnet;
    }
    public void setNeuralnet(NeuralNetConfig neuralnet) {
        this.neuralnet = neuralnet;
    }

    @JsonProperty
    public QueryConfig getQuery() {
        return query;
    }
    public void setQuery(QueryConfig query) {
        this.query = query;
    }

    /**
     * Returns the {@link ImageCacheConfig} as specified in the config file. If nothing is specified in the configuration file, the default values are returned, see {@link ImageCacheConfig}
     * @return
     */
    public static ImageCacheConfig getImageMemoryConfig() {
        return sharedConfig().imagecache;
    }

    public static ExtractorConfig getExtractorConfig() {
        return  sharedConfig().extractor;
    }

    public static RetrieverConfig getRetrieverConfig() {
        return  sharedConfig().retriever;
    }

    public static DecoderConfig getDecoderConfig() {
        return  sharedConfig().decoder;
    }

    public static APIConfig getApiConfig() {
        return  sharedConfig().api;
    }

    public static DatabaseConfig getDatabaseConfig() {
        return  sharedConfig().database;
    }

    public static QueryConfig getQueryConfig() {
        return sharedConfig().query;
    }

    public static NeuralNetConfig getNeuralNetConfig() {
        return sharedConfig().neuralnet;
    }

    public static VisualizationConfig getVisualizationConfig() {
        return sharedConfig().visualization;
    }
}
