package org.vitrivr.cineast.core.render.lwjgl.util.datatype;

import java.io.Serial;

/**
 * Exception thrown by the {@link Variant} class.
 * Used to indicate that a value could not be converted to the requested type.
 * Used to indicate that a key is not valid.
 */
@SuppressWarnings("unused")
public class VariantException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 3713210701207037554L;

    public VariantException(String message) {
        super(message);
    }

    public VariantException(String message, Throwable cause) {
        super(message, cause);
    }

    public VariantException(Throwable cause) {
        super(cause);
    }
}
