package org.vitrivr.cineast.api.messages.abstracts;

import org.vitrivr.cineast.api.messages.interfaces.QueryResultMessage;

import java.util.List;

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

    @Override
    public String toString() {
        return "AbstractQueryResultMessage{" +
                "content=" + content +
                ", queryId='" + queryId + '\'' +
                '}';
    }
}
