package org.vitrivr.cineast.core.data.query.containers;

import org.vitrivr.cineast.core.data.segments.SegmentContainer;

/**
 * @author rgasser
 * @version 1.0
 * @created 09.02.17
 */
public interface QueryContainer extends SegmentContainer {

    /**
     *
     * @return
     */
    float getWeight();

    /**
     *
     * @param weight
     */
    void setWeight(float weight);

    /**
     *
     * @return
     */
    default boolean hasId() {
        return this.getId() != null;
    }
}
