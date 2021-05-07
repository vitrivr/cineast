package org.vitrivr.cineast.api.messages.interfaces;

import java.util.List;

/**
 * Defines the abstracts structure of a QueryResultMessage.
 *
 * @author rgasser
 * @created 11.01.17
 */
public interface QueryResultMessage<T> extends Message {

  /**
   * Returns the unique QueryId to which this QueryResultMessage belongs.
   *
   * @return QueryId (unique)
   */
  public String getQueryId();

  /**
   * Returns a list of the content this QueryResultMessage contains.
   *
   * @return List of type T
   */
  public List<T> getContent();

  /**
   * Returns the type of the query result content.
   *
   * @return Class of type T
   */
  public Class<T> getContentType();

  /**
   * Returns the number of items in the QueryResultMessage.
   *
   * @return Number of item in List<T>, returned by getContent()
   */
  int count();
}
