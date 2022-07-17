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
 * @param id         Unique session id.
 * @param validUntil Time until session becomes invalid.
 * @param type       Type of the session used to determine access rights.
 */
public record SessionState(String id, long validUntil, SessionType type) {

  /**
   * Copy constructor for the SessionState object from a previously created Session.
   *
   * @param session Previously created session with the same properties as this session state.
   */
  public SessionState(Session session) {
    this(session.getSessionId(), session.getEndTimeStamp(), session.getSessionType());
  }

}
