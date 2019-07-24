package org.vitrivr.cineast.core.util.distance;

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
    if (o1.containsKey("distance") && o2.containsKey("distance")) {
      double d1 = o1.get("distance").getDouble();
      double d2 = o2.get("distance").getDouble();
      compare = Double.compare(d1, d2);
    } else {
      compare = comp.compare(o1.get(vectorName).getFloatArray(),
          o2.get(vectorName).getFloatArray());
    }
    if (compare != 0) {
      return compare;
    }
    if (o1.containsKey("id") && o2.containsKey("id")) {
      return o1.get("id").getString().compareTo(o2.get("id").getString());
    }
    return Integer.compare(o1.hashCode(), o2.hashCode());

  }

}
