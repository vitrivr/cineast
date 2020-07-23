package org.vitrivr.cineast.standalone.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.vitrivr.cineast.core.config.IdConfig;

/**
 * @author rgasser
 * @version 1.0
 * @created 23.01.17
 */
public class InputConfig {
    private String path;
    private String name;
    private String relTo;
    private Integer depth = 1;

    private Integer skip = 0;
    private Integer limit = Integer.MAX_VALUE;
    private IdConfig id = new IdConfig();

    @JsonProperty
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    @JsonProperty(required = true)
    public String getPath() {
        return path;
    }
    public void setPath(String path) {
        this.path = path;
    }

    @JsonProperty(required = true)
    public String getRelTo() {
        return relTo;
    }
    public void setRelTo(String relTo) {
        this.relTo = relTo;
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
    public Integer getSkip() {
        return skip;
    }
    public void setSkip(Integer skip) {
        this.skip = skip;
    }

    @JsonProperty
    public Integer getLimit() {
        return limit;
    }
    public void setLimit(Integer limit) {
        this.limit = limit;
    }
}
