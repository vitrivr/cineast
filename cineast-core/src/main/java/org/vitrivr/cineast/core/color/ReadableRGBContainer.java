package org.vitrivr.cineast.core.color;

public class ReadableRGBContainer extends AbstractColorContainer<ReadableRGBContainer> implements
    Cloneable {

  public static final int WHITE_INT = ReadableRGBContainer.toIntColor(255, 255, 255);

  protected int r, g, b, a;

  public ReadableRGBContainer(int r, int g, int b, int a) {
    if (r > 255 || r < 0) {
      throw new IllegalArgumentException(r + "is outside of allowed range for red");
    }
    if (g > 255 || g < 0) {
      throw new IllegalArgumentException(g + "is outside of allowed range for green");
    }
    if (b > 255 || b < 0) {
      throw new IllegalArgumentException(b + "is outside of allowed range for blue");
    }
    if (a > 255 || a < 0) {
      throw new IllegalArgumentException(b + "is outside of allowed range for alpha");
    }

    this.r = r;
    this.g = g;
    this.b = b;
    this.a = a;
  }

  public ReadableRGBContainer(int r, int g, int b) {
    this(r, g, b, 255);
  }

  public ReadableRGBContainer(float r, float g, float b) {
    this(r, g, b, 1);
  }

  public ReadableRGBContainer(float r, float g, float b, float a) {
    this(Math.round(255f * r),
        Math.round(255f * g),
        Math.round(255f * b),
        Math.round(255f * a));
  }

  public ReadableRGBContainer(double r, double g, double b) {
    this(r, g, b, 1);
  }

  public ReadableRGBContainer(double r, double g, double b, double a) {
    this((float) r, (float) g, (float) b, (float) a);
  }

  public ReadableRGBContainer(int color) {
    this((color >> 16 & 0xFF), (color >> 8 & 0xFF), (color & 0xFF), (color >> 24 & 0xFF));
  }

  public int toIntColor() {
    return (b & 0xFF) | ((g & 0xFF) << 8) | ((r & 0xFF) << 16) | (a << 24);
  }

  @Override
  public String toString() {
    return "RGBContainer(" + r + ", " + g + ", " + b + ")";
  }

  public float getLuminance() {
    return 0.2126f * r + 0.7152f * g + 0.0722f * b;
  }

  /**
   * http://www.itu.int/rec/R-REC-BT.601
   */
  public static float getLuminanceBT601(int color) {
    return getRed(color) * 0.299f + getGreen(color) * 0.587f
        + getBlue(color) * 0.114f;
  }

  public static int getRed(int color) {
    return (color >> 16 & 0xFF);
  }

  public static int getGreen(int color) {
    return (color >> 8 & 0xFF);
  }

  public static int getBlue(int color) {
    return (color & 0xFF);
  }

  public static int getAlpha(int color) {
    return (color >> 24 & 0xFF);
  }

  public static float getLuminance(int r, int g, int b) {
    return 0.2126f * r + 0.7152f * g + 0.0722f * b;
  }

  public static float getLuminance(int color) {
    return getLuminance(getRed(color), getGreen(color), getBlue(color));
  }

  public static int toIntColor(int r, int g, int b) {
    return toIntColor(r, g, b, 255);
  }

  public static int toIntColor(int r, int g, int b, int a) {
    return (b & 0xFF) | ((g & 0xFF) << 8) | ((r & 0xFF) << 16) | (a << 24);
  }

  @Override
  public float getElement(int num) {
    switch (num) {
      case 0:
        return r / 255f;
      case 1:
        return g / 255f;
      case 2:
        return b / 255f;
      case 3:
        return a / 255f;
      default:
        throw new IndexOutOfBoundsException(num + ">= 4");
    }
  }

  public String toFeatureString() {
    return "<" + r + ", " + g + ", " + b + ">";
  }

  public static ReadableRGBContainer fromColorString(String colorString){
    if (colorString == null || colorString.length() != 7){
      return new ReadableRGBContainer(0);
    }

    int r = 0, g = 0, b = 0;

    try{
      r = Integer.parseInt(colorString.substring(1,3), 16);
    }catch (NumberFormatException e){}

    try{
      g = Integer.parseInt(colorString.substring(3,5), 16);
    }catch (NumberFormatException e){}

    try{
      b = Integer.parseInt(colorString.substring(5,7), 16);
    }catch (NumberFormatException e){}

    return new ReadableRGBContainer(r, g, b);

  }
}
