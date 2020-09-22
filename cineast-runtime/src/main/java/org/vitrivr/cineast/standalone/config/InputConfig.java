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

    @JsonProperty()
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
}
