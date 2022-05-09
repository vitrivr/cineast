package org.vitrivr.cineast.api.messages.query;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Contains the data of a particular {@link QueryTerm}.
 *
 * @param categories List of categories defined as part of this {@link QueryTerm}. This ultimately determines the features used for retrieval.
 * @param type       Denotes the type of {@link QueryTerm}.
 * @param data       String representation of the query object associated with this query term. Usually base 64 encoded.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record QueryTerm(@JsonProperty(required = true) List<String> categories, @JsonProperty(required = true) QueryTermType type, @JsonProperty(required = true) String data) {

}
