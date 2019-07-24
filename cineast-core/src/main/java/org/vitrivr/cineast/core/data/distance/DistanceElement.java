package org.vitrivr.cineast.core.data.distance;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.vitrivr.cineast.core.data.CorrespondenceFunction;
import org.vitrivr.cineast.core.data.score.ScoreElement;
import org.vitrivr.cineast.core.util.GroupingUtil;

public interface DistanceElement {
  String getId();

  double getDistance();

  ScoreElement toScore(CorrespondenceFunction f);

  static <T extends DistanceElement> List<ScoreElement> toScore(List<T> distances,
      CorrespondenceFunction f) {
    return distances.stream().map(e -> e.toScore(f)).collect(Collectors.toList());
  }

  static <T extends DistanceElement> T create(Class<T> clazz, String id, double distance) {
    if (clazz == SegmentDistanceElement.class) {
      return clazz.cast(new SegmentDistanceElement(id, distance));
    } else if (clazz == ObjectDistanceElement.class) {
      return clazz.cast(new ObjectDistanceElement(id, distance));
    } else {
      // This is a programming error
      String errorMessage = "Unknown subclass " + clazz.getSimpleName() + " of DistanceElement";
      LogManager.getLogger().error(errorMessage);
      throw new IllegalArgumentException(errorMessage);
    }
  }

  Comparator<DistanceElement> INVERSE_DISTANCE_COMPARATOR =
      Comparator.<DistanceElement>comparingDouble(e -> e.getDistance()).reversed();

  static List<DistanceElement> filterMinimumDistances(Stream<DistanceElement> elements) {
    return GroupingUtil.filterMaxByGroup(elements, e -> e.getId(), INVERSE_DISTANCE_COMPARATOR);
  }
}
