package org.vitrivr.cineast.core.data;

import org.vitrivr.cineast.core.extraction.decode.subtitle.SubtitleItem;

public class QuerySubTitleItem implements SubtitleItem {

	private final String text;
	
	public QuerySubTitleItem(String text){
		this.text = text;
	}
	
	@Override
	public int getLength() {
		return 0;
	}

	@Override
	public String getText() {
		return text;
	}

	@Override
	public long getStartTimestamp() {
		return 0;
	}

	@Override
	public long getEndTimestamp() {
		return 0;
	}
}
