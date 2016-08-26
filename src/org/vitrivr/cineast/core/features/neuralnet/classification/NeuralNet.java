package org.vitrivr.cineast.core.features.neuralnet.classification;

import java.awt.image.BufferedImage;

/**
 * Interface for abstracting Neural Nets
 * <p>
 * Maybe this needs a Top-1 method if some NN Implementation don't like giving the entire probability-array
 * Maybe this also needs some sort of abstraction for getting layers before the final layer such as prob-1 or prob-2
 * <p>
 * Created by silvan on 23.08.16.
 */
public interface NeuralNet {

    /**
     * Classify the given image to a vector of probabilities. No constraints are placed on the BufferedImage
     *
     * @return a vector of probabilities. Labels for indicies should be provided with getLabels()
     */
    float[] classify(BufferedImage img);

    /**
     * Get human-readable labels for the final layer
     */
    String[] getLabels();

    /**
     * Get wordnet labels for the final layer
     */
    String[] getSynSetLabels();
}
