package org.vitrivr.cineast.core.render.lwjgl.util.fsm.abstractworker;

import java.io.Serial;

/**
 * Exception thrown by the {@link StateProviderAnnotationParser} if an error occurs during parsing.
 */
@SuppressWarnings("unused")
public class StateProviderException extends RuntimeException  {

    @Serial
    private static final long serialVersionUID = 2621424721657557641L;

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
