package org.vitrivr.cineast.core.config;

import com.eclipsesource.json.JsonObject;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.vitrivr.cineast.core.features.neuralnet.classification.NeuralNet;
import org.vitrivr.cineast.core.features.neuralnet.classification.NeuralNetFactory;
import org.vitrivr.cineast.core.features.neuralnet.classification.tf.VGG16Net;

/**
 * Config for neural nets
 * neuralNetGenerator points to a Factory implementing classification.NeuralNetFactory
 * <p>
 * Created by silvan on 26.08.16.
 */
public class NeuralNetConfig {

    private static final String DEFAULT_MODEL_PATH = "resources/vgg16/vgg16.tfmodel";
    private static final float DEFAULT_CUTOFF = 0.2f;
    private static final String DEFAULT_LABEL_PATH = "resources/vgg16/synset.txt";
    private static final String DEFAULT_CONCEPT_PATH = "resources/classes.csv";
    private static final NeuralNetFactory DEFAULT_NEURAL_NET_FACTORY = () -> new VGG16Net(DEFAULT_MODEL_PATH, DEFAULT_LABEL_PATH);

    private String modelPath;
    private float cutoff;
    private NeuralNetFactory neuralNetFactory;
    private String labelPath;
    private String conceptsPath;


    @JsonCreator
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

    @JsonProperty
    public String getModelPath() {
        return modelPath;
    }
    public void setModelPath(String modelPath) {
        this.modelPath = modelPath;
    }

    @JsonProperty
    public float getCutoff() {
        return cutoff;
    }
    public void setCutoff(float cutoff) {
        this.cutoff = cutoff;
    }

    @JsonProperty
    public NeuralNetFactory getNeuralNetFactory() {
        return neuralNetFactory;
    }
    public void setNeuralNetFactory(NeuralNetFactory neuralNetFactory) {
        this.neuralNetFactory = neuralNetFactory;
    }

    @JsonProperty
    public String getLabelPath() {
        return labelPath;
    }
    public void setLabelPath(String labelPath) {
        this.labelPath = labelPath;
    }

    @JsonProperty
    public String getConceptPath(){return conceptsPath;}
    public void setConceptsPath(String conceptsPath) {
        this.conceptsPath = conceptsPath;
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
