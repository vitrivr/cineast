package org.vitrivr.cineast.core.features;

import georegression.struct.point.Point2D_F32;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.map.hash.TObjectDoubleHashMap;
import org.apache.commons.lang3.NotImplementedException;
import org.vitrivr.cineast.core.config.QueryConfig;
import org.vitrivr.cineast.core.config.ReadableQueryConfig;
import org.vitrivr.cineast.core.data.Pair;
import org.vitrivr.cineast.core.data.Skeleton;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;
import org.vitrivr.cineast.core.data.score.ScoreElement;
import org.vitrivr.cineast.core.data.score.SegmentScoreElement;
import org.vitrivr.cineast.core.data.segments.SegmentContainer;
import org.vitrivr.cineast.core.db.PersistencyWriterSupplier;
import org.vitrivr.cineast.core.db.setup.AttributeDefinition;
import org.vitrivr.cineast.core.db.setup.EntityCreator;
import org.vitrivr.cineast.core.features.abstracts.AbstractFeatureModule;
import org.vitrivr.cineast.core.util.HungarianAlgorithm;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static org.vitrivr.cineast.core.util.CineastConstants.DB_DISTANCE_VALUE_QUALIFIER;
import static org.vitrivr.cineast.core.util.CineastConstants.GENERIC_ID_COLUMN_QUALIFIER;

public class SkeletonPose extends AbstractFeatureModule {

    private static final String PERSON_ID_COL = "person";
    private static final String FEATURE_COL = "skeleton";
    private static final String WEIGHT_COL = "weights";

    public SkeletonPose() {
        super("feature_skeletonpose", 1, 8);
    }

    @Override
    public void init(PersistencyWriterSupplier phandlerSupply, int batchSize) {
        super.init(phandlerSupply, batchSize);
        this.phandler.setFieldNames(GENERIC_ID_COLUMN_QUALIFIER, PERSON_ID_COL, FEATURE_COL, WEIGHT_COL);
    }

    @Override
    public void initalizePersistentLayer(Supplier<EntityCreator> supply) {
        supply.get().createFeatureEntity(this.tableName, false,
                new AttributeDefinition(GENERIC_ID_COLUMN_QUALIFIER, AttributeDefinition.AttributeType.STRING),
                new AttributeDefinition(PERSON_ID_COL, AttributeDefinition.AttributeType.INT),
                new AttributeDefinition(FEATURE_COL, AttributeDefinition.AttributeType.VECTOR, this.vectorLength),
                new AttributeDefinition(WEIGHT_COL, AttributeDefinition.AttributeType.VECTOR, this.vectorLength)
                );
    }

    @Override
    public void processSegment(SegmentContainer segmentContainer) {
        throw new NotImplementedException("not currently available");
    }

