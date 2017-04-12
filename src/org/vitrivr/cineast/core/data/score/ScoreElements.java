package org.vitrivr.cineast.core.data.score;

import com.google.common.collect.ImmutableList;
import gnu.trove.map.TObjectDoubleMap;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;
import org.vitrivr.cineast.core.data.CorrespondenceFunction;
import org.vitrivr.cineast.core.util.GroupingUtils;

public class ScoreElements {
  public static final Comparator<ScoreElement> SCORE_COMPARATOR =
      Comparator.comparingDouble(e -> e.getScore());

  private ScoreElements() {
  }

  /**
   * Filters the given {@code elements} by keeping only the maximum score element of each id.
   *
   * @param elements elements to filter
   * @return list of the maximum score element of each id
   */
  public static List<ScoreElement> filterMaximumScores(Stream<ScoreElement> elements) {
    return GroupingUtils.filterMaxByGroup(elements, e -> e.getId(), SCORE_COMPARATOR);
  }

  public static List<SegmentScoreElement> segmentsFromSegmentsMap(
      TObjectDoubleMap<String> scoreBySegmentId) {
    return segmentsFromSegmentsDistanceMap(scoreBySegmentId, CorrespondenceFunction.identity());
  }

  public static List<ScoreElement> scoresFromSegmentsDistanceMap(
      TObjectDoubleMap<String> distanceBySegmentId, CorrespondenceFunction correspondence) {
    List<SegmentScoreElement> segments = segmentsFromSegmentsDistanceMap(distanceBySegmentId,
        correspondence);

    // This should not make a copy because of the immutability of 'segments'
    return ImmutableList.copyOf(segments);
  }

  public static List<SegmentScoreElement> segmentsFromSegmentsDistanceMap(
      TObjectDoubleMap<String> distanceBySegmentId, CorrespondenceFunction correspondence) {
    ImmutableList.Builder<SegmentScoreElement> builder = ImmutableList.builder();
    distanceBySegmentId.forEachEntry((id, score) -> {
      builder.add(new SegmentScoreElement(id, correspondence.applyAsDouble(score)));
      return true;
    });
    return builder.build();
  }
}
