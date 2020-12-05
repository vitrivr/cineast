package org.vitrivr.cineast.core.db;

import static org.vitrivr.cineast.core.util.CineastConstants.DB_DISTANCE_VALUE_QUALIFIER;
import static org.vitrivr.cineast.core.util.CineastConstants.GENERIC_ID_COLUMN_QUALIFIER;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.config.QueryConfig;
import org.vitrivr.cineast.core.config.ReadableQueryConfig;
import org.vitrivr.cineast.core.data.FixedSizePriorityQueue;
import org.vitrivr.cineast.core.data.distance.DistanceElement;
import org.vitrivr.cineast.core.data.providers.primitive.FloatTypeProvider;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;
import org.vitrivr.cineast.core.data.providers.primitive.ProviderDataType;
import org.vitrivr.cineast.core.importer.Importer;
import org.vitrivr.cineast.core.util.distance.*;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public abstract class ImporterSelector<T extends Importer<?>> implements DBSelector {

  protected ImporterSelector(File baseDirectory){
    this.baseDirectory = baseDirectory;
  }

  private File file;
  private final File baseDirectory;
  private static final Logger LOGGER = LogManager.getLogger();

  @Override
  public boolean open(String name) {
    this.file = new File(this.baseDirectory, name + getFileExtension());
    return file.exists() && file.isFile() && file.canRead();
  }

  public boolean openFile(File file) {
    if (file == null) {
      throw new NullPointerException("file cannot be null");
    }
    this.file = file;
    return file.exists() && file.isFile() && file.canRead();
  }

  @Override
  public boolean close() {
    return true;
  }

  @Override
  public <E extends DistanceElement> List<E> getNearestNeighboursGeneric(int k,
      PrimitiveTypeProvider queryProvider, String column, Class<E> distanceElementClass,
      ReadableQueryConfig config) {
    List<Map<String, PrimitiveTypeProvider>> results;
    if (queryProvider.getType().equals(ProviderDataType.FLOAT_ARRAY) || queryProvider.getType()
        .equals(ProviderDataType.INT_ARRAY)) {
      results = getNearestNeighbourRows(k, queryProvider.getFloatArray(), column, config);
    } else {
     results = getNearestNeighbourRows(k, queryProvider, column, config);
    }
    return results.stream()
        .map(m -> DistanceElement.create(
            distanceElementClass, m.get(GENERIC_ID_COLUMN_QUALIFIER).getString(), m.get(DB_DISTANCE_VALUE_QUALIFIER).getDouble()))
        .limit(k)
        .collect(Collectors.toList());
  }

  /**
   * Full table scan. Don't do it for performance-intensive stuff.
   */
  @SuppressWarnings("unchecked")
  private List<Map<String, PrimitiveTypeProvider>> getNearestNeighbourRows(int k,
      PrimitiveTypeProvider queryProvider, String column, ReadableQueryConfig config) {
    if (queryProvider.getType().equals(ProviderDataType.FLOAT_ARRAY) || queryProvider.getType()
        .equals(ProviderDataType.INT_ARRAY)) {
      return getNearestNeighbourRows(k, PrimitiveTypeProvider.getSafeFloatArray(queryProvider),
          column, config);
    }
    LOGGER.debug("Switching to non-float based lookup, reading from file");
    Importer<?> importer = newImporter(this.file);

    Distance distance;
    FixedSizePriorityQueue<Map<String, PrimitiveTypeProvider>> knn;
    if (queryProvider.getType().equals(ProviderDataType.BITSET)) {
      distance = new BitSetHammingDistance();
      knn = FixedSizePriorityQueue
          .create(k, new BitSetComparator(column, distance, queryProvider.getBitSet()));
    } else {
      throw new RuntimeException(queryProvider.getType().toString());
    }

    Map<String, PrimitiveTypeProvider> map;
    while ((map = importer.readNextAsMap()) != null) {
      if (!map.containsKey(column)) {
        continue;
      }
      double d;
      if (queryProvider.getType().equals(ProviderDataType.BITSET)) {
        d = distance
            .applyAsDouble(queryProvider.getBitSet(), map.get(column).getBitSet());
        map.put("distance", new FloatTypeProvider((float) d));
        knn.add(map);
      } else {
        throw new RuntimeException(queryProvider.getType().toString());
      }
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
  public List<Map<String, PrimitiveTypeProvider>> getNearestNeighbourRows(int k, float[] vector,
      String column, ReadableQueryConfig config) {

    config = QueryConfig.clone(config);

    Importer<?> importer = newImporter(this.file);

    FloatArrayDistance distance = FloatArrayDistance.fromQueryConfig(config);

    FixedSizePriorityQueue<Map<String, PrimitiveTypeProvider>> knn = FixedSizePriorityQueue
            .create(k, new PrimitiveTypeMapDistanceComparator(column, vector, distance));

    HashSet<String> relevant = null;
    if (config.hasRelevantSegmentIds()) {
      Set<String> ids = config.getRelevantSegmentIds();
      relevant = new HashSet<>(ids.size());
      relevant.addAll(ids);
    }

    Map<String, PrimitiveTypeProvider> map;
    while ((map = importer.readNextAsMap()) != null) {
      if (!map.containsKey(column)) {
        continue;
      }
      if (relevant != null && !relevant.contains(map.get(GENERIC_ID_COLUMN_QUALIFIER))) {
        continue;
      }
      double d = distance
              .applyAsDouble(vector, PrimitiveTypeProvider.getSafeFloatArray(map.get(column)));
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
  public List<float[]> getFeatureVectors(String fieldName, PrimitiveTypeProvider value, String vectorName) {
    ArrayList<float[]> _return = new ArrayList<>(1);

    if (value == null || value.getString().isEmpty()) {
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
        _return.add(PrimitiveTypeProvider.getSafeFloatArray(map.get(vectorName)));
      }
    }

    return _return;
  }

  @Override
  public List<Map<String, PrimitiveTypeProvider>> getRows(String fieldName, PrimitiveTypeProvider... values) {
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
      Iterable<PrimitiveTypeProvider> values) {
    if (values == null) {
      return new ArrayList<>(0);
    }

    ArrayList<PrimitiveTypeProvider> tmp = new ArrayList<>();
    for (PrimitiveTypeProvider value : values) {
      tmp.add(value);
    }

    PrimitiveTypeProvider[] valueArr = new PrimitiveTypeProvider[tmp.size()];
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
    File file = new File(this.baseDirectory, name + getFileExtension());
    return file.exists() && file.isFile() && file.canRead();
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
  public List<Map<String, PrimitiveTypeProvider>> getRows(String fieldName,
      RelationalOperator operator, Iterable<PrimitiveTypeProvider> values) {
    throw new IllegalStateException("Not implemented.");
  }

  public List<Map<String, PrimitiveTypeProvider>> getFulltextRows(int rows, String fieldname, ReadableQueryConfig queryConfig,
      String... terms) {
    throw new IllegalStateException("Not implemented.");
  }

  @Override
  public boolean ping() {
    return true;
  }
}
