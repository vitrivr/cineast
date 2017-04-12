package org.vitrivr.cineast.core.data.score;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.vitrivr.cineast.core.data.CorrespondenceFunction;
import org.vitrivr.cineast.core.util.GroupingUtils;

public class DistanceElements {
  public static final Comparator<DistanceElement> INVERSE_DISTANCE_COMPARATOR =
      Comparator.<DistanceElement>comparingDouble(e -> e.getDistance()).reversed();

  private DistanceElements() {}

  public static <T extends DistanceElement> List<ScoreElement> toScore(List<T> distances,
      CorrespondenceFunction f) {
    return distances.stream().map(e -> e.toScore(f)).collect(Collectors.toList());
  }

  public static List<DistanceElement> filterMinimumDistances(Stream<DistanceElement> elements) {
    return GroupingUtils.filterMaxByGroup(elements, e -> e.getId(), INVERSE_DISTANCE_COMPARATOR);
  }
}
