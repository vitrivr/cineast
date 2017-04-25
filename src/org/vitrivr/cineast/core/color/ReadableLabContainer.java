package org.vitrivr.cineast.core.color;

public class ReadableLabContainer extends AbstractColorContainer<ReadableLabContainer> implements
    Cloneable {

  public static final float SPACE_DIAMETER_SQUARED = 100 * 100 +
      184.439f * 184.439f +    //[-86.185, 98,254]
      202.345f * 202.345f;    //[-107.863, 94.482]

  public static final float SPACE_DIAMETER = (float) Math.sqrt(SPACE_DIAMETER_SQUARED);

  public static final float MAX_CHROMA = (float) Math
      .sqrt(98.254 * 98.254 + 107.863 * 107.863); // ~146

  protected float L, a, b, alpha = 1f;

  public ReadableLabContainer(double L, double a, double b) {
    this((float) L, (float) a, (float) b);
  }

  public ReadableLabContainer(float L, float a, float b) {
    this.L = L;
    this.a = a;
    this.b = b;
  }

  public ReadableLabContainer(double L, double a, double b, double alpha) {
    this((float) L, (float) a, (float) b, (float) alpha);
  }

  public ReadableLabContainer(float L, float a, float b, float alpha) {
    this.L = L;
    this.a = a;
    this.b = b;
    this.alpha = alpha;
  }

  ReadableLabContainer() {
    this(0f, 0f, 0f);
  }

  @Override
  public ReadableLabContainer clone() {
    return new ReadableLabContainer(L, a, b, alpha);
  }

  @Override
  public String toString() {
    return "LabContainer(" + L + ", " + a + ", " + b + ")";
  }

  public float getL() {
    return L;
  }

  public float getA() {
    return a;
  }

  public float getB() {
    return b;
  }

  public float getAlpha() {
    return alpha;
  }

  public float getChroma() {
    return (float) Math.sqrt(a * a + b * b);
  }

  /**
   * see Lübbe, Eva (2010). Colours in the Mind - Colour Systems in Reality- A formula for colour
   * saturation
   *
   * @return the saturation
   */
  public float getSaturation() {
    float c = getChroma();
    if (c > 0f) {
      return c / (float) Math.sqrt(c * c + L * L);
    }
    return 0;
  }

  public float getHue() {
    return (float) ((Math.atan2(b, a) + Math.PI) / (2d * Math.PI));
  }

  @Override
  public float getElement(int num) {
    switch (num) {
      case 0:  return L;
      case 1:  return a;
      case 2:  return b;
      case 3:  return alpha;
      default: throw new IndexOutOfBoundsException(num + ">= 4");
    }
  }


  public String toFeatureString() {
    return "<" + L + ", " + a + ", " + b + ">";
  }
}
