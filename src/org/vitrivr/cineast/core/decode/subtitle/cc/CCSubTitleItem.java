package org.vitrivr.cineast.core.decode.subtitle.cc;

import org.vitrivr.cineast.core.decode.subtitle.AbstractSubtitleItem;
import org.vitrivr.cineast.core.decode.subtitle.SubTitleDecoder;

public class CCSubTitleItem extends AbstractSubtitleItem {

	protected CCSubTitleItem(int id, long start, long end, String text) {
		super(id, start, end, text);
	}

	@Override
	public String getText() {
		return this.text.trim();
	}
}
