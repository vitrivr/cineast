package ch.unibas.cs.dbis.cineast.core.decode.subtitle.srt;

import ch.unibas.cs.dbis.cineast.core.decode.subtitle.AbstractSubtitleItem;

public class SRTSubtitleItem extends AbstractSubtitleItem {

	int id;
	long start, end;
	String text;
	
	public SRTSubtitleItem(int id, long start, long end, String text, SRTSubTitle st){
		super(id, start, end, text, st);
	}

	/* (non-Javadoc)
	 * @see subsync.SubItem#getText()
	 */
	@Override
	public String getText(){
		return this.text.replaceAll("<[^>]*>", "");
	}

	
	
}
