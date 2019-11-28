package org.vitrivr.cineast.api.messages.lookup;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.vitrivr.cineast.api.messages.interfaces.Message;
import org.vitrivr.cineast.api.messages.interfaces.MessageType;

import java.util.Arrays;
import java.util.List;

public class IdList implements Message {

  private String[] ids;
  
  @JsonCreator
  public IdList(@JsonProperty("ids")String[] ids) {
    this.ids = ids;
  }
  
  public String[] getIds(){
    return this.ids;
  }

  public List<String> getIdList(){ return Arrays.asList(this.ids);}
  
  @Override
  public MessageType getMessageType() {
    return null;
  }

  @Override
  public String toString() {
    return "IdList{" +
            "ids=" + Arrays.toString(ids) +
            '}';
  }
}
