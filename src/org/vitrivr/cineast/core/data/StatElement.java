package org.vitrivr.cineast.core.data;

public class StatElement {

	private int count = 0;
	private float sum = 0, sumsquared = 0;
	
	public StatElement(){
	  
	}
	
	public StatElement(float initialValue){
    add(initialValue);
  }
	
	public void reset(){
		this.count = 0;
		this.sum = 0;
		this.sumsquared = 0;
	}
	
	public int getCount(){
		return this.count;
	}
	
	public float getSum(){
		return this.sum;
	}
	
	public StatElement add(float f){
		this.sum += f;
		this.sumsquared += f * f;
		++count;
		return this;
	}
	
	public StatElement add(float... f){
		for(int i = 0; i < f.length; ++i){
			this.add(f);
		}
		return this;
	}
	
	public StatElement add(FloatVector f){
		for(int i = 0; i < f.getElementCount(); ++i){
			this.add(f.getElement(i));
		}
		return this;
	}
	
	public float getAvg(){
		return (this.count == 0) ? 0 : this.sum / this.count;
	}
	
	public float getVariance(){
		float u = getAvg();
		float var = (this.sumsquared - 2 * u * this.sum + this.count * u * u) / (this.count - 1);
		return Float.isNaN(var) ? 0f : var;
	}
	
}
