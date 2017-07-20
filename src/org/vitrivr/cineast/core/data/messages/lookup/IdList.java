package org.vitrivr.cineast.core.data.messages.lookup;

import org.vitrivr.cineast.core.data.messages.interfaces.Message;
import org.vitrivr.cineast.core.data.messages.interfaces.MessageType;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class IdList implements Message {

  private String[] ids;
  
  @JsonCreator
  public IdList(@JsonProperty("ids")String[] ids) {
    this.ids = ids;
  }
  
  public String[] getIds(){
    return this.ids;
  }
  
  @Override
  public MessageType getMessageType() {
    return null;
  }

}
