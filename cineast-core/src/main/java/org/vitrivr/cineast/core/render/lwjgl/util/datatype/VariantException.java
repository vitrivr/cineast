package org.vitrivr.cineast.core.render.lwjgl.util.datatype;

/**
 * Exception thrown by the {@link Variant} class.
 * Used to indicate that a value could not be converted to the requested type.
 * Used to indicate that a key is not valid.
 */
public class VariantException extends RuntimeException {

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
