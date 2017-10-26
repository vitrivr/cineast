package org.vitrivr.cineast.core.data.messages.abstracts;

import java.util.List;

import org.vitrivr.cineast.core.data.messages.interfaces.QueryResultMessage;

/**
 * @author rgasser
 * @version 1.0
 * @created 12.01.17
 */
public abstract class AbstractQueryResultMessage<T> implements QueryResultMessage<T> {

    /**
     *
     */
    private List<T> content;

    /**
     *
     */
    private final String queryId;

    /**
     *
     * @param queryId
     * @param content
     */
    public AbstractQueryResultMessage(String queryId, List<T> content) {
        this.queryId = queryId;
        this.content = content;
    }

    /**
     *
     */
    @Override
    public String getQueryId() {
        return this.queryId;
    }

    /**
     * @return
     */
    @Override
    public List<T> getContent() {
        return this.content;
    }

    /**
     * @return
     */
    @Override
    public int count() {
        if (this.content != null) {
            return this.content.size();
        } else {
            return 0;
        }
    }
}
