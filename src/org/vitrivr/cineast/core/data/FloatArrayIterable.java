package org.vitrivr.cineast.core.data;

import java.util.Iterator;

public class FloatArrayIterable implements Iterable<Float> {

	private final float[] arr;
	
	public FloatArrayIterable(float[] arr){
		this.arr = arr;
	}
	
	@Override
	public Iterator<Float> iterator() {
		return new FloatArrayIterator();
	}
	
	class FloatArrayIterator implements Iterator<Float>{

		int i = 0;
		
		@Override
		public boolean hasNext() {
			return i < arr.length;
		}

		@Override
		public Float next() {
			return arr[i++];
		}

		@Override
		public void remove() {
			//ignore
		}

	}

}
