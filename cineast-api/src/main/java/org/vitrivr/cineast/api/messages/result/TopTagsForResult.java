package org.vitrivr.cineast.api.messages.result;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.List;
import org.vitrivr.cineast.api.messages.interfaces.Message;
import org.vitrivr.cineast.api.messages.interfaces.MessageType;
import org.vitrivr.cineast.core.data.tag.TagWithCount;

/**
 * List of tags and their count
 */

public class TopTagsForResult implements Message {

  public final String queryId;
  public final List<TagWithCount> tags;

  @JsonCreator
  public TopTagsForResult(String queryId, List<TagWithCount> tags) {
    this.queryId = queryId;
    this.tags = tags;
  }

  @Override
  public MessageType getMessageType() {
    return MessageType.QR_TOPTAGS;
  }
}
