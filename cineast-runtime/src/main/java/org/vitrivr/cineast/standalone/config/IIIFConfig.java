package org.vitrivr.cineast.standalone.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.LinkedList;
import java.util.List;

/**
 * @author singaltanmay
 * @version 1.0
 * @created 28.05.21
 */
public class IIIFConfig {

  /**
   * List of IIIF resource URLs
   */
  private List<String> resourceUrlList = new LinkedList<>();

  public List<String> getResourceUrlList() {
    return resourceUrlList;
  }

  @JsonProperty(required = false)
  public void setResourceUrlList(List<String> resourceUrlList) {
    this.resourceUrlList = resourceUrlList;
  }
}
