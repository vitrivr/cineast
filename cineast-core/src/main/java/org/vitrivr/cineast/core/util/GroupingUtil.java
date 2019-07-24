package org.vitrivr.cineast.core.util;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

import com.google.common.collect.ImmutableList;

public class GroupingUtil {
  private GroupingUtil() {}

  /**
   * Filters the given {@code elements} by keeping only the greatest element of each group.
   *
   * @param elements stream of elements to filter
   * @param groupFunction function that returns the group attribute given an element
   * @param comparator comparator to determine the greatest element
   * @param <E> type of elements
   * @param <G> type of the group attribute
   * @return list of elements containing the maximum element of each group
   */
  public static <E, G> List<E> filterMaxByGroup(Stream<E> elements, Function<E, G> groupFunction, Comparator<E> comparator) {
    Map<G, E> maxMap = new HashMap<>();
    Iterator<E> iterator = elements.iterator();
    while (iterator.hasNext()) {
      E next = iterator.next();
      G groupAttribute = groupFunction.apply(next);
      E existing = maxMap.get(groupAttribute);
      if (existing == null || comparator.compare(next, existing) > 0) {
        maxMap.put(groupAttribute, next);
      }
    }
    return ImmutableList.copyOf(maxMap.values());
  }
}
