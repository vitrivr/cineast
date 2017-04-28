package org.vitrivr.cineast.core.db.dao.reader;

/**
 *
 * @author rgasser
 * @version 1.0
 * @created 10.02.17
 */
public class DatabaseLookupException extends Exception {

  private static final long serialVersionUID = 1759949037860773209L;

  /**
   * Constructor
   *
   * @param message
   */
  public DatabaseLookupException(String message) {
    super(message);
  }

  /**
   * Constructor
   *
   * @param message
   * @param cause
   */
  public DatabaseLookupException(String message, Throwable cause) {
    super(message, cause);
  }
}
