package org.vitrivr.cineast.api.messages.session;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.vitrivr.cineast.api.session.Session;
import org.vitrivr.cineast.api.session.SessionType;

/**
 * This object stores the session state of a session with a requester.
 *
 * @author lucaro
 * @created 08.05.17
 */
public class SessionState {

  /**
   * Unique ID of the session.
   */
  private final String id;

  /**
   * Unix time until this session becomes expired.
   */
  private final long validUntil;

  /**
   * Type of the session to identify the access rights of the session user. Restricted to the {@link SessionType} enum types.
   */
  private final SessionType type;

  /**
   * Constructor for the SessionState object.
   *
   * @param id         Unique session id.
   * @param validUntil Time until session becomes invalid.
   * @param type       Type of the session used to determine access rights.
   */
  @JsonCreator
  public SessionState(@JsonProperty("id") String id, @JsonProperty("validUntil") long validUntil, @JsonProperty("type") SessionType type) {
    this.id = id;
    this.validUntil = validUntil;
    this.type = type;
  }

  /**
   * Copy constructor for the SessionState object from a previously created Session.
   *
   * @param session Previously created session with the same properties as this session state.
   */
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
