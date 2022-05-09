package org.vitrivr.cineast.api.messages.lookup;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record SelectSpecification(@JsonProperty(required = true) String table, @JsonProperty(required = true) List<String> columns, @JsonProperty(required = true) Integer limit) {

}
