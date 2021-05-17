package org.vitrivr.cineast.api.messages.result;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.List;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.vitrivr.cineast.api.messages.abstracts.AbstractQueryResultMessage;
import org.vitrivr.cineast.api.messages.interfaces.MessageType;
import org.vitrivr.cineast.core.data.StringDoublePair;

/**
 * A {@link SimilarityQueryResult} contains a list of {@link StringDoublePair}s as content of the result message, with the Pair representing a segmentid to score mapping
 * It is a response for a similarity query as well as a temporal query as a result of a temporal container.
 *
 * @author rgasser
 * @created 11.01.17
 */
public class SimilarityQueryResult extends AbstractQueryResultMessage<StringDoublePair> {

  /**
   * The category to which the content of this similarity query result belong.
   */
  private final String category;

  /**
   * The container which this similarity query result belongs. Important for the temporal queries with more than one containers chained in a total temporal order.
   */
  private final int containerId;

  /**
   * Constructor for the SimilarityQueryResult object.
   *
   * @param queryId     String representing the ID of the query to which this part of the result message.
   * @param category    String category to which this result belongs.
   * @param containerId int id of the temporal container to which this result belongs.
   * @param content     List of {@link StringDoublePair} of the segments belonging to this result with their respective score.
   */
  @JsonCreator
  public SimilarityQueryResult(String queryId, String category, int containerId, List<StringDoublePair> content) {
    super(queryId, StringDoublePair.class, content);
    this.category = category;
    this.containerId = containerId;
  }

  public int getContainerId() {
    return this.containerId;
  }

  public String getCategory() {
    return category;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public MessageType getMessageType() {
    return MessageType.QR_SIMILARITY;
  }

  @Override
  public String toString() {
    return ReflectionToStringBuilder.toString(this, ToStringStyle.JSON_STYLE);
  }
}
