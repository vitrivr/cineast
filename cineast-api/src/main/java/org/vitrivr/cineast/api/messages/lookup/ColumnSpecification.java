package org.vitrivr.cineast.api.messages.lookup;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Message from the requester specifying from which table and which column information should be fetched.
 */
public record ColumnSpecification(@JsonProperty(required = true) String column, @JsonProperty(required = true) String table) {

}
