package ch.unibas.cs.dbis.cineast.core.db;

public interface PersistencyWriter <R> {

	/**
	 * @return true if the writer was successfully opened
	 */
	boolean open(String name);
	
	/**
	 * @return true if writer is closed after the call
	 */
	boolean close();
	
	boolean idExists(String id);
	
	PersistentTuple<R> generateTuple(Object...objects);
	
	boolean persist(PersistentTuple<R> tuple);
	
	void setFieldNames(String...names);
	
}
