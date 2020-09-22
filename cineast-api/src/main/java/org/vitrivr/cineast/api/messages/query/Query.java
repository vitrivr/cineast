package org.vitrivr.cineast.api.messages.query;


import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.vitrivr.cineast.core.config.QueryConfig;
import org.vitrivr.cineast.core.config.ReadableQueryConfig;

import com.fasterxml.jackson.annotation.JsonIgnore;

import org.vitrivr.cineast.api.messages.interfaces.Message;

public abstract class Query implements Message {
    /** The {@link ReadableQueryConfig} that should be used to configure the query. May be null! */
    protected final QueryConfig config;

    public Query(QueryConfig config) {
        this.config = config;
    }

    /**
     * JSON: the config field is specified in subclasses
     * The JsonIgnore is needed because the actual field is not called queryconfig
     */
    @JsonIgnore
    public final QueryConfig getQueryConfig() {
        return this.config;
    }

    @Override
    public String toString(){
        return ReflectionToStringBuilder.toString(this, ToStringStyle.MULTI_LINE_STYLE);
    }
}
