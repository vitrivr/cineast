package org.vitrivr.cineast.core.data.providers;

/**
 * @author rgasser
 * @version 1.0
 * @created 11.01.17
 */
public interface IdProvider {
    /**
     * @return a unique id of this
     */
    String getId();

    String getSuperId();

    /**
     * @return a unique id of this
     */
    void setId(String id);

    /**
     *
     * @param id
     */
    void setSuperId(String id);
}
