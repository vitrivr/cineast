package ch.unibas.cs.dbis.cineast.core.db;

import java.util.List;

import ch.unibas.cs.dbis.cineast.core.config.QueryConfig;
import ch.unibas.cs.dbis.cineast.core.data.StringDoublePair;

public interface DBSelector {

	boolean open(String name);
	
	boolean close();
	/**
	 * @return a list of pairs containing an id and the distance to the query vector
	 */
	List<StringDoublePair> getNearestNeighbours(int k, float[] vector, String column, QueryConfig config);
	
	List<float[]> getFeatureVectors(String fieldName, String value, String vectorName);
	
}
