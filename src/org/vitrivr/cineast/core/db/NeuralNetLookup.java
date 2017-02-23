package org.vitrivr.cineast.core.db;

import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.config.Config;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;
import org.vitrivr.cineast.core.features.neuralnet.NeuralNetFeature;

/**
 * Created by silvan on 13.09.16.
 */
public class NeuralNetLookup {

    private static final Logger LOGGER = LogManager.getLogger();
    private final DBSelector selector;
    private final DBSelector classificationSelector;
    private NeuralNetFeature feature;
    private Map<String, String[]> labelMapping = new HashMap<>();

    public NeuralNetLookup(NeuralNetFeature feature) {
        this.selector = Config.sharedConfig().getDatabase().getSelectorSupplier().get();
        this.selector.open(NeuralNetFeature.getClassTableName());
        this.classificationSelector = Config.sharedConfig().getDatabase().getSelectorSupplier().get();
        this.classificationSelector.open(feature.getClassificationTable());
        this.feature = feature;
    }

    public void close() {
        this.selector.close();
        this.classificationSelector.close();
    }

    public NeuralNetDescriptor lookUpShot(String segmentId) {
        return this.lookUpShots(segmentId).get(segmentId);
    }

    public Map<String, NeuralNetDescriptor> lookUpShots(String... segmentIds) {
        for (Map<String, PrimitiveTypeProvider> map : classificationSelector.getRows("segmentid", segmentIds)) {
            //TODO
        }
        return null;
    }

    public Map<String, NeuralNetDescriptor> lookUpAllSegments(String mmoid) {
        //TODO get SegmentIDs
        String[] segmentIDs = null;
        return lookUpShots(segmentIDs);
    }

    //TODO This needs refactoring to make it dependent on the NN-Featuremodule used to classify
    //TODO Maybe we should perform for the wnLabels all at once in the methods.
    private NeuralNetDescriptor mapToDescriptor(Map<String, PrimitiveTypeProvider> map) {

        PrimitiveTypeProvider idProvider = map.get("segmentid");
        PrimitiveTypeProvider probabilityProvider = map.get("probability");
        PrimitiveTypeProvider wnLabelProvider = map.get("label");
        String[] humanLabels = null;
        if (!labelMapping.containsKey(wnLabelProvider.getString())) {
            selector.getRows("objectid",wnLabelProvider.getString());
        }
        return new NeuralNetDescriptor(idProvider.getString(), humanLabels, wnLabelProvider.getString(), probabilityProvider.getFloat());
    }


    public static class NeuralNetDescriptor{

        private final String segmentId;
        private final String[] humanLabels;
        private final String wnLabel;
        private final float probability;

        private NeuralNetDescriptor(String segmentId, String[] humanLabels, String wnLabel, float probabillity) {
            this.segmentId = segmentId;
            this.humanLabels = humanLabels;
            this.wnLabel = wnLabel;
            this.probability = probabillity;
        }
        public String[] getHumanLabels() {
            return humanLabels;
        }

        @Override
        public String toString() {
            return segmentId+" "+wnLabel+", "+probability+": "+String.join(", ", humanLabels);
        }

        public String getWnLabel() {
            return wnLabel;
        }

        public String getSegmentId() {
            return segmentId;
        }

        public float getProbability() {
            return probability;
        }
    }

    @Override
    protected void finalize() throws Throwable {
        close();
        super.finalize();
    }

}
