package org.vitrivr.cineast.api.messages.interfaces;

import java.util.List;
import org.vitrivr.cineast.api.messages.result.MediaSegmentMetadataQueryResult;

/**
 * A {@link QueryResultMessage} represents a Query result to be implemented e.g. a result for a metadata lookup {@link MediaSegmentMetadataQueryResult}.
 * <br>
 * It is an interface so it can be extended with record classes using appropriate variable names
 */
public interface QueryResultMessage<T> extends Message {

  /**
   * Returns the unique QueryId to which this QueryResultMessage belongs.
   *
   * @return QueryId (unique)
   */
  String queryId();

  /**
   * Returns a list of the content this QueryResultMessage contains. Has type
   *
   * @return List of type T
   */
  List<T> content();

  /**
   * Returns the type of the query result content
   *
   * @return Class instance of type T
   */
  Class<T> contentType();

}
