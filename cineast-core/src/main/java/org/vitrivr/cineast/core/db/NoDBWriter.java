package org.vitrivr.cineast.core.db;

import java.util.List;

public class NoDBWriter extends AbstractPersistencyWriter<Object> {

  @Override
  public boolean open(String name) {
    return false;
  }

  @Override
  public boolean close() {
    return true;
  }

  @Override
  public boolean idExists(String id) {
    return false;
  }

  @Override
  public boolean exists(String key, String value) {
    return false;
  }

  @Override
  public boolean persist(PersistentTuple tuple) {
    return false;
  }

  @Override
  public boolean persist(List<PersistentTuple> tuples) {
    return false;
  }

  @Override
  public Object getPersistentRepresentation(PersistentTuple tuple) {
    return new Object();
  }
}
