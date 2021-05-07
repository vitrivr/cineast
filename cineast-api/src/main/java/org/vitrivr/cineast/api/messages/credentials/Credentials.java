package org.vitrivr.cineast.api.messages.credentials;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Credentials of an API session.
 *
 * @author lucaro
 * @version 1.0
 * @created 08.05.17
 */
public class Credentials {

  /**
   * Username and password of an user in a session.
   *
   * <p> More options to come</p>
   */
  private String username;
  private String password;

  /**
   * Constructor for the Credentials object.
   *
   * @param username Username of the user credentials.
   * @param password Password of the user credentials.
   */
  @JsonCreator
  public Credentials(@JsonProperty("username") String username,
      @JsonProperty("password") String password) {
    this.username = username;
    this.password = password;
  }

  /**
   * Getter for username.
   *
   * @return String
   */
  public String getUsername() {
    return this.username;
  }

  /**
   * Getter for password.
   *
   * @return String
   */
  public String getPassword() {
    return this.password;
  }

  @Override
  public String toString() {
    return String.format("Credentials [username=%s, password=%s]", username, password);
  }

}
