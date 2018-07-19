package org.vitrivr.cineast.core.features.neuralnet.tf.models.yolo.util;

import java.util.Comparator;

/**
 * Size class to store information about the object size.
 */
public class Size {

  public static final Comparator<Size> SIZE_COMPARATOR = new Comparator<Size>()

  {
    @Override
    public int compare(final Size lhs, final Size rhs) {
      // We cast here to ensure the multiplications won't overflow
      return Long.signum((long) lhs.getWidth() * lhs.getHeight()
          - (long) rhs.getWidth() * rhs.getHeight());
    }
  };


  private int width;
  private int height;

  public int getWidth() {
    return width;
  }

  public void setWidth(int width) {
    this.width = width;
  }

  public int getHeight() {
    return height;
  }

  public void setHeight(int height) {
    this.height = height;
  }

}
