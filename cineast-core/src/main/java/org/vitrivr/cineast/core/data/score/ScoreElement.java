package org.vitrivr.cineast.core.data.score;

import com.google.common.collect.ImmutableList;
import gnu.trove.map.TObjectDoubleMap;
import gnu.trove.map.hash.TObjectDoubleHashMap;
import org.vitrivr.cineast.core.data.CorrespondenceFunction;
import org.vitrivr.cineast.core.data.Pair;
import org.vitrivr.cineast.core.util.GroupingUtil;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

/**
 * Instances of this class denote a specific score for a particular document, i.e., a document id
 * paired together with a score. Note that implementations should stay immutable.
 */
public interface ScoreElement {
    String getId();

    double getScore();

    // Note: withScore returning the same type is not enforced through generics for sake of brevity and readability
    /**
     * @param newScore Score value for the duplicated {@link ScoreElement}
     * @return a new instance of the same type of {@link ScoreElement} based of {@code this} with the
     * given {@code newScore}.
     */
    ScoreElement withScore(double newScore);

    Comparator<ScoreElement> SCORE_COMPARATOR =
            Comparator.comparingDouble(e -> e.getScore());

    /**
     * Filters the given {@code elements} by keeping only the maximum score element of each id.
     *
     * @param elements elements to filter
     * @return list of the maximum score element of each id
     */
    static List<ScoreElement> filterMaximumScores(Stream<ScoreElement> elements) {
        return GroupingUtil.filterMaxByGroup(elements, e -> e.getId(), SCORE_COMPARATOR);
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

    /**
     * Merges the entries in a collection of ScoreElements into the provided TObjectDoubleHashMap where the ScoreElements ID serves as key
    * and its score serves as value. If an entry already exists, the value of that entry is adjusted. Every score-value is multiplied with a weight before being merged to the map.
     *
     * <b>TemporalScoring Hack</b> If the Collection contains elements of type SegmentScoreElement, then the key of the map is of format
     * <code>{@link SegmentScoreElement#getId()}${@link SegmentScoreElement#getQueryContainerId()}</<code> (i.e. id$qcid)
     *
     * @param collection The collection of ScoreElements to merge.
     * @param map The score-map to merge the elements into.
     * @param weight The weight that should be applied to each score.
     * @return TObjectDoubleHashMap
     */
    static <T extends ScoreElement> TObjectDoubleHashMap<String> mergeWithScoreMap(Collection<T> collection, TObjectDoubleHashMap<String> map, double weight) {
        for (T element : collection) {
            double score = element.getScore();
            if (Double.isInfinite(score) || Double.isNaN(score)) {
                continue;
            }
            double weightedScore = score * weight;
            map.adjustOrPutValue(element.getId(), weightedScore, weightedScore);
        }
        return map;
    }

    /**
     * Merges the entries in a collection of ScoreElements into the provided TObjectDoubleHashMap where the ScoreElements ID serves as key
     * and its score serves as value. If an entry already exists, the value of that entry is adjusted. Every score-value is multiplied with a weight before being merged to the map.
     *
     * <b>TemporalScoring Hack</b> If the Collection contains elements of type SegmentScoreElement, then the key of the map is of format
     * <code>{@link SegmentScoreElement#getId()}${@link SegmentScoreElement#getQueryContainerId()}</<code> (i.e. id$qcid)
     *
     * @param collection The collection of ScoreElements to merge.
     * @param map The score-map to merge the elements into.
     * @param weight The weight that should be applied to each score.
     * @return TObjectDoubleHashMap
     */
    static TObjectDoubleHashMap<Pair<String,String>> mergeWithSegmentScoreMap(Collection<SegmentScoreElement> collection, TObjectDoubleHashMap<Pair<String,String>> map, double weight) {
        for (SegmentScoreElement element : collection) {
            double score = element.getScore();
            if (Double.isInfinite(score) || Double.isNaN(score)) {
                continue;
            }
            double weightedScore = score * weight;
                map.adjustOrPutValue(new Pair<>(element.getId(), element.getQueryContainerId()), weightedScore, weightedScore);
        }
        return map;
    }
}
