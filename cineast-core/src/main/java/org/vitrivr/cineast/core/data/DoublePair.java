package org.vitrivr.cineast.core.data;

public class DoublePair<T> {

	public T key;
	public double value;
	
	public DoublePair(T k, double v){
		this.key = k;
		this.value = v;
	}
	
	public static <V> DoublePair<V> pair(V key, double value){
		return new DoublePair<V>(key, value);
	}
	
}
