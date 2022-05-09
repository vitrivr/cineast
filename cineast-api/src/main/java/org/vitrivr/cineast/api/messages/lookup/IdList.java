package org.vitrivr.cineast.api.messages.lookup;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Object to store a list of string IDs.
 */
public record IdList(@JsonProperty(required = true) List<String> ids) {

}
