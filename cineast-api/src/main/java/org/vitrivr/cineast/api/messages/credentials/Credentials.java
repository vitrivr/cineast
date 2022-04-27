package org.vitrivr.cineast.api.messages.credentials;

import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * Credentials of an API session.
 *
 * @param username Username of the user credentials.
 * @param password Password of the user credentials.
 */
public record Credentials(String username, String password) {
}
