package org.vitrivr.cineast.core.features.neuralnet.classification.tf;

import org.vitrivr.cineast.core.features.neuralnet.classification.NeuralNet;
import org.vitrivr.cineast.core.features.neuralnet.classification.NeuralNetFactory;

import java.awt.image.BufferedImage;
import java.util.List;

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
    public List<List<String>> getAllLabels() {
        return model.getAllLabels();
    }

    @Override
    public String[] getSynSetLabels() {
        return model.getSynSetLabels();
    }

    @Override
    public String[] getLabels(String i) {
        return new String[0];
    }

    public static TensorFlowNet getCurrentImpl() {
        return VGG16();
    }

    /**
     * The VGG16-Tensorflow Model
     */
    private static TensorFlowNet VGG16() {
        TensorFlowModel vgg = new VGG16Model();
        return new TensorFlowNet(vgg);
    }
    @Override
    public NeuralNet get() {
        return getCurrentImpl();
    }
}
