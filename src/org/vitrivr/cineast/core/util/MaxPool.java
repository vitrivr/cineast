package org.vitrivr.cineast.core.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.vitrivr.cineast.core.data.LongDoublePair;

import gnu.trove.iterator.TLongDoubleIterator;
import gnu.trove.map.hash.TLongDoubleHashMap;

public class MaxPool {

	private MaxPool(){}
	
	public static List<LongDoublePair> maxPool(List<LongDoublePair> list){
		TLongDoubleHashMap map = new TLongDoubleHashMap();
		for(LongDoublePair ldp : list){
			if(map.contains(ldp.key)){
				if(map.get(ldp.key) < ldp.value){
					map.put(ldp.key, ldp.value);
				}
			}else{
				map.put(ldp.key, ldp.value);
			}
		}
		
		ArrayList<LongDoublePair> _return = new ArrayList<>(map.size());
		TLongDoubleIterator iter = map.iterator();
		while(iter.hasNext()){
			iter.advance();
			_return.add(new LongDoublePair(iter.key(), iter.value()));
		}
		Collections.sort(_return, LongDoublePair.COMPARATOR);
		return _return;
	}
	
}
