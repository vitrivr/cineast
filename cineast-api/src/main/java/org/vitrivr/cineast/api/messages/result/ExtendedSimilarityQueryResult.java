package org.vitrivr.cineast.api.messages.result;

import com.fasterxml.jackson.annotation.JsonCreator;
import org.vitrivr.cineast.api.messages.abstracts.AbstractQueryResultMessage;
import org.vitrivr.cineast.api.messages.interfaces.MessageType;
import org.vitrivr.cineast.core.data.StringDoublePair;
import org.vitrivr.cineast.core.data.StringDoubleTriple;

import java.util.List;

/**
 * @deprecated Not needed anymore, as temporal scoring is handled differently
 */
@Deprecated
public class ExtendedSimilarityQueryResult extends AbstractQueryResultMessage<StringDoubleTriple> {
    private String category;

    /**
     * @param category
     * @param content
     */
    @JsonCreator
    public ExtendedSimilarityQueryResult(String queryId, String category, List<StringDoubleTriple> content) {
        super(queryId, content);
        this.category = category;
    }

    /**
     * @return
     */
    public String getCategory() {
        return category;
    }

    /**
     * @return
     */
    @Override
    public MessageType getMessageType() {
        return MessageType.QR_SIMILARITY;
    }

    @Override
    public String toString() {
        return "SimilarityQueryResult{" +
                "category='" + category + '\'' +
                '}';
    }
}