    @Override
    public List<ScoreElement> getSimilar(SegmentContainer sc, ReadableQueryConfig qc) {

        List<Skeleton> skeletons = sc.getSkeletons();

        if (skeletons == null || skeletons.isEmpty()) {
            return Collections.emptyList();
        }

        HashMap<String, TObjectDoubleHashMap<Pair<Integer, Integer>>> segmentDistancesMap = new HashMap<>(qc.getRawResultsPerModule() * skeletons.size());

        int queryPersonId = 0;

        //query all skeletons
        for (Skeleton skeleton : skeletons) {

            Pair<float[], float[]> pair = getAnglesandWeights(skeleton);

            List<Map<String, PrimitiveTypeProvider>> rows = this.selector.getNearestNeighbourRows(qc.getRawResultsPerModule(), pair.first, FEATURE_COL, QueryConfig.clone(qc)
                    .setDistanceWeights(pair.second)
                    .setDistance(ReadableQueryConfig.Distance.manhattan));

            for (Map<String, PrimitiveTypeProvider> row : rows) {

                String segment = row.get(GENERIC_ID_COLUMN_QUALIFIER).getString();

                if (!segmentDistancesMap.containsKey(segment)) {
                    segmentDistancesMap.put(segment, new TObjectDoubleHashMap<>());
                }

                segmentDistancesMap.get(segment).put(new Pair<>(queryPersonId, row.get(PERSON_ID_COL).getInt()), row.get(DB_DISTANCE_VALUE_QUALIFIER).getDouble());

            }

            ++queryPersonId;

        }

        ArrayList<ScoreElement> results = new ArrayList<>(segmentDistancesMap.size());

        //compute assignment
        if (queryPersonId == 1) { //only one query skeleton
            for (String segment : segmentDistancesMap.keySet()) {
                TObjectDoubleHashMap<Pair<Integer, Integer>> distances = segmentDistancesMap.get(segment);
                double minDist = Arrays.stream(distances.values()).min().orElse(Double.MAX_VALUE);
                results.add(new SegmentScoreElement(segment, this.correspondence.applyAsDouble(minDist)));
            }
            results.sort(SegmentScoreElement.SCORE_COMPARATOR);
            return results.subList(0, Math.min(results.size(), qc.getRawResultsPerModule()) - 1);
        }


        //more than query skeleton
        for (String segment : segmentDistancesMap.keySet()) {

            TObjectDoubleHashMap<Pair<Integer, Integer>> distances = segmentDistancesMap.get(segment);

            if (distances.isEmpty()) {
                continue; //should never happen
            }

            Set<Integer> personIds = distances.keySet().stream().map(p -> p.second).collect(Collectors.toSet());

            if (personIds.size() == 1) { //only one retrieved skeleton
                double minDist = Arrays.stream(distances.values()).min().orElse(Double.MAX_VALUE);
                results.add(new SegmentScoreElement(segment, this.correspondence.applyAsDouble(minDist) / skeletons.size()));
                continue;
            }

            //more than one retrieved skeletons

            double[][] costs = new double[skeletons.size()][personIds.size()];
            TIntIntHashMap inversePersonIdMapping = new TIntIntHashMap(personIds.size());
            int i = 0;
            for (int personId : personIds){
                inversePersonIdMapping.put(personId, i++);
            }

            for (Pair<Integer, Integer> p : distances.keySet()) {
                costs[p.first][inversePersonIdMapping.get(p.second)] = -distances.get(p);
            }

            HungarianAlgorithm hungarianAlgorithm = new HungarianAlgorithm(costs);
            int[] assignment = hungarianAlgorithm.execute();

            double scoreSum = 0;

            for (i = 0; i < Math.min(personIds.size(), skeletons.size()); ++i) {
                scoreSum += this.correspondence.applyAsDouble(-costs[i][assignment[i]]);
            }

            results.add(new SegmentScoreElement(segment, scoreSum / skeletons.size()));

        }


        return results;
    }

