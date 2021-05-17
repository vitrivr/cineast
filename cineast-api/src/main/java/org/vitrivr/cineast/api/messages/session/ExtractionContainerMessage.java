package org.vitrivr.cineast.api.messages.session;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.vitrivr.cineast.api.messages.interfaces.Message;
import org.vitrivr.cineast.api.messages.interfaces.MessageType;
import org.vitrivr.cineast.standalone.run.ExtractionItemContainer;

/**
 * This object represents a container for multiple extract item requests and contains {@link ExtractionItemContainer} as a body of the message.
 *
 * @author silvanheller
 * @created 22.01.18
 */
public class ExtractionContainerMessage implements Message {

  /**
   * List of {@link ExtractionItemContainer} items that are part of this extraction container message.
   */
  private final List<ExtractionItemContainer> items;

  /**
   * Constructor for the ExtractionContainerMessage object.
   *
   * @param items Items that should be sent back with this message.
   */
  public ExtractionContainerMessage(ExtractionItemContainer[] items) {
    this.items = Arrays.asList(items);
  }

  /**
   * Constructor for the ExtractionContainerMessage object able to create a JSON file from an instance of this class created by this constructor.
   *
   * @param items Items that should be sent back with this message.
   */
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
