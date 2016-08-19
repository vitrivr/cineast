package ch.unibas.cs.dbis.cineast.core.decode.subtitle;

public interface SubtitleItem {

	/**
	 * @return length in ms
	 */
	int getLength();

	String getRawText();

	String getText();
	
	int getStartFrame();
	
	int getEndFrame();
	
	SubTitle getSubTitle();

}