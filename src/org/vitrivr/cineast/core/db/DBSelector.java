package org.vitrivr.cineast.core.db;

import java.util.List;
import java.util.Map;

import org.vitrivr.cineast.core.config.ReadableQueryConfig;
import org.vitrivr.cineast.core.data.StringDoublePair;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;

public interface DBSelector {

  boolean open(String name);

  boolean close();

  /**
   * @return a list of pairs containing an id and the distance to the query vector
   */
  List<StringDoublePair> getNearestNeighbours(int k, float[] vector, String column,
      ReadableQueryConfig config);

  List<Map<String, PrimitiveTypeProvider>> getNearestNeighbourRows(int k, float[] vector,
      String column, ReadableQueryConfig config);

  List<float[]> getFeatureVectors(String fieldName, String value, String vectorName);

  List<Map<String, PrimitiveTypeProvider>> getRows(String fieldName, String value);

  List<Map<String, PrimitiveTypeProvider>> getRows(String fieldName, String... values);
  
  List<Map<String, PrimitiveTypeProvider>> getRows(String fieldName, Iterable<String> values);

  /**
   * SELECT column from the table. Be careful with large entities
   */
  List<PrimitiveTypeProvider> getAll(String column);

  /**
   * SELECT * from
   */
  List<Map<String, PrimitiveTypeProvider>> getAll();

  boolean existsEntity(String name);

  /** Get first k rows */
  List<Map<String, PrimitiveTypeProvider>> preview(int k);
}
