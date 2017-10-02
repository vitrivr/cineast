package org.vitrivr.cineast.core.data.providers;

import java.util.ArrayList;
import java.util.List;

import org.vitrivr.cineast.core.data.tag.Tag;

public interface TagProvider {

	public default List<Tag> getTags(){
	   return new ArrayList<>(0);
	}
	
}
