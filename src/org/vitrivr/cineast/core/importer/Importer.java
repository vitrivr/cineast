package org.vitrivr.cineast.core.importer;

import java.util.ArrayList;
import java.util.List;

public interface Importer<T> {

	/**
	 * @return the next available element or null in case the end is reached
	 */
	T readNext();
	
	/**
	 * @return a list of all remaining elements from the current position to the end 
	 */
	default List<T> readAllRemaining(){
		ArrayList<T> _return = new ArrayList<>();
		
		T t = null;
		while((t = readNext()) != null){
			_return.add(t);
		}
		return _return;
	}
	
}
