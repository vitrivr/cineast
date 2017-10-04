package org.vitrivr.cineast.core.data.messages.result;

import java.util.List;

import org.vitrivr.cineast.core.data.StringDoublePair;
import org.vitrivr.cineast.core.data.messages.abstracts.AbstractQueryResultMessage;
import org.vitrivr.cineast.core.data.messages.interfaces.MessageType;

import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * @author rgasser
 * @version 1.0
 * @created 11.01.17
 */
public class SimilarityQueryResult extends AbstractQueryResultMessage<StringDoublePair> {
    private String category;

    /**
     *
     * @param category
     * @param content
     */
    @JsonCreator
    public SimilarityQueryResult(String queryId, String category, List<StringDoublePair> content) {
        super(queryId, content);
        this.category = category;
    }

    /**
     *
     * @return
     */
    public String getCategory() {
        return category;
    }

    /**
     *
     * @return
     */
    @Override
    public MessageType getMessageType() {
        return MessageType.QR_SIMILARITY;
    }
}
