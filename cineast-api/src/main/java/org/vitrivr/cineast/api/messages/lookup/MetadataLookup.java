package org.vitrivr.cineast.api.messages.lookup;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.vitrivr.cineast.api.messages.abstracts.AbstractMessage;
import org.vitrivr.cineast.api.messages.interfaces.MessageType;

/**
 * Message of a metadata lookup query by a requester.
 */
public class MetadataLookup extends AbstractMessage {

  /**
   * List of object ID's for which metadata should be looked up.
   */
  private final String[] objectIds;

  /**
   * List of metadata domains that should be considered. If empty, all domains are considered!
   */
  private final String[] domains;

  /**
   * Constructor for the MetadataLookup object.
   *
   * @param ids     Array of String object IDs to be looked up.
   * @param domains Array of String of the metadata domains to be considered.
   */
  @JsonCreator
  public MetadataLookup(@JsonProperty("objectids") String[] ids, @JsonProperty("domains") String[] domains) {
    this.objectIds = ids;
    this.domains = domains;
  }

  public List<String> getIds() {
    if (this.objectIds != null) {
      return Arrays.asList(this.objectIds);
    } else {
      return new ArrayList<>(0);
    }
  }


  public List<String> getDomains() {
    if (this.domains != null) {
      return Arrays.asList(this.domains);
    } else {
      return new ArrayList<>(0);
    }
  }

  @Override
  public MessageType getMessageType() {
    return MessageType.M_LOOKUP;
  }

}
