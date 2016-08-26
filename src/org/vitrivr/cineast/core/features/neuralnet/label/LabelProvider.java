package org.vitrivr.cineast.core.features.neuralnet.label;

/**
 * Simple Interface for Label providers
 * <p>
 * TODO Maybe this needs a method for returning all labels
 * Created by silvan on 23.08.16.
 */
public interface LabelProvider {

    /**
     * Returns a human-readable label associated with the given Index
     *
     * @param index Should be within [0 ... #classes-1]
     */
    String getLabel(int index);
}

