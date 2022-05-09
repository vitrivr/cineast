package org.vitrivr.cineast.api.messages.session;

import org.vitrivr.cineast.api.messages.credentials.Credentials;
import org.vitrivr.cineast.api.messages.interfaces.Message;
import org.vitrivr.cineast.api.messages.interfaces.MessageType;

/**
 * Message from the requester to transfer the access credentials of the current session.
 *
 * @param credentials Credentials of the current session from the user.
 */
public record StartSessionMessage(Credentials credentials, MessageType messageType) implements Message {

  public StartSessionMessage {
    if (messageType != MessageType.SESSION_START) {
      throw new IllegalStateException("MessageType was not SESSION_START, but " + messageType);
    }
  }

  public StartSessionMessage(Credentials credentials) {
    this(credentials, MessageType.SESSION_START);
  }

}
