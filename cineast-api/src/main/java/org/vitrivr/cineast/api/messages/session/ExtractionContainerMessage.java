package org.vitrivr.cineast.api.messages.session;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Arrays;
import java.util.List;

import org.vitrivr.cineast.api.messages.interfaces.Message;
import org.vitrivr.cineast.api.messages.interfaces.MessageType;
import org.vitrivr.cineast.standalone.run.ExtractionItemContainer;

/**
 * @author silvan on 22.01.18.
 */
public class ExtractionContainerMessage implements Message {
  
  private List<ExtractionItemContainer> items;
  
  public ExtractionContainerMessage(ExtractionItemContainer[] items) {
    this.items = Arrays.asList(items);
  }
  
  @JsonCreator
  public ExtractionContainerMessage(@JsonProperty("items") List<ExtractionItemContainer> items) {
    this.items = items;
  }
  
  public List<ExtractionItemContainer> getItems() {
    return this.items;
  }
  
  @JsonIgnore
  public ExtractionItemContainer[] getItemsAsArray() {
    return this.items.toArray(new ExtractionItemContainer[0]);
  }
  
  @Override
  public MessageType getMessageType() {
    return null;
  }
  
  @Override
  public String toString() {
    return "ExtractionContainerMessage{" +
        "items=" + Arrays.toString(items.toArray()) +
        '}';
  }
}
