package org.vitrivr.cineast.api.messages.session;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
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
  private String id;

  /**
   * Unix time until this session becomes expired.
   */
  private long validUntil;

  /**
   * Type of the session to identify the access rights of the session user. Restricted to the {@link
   * SessionType} enum types.
   */
  private SessionType type;

  /**
   * Constructor for the SessionState object.
   *
   * @param id         Unique session id.
   * @param validUntil Time until session becomes invalid.
   * @param type       Type of the session used to determine access rights.
   */
  @JsonCreator
  public SessionState(@JsonProperty("id") String id, @JsonProperty("validUntil") long validUntil,
      @JsonProperty("type") SessionType type) {
    this.id = id;
    this.validUntil = validUntil;
    this.type = type;
  }

  /**
   * Constructor for the SessionState object from a previously created Session.
   *
   * @param session Previously created session with the same properties as this session state.
   */
  public SessionState(Session session) {
    this(session.getSessionId(), session.getEndTimeStamp(), session.getSessionType());
  }

  /**
   * Getter for session ID.
   *
   * @return String
   */
  public String getSessionId() {
    return this.id;
  }

  /**
   * Getter for validUntil.
   *
   * @return long
   */
  public long getValidUntil() {
    return this.validUntil;
  }

  /**
   * Getter for session type.
   *
   * @return {@link SessionType}
   */
  public SessionType getType() {
    return this.type;
  }

  @Override
  public String toString() {
    return "SessionState{" +
        "id='" + id + '\'' +
        ", validUntil=" + validUntil +
        ", type=" + type +
        '}';
  }
}
