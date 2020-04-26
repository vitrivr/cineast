package org.vitrivr.cineast.api.messages.lookup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.vitrivr.cineast.api.messages.interfaces.Message;
import org.vitrivr.cineast.api.messages.interfaces.MessageType;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class IdList implements Message {

  private List<String> ids;

  public IdList(String[] ids) {
    this.ids = Arrays.asList(ids);
  }

  @JsonCreator
  public IdList(@JsonProperty("ids")List<String> ids) {
    this.ids = new ArrayList<>(ids);
  }
  
  public String[] getIds(){
    return this.ids.toArray(new String[0]);
  }

  public List<String> getIdList(){ return this.ids;}
  
  @Override
  public MessageType getMessageType() {
    return null;
  }

}
