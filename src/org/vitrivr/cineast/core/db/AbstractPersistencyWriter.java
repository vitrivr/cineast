package org.vitrivr.cineast.core.db;

public abstract class AbstractPersistencyWriter<R> implements PersistencyWriter<R> {

  protected String[] names; 

  protected AbstractPersistencyWriter(String...names){
    this.names = names;
  }
  
  protected AbstractPersistencyWriter(){
    this("id", "feature");
  }
  
  public void setFieldNames(String...names){
    if(names != null && names.length > 0){
      this.names = names;
    }
  }
  
  @Override
  public PersistentTuple generateTuple(Object... objects) {
    return new PersistentTuple(objects);
  }
  
}
