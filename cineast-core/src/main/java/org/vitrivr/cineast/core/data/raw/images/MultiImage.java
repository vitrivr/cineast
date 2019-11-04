package org.vitrivr.cineast.core.data.raw.images;

import net.coobird.thumbnailator.Thumbnails;

import org.vitrivr.cineast.core.data.raw.CacheableData;
import org.vitrivr.cineast.core.data.raw.CachedDataFactory;

import java.awt.image.BufferedImage;
import java.io.IOException;

public interface MultiImage extends CacheableData {
  
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
    public CachedDataFactory factory() { return null; }
    
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

	CachedDataFactory factory();

    void clear();

  /**
   *
   * @param width
   * @param height
   * @param colors
   * @return
   */
  static int checkHeight(int width, int height, int[] colors){
    if(colors.length / width != height){
      height = colors.length / width;
    }
    return height;
  }

  /**
   * Generates a thumbnail version out of the provided {@link BufferedImage} and returns it.
   *
   * @param img The image from which to create a thumbnail version.
   * @return The thumbnail image.
   */
  static BufferedImage generateThumb(BufferedImage img){
    double scale = MAX_THUMB_SIZE / Math.max(img.getWidth(), img.getHeight());
    if (scale >= 1 || scale <= 0){
      return img;
    } else{
      try {
        return Thumbnails.of(img).scale(scale).asBufferedImage();
      } catch (IOException e) {
        return img;
      }
    }
  }
}
