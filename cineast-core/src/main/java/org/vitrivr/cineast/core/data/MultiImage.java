package org.vitrivr.cineast.core.data;

import java.awt.image.BufferedImage;

public interface MultiImage {
  
  public static final MultiImage EMPTY_MULTIIMAGE = new MultiImage() {
    
    private int[] emptyArray = new int[0];
    private BufferedImage emptyImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
    
    @Override
    public int getWidth() {
      return 1;
    }
    
    @Override
    public BufferedImage getThumbnailImage() {
      return emptyImage;
    }
    
    @Override
    public int[] getThumbnailColors() {
      return emptyArray;
    }
    
    @Override
    public int getHeight() {
      return 1;
    }
    
    @Override
    public int[] getColors() {
      return emptyArray;
    }
    
    @Override
    public BufferedImage getBufferedImage() {
      return emptyImage;
    }
    
    @Override
    public void clear() {}
  };
	
	static final double MAX_THUMB_SIZE = 200;

	BufferedImage getBufferedImage();

	BufferedImage getThumbnailImage();

	int[] getColors();

	int[] getThumbnailColors();

	int getWidth();

	int getHeight();
	
	void clear();

}
