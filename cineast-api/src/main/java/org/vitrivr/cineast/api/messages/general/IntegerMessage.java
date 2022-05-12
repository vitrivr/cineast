package org.vitrivr.cineast.api.messages.general;

import com.fasterxml.jackson.annotation.JsonProperty;

public record IntegerMessage(@JsonProperty(required = true) Integer value) {

}
