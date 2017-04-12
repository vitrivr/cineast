package org.vitrivr.cineast.core.features.neuralnet.classification.random;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.config.Config;
import org.vitrivr.cineast.core.config.NeuralNetConfig;
import org.vitrivr.cineast.core.config.ReadableQueryConfig;
import org.vitrivr.cineast.core.data.FloatVectorImpl;
import org.vitrivr.cineast.core.data.score.ScoreElement;
import org.vitrivr.cineast.core.data.segments.SegmentContainer;
import org.vitrivr.cineast.core.db.DBSelector;
import org.vitrivr.cineast.core.db.PersistencyWriter;
import org.vitrivr.cineast.core.db.PersistencyWriterSupplier;
import org.vitrivr.cineast.core.db.PersistentTuple;
import org.vitrivr.cineast.core.features.neuralnet.NeuralNetFeature;
import org.vitrivr.cineast.core.features.neuralnet.classification.NeuralNet;
import org.vitrivr.cineast.core.setup.EntityCreator;
import org.vitrivr.cineast.core.util.TimeHelper;

/**
 * RandomNet Feature Module
 * Created by silvan on 28.10.16.
 */
public class RandomNetFeature extends NeuralNetFeature {

    private static final Logger LOGGER = LogManager.getLogger();

    private static final String fullVectorTableName = "features_neuralnet_random_fullvector";
    private static final String generatedLabelsTableName = "features_neuralnet_vgg16_classifiedlabels";
    private RandomNet net;

    private DBSelector classificationSelector;
    private PersistencyWriter<?> classificationWriter;

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
        this(Config.sharedConfig().getNeuralnet());
    }

    public RandomNetFeature(NeuralNetConfig neuralNetConfig) {
        super(fullVectorTableName);
        this.net = new RandomNet(neuralNetConfig.getLabelPath());
    }

    @Override
    public void processShot(SegmentContainer shot) {
        LOGGER.entry();
        TimeHelper.tic();
        if (!phandler.idExists(shot.getId())) {
            float[] probabilities = net.classify(new BufferedImage(100,100,0));
            int maxIdx = -1;
            float max = -1;
            for (int i = 0; i < probabilities.length; i++) {
                if(max<probabilities[i]){
                    maxIdx = i;
                    max = probabilities[i];
                }
            }
            LOGGER.info("Best Match for shot {}: {} with probability {}", shot.getId(), String.join(", ", net.getLabels(net.getSynSetLabels()[maxIdx])), probabilities[maxIdx]);

            String id = UUID.randomUUID().toString();
            PersistentTuple tuple = classificationWriter.generateTuple(id, shot.getId(), net.getSynSetLabels()[maxIdx], probabilities[maxIdx]);
            classificationWriter.persist(tuple);

            persist(shot.getId(), new FloatVectorImpl(probabilities));
            LOGGER.trace("NeuralNetFeature.processShot() done in {}",
                    TimeHelper.toc());
        }
        LOGGER.exit();
    }


    @Override
    public List<ScoreElement> getSimilar(SegmentContainer sc, ReadableQueryConfig qc) {
        return getSimilar(sc, qc, classificationSelector, 0.1f);
    }

    @Override
    public void fillLabels(Map<String, String> options) {

    }

    @Override
    public void finish() {
        super.finish();
        if (this.classificationWriter != null) {
            this.classificationWriter.close();
            this.classificationWriter = null;
        }
        if (this.classificationSelector != null) {
            this.classificationSelector.close();
            this.classificationSelector = null;
        }
    }

    @Override
    protected NeuralNet getNet() {
        return net;
    }

    @Override
    public void initalizePersistentLayer(Supplier<EntityCreator> supply) {
        super.initalizePersistentLayer(supply);
        createLabelsTable(supply, generatedLabelsTableName);
    }

    @Override
    public void init(PersistencyWriterSupplier phandlerSupply) {
        super.init(phandlerSupply);
        classificationWriter = phandlerSupply.get();
        classificationWriter.open(this.getClassificationTable());
        classificationWriter.setFieldNames("id", "segmentid", getWnLabelColName(), "probability");
    }

    @Override
    public String getClassificationTable() {
        return generatedLabelsTableName;
    }
}
