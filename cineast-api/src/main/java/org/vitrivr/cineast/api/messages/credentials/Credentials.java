package org.vitrivr.cineast.api.messages.credentials;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Credentials of an API session.
 *
 * @param username Username of the user credentials.
 * @param password Password of the user credentials.
 */
public record Credentials(@JsonProperty(required = true) String username, @JsonProperty(required = true) String password) {

}
