package org.vitrivr.cineast.core.data.providers;

import java.util.ArrayList;
import java.util.List;

import org.vitrivr.cineast.core.decode.subtitle.SubtitleItem;

public interface SubtitleItemProvider {

	public default List<SubtitleItem> getSubtitleItems(){
	   return new ArrayList<>(0);
	}
	
}
