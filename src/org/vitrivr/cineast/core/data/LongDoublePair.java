package org.vitrivr.cineast.core.data;

import java.util.Comparator;

public class LongDoublePair {

	@Override
	public String toString() {
		return "LongDoublrPair(" + key + ", " + value + ")";
	}

	public long key;
	public double value;
	
	public LongDoublePair(long k, double v){
		this.key = k;
		this.value = v;
	}
	
	public static final Comparator<LongDoublePair> COMPARATOR = new Comparator<LongDoublePair>(){
		
		@Override
		public int compare(LongDoublePair o1, LongDoublePair o2) {
			return Double.compare(o2.value, o1.value);
		}
		
	};
	
}
