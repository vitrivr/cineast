package org.vitrivr.cineast.core.features;

import georegression.struct.point.Point2D_F32;
import org.apache.commons.lang3.NotImplementedException;
import org.vitrivr.cineast.core.config.ReadableQueryConfig;
import org.vitrivr.cineast.core.data.Pair;
import org.vitrivr.cineast.core.data.Skeleton;
import org.vitrivr.cineast.core.data.score.ScoreElement;
import org.vitrivr.cineast.core.data.segments.SegmentContainer;
import org.vitrivr.cineast.core.db.PersistencyWriterSupplier;
import org.vitrivr.cineast.core.db.setup.AttributeDefinition;
import org.vitrivr.cineast.core.db.setup.EntityCreator;
import org.vitrivr.cineast.core.features.abstracts.AbstractFeatureModule;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import static org.vitrivr.cineast.core.util.CineastConstants.GENERIC_ID_COLUMN_QUALIFIER;

public class SkeletonPose extends AbstractFeatureModule {

    public SkeletonPose() {
        super("feature_skeletonpose", 1, 20);
    }

    @Override
    public void init(PersistencyWriterSupplier phandlerSupply, int batchSize) {
        super.init(phandlerSupply, batchSize);
        this.phandler.setFieldNames(GENERIC_ID_COLUMN_QUALIFIER, "person", "skeleton", "weights");
    }

    @Override
    public void initalizePersistentLayer(Supplier<EntityCreator> supply) {
        supply.get().createFeatureEntity(this.tableName, false,
                new AttributeDefinition(GENERIC_ID_COLUMN_QUALIFIER, AttributeDefinition.AttributeType.STRING),
                new AttributeDefinition("person", AttributeDefinition.AttributeType.INT),
                new AttributeDefinition("skeleton", AttributeDefinition.AttributeType.VECTOR, 20),
                new AttributeDefinition("weights", AttributeDefinition.AttributeType.VECTOR, 20)
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

        //TODO query

        return null;
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
