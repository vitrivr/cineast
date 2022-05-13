package org.vitrivr.cineast.api.messages.query;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import org.vitrivr.cineast.api.messages.interfaces.MessageType;
import org.vitrivr.cineast.api.messages.interfaces.Query;
import org.vitrivr.cineast.core.config.QueryConfig;
import org.vitrivr.cineast.core.db.dao.MetadataAccessSpecification;

/**
 * This object represents a MoreLikeThisQuery message, i.e. a request for a similarity-search.
 *
 * @param segmentId  of the segment that serves as basis for the MLT query.
 * @param categories List of feature categories that should be considered by the MLT query.
 */
public record MoreLikeThisQuery(@JsonProperty(required = true) String segmentId, @JsonProperty(required = true) List<String> categories, List<MetadataAccessSpecification> metadataAccessSpec, QueryConfig config, @JsonProperty(required = true) MessageType messageType) implements Query {

}


