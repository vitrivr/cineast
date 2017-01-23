package org.vitrivr.cineast.core.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.nio.file.Path;

/**
 * @author rgasser
 * @version 1.0
 * @created 23.01.17
 */
public class InputConfig {
    private Path path;
    private String name;
    private Integer depth = 1;
    private Integer limit = Integer.MAX_VALUE;
    private IdConfig id = new IdConfig();

    @JsonProperty
    public Path getPath() {
        return path;
    }
    public void setPath(Path path) {
        this.path = path;
    }

    @JsonProperty
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    @JsonProperty
    public IdConfig getId() {
        return id;
    }
    public void setId(IdConfig id) {
        this.id = id;
    }

    @JsonProperty
    public Integer getDepth() {
        return depth;
    }
    public void setDepth(Integer depth) {
        this.depth = depth;
    }

    @JsonProperty
    public Integer getLimit() {
        return limit;
    }
    public void setLimit(Integer limit) {
        this.limit = limit;
    }
}