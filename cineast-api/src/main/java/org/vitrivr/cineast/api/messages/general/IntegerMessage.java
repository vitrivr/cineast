package org.vitrivr.cineast.api.messages.general;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.vitrivr.cineast.api.messages.abstracts.AbstractMessage;

public class IntegerMessage extends AbstractMessage {

  private final int value;

  @JsonCreator
  public IntegerMessage(@JsonProperty("value") int value) {
    this.value = value;
  }

  public int getValue() {
    return value;
  }

}
