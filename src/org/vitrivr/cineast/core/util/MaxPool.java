package org.vitrivr.cineast.core.util;

import gnu.trove.iterator.TLongDoubleIterator;
import gnu.trove.iterator.TObjectDoubleIterator;
import gnu.trove.map.hash.TLongDoubleHashMap;
import gnu.trove.map.hash.TObjectDoubleHashMap;
import org.vitrivr.cineast.core.data.LongDoublePair;
import org.vitrivr.cineast.core.data.StringDoublePair;

import java.util.*;

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

	public static List<StringDoublePair> maxPoolStringId(List<StringDoublePair> list){
		TObjectDoubleHashMap map = new TObjectDoubleHashMap();
		for(StringDoublePair sdp : list){
			if(map.containsKey(sdp.key)){
				if(map.get(sdp.key) < sdp.value){
					map.put(sdp.key, sdp.value);
				}
			}else{
				map.put(sdp.key, sdp.value);
			}
		}

		ArrayList<StringDoublePair> _return = new ArrayList<>(map.size());
		TObjectDoubleIterator iter = map.iterator();
		while(iter.hasNext()){
			iter.advance();
			_return.add(new StringDoublePair(iter.key().toString(), iter.value()));
		}
		Collections.sort(_return, StringDoublePair.COMPARATOR);
		return _return;
	}
	
}
