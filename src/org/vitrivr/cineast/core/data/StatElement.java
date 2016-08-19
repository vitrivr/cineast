package ch.unibas.cs.dbis.cineast.core.data;

public class StatElement {

	private int count = 0;
	private float sum = 0, sumsquared = 0;
	
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
	
	public void add(float f){
		this.sum += f;
		this.sumsquared += f * f;
		++count;
	}
	
	public void add(float... f){
		for(int i = 0; i < f.length; ++i){
			this.add(f);
		}
	}
	
	public void add(FloatVector f){
		for(int i = 0; i < f.getElementCount(); ++i){
			this.add(f.getElement(i));
		}
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
