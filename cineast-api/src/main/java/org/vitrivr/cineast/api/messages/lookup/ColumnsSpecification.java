package org.vitrivr.cineast.api.messages.lookup;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Message from the requester specifying from which table and which multiple columns information should be fetched.
 */
public record ColumnsSpecification(@JsonProperty(required = true) List<String> columns, @JsonProperty(required = true) String table) {

}
