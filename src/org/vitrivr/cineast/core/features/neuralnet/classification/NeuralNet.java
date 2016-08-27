package org.vitrivr.cineast.core.features.neuralnet.classification;

import org.vitrivr.cineast.core.features.neuralnet.label.LabelProvider;

import java.awt.image.BufferedImage;

/**
 * Interface for abstracting Neural Nets
 * <p>
 * Maybe this needs a Top-1 method if some NN Implementation don't like giving the entire probability-array
 * Maybe this also needs some sort of abstraction for getting layers before the final layer such as prob-1 or prob-2
 * <p>
 * Created by silvan on 23.08.16.
 */
public interface NeuralNet extends LabelProvider {

    /**
     * Classify the given image to a vector of probabilities. No constraints are placed on the BufferedImage
     *
     * @return a vector of probabilities. Labels for indicies should be provided with getAllLabels()
     */
    float[] classify(BufferedImage img);
}
