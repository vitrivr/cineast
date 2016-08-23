package org.vitrivr.cineast.playground.classification.tf;

import java.awt.image.BufferedImage;

/**
 * Interface which TFModels should implement. Work in progress.
 * <p>
 * Created by silvan on 23.08.16.
 */
public interface TensorFlowModel {

    /**
     * Return prob-Vector
     */
    float[] classify(BufferedImage img);

    /**
     * Return layers
     */
    String[] getLabels();
}
