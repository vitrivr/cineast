package org.vitrivr.cineast.api.messages.query;


import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.vitrivr.cineast.core.config.QueryConfig;
import org.vitrivr.cineast.core.config.ReadableQueryConfig;

import com.fasterxml.jackson.annotation.JsonIgnore;

import org.vitrivr.cineast.api.messages.interfaces.Message;

/**
 * @author rgasser
 * @version 1.0
 * @created 11.01.17
 */
public abstract class Query implements Message {
    /** The {@link ReadableQueryConfig} that should be used to configure the query. May be null! */
    protected final QueryConfig config;

    /**
     * Constructor for {@link Query} message.
     *
     * @param config The {@link ReadableQueryConfig}
     */
    public Query(QueryConfig config) {
        this.config = config;
    }

    /**
     * Getter for {@link Query#config}.
     *
     * @return
     */
    @JsonIgnore //QueryConfig is handled by subclasses
    public final QueryConfig getQueryConfig() {
        return this.config;
    }

    @Override
    public String toString(){
        return ReflectionToStringBuilder.toString(this, ToStringStyle.MULTI_LINE_STYLE);
    }
}
