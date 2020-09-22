package org.vitrivr.cineast.core.data.providers;

import org.vitrivr.cineast.core.data.tag.Tag;

import java.util.ArrayList;
import java.util.List;

public interface TagProvider {

	public default List<Tag> getTags(){
	   return new ArrayList<>(0);
	}
	
}
