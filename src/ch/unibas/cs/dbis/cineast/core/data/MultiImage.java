package ch.unibas.cs.dbis.cineast.core.data;

import java.awt.image.BufferedImage;

public interface MultiImage {
	
	static final double MAX_THUMB_SIZE = 200;

	BufferedImage getBufferedImage();

	BufferedImage getThumbnailImage();

	int[] getColors();

	int[] getThumbnailColors();

	int getWidth();

	int getHeight();
	
	void clear();

}
