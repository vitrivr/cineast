package org.vitrivr.cineast.core.db.dao;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Use '*' in either domain or key to retrieve simply all information
 */
public class MetadataAccessSpecification {

    public final MetadataType type;
    public final String domain;
    public final String key;

    public MetadataAccessSpecification(
            @JsonProperty("type") MetadataType type,
            @JsonProperty("domain") String domain,
            @JsonProperty("key") String key) {
        this.type = type;
        this.domain = domain;
        this.key = key;
    }
}
