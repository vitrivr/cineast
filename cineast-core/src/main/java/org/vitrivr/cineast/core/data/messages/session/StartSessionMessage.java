package org.vitrivr.cineast.core.data.messages.session;

import org.vitrivr.cineast.core.data.messages.credentials.Credentials;
import org.vitrivr.cineast.core.data.messages.interfaces.Message;
import org.vitrivr.cineast.core.data.messages.interfaces.MessageType;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class StartSessionMessage implements Message {

  private Credentials credentials;
  
  @JsonCreator
  public StartSessionMessage(@JsonProperty("credentials")Credentials credentials){
    this.credentials = credentials;
  }
  
  public Credentials getCredentials(){
    return this.credentials;
  }
  
  @Override
  public MessageType getMessageType() {
    return MessageType.SESSION_START;
  }

  @Override
  public String toString() {
    return String.format("StartSessionMessage [credentials=%s]", credentials);
  }

}
