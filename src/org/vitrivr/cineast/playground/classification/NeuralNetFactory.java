package org.vitrivr.cineast.playground.classification;

import org.vitrivr.cineast.playground.classification.tf.TensorFlowNet;

/**
 * Factory for getting neural nets
 *
 * Created by silvan on 24.08.16.
 */
public class NeuralNetFactory {

    /**
     * TODO Maybe have a config-file? Or tensorflownet reads said config-file to get desired Impl
     */
    public static NeuralNet generateTensorflowNet() {
        return TensorFlowNet.getCurrentImpl();
    }

    /** This method will always return a valid neural net. */
    public static NeuralNet generateDefaultNet(){return generateTensorflowNet();}
}
