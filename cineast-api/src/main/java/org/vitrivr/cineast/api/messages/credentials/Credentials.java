package org.vitrivr.cineast.api.messages.credentials;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.vitrivr.cineast.api.messages.abstracts.AbstractMessage;
import org.vitrivr.cineast.api.messages.interfaces.MessageType;

/**
 * Credentials of an API session.
 */
public class Credentials extends AbstractMessage {

  /**
   * Username and password of an user in a session.
   *
   * <p> More options to come</p>
   */
  private final String username;
  private final String password;

  /**
   * Constructor for the Credentials object.
   *
   * @param username Username of the user credentials.
   * @param password Password of the user credentials.
   */
  @JsonCreator
  public Credentials(@JsonProperty("username") String username, @JsonProperty("password") String password) {
    this.username = username;
    this.password = password;
  }

  public String getUsername() {
    return this.username;
  }

  public String getPassword() {
    return this.password;
  }

}
