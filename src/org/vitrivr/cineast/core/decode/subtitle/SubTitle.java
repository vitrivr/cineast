package ch.unibas.cs.dbis.cineast.core.decode.subtitle;

public interface SubTitle {

	int getNumerOfItems();

	SubtitleItem getItem(int id);
	
	float getFrameRate();

}