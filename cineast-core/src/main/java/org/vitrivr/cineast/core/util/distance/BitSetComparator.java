package org.vitrivr.cineast.core.util.distance;

import static org.vitrivr.cineast.core.util.CineastConstants.DB_DISTANCE_VALUE_QUALIFIER;
import static org.vitrivr.cineast.core.util.CineastConstants.GENERIC_ID_COLUMN_QUALIFIER;

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
    if (o1.containsKey(DB_DISTANCE_VALUE_QUALIFIER) && o2.containsKey(DB_DISTANCE_VALUE_QUALIFIER)) {
      double d1 = o1.get(DB_DISTANCE_VALUE_QUALIFIER).getDouble();
      double d2 = o2.get(DB_DISTANCE_VALUE_QUALIFIER).getDouble();
      compare = Double.compare(d1, d2);
    } else {
      compare = Double.compare(distance.applyAsDouble(o1.get(vectorName).getBitSet(), query),
          distance.applyAsDouble(o1.get(vectorName).getBitSet(), query));
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
