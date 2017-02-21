package org.vitrivr.cineast.api.rest.exceptions;

/**
 * @author rgasser
 * @version 1.0
 * @created 11.01.17
 */
public class ActionHandlerException extends Exception {
    ActionHandlerException(String message) {
        super(message);
    }
    ActionHandlerException(String message, Throwable cause) {
        super(message, cause);
    }
}
