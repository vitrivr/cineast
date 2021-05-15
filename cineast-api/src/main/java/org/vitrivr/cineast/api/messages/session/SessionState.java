package org.vitrivr.cineast.api.messages.session;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.vitrivr.cineast.api.session.Session;
import org.vitrivr.cineast.api.session.SessionType;

public class SessionState {

  private String id;
  private long validUntil;
  private SessionType type;

  @JsonCreator
  public SessionState(@JsonProperty("id") String id, @JsonProperty("validUntil") long validUntil, @JsonProperty("type") SessionType type) {
    this.id = id;
    this.validUntil = validUntil;
    this.type = type;
  }

  public SessionState(Session session) {
    this(session.getSessionId(), session.getEndTimeStamp(), session.getSessionType());
  }

  public String getSessionId() {
    return this.id;
  }

  public long getValidUntil() {
    return this.validUntil;
  }

  public SessionType getType() {
    return this.type;
  }

  @Override
  public String toString() {
    return ReflectionToStringBuilder.toString(this, ToStringStyle.JSON_STYLE);
  }
}
