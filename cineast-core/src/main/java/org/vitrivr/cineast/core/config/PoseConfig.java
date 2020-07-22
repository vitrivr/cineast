package org.vitrivr.cineast.core.config;

import com.fasterxml.jackson.annotation.JsonProperty;

public final class PoseConfig {
    public static final class Resolution {
        public int x;
        public int y;
    }

    private String modelPath;
    private Resolution bodyNetResolution;
    private Resolution handNetResolution;

    @JsonProperty
    public String getModelPath() {
        return modelPath;
    }
    public void setModelPath(String modelPath) {
        this.modelPath = modelPath;
    }

    @JsonProperty
    public Resolution getBodyNetResolution() {
        return bodyNetResolution;
    }
    public void setBodyNetResolution(Resolution bodyNetResolution) {
        this.bodyNetResolution = bodyNetResolution;
    }

    @JsonProperty
    public Resolution getHandNetResolution() {
        return handNetResolution;
    }
    public void setHandNetResolution(Resolution handNetResolution) {
        this.handNetResolution = handNetResolution;
    }
}
