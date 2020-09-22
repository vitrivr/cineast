package org.vitrivr.cineast.core.data.providers;

import org.vitrivr.cineast.core.extraction.decode.subtitle.SubtitleItem;

import java.util.ArrayList;
import java.util.List;

public interface SubtitleItemProvider {

	public default List<SubtitleItem> getSubtitleItems(){
	   return new ArrayList<>(0);
	}
	
}
