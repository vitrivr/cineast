package org.vitrivr.cineast.core.render.lwjgl.util.datatype;

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
