package org.vitrivr.cineast.core.data.messages.query;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.vitrivr.cineast.core.data.messages.interfaces.Message;
import org.vitrivr.cineast.core.data.messages.interfaces.MessageType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This object represents a MoreLikeThisQuery message, i.e. a request for a similarity-search.
 *
 * @author rgasser
 * @version 1.0
 * @created 27.04.17
 */
public class MoreLikeThisQuery implements Message {
    /** ID of the segment that serves as example for the MLT query. */
    private String segmentId;

    /** List of feature categories that should be considered by the MLT query. */
    private String[] categories;

    /**
     * Constructor for the SimilarityQuery object.
     *
     * @param segmentId SegmentId.
     * @param categories List of named feature categories.
     */
    @JsonCreator
    public MoreLikeThisQuery(@JsonProperty("segmentId") String segmentId, @JsonProperty("categories") String[] categories) {
        this.segmentId = segmentId;
        this.categories = categories;
    }

    /**
     * Getter for segmentId.
     *
     * @return
     */
    public String getSegmentId() {
        return segmentId;
    }

    /**
     * Getter for categories.
     *
     * @return
     */
    public List<String> getCategories() {
        if (this.categories != null) {
            return Arrays.asList(this.categories);
        } else {
            return new ArrayList<>(0);
        }
    }

    /**
     * Returns the type of particular message. Expressed as MessageTypes enum.
     *
     * @return
     */
    @Override
    public MessageType getMessagetype() {
        return MessageType.Q_MLT;
    }
}


