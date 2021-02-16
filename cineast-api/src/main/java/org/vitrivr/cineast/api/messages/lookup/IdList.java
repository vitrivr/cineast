package org.vitrivr.cineast.api.messages.lookup;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.vitrivr.cineast.api.messages.interfaces.Message;
import org.vitrivr.cineast.api.messages.interfaces.MessageType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class IdList{

  private List<String> ids;

  public IdList(String[] ids) {
    this.ids = Arrays.asList(ids);
  }

  @JsonCreator
  public IdList(@JsonProperty("ids") List<String> ids) {
    this.ids = new ArrayList<>(ids);
  }

  public String[] getIds() {
    return this.ids.toArray(new String[0]);
  }

  @JsonIgnore //prevents IDs being included in json a second time as "idList"
  public List<String> getIdList() {
    return this.ids;
  }

  @Override
  public String toString() {
    return "IdList{" +
        "ids=" + Arrays.toString(ids.toArray()) +
        '}';
  }
}
