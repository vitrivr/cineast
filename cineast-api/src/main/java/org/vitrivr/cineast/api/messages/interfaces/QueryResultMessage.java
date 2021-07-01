package org.vitrivr.cineast.api.messages.interfaces;

import java.util.List;

/**
 * Defines the abstracts structure of a QueryResultMessage.
 */
public interface QueryResultMessage<T> extends Message {

  /**
   * Returns the unique QueryId to which this QueryResultMessage belongs.
   *
   * @return QueryId (unique)
   */
  public String getQueryId();

  /**
   * Returns a list of the content this QueryResultMessage contains. Has type
   *
   * @return List of type T
   */
  public List<T> getContent();

  /**
   * Returns the type of the query result content
   *
   * @return Class instance of type T
   */
  public Class<T> getContentType();

  /**
   * Returns the number of items in the QueryResultMessage.
   *
   * @return Number of item in List<T>, returned by getContent()
   */
  int count();
}
