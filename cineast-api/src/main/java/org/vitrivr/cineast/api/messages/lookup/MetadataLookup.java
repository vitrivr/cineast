package org.vitrivr.cineast.api.messages.lookup;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.vitrivr.cineast.api.messages.interfaces.Message;
import org.vitrivr.cineast.api.messages.interfaces.MessageType;

/**
 * Message of a metadata lookup query by a requester.
 *
 * @author rgasser
 * @version 1.0
 * @created 10.02.17
 */
public class MetadataLookup implements Message {

  /**
   * Array of object ID's for which metadata should be looked up.
   */
  private String[] objectIds;

  /**
   * Array of metadata domains that should be considered. If empty, all domains are considered!
   */
  private String[] domains;

  /**
   * Constructor for the MetadataLookup object.
   *
   * @param ids     Array of String object IDs to be looked up.
   * @param domains Array of String of the metadata domains to be considered.
   */
  @JsonCreator
  public MetadataLookup(@JsonProperty("objectids") String[] ids,
      @JsonProperty("domains") String[] domains) {
    this.objectIds = ids;
    this.domains = domains;
  }

  /**
   * Getter for List of object IDs.
   *
   * @return List of String
   */
  public List<String> getIds() {
    if (this.objectIds != null) {
      return Arrays.asList(this.objectIds);
    } else {
      return new ArrayList<>(0);
    }
  }

  /**
   * Getter for List of domains.
   *
   * @return List of String
   */
  public List<String> getDomains() {
    if (this.domains != null) {
      return Arrays.asList(this.domains);
    } else {
      return new ArrayList<>(0);
    }
  }

  /**
   * Returns the type of particular message. Expressed as MessageTypes enum.
   *
   * @return {@link MessageType}
   */
  @Override
  public MessageType getMessageType() {
    return MessageType.M_LOOKUP;
  }

  @Override
  public String toString() {
    return "MetadataLookup{" +
        "objectIds=" + Arrays.toString(objectIds) +
        ", domains=" + Arrays.toString(domains) +
        '}';
  }
}
