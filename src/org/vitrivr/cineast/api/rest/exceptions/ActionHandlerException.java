package org.vitrivr.cineast.api.rest.exceptions;

/**
 * @author rgasser
 * @version 1.0
 * @created 11.01.17
 */
public class ActionHandlerException extends Exception {

    private static final long serialVersionUID = -3925174394057916202L;

    ActionHandlerException(String message) {
        super(message);
    }

    ActionHandlerException(String message, Throwable cause) {
        super(message, cause);
    }
}
