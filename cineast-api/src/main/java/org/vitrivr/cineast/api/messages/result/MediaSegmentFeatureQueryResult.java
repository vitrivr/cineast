package org.vitrivr.cineast.api.messages.result;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.List;

public class MediaSegmentFeatureQueryResult {

    public final String queryId;
    public final List<String> content;

    @JsonCreator
    public MediaSegmentFeatureQueryResult(String queryId, List<String> content) {
        this.queryId = queryId;
        this.content = content;
    }

}
