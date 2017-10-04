package org.vitrivr.cineast.core.features.neuralnet.classification.random;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Random;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.features.neuralnet.classification.NeuralNet;
import org.vitrivr.cineast.core.features.neuralnet.label.SynLabelProvider;

/**
 * Randomized NeuralNet
 * Created by silvan on 28.10.16.
 */
class RandomNet implements NeuralNet {

    private SynLabelProvider labelProvider;
    private static final Logger LOGGER = LogManager.getLogger();

    RandomNet(String labels){
        InputStream is = this.getClass().getResourceAsStream(labels);
        if (is == null) {
            try {
                is = Files.newInputStream(Paths.get(labels));
            } catch (IOException e) {
                //Intellij Fix
                try {
                    is = Files.newInputStream(Paths.get("src/", labels));
                } catch (IOException e1) {
                    throw new RuntimeException(e);
                }
            }
            if (is == null) {
                LOGGER.fatal("Could not load labels in RandomNet");
                throw new RuntimeException("Unable to load labels");
            }
        }
        labelProvider = new SynLabelProvider(is);
    }

    @Override
    public List<List<String>> getAllLabels() {
        return labelProvider.getAllLabels();
    }

    @Override
    public String[] getSynSetLabels() {
        return labelProvider.getSynSetLabels();
    }

    @Override
    public String[] getLabels(String i) {
        return labelProvider.getLabels(i);
    }

    @Override
    public float[] classify(BufferedImage img) {
        Random rng = new Random();
        float[] _return = new float[1000];
        float sum = 0;
        for(int i = 0; i < 1000;i++){
            _return[i] = rng.nextFloat();
            sum+=Math.pow(_return[i], 2);
        }

        float norm = (float) Math.sqrt(sum);
        for(int i = 0; i < 1000;i++){
            _return[i] = _return[i]/norm;
        }

        return _return;
    }
}
