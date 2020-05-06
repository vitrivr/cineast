package org.vitrivr.cineast.api.messages.lookup;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.vitrivr.cineast.api.messages.components.AbstractMetadataFilterDescriptor;
import org.vitrivr.cineast.api.messages.interfaces.Message;
import org.vitrivr.cineast.api.messages.interfaces.MessageType;

import java.util.Arrays;
import java.util.List;

/**
 * Ignores unkown json properties, e.g. may contain no filter at all.
 *
 * @author loris.sauter
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class OptionallyFilteredIdList implements Message {

  /*
  https://layer4.fr/blog/2013/08/19/how-to-map-unknown-json-properties-with-jackson/
  how to still realise that there are unkown properties
   */

  private List<AbstractMetadataFilterDescriptor> filters;

  private List<String> ids;

  /**
   * This default constructor is required for deserialization by fasterxml/jackson.
   */
  public OptionallyFilteredIdList() {
  }

  public String[] getIds(){
    return this.ids.toArray(new String[0]);
  }

  public List<String> getIdList(){ return this.ids;}

  @Override
  public MessageType getMessageType() {
    return null;
  }

  public List<AbstractMetadataFilterDescriptor> getFilters() {
    return filters;
  }

  @JsonIgnore
  public boolean hasFilters(){
    return filters != null;
  }

  public void setFilters(
      AbstractMetadataFilterDescriptor[] filters) {
    this.filters = Arrays.asList(filters);
  }

  @JsonIgnore //prevents filters being included in json a second time as "filterList"
  public List<AbstractMetadataFilterDescriptor> getFilterList(){
    return this.filters;
  }

  @Override
  public String toString() {
    return "OptionallyFilteredIdList{" +
            "filters=" + Arrays.toString(filters.toArray()) +
            ", ids=" + Arrays.toString(ids.toArray()) +
            '}';
  }
}
