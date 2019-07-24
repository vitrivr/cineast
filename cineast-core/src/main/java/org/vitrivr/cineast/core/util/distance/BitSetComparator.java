package org.vitrivr.cineast.core.util.distance;

import com.googlecode.javaewah.datastructure.BitSet;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;

import java.util.Comparator;
import java.util.Map;

public class BitSetComparator implements Comparator<Map<String, PrimitiveTypeProvider>> {

  private final String vectorName;
  private final Distance<BitSet> distance;
  private final BitSet query;

  public BitSetComparator(String vectorName, Distance<BitSet> distance, BitSet query) {
    this.vectorName = vectorName;
    this.distance = distance;
    this.query = query;
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
      compare = Double.compare(distance.applyAsDouble(o1.get(vectorName).getBitSet(), query),
          distance.applyAsDouble(o1.get(vectorName).getBitSet(), query));
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
