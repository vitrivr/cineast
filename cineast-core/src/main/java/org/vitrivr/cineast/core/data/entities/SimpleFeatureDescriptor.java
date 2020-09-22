package org.vitrivr.cineast.core.data.entities;

import org.vitrivr.cineast.core.data.ReadableFloatVector;

/**
 * @author rgasser
 * @version 1.0
 * @created 28.01.17
 */
public class SimpleFeatureDescriptor {
    /** */
    public final String segmentId;

    /** */
    public final ReadableFloatVector feature;

    /**
     *
     * @param segmentId
     * @param feature
     */
    public SimpleFeatureDescriptor(String segmentId, ReadableFloatVector feature) {
        this.segmentId = segmentId;
        this.feature = feature;
    }

    public String getSegmentId() {
        return segmentId;
    }
    public ReadableFloatVector getFeature() {
        return feature;
    }
}
