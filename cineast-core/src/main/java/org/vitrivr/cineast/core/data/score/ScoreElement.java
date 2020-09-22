package org.vitrivr.cineast.core.data.score;

import com.google.common.collect.ImmutableList;
import gnu.trove.map.TObjectDoubleMap;
import gnu.trove.map.hash.TObjectDoubleHashMap;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;
import org.vitrivr.cineast.core.data.CorrespondenceFunction;
import org.vitrivr.cineast.core.data.Pair;
import org.vitrivr.cineast.core.util.GroupingUtil;

/**
 * Instances of this class denote a specific score for a particular document, i.e., a document id paired together with a score. Note that implementations should stay immutable.
 */
public interface ScoreElement {

  String getId();

  double getScore();

  /**
   * @param newScore Score value for the duplicated {@link ScoreElement}
   * @return a new instance of the same type of {@link ScoreElement} based of {@code this} with the given {@code newScore}. Note: withScore returning the same type is not enforced through generics for sake of brevity and readability
   */
  ScoreElement withScore(double newScore);

  Comparator<ScoreElement> SCORE_COMPARATOR =
      Comparator.comparingDouble(ScoreElement::getScore);

  /**
   * Filters the given {@code elements} by keeping only the maximum score element of each id.
   *
   * @param elements elements to filter
   * @return list of the maximum score element of each id
   */
  static List<ScoreElement> filterMaximumScores(Stream<ScoreElement> elements) {
    return GroupingUtil.filterMaxByGroup(elements, ScoreElement::getId, SCORE_COMPARATOR);
  }

  static List<SegmentScoreElement> segmentsFromSegmentsMap(
      TObjectDoubleMap<String> scoreBySegmentId) {
    return segmentsFromSegmentsDistanceMap(scoreBySegmentId, CorrespondenceFunction.identity());
  }

  static List<ScoreElement> scoresFromSegmentsDistanceMap(
      TObjectDoubleMap<String> distanceBySegmentId, CorrespondenceFunction correspondence) {
    List<SegmentScoreElement> segments = segmentsFromSegmentsDistanceMap(distanceBySegmentId,
        correspondence);

    // This should not make a copy because of the immutability of 'segments'
    return ImmutableList.copyOf(segments);
  }

  static List<SegmentScoreElement> segmentsFromSegmentsDistanceMap(
      TObjectDoubleMap<String> distanceBySegmentId, CorrespondenceFunction correspondence) {
    ImmutableList.Builder<SegmentScoreElement> builder = ImmutableList.builder();
    distanceBySegmentId.forEachEntry((id, score) -> {
      builder.add(new SegmentScoreElement(id, correspondence.applyAsDouble(score)));
      return true;
    });
    return builder.build();
  }
}
