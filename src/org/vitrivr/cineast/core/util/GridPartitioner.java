package org.vitrivr.cineast.core.util;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class GridPartitioner {

	private GridPartitioner(){}
	
	public static <T> ArrayList<LinkedList<T>> partition(List<T> input, int width, int height, int xpartitions, int ypartitions){
		ArrayList<LinkedList<T>> _return = new ArrayList<LinkedList<T>>(xpartitions * ypartitions);
		for(int i = 0; i < xpartitions * ypartitions; ++i){
			_return.add(new LinkedList<T>());
		}
		
		int i = 0;
		for(T t : input){
			int index = (((i % width) * xpartitions) / width) + xpartitions * (i * ypartitions / width / height);
			_return.get(index).add(t);
			++i;
		}
		
		return _return;
	}
	
}
