package org.vitrivr.cineast.api.messages.session;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.vitrivr.cineast.api.messages.credentials.Credentials;
import org.vitrivr.cineast.api.messages.interfaces.Message;
import org.vitrivr.cineast.api.messages.interfaces.MessageType;

/**
 * Message from the requester to transfer the access credentials of the current session.
 */
public class StartSessionMessage implements Message {

  /**
   * {@link Credentials} of the session with the user.
   */
  private final Credentials credentials;

  /**
   * Constructor for the StartSessionMessage object.
   *
   * @param credentials Credentials of the current session from the user.
   */
  @JsonCreator
  public StartSessionMessage(@JsonProperty("credentials") Credentials credentials) {
    this.credentials = credentials;
  }

  public Credentials getCredentials() {
    return this.credentials;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public MessageType getMessageType() {
    return MessageType.SESSION_START;
  }

  @Override
  public String toString() {
    return ReflectionToStringBuilder.toString(this, ToStringStyle.JSON_STYLE);
  }
}
