package org.vitrivr.cineast.core.features.neuralnet.label;

import java.util.List;

/**
 * Simple Interface for Label providers
 * <p>
 * TODO Maybe this needs a method for returning all labels
 * Created by silvan on 23.08.16.
 */
public interface LabelProvider {
    /**
     * Get human-readable labels per index
     */
    List<List<String>> getAllLabels();

    /**
     * Get wordnet labels from this label provider
     */
    String[] getSynSetLabels();

    /**
     * Get all human-readable labels this labelprovider associates with wordnet-index i
     */
    String[] getLabels(String i);
}

