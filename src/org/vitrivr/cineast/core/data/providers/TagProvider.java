package org.vitrivr.cineast.core.data.providers;

import java.util.ArrayList;
import java.util.List;

public interface TagProvider {

	public default List<String> getTags(){
	   return new ArrayList<>(0);
	}
	
}
