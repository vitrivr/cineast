package ch.unibas.cs.dbis.cineast.core.data;

import java.util.ArrayList;
import java.util.List;

public class FloatVectorImpl implements FloatVector {

	private ArrayList<Float> list;
	
	public FloatVectorImpl(ArrayList<Float> list){
		this.list = list;
	}
	
	public FloatVectorImpl(float[] array){
		this();
		for(float f : array){
			this.list.add(f);
		}
	}
	
	public FloatVectorImpl(){
		this(new ArrayList<Float>());
	}
	
	public FloatVectorImpl(List<Double> list) {
		this();
		for(double d : list){
			this.list.add((float)d);
		}
	}

	public FloatVectorImpl(short[] array) {
		this();
		for(short s : array){
			this.list.add((float)s);
		}
	}

	public FloatVectorImpl(double[] array) {
		this();
		for(double s : array){
			this.list.add((float)s);
		}
	}

	@Override
	public int getElementCount() {
		return this.list.size();
	}

	@Override
	public float getElement(int num) {
		return this.list.get(num);
	}

	@Override
	public void setElement(int num, float val) {
		this.list.set(num, val);
	}
	
	public void add(float element){
		this.list.add(element);
	}

	@Override
	public String toFeatureString() {
		StringBuffer buf = new StringBuffer();
		buf.append('<');
		for(int i = 0; i < this.list.size(); ++i){
			buf.append(list.get(i));
			if(i < this.list.size() - 1){
				buf.append(", ");
			}
		}
		buf.append('>');
		return buf.toString();
	}
	
	@Override
	public String toString(){
		return this.toFeatureString();
	}

	@Override
	public double getDistance(ReadableFloatVector other) {
		int len = Math.min(this.getElementCount(), other.getElementCount());
		double d = 0d, e = 0d;
		for(int i = 0; i < len; ++i){
			e = getElement(i) - other.getElement(i);
			d += e * e;
		}
		return Math.sqrt(d);
	}

	@Override
	public float[] toFloatArray() {
		float[] _return = new float[this.list.size()];
		for(int i = 0; i < _return.length; ++i){
			_return[i] = this.list.get(i);
		}
		return _return;
	}

}
