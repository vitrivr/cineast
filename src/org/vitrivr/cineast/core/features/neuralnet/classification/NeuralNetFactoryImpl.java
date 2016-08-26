package org.vitrivr.cineast.core.features.neuralnet.classification;

import org.vitrivr.cineast.core.config.Config;
import org.vitrivr.cineast.core.features.neuralnet.classification.tf.TensorFlowNet;

/**
 * Factory for getting neural nets
 * <p>
 * Created by silvan on 24.08.16.
 */
public class NeuralNetFactoryImpl implements NeuralNetFactory {

    /**
     * Get current Implementation of a TF-Net
     */
    public static NeuralNet generateTensorflowNet() {
        return TensorFlowNet.getCurrentImpl();
    }

    /**
     * This method will always return a valid neural net.
     */
    public static NeuralNet generateConfigNet() {
        return Config.getNeuralNetConfig().getNeuralNetFactory().generate();
    }

    @Override
    public NeuralNet generate() {
        return generateConfigNet();
    }
}
