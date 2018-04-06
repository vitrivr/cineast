package org.vitrivr.cineast.core.data.messages.session;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.vitrivr.cineast.core.data.messages.interfaces.Message;
import org.vitrivr.cineast.core.data.messages.interfaces.MessageType;

/**
 * @author silvan on 22.01.18.
 */
public class UriList implements Message {

  private String[] uris;

  @JsonCreator
  public UriList(@JsonProperty("uris") String[] uris) {
    this.uris = uris;
  }

  public String[] getUris() {
    return this.uris;
  }

  @Override
  public MessageType getMessageType() {
    return null;
  }
}
