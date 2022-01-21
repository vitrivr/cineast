package org.vitrivr.cineast.core.data;

import georegression.struct.point.Point2D_F32;

import java.util.ArrayList;
import java.util.List;

public class Skeleton {

    public enum SkeletonPointName {
        NOSE,
        LEFT_EYE,
        RIGHT_EYE,
        LEFT_EAR,
        RIGHT_EAR,
        LEFT_SHOULDER,
        RIGHT_SHOULDER,
        LEFT_ELBOW,
        RIGHT_ELBOW,
        LEFT_WRIST,
        RIGHT_WRIST,
        LEFT_HIP,
        RIGHT_HIP,
        LEFT_KNEE,
        RIGHT_KNEE,
        LEFT_ANKLE,
        RIGHT_ANKLE
    }

    /*

    [15, 13], [13, 11], [16, 14], [14, 12], [11, 12], [5, 11], [6, 12], [5, 6], [5, 7],
    [6, 8], [7, 9], [8, 10], [1, 2], [0, 1], [0, 2], [1, 3], [2, 4],
    [0, 5], [0, 6]

     */

    private static final int POINT_COUNT = 17;

    private final Point2D_F32[] points = new Point2D_F32[POINT_COUNT];
    private final float[] weights = new float[POINT_COUNT];

    public Skeleton(float[] coordinates, float[] weights) {

        if (coordinates == null || coordinates.length < 2 * POINT_COUNT || weights == null || weights.length < POINT_COUNT) {
            throw new IllegalArgumentException();
        }

        for (int i = 0; i < POINT_COUNT; ++i) {
            this.weights[i] = weights[i];
            this.points[i] = new Point2D_F32(coordinates[2 * i], coordinates[2 * i + 1]);
        }

    }

    public List<Point2D_F32> getPoints() {
        ArrayList<Point2D_F32> _return = new ArrayList<>(POINT_COUNT);
        for (int i = 0; i < POINT_COUNT; ++i) {
            _return.add(this.points[i].copy());
        }
        return _return;
    }

    public Point2D_F32 getPoint(SkeletonPointName name) {
        return this.points[name.ordinal()].copy();
    }

    public float getWeight(SkeletonPointName name) {
        return this.weights[name.ordinal()];
    }


}
