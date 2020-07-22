package org.vitrivr.cineast.core.data.pose;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.vitrivr.cineast.core.data.ExistenceCheck;

public class PoseKeypoints implements ExistenceCheck {
    final private float[][] keypoints;

    @JsonCreator
    public PoseKeypoints(@JsonProperty("keypoints") float[][] keypoints) {
        this.keypoints = keypoints;
    }

    @JsonProperty
    public final float[][] getKeypoints() {
        return this.keypoints;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.JSON_STYLE);
    }

    @Override
    public boolean exists() {
        return keypoints != null;
    }
}
