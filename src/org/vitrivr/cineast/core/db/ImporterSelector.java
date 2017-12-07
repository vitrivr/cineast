package org.vitrivr.cineast.core.db;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.vitrivr.cineast.core.config.Config;
import org.vitrivr.cineast.core.config.QueryConfig;
import org.vitrivr.cineast.core.config.ReadableQueryConfig;
import org.vitrivr.cineast.core.data.FixedSizePriorityQueue;
import org.vitrivr.cineast.core.data.distance.DistanceElement;
import org.vitrivr.cineast.core.data.providers.primitive.FloatTypeProvider;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;
import org.vitrivr.cineast.core.importer.Importer;
import org.vitrivr.cineast.core.util.distance.FloatArrayDistance;
import org.vitrivr.cineast.core.util.distance.PrimitiveTypeMapDistanceComparator;

public abstract class ImporterSelector<T extends Importer<?>> implements DBSelector {

  private File file;

  @Override
  public boolean open(String name) {
    this.file = new File(
        Config.sharedConfig().getDatabase().getHost() + "/" + name + getFileExtension());
    return file.exists() && file.isFile() && file.canRead();
  }

  @Override
  public boolean close() {
    return true;
  }

  @Override
  public <E extends DistanceElement> List<E> getNearestNeighbours(int k, float[] vector,
      String column, Class<E> distanceElementClass, ReadableQueryConfig config) {
    List<Map<String, PrimitiveTypeProvider>> results = getNearestNeighbourRows(k, vector, column,
        config);
    return results.stream()
        .map(m -> DistanceElement.create(
            distanceElementClass, m.get("id").getString(), m.get("distance").getDouble()))
        .limit(k)
        .collect(Collectors.toList());
  }

  @Override
  public List<Map<String, PrimitiveTypeProvider>> getNearestNeighbourRows(int k, float[] vector,
      String column, ReadableQueryConfig config) {

    config = QueryConfig.clone(config);

    Importer<?> importer = newImporter(this.file);

    FloatArrayDistance distance = FloatArrayDistance.fromQueryConfig(config);

    FixedSizePriorityQueue<Map<String, PrimitiveTypeProvider>> knn = FixedSizePriorityQueue
        .create(k, new PrimitiveTypeMapDistanceComparator(column, vector, distance));

    Map<String, PrimitiveTypeProvider> map;
    while ((map = importer.readNextAsMap()) != null) {
      if(!map.containsKey(column)){
        continue;
      }
      double d = distance.applyAsDouble(vector, map.get(column).getFloatArray());
      map.put("distance", new FloatTypeProvider((float) d));
      knn.add(map);
    }

    int len = Math.min(knn.size(), k);
    ArrayList<Map<String, PrimitiveTypeProvider>> _return = new ArrayList<>(len);

    for (Map<String, PrimitiveTypeProvider> i : knn) {
      _return.add(i);
      if (_return.size() >= len) {
        break;
      }
    }

    return _return;
  }

  @Override
  public List<float[]> getFeatureVectors(String fieldName, String value, String vectorName) {
    ArrayList<float[]> _return = new ArrayList<>(1);

    if (value == null || value.isEmpty()) {
      return _return;
    }

    Importer<?> importer = newImporter(this.file);
    Map<String, PrimitiveTypeProvider> map;
    while ((map = importer.readNextAsMap()) != null) {
      if (!map.containsKey(fieldName)) {
        continue;
      }
      if (!map.containsKey(vectorName)) {
        continue;
      }
      if (value.equals(map.get(fieldName).getString())) {
        _return.add(map.get(vectorName).getFloatArray());
      }
    }

    return _return;
  }

  @Override
  public List<Map<String, PrimitiveTypeProvider>> getRows(String fieldName, String value) {
    return this.getRows(fieldName, new String[] { value });
  }

  @Override
  public List<Map<String, PrimitiveTypeProvider>> getRows(String fieldName, String... values) {
    ArrayList<Map<String, PrimitiveTypeProvider>> _return = new ArrayList<>(1);

    if (values == null || values.length == 0) {
      return _return;
    }

    Importer<?> importer = newImporter(this.file);
    Map<String, PrimitiveTypeProvider> map;
    while ((map = importer.readNextAsMap()) != null) {
      if (!map.containsKey(fieldName)) {
        continue;
      }
      for (int i = 0; i < values.length; ++i) {
        if (values[i].equals(map.get(fieldName).getString())) {
          _return.add(map);
          break;
        }
      }

    }

    return _return;
  }

  @Override
  public List<Map<String, PrimitiveTypeProvider>> getRows(String fieldName,
      Iterable<String> values) {
    if (values == null) {
      return new ArrayList<>(0);
    }

    ArrayList<String> tmp = new ArrayList<>();
    for (String value : values) {
      tmp.add(value);
    }

    String[] valueArr = new String[tmp.size()];
    tmp.toArray(valueArr);

    return this.getRows(fieldName, valueArr);
  }

  @Override
  public List<PrimitiveTypeProvider> getAll(String column) {
    List<PrimitiveTypeProvider> _return = new ArrayList<>();

    Importer<?> importer = newImporter(this.file);
    Map<String, PrimitiveTypeProvider> map;
    while ((map = importer.readNextAsMap()) != null) {
      PrimitiveTypeProvider p = map.get(column);
      if (p != null) {
        _return.add(p);
      }
    }
    return _return;
  }

  @Override
  public List<Map<String, PrimitiveTypeProvider>> getAll() {

    List<Map<String, PrimitiveTypeProvider>> _return = new ArrayList<>();

    Importer<?> importer = newImporter(this.file);
    Map<String, PrimitiveTypeProvider> map;
    while ((map = importer.readNextAsMap()) != null) {
      _return.add(map);
    }
    return _return;
  }

  @Override
  public boolean existsEntity(String name) {
    File file = new File(
        Config.sharedConfig().getDatabase().getHost() + "/" + name + getFileExtension());
    return file.exists() && file.isFile() && file.canRead();
  }

  @Override
  public List<Map<String, PrimitiveTypeProvider>> preview(int k) {
    List<Map<String, PrimitiveTypeProvider>> _return = new ArrayList<>(k);

    if (k <= 0) {
      return _return;
    }

    Importer<?> importer = newImporter(this.file);
    Map<String, PrimitiveTypeProvider> map;
    while ((map = importer.readNextAsMap()) != null) {
      _return.add(map);
      if (_return.size() >= k) {
        break;
      }
    }
    return _return;
  }

  protected abstract T newImporter(File f);

  protected abstract String getFileExtension();

  @Override
  public <T extends DistanceElement> List<T> getBatchedNearestNeighbours(int k,
      List<float[]> vectors, String column, Class<T> distanceElementClass,
      List<ReadableQueryConfig> configs) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public <T extends DistanceElement> List<T> getCombinedNearestNeighbours(int k,
      List<float[]> vectors, String column, Class<T> distanceElementClass,
      List<ReadableQueryConfig> configs, MergeOperation merge, Map<String, String> options) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<Map<String, PrimitiveTypeProvider>> getRows(String fieldName, RelationalOperator operator, String value) {
    throw new IllegalStateException("Not implemented.");
  }

  @Override
  public List<Map<String, PrimitiveTypeProvider>> getRows(String fieldName, RelationalOperator operator, Iterable<String> values) {
    throw new IllegalStateException("Not implemented.");
  }
}