    private Pair<float[], float[]> getAnglesandWeights(Skeleton skeleton) {
        float[] angles = new float[]{
                angle(skeleton.getPoint(Skeleton.SkeletonPointName.LEFT_ANKLE),skeleton.getPoint(Skeleton.SkeletonPointName.LEFT_KNEE), skeleton.getPoint(Skeleton.SkeletonPointName.LEFT_HIP)),
                angle(skeleton.getPoint(Skeleton.SkeletonPointName.LEFT_KNEE),skeleton.getPoint(Skeleton.SkeletonPointName.LEFT_HIP), skeleton.getPoint(Skeleton.SkeletonPointName.RIGHT_HIP)),
                angle(skeleton.getPoint(Skeleton.SkeletonPointName.LEFT_HIP),skeleton.getPoint(Skeleton.SkeletonPointName.RIGHT_HIP), skeleton.getPoint(Skeleton.SkeletonPointName.RIGHT_KNEE)),
                angle(skeleton.getPoint(Skeleton.SkeletonPointName.RIGHT_HIP),skeleton.getPoint(Skeleton.SkeletonPointName.RIGHT_KNEE), skeleton.getPoint(Skeleton.SkeletonPointName.RIGHT_ANKLE)),

                angle(skeleton.getPoint(Skeleton.SkeletonPointName.LEFT_WRIST),skeleton.getPoint(Skeleton.SkeletonPointName.LEFT_ELBOW), skeleton.getPoint(Skeleton.SkeletonPointName.LEFT_SHOULDER)),
                angle(skeleton.getPoint(Skeleton.SkeletonPointName.LEFT_ELBOW),skeleton.getPoint(Skeleton.SkeletonPointName.LEFT_SHOULDER), skeleton.getPoint(Skeleton.SkeletonPointName.RIGHT_SHOULDER)),
                angle(skeleton.getPoint(Skeleton.SkeletonPointName.LEFT_SHOULDER),skeleton.getPoint(Skeleton.SkeletonPointName.RIGHT_SHOULDER), skeleton.getPoint(Skeleton.SkeletonPointName.RIGHT_ELBOW)),
                angle(skeleton.getPoint(Skeleton.SkeletonPointName.RIGHT_SHOULDER),skeleton.getPoint(Skeleton.SkeletonPointName.RIGHT_ELBOW), skeleton.getPoint(Skeleton.SkeletonPointName.RIGHT_WRIST))
        };

        float[] weights = new float[]{
                min(skeleton.getWeight(Skeleton.SkeletonPointName.LEFT_ANKLE),skeleton.getWeight(Skeleton.SkeletonPointName.LEFT_KNEE), skeleton.getWeight(Skeleton.SkeletonPointName.LEFT_HIP)),
                min(skeleton.getWeight(Skeleton.SkeletonPointName.LEFT_KNEE),skeleton.getWeight(Skeleton.SkeletonPointName.LEFT_HIP), skeleton.getWeight(Skeleton.SkeletonPointName.RIGHT_HIP)),
                min(skeleton.getWeight(Skeleton.SkeletonPointName.LEFT_HIP),skeleton.getWeight(Skeleton.SkeletonPointName.RIGHT_HIP), skeleton.getWeight(Skeleton.SkeletonPointName.RIGHT_KNEE)),
                min(skeleton.getWeight(Skeleton.SkeletonPointName.RIGHT_HIP),skeleton.getWeight(Skeleton.SkeletonPointName.RIGHT_KNEE), skeleton.getWeight(Skeleton.SkeletonPointName.RIGHT_ANKLE)),

                min(skeleton.getWeight(Skeleton.SkeletonPointName.LEFT_WRIST),skeleton.getWeight(Skeleton.SkeletonPointName.LEFT_ELBOW), skeleton.getWeight(Skeleton.SkeletonPointName.LEFT_SHOULDER)),
                min(skeleton.getWeight(Skeleton.SkeletonPointName.LEFT_ELBOW),skeleton.getWeight(Skeleton.SkeletonPointName.LEFT_SHOULDER), skeleton.getWeight(Skeleton.SkeletonPointName.RIGHT_SHOULDER)),
                min(skeleton.getWeight(Skeleton.SkeletonPointName.LEFT_SHOULDER),skeleton.getWeight(Skeleton.SkeletonPointName.RIGHT_SHOULDER), skeleton.getWeight(Skeleton.SkeletonPointName.RIGHT_ELBOW)),
                min(skeleton.getWeight(Skeleton.SkeletonPointName.RIGHT_SHOULDER),skeleton.getWeight(Skeleton.SkeletonPointName.RIGHT_ELBOW), skeleton.getWeight(Skeleton.SkeletonPointName.RIGHT_WRIST)),
        };

        return new Pair<>(angles, weights);

    }

    private static float angle(Point2D_F32 p1, Point2D_F32 c, Point2D_F32 p2) {
        return (float) (Math.atan2(p2.y - c.y, p2.x - c.x) - Math.atan2(p1.y - c.y, p1.x - c.x));
    }

    private static float min(float f, float g, float h) {
        return Math.min(f, Math.min(g, h));
    }

}
