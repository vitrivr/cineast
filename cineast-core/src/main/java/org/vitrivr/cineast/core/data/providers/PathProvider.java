package org.vitrivr.cineast.core.data.providers;

import georegression.struct.point.Point2D_F32;
import org.vitrivr.cineast.core.data.Pair;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;


public interface PathProvider {

	public default List<Pair<Integer, LinkedList<Point2D_F32>>> getPaths(){
	  return new ArrayList<>(0);
	}
	
	public default List<Pair<Integer, LinkedList<Point2D_F32>>> getBgPaths(){
	   return new ArrayList<>(0);
	}
	
}
