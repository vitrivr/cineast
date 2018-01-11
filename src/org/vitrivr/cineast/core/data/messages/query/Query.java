package org.vitrivr.cineast.core.data.messages.query;


import org.vitrivr.cineast.core.config.QueryConfig;
import org.vitrivr.cineast.core.config.ReadableQueryConfig;
import org.vitrivr.cineast.core.data.messages.interfaces.Message;

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
    public final QueryConfig getQueryConfig() {
        return this.config;
    }
}
