package org.vitrivr.cineast.api.messages.result;

import java.util.List;
import org.vitrivr.cineast.api.messages.interfaces.MessageType;
import org.vitrivr.cineast.api.messages.interfaces.QueryResultMessage;
import org.vitrivr.cineast.core.data.TemporalObject;

public record TemporalQueryResult(String queryId, List<TemporalObject> content, MessageType messageType) implements QueryResultMessage<TemporalObject> {

  public TemporalQueryResult {
    if (messageType != MessageType.QR_TEMPORAL) {
      throw new IllegalStateException("MessageType was not QR_TEMPORAL, but " + messageType);
    }
  }

  public TemporalQueryResult(String queryId, List<TemporalObject> content) {
    this(queryId, content, MessageType.QR_TEMPORAL);
  }

  @Override
  public Class<TemporalObject> contentType() {
    return TemporalObject.class;
  }
}
