package org.vitrivr.cineast.core.color;

import java.util.ArrayList;
import java.util.List;

public class ReadableHSVContainer extends AbstractColorContainer<ReadableHSVContainer> implements Cloneable{

	protected float h, s, v;
	
	public ReadableHSVContainer(float h, float s, float v){
		this.h = h;
		this.s = s;
		this.v = v;
	}
	
	public ReadableHSVContainer(double h, double s, double v){
		this((float)h, (float)s, (float)v);
	}
	
	@Override
	public float getElement(int num) {
		switch (num) {
		case 0: return h;
		case 1: return s;
		case 2: return v;
		default: throw new IndexOutOfBoundsException(num + ">= 3");
		}
	}
	
	
	public String toFeatureString() {
		return "<" + h + ", " + s + ", " + v + ">";
	}

	@Override
	public String toString() {
		return "HSVContainer(" + h + ", " + s + ", " + v + ")";
	}
	
	public float getH(){
		return this.h;
	}
	
	public float getS(){
		return this.s;
	}
	
	public float getV(){
		return this.v;
	}

	@Override
	public float[] toArray(float[] arr) {
		if(arr != null && arr.length == 3){
			arr[0] = h;
			arr[1] = s;
			arr[2] = v;
			return arr;
		}
		return new float[]{h, s, v};
	}

	@Override
	public List<Float> toList(List<Float> list) {
		if(list != null){
			list.clear();
		}else{
			list = new ArrayList<>(3);
		}
		list.add(h);
		list.add(s);
		list.add(v);
		return list;
	}
	
}
