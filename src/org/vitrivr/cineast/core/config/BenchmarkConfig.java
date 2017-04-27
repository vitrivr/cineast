package org.vitrivr.cineast.core.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.vitrivr.cineast.core.benchmark.model.BenchmarkMode;

/**
 * @author rgasser
 * @version 1.0
 * @created 24.04.17
 */
public class BenchmarkConfig {
    private BenchmarkMode mode = BenchmarkMode.OFF;

    private String path;

    @JsonCreator
    public BenchmarkConfig() {}

    @JsonProperty
    public BenchmarkMode getMode() {
        return mode;
    }
    public void setMode(BenchmarkMode mode) {
        this.mode = mode;
    }

    @JsonProperty
    public String getPath() {
        return this.path;
    }
    public void setPath(String path) {
        this.path = path;
    }
}
