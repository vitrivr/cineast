package org.vitrivr.cineast.api.messages.result;

import com.fasterxml.jackson.annotation.JsonCreator;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.vitrivr.cineast.core.data.StringDoublePair;
import org.vitrivr.cineast.api.messages.abstracts.AbstractQueryResultMessage;
import org.vitrivr.cineast.api.messages.interfaces.MessageType;

import java.util.List;

public class SimilarityQueryResult extends AbstractQueryResultMessage<StringDoublePair> {
    private String category;

    @JsonCreator
    public SimilarityQueryResult(String queryId, String category, List<StringDoublePair> content) {
        super(queryId, content);
        this.category = category;
    }

    public String getCategory() {
        return category;
    }

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

    public static class ContainerSimilarityQueryResult extends SimilarityQueryResult{

        private final String containerId;

        public ContainerSimilarityQueryResult(String queryId, String category, String containerId, List<StringDoublePair> content) {
            super(queryId, category, content);
            this.containerId = containerId;
        }

        public String getContainerId() {
            return containerId;
        }

        @Override
        public String toString() {
            return "ContainerSimilarityQueryResult{" +
                    "category='" + getCategory() + '\'' +
                    ", containerId='" + containerId + '\'' +
                    '}';
        }
    }
}
