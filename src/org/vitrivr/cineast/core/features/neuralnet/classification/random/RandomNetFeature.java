package org.vitrivr.cineast.core.features.neuralnet.classification.random;

import org.vitrivr.cineast.core.config.Config;
import org.vitrivr.cineast.core.config.NeuralNetConfig;
import org.vitrivr.cineast.core.config.QueryConfig;
import org.vitrivr.cineast.core.data.SegmentContainer;
import org.vitrivr.cineast.core.data.StringDoublePair;
import org.vitrivr.cineast.core.features.neuralnet.NeuralNetFeature;

import java.util.List;
import java.util.Map;

/**
 * Created by silvan on 28.10.16.
 */
public class RandomNetFeature extends NeuralNetFeature {

    private static final String fullVectorTableName = "features_neuralnet_random_fullvector";
    private RandomNet net;
    /**
     * Needs to be public so the extraction runner has access with a config-object
     */
    @SuppressWarnings("unused")
    public RandomNetFeature(com.eclipsesource.json.JsonObject config) {
        this(NeuralNetConfig.parse(config));
    }

    /**
     * Also needs to be public since the retriever config needs access.
     * Passes the current NN-Config to the next constructor since we need a net to init and we get that net from the config
     */
    @SuppressWarnings("unused")
    public RandomNetFeature() {
        this(Config.getNeuralNetConfig());
    }

    public RandomNetFeature(NeuralNetConfig neuralNetConfig) {
        super(fullVectorTableName);
        this.net = new RandomNet(neuralNetConfig.getLabelPath());
    }

    @Override
    public void processShot(SegmentContainer shot) {

    }

    @Override
    public List<StringDoublePair> getSimilar(SegmentContainer sc, QueryConfig qc) {
        return null;
    }

    @Override
    public void fillLabels(Map<String, String> options) {

    }

    @Override
    public String getClassificationTable() {
        return null;
    }
}
