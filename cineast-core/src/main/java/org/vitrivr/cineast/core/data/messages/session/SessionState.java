package org.vitrivr.cineast.core.data.messages.session;

import org.vitrivr.cineast.api.session.Session;
import org.vitrivr.cineast.api.session.SessionType;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class SessionState {

  private String id;
  private long validUntil;
  private SessionType type;
  
  @JsonCreator
  public SessionState(@JsonProperty("id")String id, @JsonProperty("validUntil")long validUntil, @JsonProperty("type")SessionType type){
    this.id = id;
    this.validUntil = validUntil;
    this.type = type;
  }
  
  public SessionState(Session session){
    this(session.getSessionId(), session.getEndTimeStamp(), session.getSessionType());
  }
  
  public String getSessionId(){
    return this.id;
  }
  
  public long getValidUntil(){
    return this.validUntil;
  }
  
  public SessionType getType(){
    return this.type;
  }
  
}
