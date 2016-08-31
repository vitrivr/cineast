package org.vitrivr.cineast.playground;

import org.vitrivr.cineast.core.config.Config;
import org.vitrivr.cineast.core.features.neuralnet.classification.NeuralNet;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.List;


/**
 * Short demo-class while we are in the process of integrating into the main- codebase
 * Created by silvan on 22.08.16.
 */
public class NN_demo {

    public static void main(String[] args) throws IOException {

        NeuralNet nn = Config.getNeuralNetConfig().getNeuralNetFactory().get();
        float[] probs = nn.classify(ImageIO.read(new File("src/resources/cat.jpg")));
        List<List<String>> labels = nn.getAllLabels();
        for (int i = 0; i < probs.length; i++) {
            if (probs[i] > 0.05) {
                //Wow, Java 8 added a built-in functionality for converting a list to a string.
                System.out.println("Probability for " + String.join(", ", labels.get(i)) + "=" + probs[i]);
            }
        }
    }
}

