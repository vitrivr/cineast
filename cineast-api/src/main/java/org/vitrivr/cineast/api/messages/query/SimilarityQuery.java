package org.vitrivr.cineast.api.messages.query;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import org.vitrivr.cineast.api.messages.interfaces.MessageType;
import org.vitrivr.cineast.api.messages.interfaces.Query;
import org.vitrivr.cineast.core.config.QueryConfig;
import org.vitrivr.cineast.core.db.dao.MetadataAccessSpecification;

/**
 * A {@link SimilarityQuery} contains a list of {@link QueryTerm}s. This object represents a similarity-query message, i.e. a request for a similarity-search.
 */
public record SimilarityQuery(@JsonProperty(required = true) List<QueryTerm> terms, List<MetadataAccessSpecification> metadataAccessSpec, QueryConfig config, MessageType messageType) implements Query {

}
