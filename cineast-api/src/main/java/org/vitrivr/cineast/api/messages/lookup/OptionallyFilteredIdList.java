package org.vitrivr.cineast.api.messages.lookup;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import org.vitrivr.cineast.api.messages.components.AbstractMetadataFilterDescriptor;

/**
 * Ignores unkown json properties, e.g. may contain no filter at all. Message of an optionally filtered list of IDs and filters to be applied on the metadata lookup.
 *
 * <p>ignoreUnknown = true ignores unknown json properties, e.g. may contain no filter at all.</p>
 * <p>https://layer4.fr/blog/2013/08/19/how-to-map-unknown-json-properties-with-jackson/ how to
 * still realise that there are unknown properties</p>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record OptionallyFilteredIdList(@JsonProperty(required = true) List<AbstractMetadataFilterDescriptor> filters, @JsonProperty(required = true) List<String> ids) {

  /**
   * Check if the optionally filtered list contains filters.
   *
   * <p>JsonIgnore prevents filters being included in json a second time as "filterList".</p>
   *
   * @return boolean
   */
  @JsonIgnore
  public boolean hasFilters() {
    return filters != null;
  }

  /**
   * Get the list of abstract metadata filter descriptors.
   *
   * <p>JsonIgnore prevents filters being included in json a second time as "filterList".</p>
   *
   * @return boolean
   */
  @JsonIgnore
  public List<AbstractMetadataFilterDescriptor> getFilterList() {
    return this.filters;
  }
}
