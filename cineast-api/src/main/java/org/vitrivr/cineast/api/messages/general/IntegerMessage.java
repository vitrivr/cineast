package org.vitrivr.cineast.api.messages.general;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class IntegerMessage {

  private final int value;

  @JsonCreator
  public IntegerMessage(@JsonProperty("value") int value) {
    this.value = value;
  }

  public int getValue() {
    return value;
  }
}
