package org.vitrivr.cineast.core.decode.subtitle.cc;

import org.vitrivr.cineast.core.decode.subtitle.AbstractSubtitleItem;
import org.vitrivr.cineast.core.decode.subtitle.SubTitle;

public class CCSubTitleItem extends AbstractSubtitleItem {

	protected CCSubTitleItem(int id, long start, long end, String text,
			SubTitle st) {
		super(id, start, end, text, st);
	}

	@Override
	public String getText() {
		return this.text.trim();
	}

}
