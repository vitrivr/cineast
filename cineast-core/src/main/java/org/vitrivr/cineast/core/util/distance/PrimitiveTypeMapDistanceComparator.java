package org.vitrivr.cineast.core.util.distance;

import static org.vitrivr.cineast.core.util.CineastConstants.DB_DISTANCE_VALUE_QUALIFIER;
import static org.vitrivr.cineast.core.util.CineastConstants.GENERIC_ID_COLUMN_QUALIFIER;

import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;

import java.util.Comparator;
import java.util.Map;

public class PrimitiveTypeMapDistanceComparator
    implements Comparator<Map<String, PrimitiveTypeProvider>> {

  private final String vectorName;
  private final FloatArrayDistanceComparator comp;

  public PrimitiveTypeMapDistanceComparator(String vectorName, float[] query,
      FloatArrayDistance distance) {
    if (vectorName == null) {
      throw new NullPointerException("vectorName can not be null");
    }
    if (vectorName.isEmpty()) {
      throw new IllegalArgumentException("vectorName can not be empty");
    }
    this.vectorName = vectorName;
    this.comp = new FloatArrayDistanceComparator(query, distance);
  }

  @Override
  public int compare(Map<String, PrimitiveTypeProvider> o1, Map<String, PrimitiveTypeProvider> o2) {
    if (o1 == o2) { // identical
      return 0;
    }
    int compare = 0;
    if (o1.containsKey(DB_DISTANCE_VALUE_QUALIFIER) && o2.containsKey(DB_DISTANCE_VALUE_QUALIFIER)) {
      double d1 = o1.get(DB_DISTANCE_VALUE_QUALIFIER).getDouble();
      double d2 = o2.get(DB_DISTANCE_VALUE_QUALIFIER).getDouble();
      compare = Double.compare(d1, d2);
    } else {
      compare = comp.compare(o1.get(vectorName).getFloatArray(),
          o2.get(vectorName).getFloatArray());
    }
    if (compare != 0) {
      return compare;
    }
    if (o1.containsKey(GENERIC_ID_COLUMN_QUALIFIER) && o2.containsKey(GENERIC_ID_COLUMN_QUALIFIER)) {
      return o1.get(GENERIC_ID_COLUMN_QUALIFIER).getString().compareTo(o2.get(GENERIC_ID_COLUMN_QUALIFIER).getString());
    }
    return Integer.compare(o1.hashCode(), o2.hashCode());

  }

}
