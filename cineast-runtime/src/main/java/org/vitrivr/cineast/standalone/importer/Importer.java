package org.vitrivr.cineast.standalone.importer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;

public interface Importer<T> {

	/**
	 * @return the next available element or null in case the end is reached
	 */
	T readNext();
	
	/**
	 * converts the native type of the importer to a general representation
	 */
	Map<String, PrimitiveTypeProvider> convert(T data);
	
	/**
	 * @return the next available element converted to a general representation or null in case the end is reached
	 */
	default Map<String, PrimitiveTypeProvider> readNextAsMap(){
		T next = readNext();
		if(next == null){
			return null;
		}
		return convert(next);
	}
	
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
	
	default List<Map<String, PrimitiveTypeProvider>> readAllRemainingAsMap(){
		ArrayList<Map<String, PrimitiveTypeProvider>> _return = new ArrayList<>();
		
		Map<String, PrimitiveTypeProvider> next = null;
		while((next = readNextAsMap()) != null){
			_return.add(next);
		}
		return _return;
	}
	
}
