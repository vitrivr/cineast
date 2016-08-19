package org.vitrivr.cineast.core.data;

import java.util.Comparator;

public class StringDoublePair {
	
	@Override
	public String toString() {
		return "StringDoublePair(" + key + ", " + value + ")";
	}

	public String key;
	public double value;
	
	public StringDoublePair(String k, double v){
		this.key = k;
		this.value = v;
	}
	
	public static final Comparator<StringDoublePair> COMPARATOR = new Comparator<StringDoublePair>(){
		
		@Override
		public int compare(StringDoublePair o1, StringDoublePair o2) {
			return Double.compare(o2.value, o1.value);
		}
		
	};
	
}
