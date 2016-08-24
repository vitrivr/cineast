package org.vitrivr.cineast.playground.classification.tf;

import org.vitrivr.cineast.playground.classification.NeuralNet;

import java.awt.image.BufferedImage;
import java.io.File;

/**
 * TensorFlow-NN Abstraction Layer
 * <p>
 * Created by silvan on 23.08.16.
 */
public class TensorFlowNet implements NeuralNet {

    private TensorFlowModel model;

    /**
     * @param model The Model which the Tensorflow-NN will perform the classifcation on.
     */
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

    /**
     * @return Current Version of a TensorFlowNet
     */
    public static TensorFlowNet getCurrentImpl() {
        return VGG16();
    }

    /**
     * The VGG16-Tensorflow Model
     */
    public static TensorFlowNet VGG16() {
        //TODO Hardcoded values
        TensorFlowModel vgg = new VGG16Model("src/resources/vgg16/vgg16.tfmodel", new File("src/resources/vgg16/synset.txt"));
        return new TensorFlowNet(vgg);
    }
}
