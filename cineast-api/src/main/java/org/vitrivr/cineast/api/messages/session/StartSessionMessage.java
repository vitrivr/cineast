package org.vitrivr.cineast.api.messages.session;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.vitrivr.cineast.api.messages.credentials.Credentials;
import org.vitrivr.cineast.api.messages.interfaces.Message;
import org.vitrivr.cineast.api.messages.interfaces.MessageType;

public class StartSessionMessage implements Message {

  private Credentials credentials;

  @JsonCreator
  public StartSessionMessage(@JsonProperty("credentials") Credentials credentials) {
    this.credentials = credentials;
  }

  public Credentials getCredentials() {
    return this.credentials;
  }

  @Override
  public MessageType getMessageType() {
    return MessageType.SESSION_START;
  }

  @Override
  public String toString() {
    return ReflectionToStringBuilder.toString(this, ToStringStyle.JSON_STYLE);
  }
}
