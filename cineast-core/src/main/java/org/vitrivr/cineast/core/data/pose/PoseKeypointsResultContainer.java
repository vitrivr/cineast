package org.vitrivr.cineast.core.data.pose;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.vitrivr.cineast.core.data.ExistenceCheck;

public class PoseKeypointsResultContainer implements ExistenceCheck {
    final private String name;
    final private float[][] keypoints;

    @JsonCreator
    public PoseKeypointsResultContainer(@JsonProperty("name") String name, @JsonProperty("keypoints") float[][] keypoints) {
        this.name = name;
        this.keypoints = keypoints;
    }

    @JsonProperty
    public final String getName() {
        return this.name;
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
