package org.vitrivr.cineast.api.messages.credentials;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * Credentials of an API session.
 *
 * @author lucaro
 * @created 08.05.17
 */
public class Credentials {

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

  @Override
  public String toString() {
    return ReflectionToStringBuilder.toString(this, ToStringStyle.JSON_STYLE);
  }
}
