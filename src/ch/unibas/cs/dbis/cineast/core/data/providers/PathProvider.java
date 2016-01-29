package ch.unibas.cs.dbis.cineast.core.data.providers;

import georegression.struct.point.Point2D_F32;

import java.util.LinkedList;
import java.util.List;

public interface PathProvider {

	List<LinkedList<Point2D_F32>> getPaths();
	
}
