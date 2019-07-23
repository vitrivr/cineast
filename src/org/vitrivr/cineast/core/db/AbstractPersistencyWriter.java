package org.vitrivr.cineast.core.db;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractPersistencyWriter<R> implements PersistencyWriter<R> {

  protected String[] names; 

  protected AbstractPersistencyWriter(String...names){
    this.names = names;
  }
  
  protected AbstractPersistencyWriter(){
    this("id", "feature");
  }
  
  @Override
  public void setFieldNames(String...names){
    if(names != null && names.length > 0){
      this.names = names;
    }
  }

  @Override
  public boolean persist(PersistentTuple tuple) {
    List<PersistentTuple> tuples = new ArrayList<>(1);
    tuples.add(tuple);
    return persist(tuples);
  }

  @Override
  public boolean idExists(String id) {
    return exists("id", id);
  }
  
  @Override
  public PersistentTuple generateTuple(Object... objects) {
    return new PersistentTuple(objects);
  }
  
}
