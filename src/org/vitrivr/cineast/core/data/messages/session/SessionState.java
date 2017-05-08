package org.vitrivr.cineast.core.data.messages.session;

import org.vitrivr.cineast.api.session.Session;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class SessionState {

  private String id;
  private long validUntil;
  
  @JsonCreator
  public SessionState(@JsonProperty("id")String id, @JsonProperty("validUntil")long validUntil){
    this.id = id;
    this.validUntil = validUntil;
  }
  
  public SessionState(Session session){
    this(session.getSessionId(), session.getEndTimeStamp());
  }
  
  public String getSessionId(){
    return this.id;
  }
  
  public long getValidUntil(){
    return this.validUntil;
  }
  
}
