package org.vitrivr.cineast.core.decode.subtitle.srt;

import org.vitrivr.cineast.core.decode.subtitle.AbstractSubtitleItem;

public class SRTSubtitleItem extends AbstractSubtitleItem {

	/**
	 *
	 */
	public SRTSubtitleItem(int id, long start, long end, String text){
		super(id, start, end, text);
	}

	/* (non-Javadoc)
	 * @see subsync.SubItem#getText()
	 */
	@Override
	public String getText(){
		return this.text.replaceAll("<[^>]*>", "");
	}

	
}
