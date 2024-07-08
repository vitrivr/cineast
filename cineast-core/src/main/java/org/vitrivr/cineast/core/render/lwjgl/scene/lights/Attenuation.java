package org.vitrivr.cineast.core.render.lwjgl.scene.lights;

public class Attenuation {

  private float constant;
  private float exponent;
  private float linear;

  public Attenuation(float constant, float linear, float exponent) {
    this.constant = constant;
    this.exponent = exponent;
    this.linear = linear;
  }

  public float getConstant() {
    return this.constant;
  }

  public float getLinear() {
    return this.linear;
  }

  public float getExponent() {
    return this.exponent;
  }

  public void setConstant(float constant) {
    this.constant = constant;
  }

  public void setLinear(float linear) {
    this.linear = linear;
  }

  public void setExponent(float exponent) {
    this.exponent = exponent;
  }

}
