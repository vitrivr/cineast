package org.vitrivr.cineast.api.messages.result;

import java.util.List;
import org.vitrivr.cineast.api.messages.interfaces.MessageType;
import org.vitrivr.cineast.api.messages.interfaces.QueryResultMessage;
import org.vitrivr.cineast.core.data.StringDoublePair;

/**
 * A {@link SimilarityQueryResult} contains a list of {@link StringDoublePair}s as content of the result message, with the Pair representing a segmentid to score mapping. It is a response for a similarity query as well as a temporal query as a result of a temporal container.
 *
 * @param queryId     String representing the ID of the query to which this part of the result message.
 * @param category    String category to which this result belongs.
 * @param containerId int id of the temporal container to which this result belongs.
 * @param content     List of {@link StringDoublePair} of the segments belonging to this result with their respective score.
 */
public record SimilarityQueryResult(String queryId, List<StringDoublePair> content, String category, int containerId, MessageType messageType) implements QueryResultMessage<StringDoublePair> {

  public SimilarityQueryResult {
    if (messageType != MessageType.QR_SIMILARITY) {
      throw new IllegalStateException("MessageType was not QR_SIMILARITY, but " + messageType);
    }
  }

  public SimilarityQueryResult(String queryId, List<StringDoublePair> content, String category, int containerId) {
    this(queryId, content, category, containerId, MessageType.QR_SIMILARITY);
  }

  @Override
  public Class<StringDoublePair> contentType() {
    return StringDoublePair.class;
  }
}
