package org.vitrivr.cineast.core.features.neuralnet.label;

import java.util.List;

/**
 * Simple Interface for Label providers
 * <p>
 * Created by silvan on 23.08.16.
 */
@Deprecated
public interface LabelProvider {

    /**
     * Get human-readable labels per index e.g. crocodile, animal, living thing for a crocodile
     */
    List<List<String>> getAllLabels();

    /**
     * Get wordnet labels for indices 0 to n
     */
    String[] getSynSetLabels();

    /**
     * Get all human-readable labels which are associated with the wordnet-label at index i
     */
    String[] getLabels(String i);
}

