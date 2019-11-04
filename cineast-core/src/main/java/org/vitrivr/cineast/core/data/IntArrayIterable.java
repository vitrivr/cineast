package org.vitrivr.cineast.core.data;

import java.util.Iterator;

public class IntArrayIterable implements Iterable<Integer> {

	private final int[] arr;
	
	public IntArrayIterable(int[] arr){
		this.arr = arr;
	}
	
	@Override
	public Iterator<Integer> iterator() {
		return new IntArrayIterator();
	}
	
	class IntArrayIterator implements Iterator<Integer>{

		int i = 0;
		
		@Override
		public boolean hasNext() {
			return i < arr.length;
		}

		@Override
		public Integer next() {
			return arr[i++];
		}

		@Override
		public void remove() {
			//ignore
		}

	}

}
