package org.vitrivr.cineast.core.render.lwjgl.util.fsm.abstractworker;

/**
 * Exception thrown by the {@link StateProviderAnnotationParser} if an error occurs during parsing.
 */
@SuppressWarnings("unused")
public class StateProviderException extends RuntimeException  {

    public StateProviderException(String message) {
        super(message);
    }

    public StateProviderException(String message, Throwable cause) {
        super(message, cause);
    }

    public StateProviderException(Throwable cause) {
        super(cause);
    }
}
