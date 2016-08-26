package org.vitrivr.cineast.core.features.neuralnet.classification.tf;

import org.vitrivr.cineast.core.config.Config;
import org.vitrivr.cineast.core.features.neuralnet.classification.NeuralNet;
import org.vitrivr.cineast.core.features.neuralnet.classification.NeuralNetFactory;

import java.awt.image.BufferedImage;

/**
 * TensorFlow-NN Abstraction Layer
 * <p>
 * Created by silvan on 23.08.16.
 */
public class TensorFlowNet implements NeuralNet, NeuralNetFactory {

    private TensorFlowModel model;

    public TensorFlowNet(TensorFlowModel model) {
        this.model = model;
    }

    @Override
    public float[] classify(BufferedImage img) {
        return model.classify(img);
    }

    @Override
    public String[] getLabels() {
        return model.getLabels();
    }

    @Override
    public String[] getSynSetLabels() {
        return model.getSynSetLabels();
    }

    public static TensorFlowNet getCurrentImpl() {
        return VGG16();
    }

    /**
     * Casts the Config-nn to a TF-Net
     */
    public static TensorFlowNet getConfigImpl() {
        return (TensorFlowNet) Config.getNeuralNetConfig().getNeuralNetFactory();
    }

    /**
     * The VGG16-Tensorflow Model
     */
    public static TensorFlowNet VGG16() {
        TensorFlowModel vgg = new VGG16Model();
        return new TensorFlowNet(vgg);
    }

    @Override
    public NeuralNet generate() {
        return getCurrentImpl();
    }
}
