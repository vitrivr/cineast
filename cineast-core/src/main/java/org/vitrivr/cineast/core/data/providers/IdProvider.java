package org.vitrivr.cineast.core.data.providers;

/**
 * An {@link IdProvider} does two things: It can give its own ID, and the ID of the larger thing it belongs to.
 *
 * Therefore, implementing the {@link IdProvider} is a poor choice if a class can be part of multiple things.
 */
public interface IdProvider {
    /**
     * @return the identifier of this object
     */
    String getId();

    /**
     * @return the identifier of the larger group this object belongs to
     */
    String getSuperId();

    void setId(String id);

    void setSuperId(String id);
}
