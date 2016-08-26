package org.vitrivr.cineast.core.config;

import com.eclipsesource.json.JsonObject;
import org.vitrivr.cineast.playground.classification.NeuralNet;
import org.vitrivr.cineast.playground.classification.NeuralNetFactory;
import org.vitrivr.cineast.playground.classification.NeuralNetFactoryImpl;

/**
 * Config for neural nets
 * neuralNetGenerator points to a Factory implementing classification.NeuralNetFactory
 * <p>
 * Created by silvan on 26.08.16.
 */
public class NeuralNetConfig {

    private final String modelPath;
    private final float cutoff;
    private final NeuralNetFactory neuralNetFactory;
    private final String labelPath;
    private String conceptsPath;

    public static final String DEFAULT_MODEL_PATH = "src/resources/vgg16/vgg16.tfmodel";
    public static final float DEFAULT_CUTOFF = 0.2f;
    public static final String DEFAULT_LABEL_PATH = "";
    public static final String DEFAULT_CONCEPT_PATH = "src/resources/classes.csv";
    //Do this last. This might depend on defaults if poorly implemented
    public static final NeuralNetFactory DEFAULT_NEURAL_NET_FACTORY = new NeuralNetFactoryImpl();

    NeuralNetConfig() {
        this(DEFAULT_MODEL_PATH, DEFAULT_CUTOFF, DEFAULT_NEURAL_NET_FACTORY, DEFAULT_LABEL_PATH, DEFAULT_CONCEPT_PATH);
    }

    private NeuralNetConfig(String modelPath, float cutoff, NeuralNetFactory neuralNetFactory, String labelPath, String conceptsPath) {
        this.modelPath = modelPath;
        this.cutoff = cutoff;
        this.neuralNetFactory = neuralNetFactory;
        this.labelPath = labelPath;
        this.conceptsPath = conceptsPath;
    }

    public String getModelPath() {
        return modelPath;
    }

    public float getCutoff() {
        return cutoff;
    }

    public NeuralNetFactory getNeuralNetFactory() {
        return neuralNetFactory;
    }

    public String getLabelPath() {
        return labelPath;
    }

    public static NeuralNetConfig parse(JsonObject obj) {
        if (obj == null) {
            throw new NullPointerException("JsonObject was null");
        }

        String _modelPath = DEFAULT_MODEL_PATH;
        if (obj.get("modelPath") != null) {
            try {
                _modelPath = obj.get("modelPath").asString();
            } catch (UnsupportedOperationException e) {
                throw new IllegalArgumentException("'modelPath' was not an String in API configuration");
            }
        }

        float _cutoff = DEFAULT_CUTOFF;
        if (obj.get("cutoff") != null) {
            try {
                _cutoff = obj.get("cutoff").asFloat();
            } catch (UnsupportedOperationException e) {
                throw new IllegalArgumentException("'cutoff' was not a float in API configuration");
            }
        }

        NeuralNetFactory _factory = DEFAULT_NEURAL_NET_FACTORY;
        if (obj.get("neuralNetGenerator") != null) {
            try {
                _factory =
                        (NeuralNetFactory) NeuralNet.class
                                .getClassLoader()
                                .loadClass(obj.get("neuralNetGenerator").asString())
                                .newInstance();
            } catch (Exception e) {
                throw new IllegalArgumentException("Something happened in the Neural-net factory.", e);
            }
        }

        String _labelPath = DEFAULT_LABEL_PATH;
        if (obj.get("labelPath") != null) {
            try {
                _labelPath = obj.get("labelPath").asString();
            } catch (UnsupportedOperationException e) {
                throw new IllegalArgumentException("'labelPath' was not a String in API configuration");
            }
        }
        String _conceptsPath = DEFAULT_CONCEPT_PATH;
        if (obj.get("conceptPath") != null) {
            try {
                _conceptsPath = obj.get("conceptPath").asString();
            } catch (UnsupportedOperationException e) {
                throw new IllegalArgumentException("'conceptPath' was not a String in API configuration");
            }
        }

        return new NeuralNetConfig(_modelPath, _cutoff, _factory, _labelPath, _conceptsPath);

    }

    public String getConceptsPath() {
        return conceptsPath;
    }
}
