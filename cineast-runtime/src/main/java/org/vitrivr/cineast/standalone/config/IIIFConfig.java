package org.vitrivr.cineast.standalone.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.LinkedList;
import java.util.List;
import org.vitrivr.cineast.core.config.CacheConfig;
import org.vitrivr.cineast.core.config.DatabaseConfig;
import org.vitrivr.cineast.core.config.SegmenterConfig;
import org.vitrivr.cineast.core.data.MediaType;

/**
 * IIIF configuration used to fetch media files from remote servers.
 *
 * @author singaltanmay
 * @version 1.0
 * @created 28.05.21
 */
public class IIIFConfig extends IngestConfig {

  /**
   * List of IIIF resource URLs
   */
  private List<String> resourceUrls;

  @JsonCreator
  public IIIFConfig(
      @JsonProperty(value = "type") MediaType type,
      @JsonProperty(value = "input", required = true) InputConfig input,
      @JsonProperty(value = "extractors") List<ExtractorConfig> extractors,
      @JsonProperty(value = "exporters") List<ExtractorConfig> exporters,
      @JsonProperty(value = "metadata") List<MetadataConfig> metadata,
      @JsonProperty(value = "database") DatabaseConfig database,
      @JsonProperty(value = "pipeline") ExtractionPipelineConfig pipeline,
      @JsonProperty(value = "segmenter") SegmenterConfig segmenter,
      @JsonProperty(value = "imagecache") CacheConfig cacheConfig,
      @JsonProperty(value = "resourceUrls") List<String> resourceUrls) {
    super(type, input, extractors, exporters, metadata, database, pipeline, segmenter, cacheConfig);

    /* Initialize list of ExtractorConfig. */
    if (resourceUrls == null) {
      resourceUrls = new LinkedList<>();
    }
    this.resourceUrls = resourceUrls;
  }

  public List<String> getResourceUrls() {
    return resourceUrls;
  }

  @JsonProperty
  public void setResourceUrls(List<String> resourceUrls) {
    this.resourceUrls = resourceUrls;
  }
}
