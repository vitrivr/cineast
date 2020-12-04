package org.vitrivr.cineast.core.db;

import static org.vitrivr.cineast.core.util.CineastConstants.FEATURE_COLUMN_QUALIFIER;
import static org.vitrivr.cineast.core.util.CineastConstants.GENERIC_ID_COLUMN_QUALIFIER;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractPersistencyWriter<R> implements PersistencyWriter<R> {

  protected String[] names; 

  protected AbstractPersistencyWriter(String...names){
    this.names = names;
  }
  
  protected AbstractPersistencyWriter(){
    this(GENERIC_ID_COLUMN_QUALIFIER, FEATURE_COLUMN_QUALIFIER);
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
    return exists(GENERIC_ID_COLUMN_QUALIFIER, id);
  }
  
  @Override
  public PersistentTuple generateTuple(Object... objects) {
    return new PersistentTuple(objects);
  }
  
}
