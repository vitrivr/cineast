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
 * This object represents a response to an extract item query and contains {@link
 * ExtractionItemContainer} as a body to be returned in the response message.
 *
 * @author silvanheller
 * @version 1.0
 * @created 22.01.18
 */
public class ExtractionContainerMessage implements Message {

  /**
   * List of {@link ExtractionItemContainer} items that are part of this extraction container
   * message.
   */
  private List<ExtractionItemContainer> items;

  /**
   * Constructor for the ExtractionContainerMessage object.
   *
   * @param items Items that should be sent back with this message.
   */
  public ExtractionContainerMessage(ExtractionItemContainer[] items) {
    this.items = Arrays.asList(items);
  }

  /**
   * Constructor for the ExtractionContainerMessage object able to create a JSON file from an
   * instance of this class created by this constructor.
   *
   * @param items Items that should be sent back with this message.
   */
  @JsonCreator
  public ExtractionContainerMessage(@JsonProperty("items") List<ExtractionItemContainer> items) {
    this.items = items;
  }

  /**
   * Getter for items.
   *
   * @return @return List of {@link ExtractionItemContainer}
   */
  public List<ExtractionItemContainer> getItems() {
    return this.items;
  }

  /**
   * Getter for items as an Array.
   *
   * @return @return Array of {@link ExtractionItemContainer}
   */
  @JsonIgnore
  public ExtractionItemContainer[] getItemsAsArray() {
    return this.items.toArray(new ExtractionItemContainer[0]);
  }

  /**
   * Returns the type of particular message. Expressed as MessageTypes enum.
   *
   * @return {@link MessageType}
   */
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
