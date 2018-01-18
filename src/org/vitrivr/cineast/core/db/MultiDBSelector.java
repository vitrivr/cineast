package org.vitrivr.cineast.core.db;

import java.util.List;
import java.util.Map;
import org.vitrivr.cineast.core.config.ReadableQueryConfig;
import org.vitrivr.cineast.core.data.distance.DistanceElement;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;

public class MultiDBSelector implements DBSelector {

  private final DBSelector knnSelector, textSelector;

  public MultiDBSelector(DBSelector knnSelector, DBSelector textSelector){
    this.knnSelector = knnSelector;
    this.textSelector = textSelector;
  }

  @Override
  public boolean open(String name) {
    return this.knnSelector.open(name) && this.textSelector.open(name);
  }

  @Override
  public boolean close() {
    return this.knnSelector.close() && this.textSelector.close();
  }

  @Override
  public <T extends DistanceElement> List<T> getNearestNeighbours(int k, float[] vector,
      String column, Class<T> distanceElementClass, ReadableQueryConfig config) {
    return this.knnSelector.getNearestNeighbours(k, vector, column, distanceElementClass, config);
  }

  @Override
  public <T extends DistanceElement> List<T> getBatchedNearestNeighbours(int k,
      List<float[]> vectors, String column, Class<T> distanceElementClass,
      List<ReadableQueryConfig> configs) {
    return this.knnSelector.getBatchedNearestNeighbours(k, vectors, column, distanceElementClass, configs);
  }

  @Override
  public <T extends DistanceElement> List<T> getCombinedNearestNeighbours(int k,
      List<float[]> vectors, String column, Class<T> distanceElementClass,
      List<ReadableQueryConfig> configs, MergeOperation merge, Map<String, String> options) {
    return this.knnSelector.getCombinedNearestNeighbours(k, vectors, column, distanceElementClass, configs, merge, options);
  }

  @Override
  public List<Map<String, PrimitiveTypeProvider>> getNearestNeighbourRows(int k, float[] vector,
      String column, ReadableQueryConfig config) {
    return this.knnSelector.getNearestNeighbourRows(k, vector, column, config);
  }

  @Override
  public List<float[]> getFeatureVectors(String fieldName, String value, String vectorName) {
    return this.knnSelector.getFeatureVectors(fieldName, value, vectorName);
  }

  @Override
  public List<Map<String, PrimitiveTypeProvider>> getRows(String fieldName, String value) {
    return this.knnSelector.getRows(fieldName, value);
  }

  @Override
  public List<Map<String, PrimitiveTypeProvider>> getRows(String fieldName, String... values) {
    return this.knnSelector.getRows(fieldName, values);
  }

  @Override
  public List<Map<String, PrimitiveTypeProvider>> getRows(String fieldName,
      Iterable<String> values) {
    return this.knnSelector.getRows(fieldName, values);
  }

  @Override
  public List<Map<String, PrimitiveTypeProvider>> getFulltextRows(int rows, String fieldname,
      String... terms) {
    return this.textSelector.getFulltextRows(rows, fieldname, terms);
  }

  @Override
  public List<Map<String, PrimitiveTypeProvider>> getRows(String fieldName,
      RelationalOperator operator, String value) {
    return this.knnSelector.getRows(fieldName, operator, value);
  }

  @Override
  public List<Map<String, PrimitiveTypeProvider>> getRows(String fieldName,
      RelationalOperator operator, Iterable<String> values) {
    return this.knnSelector.getRows(fieldName, operator, values);
  }

  @Override
  public List<PrimitiveTypeProvider> getAll(String column) {
    return this.knnSelector.getAll(column);
  }

  @Override
  public List<Map<String, PrimitiveTypeProvider>> getAll() {
    return this.knnSelector.getAll();
  }

  @Override
  public boolean existsEntity(String name) {
    return this.knnSelector.existsEntity(name) && this.textSelector.existsEntity(name);
  }

  @Override
  public List<Map<String, PrimitiveTypeProvider>> preview(int k) {
    return this.knnSelector.preview(k);
  }
}
