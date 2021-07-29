package org.vitrivr.cineast.api.messages.result;

import com.fasterxml.jackson.annotation.JsonCreator;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.vitrivr.cineast.api.messages.interfaces.Message;
import org.vitrivr.cineast.api.messages.interfaces.MessageType;
import org.vitrivr.cineast.core.data.tag.Tag;

import java.util.List;

public class BooleanLookupResult implements Message {


        /**
         * The query ID to which this tags query result belongs.
         */
        public final String queryId;

        /**
         * List of tags that represent the result of the tags query.
         */
        public final int numberofElements;

        public final int componentID;
        /**
         * Constructor for the TagsQueryResult object.
         *
         * @param queryId String representing the ID of the query to which this part of the result message.
         * @param tags    List of Strings containing the tags that represent the result of the query.
         */
        @JsonCreator
        public BooleanLookupResult(String queryId, int numberofElements, int componentID) {
            this.queryId = queryId;
            this.numberofElements= numberofElements;
            this.componentID = componentID;
        }
        @Override
        public MessageType getMessageType() {
        return MessageType.QR_BOOL;
    }

        @Override
        public String toString() {
            return ReflectionToStringBuilder.toString(this, ToStringStyle.JSON_STYLE);
        }

}
