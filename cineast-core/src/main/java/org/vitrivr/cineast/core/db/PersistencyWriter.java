package org.vitrivr.cineast.core.db;

import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public interface PersistencyWriter <R> {
	/**
	 * Logger instance used for logging.
	 */
	Logger LOGGER = LogManager.getLogger();

	/**
	 * @return true if the writer was successfully opened
	 */
	boolean open(String name);
	
	/**
	 * @return true if writer is closed after the call
	 */
	boolean close();
	
	boolean idExists(String id);
	
	boolean exists(String key, String value);
	
	PersistentTuple generateTuple(Object...objects);
	
	boolean persist(PersistentTuple tuple);

	void setFieldNames(String...names);

	boolean persist(List<PersistentTuple> tuples);
  
	R getPersistentRepresentation(PersistentTuple tuple);
}
