package org.vitrivr.cineast.core.db;

import java.io.Closeable;
import java.util.List;

public interface PersistencyWriter<R> extends Closeable {

  /**
   * @return true if the writer was successfully opened
   */
  boolean open(String name);

  void close();

  boolean idExists(String id);

  boolean exists(String key, String value);

  PersistentTuple generateTuple(Object... objects);

  boolean persist(PersistentTuple tuple);

  void setFieldNames(String... names);

  boolean persist(List<PersistentTuple> tuples);

  R getPersistentRepresentation(PersistentTuple tuple);
}
