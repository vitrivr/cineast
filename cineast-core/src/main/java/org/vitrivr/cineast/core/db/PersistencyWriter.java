package org.vitrivr.cineast.core.db;

import java.io.Closeable;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public interface PersistencyWriter<R> extends Closeable {

  /**
   * Logger instance used for logging.
   */
  Logger LOGGER = LogManager.getLogger();

  void close();

  /**
   * @return true if the writer was successfully opened
   */
  boolean open(String name);

  boolean idExists(String id);

  boolean exists(String key, String value);

  PersistentTuple generateTuple(Object... objects);

  boolean persist(PersistentTuple tuple);

  void setFieldNames(String... names);

  boolean persist(List<PersistentTuple> tuples);

  R getPersistentRepresentation(PersistentTuple tuple);

  /**
   * Batch size supported when inserting data. This is merely a hint to upper system components, that batching is supported.
   *
   * @return The supported batch size when inserting data.
   */
  int supportedBatchSize();
}
