package org.vitrivr.cineast.core.data;

import java.util.Comparator;

public record StringDoublePair(String key, double value) {

  public static final Comparator<StringDoublePair> COMPARATOR = (o1, o2) -> Double.compare(o2.value, o1.value);
}
