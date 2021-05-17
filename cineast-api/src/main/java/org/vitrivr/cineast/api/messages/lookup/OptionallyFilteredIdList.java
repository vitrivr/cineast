package org.vitrivr.cineast.api.messages.lookup;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.vitrivr.cineast.api.messages.components.AbstractMetadataFilterDescriptor;
import org.vitrivr.cineast.api.messages.interfaces.Message;
import org.vitrivr.cineast.api.messages.interfaces.MessageType;

/**
 * Ignores unkown json properties, e.g. may contain no filter at all. Message of an optionally filtered list of IDs and filters to be applied on the metadata lookup.
 *
 * <p>ignoreUnknown = true ignores unknown json properties, e.g. may contain no filter at all.</p>
 * <p>https://layer4.fr/blog/2013/08/19/how-to-map-unknown-json-properties-with-jackson/ how to
 * still realise that there are unknown properties</p>
 *
 * @author loris.sauter
 * @created 04.08.18
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class OptionallyFilteredIdList implements Message {

  /**
   * List of {@link AbstractMetadataFilterDescriptor} to be applied on the metadata lookup.
   */
  private List<AbstractMetadataFilterDescriptor> filters;

  /**
   * List of IDs for which the metadata lookup should be performed.
   */
  private List<String> ids;

  /**
   * This default constructor is required for deserialization by fasterxml/jackson.
   */
  public OptionallyFilteredIdList() {
  }

  public String[] getIds() {
    return this.ids.toArray(new String[0]);
  }

  public List<String> getIdList() {
    return this.ids;
  }

  public List<AbstractMetadataFilterDescriptor> getFilters() {
    return filters;
  }

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

  public void setFilters(
      AbstractMetadataFilterDescriptor[] filters) {
    this.filters = Arrays.asList(filters);
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

  /**
   * {@inheritDoc}
   */
  @Override
  public MessageType getMessageType() {
    return null;
  }

  @Override
  public String toString() {
    return ReflectionToStringBuilder.toString(this, ToStringStyle.JSON_STYLE);
  }
}
