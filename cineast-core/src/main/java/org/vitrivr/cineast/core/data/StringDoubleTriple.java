package org.vitrivr.cineast.core.data;

/**
 * A {@link StringDoublePair} with an extra String field, the 'extra'.
 */
public class StringDoubleTriple extends StringDoublePair {

    private final String extra;

    public StringDoubleTriple(String k, double v, String extra) {
        super(k, v);
        this.extra = extra;
    }

    /**
     * Getter for the extra of this {@link StringDoubleTriple}
     * @return The extra of this {@link StringDoubleTriple}
     */
    public String getExtra() {
        return extra;
    }
}
