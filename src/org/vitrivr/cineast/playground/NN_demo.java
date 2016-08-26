package org.vitrivr.cineast.playground;

import org.vitrivr.cineast.core.features.neuralnet.classification.NeuralNet;
import org.vitrivr.cineast.core.features.neuralnet.classification.tf.TensorFlowNet;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;


/**
 * Short demo-class while we are in the process of integrating into the main- codebase
 * Created by silvan on 22.08.16.
 */
public class NN_demo {

    public static void main(String[] args) throws IOException {

        NeuralNet nn = TensorFlowNet.getCurrentImpl();
        float[] probs = nn.classify(ImageIO.read(new File("src/resources/cat.jpg")));
        String[] labels = nn.getLabels();
        for (int i = 0; i < probs.length; i++) {
            if (probs[i] > 0.05) {
                System.out.println("Probability for " + labels[i] + "=" + probs[i]);
            }
        }
    }
}

