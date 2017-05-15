package org.vitrivr.cineast.core.features.neuralnet.classification.tf;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;
import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.geometry.Position;
import net.coobird.thumbnailator.geometry.Positions;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.config.Config;
import org.vitrivr.cineast.core.config.NeuralNetConfig;
import org.vitrivr.cineast.core.config.ReadableQueryConfig;
import org.vitrivr.cineast.core.data.FloatVectorImpl;
import org.vitrivr.cineast.core.data.score.ScoreElement;
import org.vitrivr.cineast.core.data.segments.SegmentContainer;
import org.vitrivr.cineast.core.db.DBSelector;
import org.vitrivr.cineast.core.db.DBSelectorSupplier;
import org.vitrivr.cineast.core.db.PersistencyWriter;
import org.vitrivr.cineast.core.db.PersistencyWriterSupplier;
import org.vitrivr.cineast.core.db.PersistentTuple;
import org.vitrivr.cineast.core.features.neuralnet.NeuralNetFeature;
import org.vitrivr.cineast.core.features.neuralnet.classification.NeuralNet;
import org.vitrivr.cineast.core.features.neuralnet.classification.NeuralNetFactory;
import org.vitrivr.cineast.core.setup.EntityCreator;
import org.vitrivr.cineast.core.util.NeuralNetUtil;
import org.vitrivr.cineast.core.util.TimeHelper;

/**
 * VGG16-Feature module
 * <p>
 * Created by silvan on 09.09.16.
 */
public class NeuralNetVGG16Feature extends NeuralNetFeature {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final String fullVectorTableName = "features_neuralnet_vgg16_fullvector";
    private static final String generatedLabelsTableName = "features_neuralnet_vgg16_classifiedlabels";

    private VGG16Net cachedNet = null;
    private NeuralNetFactory factory;
    private float cutoff = 0.2f;

    private DBSelector classificationSelector;
    private PersistencyWriter<?> classificationWriter;

    /**
     * Needs to be public so the extraction runner has access with a config-object
     */
    public NeuralNetVGG16Feature(com.eclipsesource.json.JsonObject config) {
        this(NeuralNetConfig.parse(config));
    }

    /**
     * Also needs to be public since the retriever config needs access.
     * Passes the current NN-Config to the next constructor since we need a net to init and we get that net from the config
     */
    @SuppressWarnings("unused")
    public NeuralNetVGG16Feature() {
        this(Config.sharedConfig().getNeuralnet());
    }

    public NeuralNetVGG16Feature(NeuralNetConfig neuralNetConfig) {
        super(fullVectorTableName);
        this.cutoff = neuralNetConfig.getCutoff();
        this.factory = neuralNetConfig.getNeuralNetFactory();
    }

    /**
     * Does not call super.fillLabels() since we do not want that to happen for every NN-Feature
     */
    @Override
    public void fillLabels(Map<String, String> options) {
        LOGGER.debug("filling labels");
        List<PersistentTuple> tuples = new ArrayList<>(1000);
        for (int i = 0; i < getNet().getSynSetLabels().length; i++) {
            String[] labels = getNet().getLabels(getNet().getSynSetLabels()[i]);
            for (String label : labels) {
                PersistentTuple tuple = getClassWriter().generateTuple(UUID.randomUUID().toString(), getNet().getSynSetLabels()[i], label);
                tuples.add(tuple);
            }
        }
        getClassWriter().persist(tuples);
    }

    @Override
    public String getClassificationTable() {
        return generatedLabelsTableName;
    }

    /**
     * Stores best classification hit if it's above 10%
     */
    @Override
    public void processShot(SegmentContainer shot) {
        LOGGER.traceEntry();
        TimeHelper.tic();
        if (!phandler.idExists(shot.getId())) {
            BufferedImage keyframe = shot.getMostRepresentativeFrame().getImage().getBufferedImage();
            float[] probabilities = classifyImage(keyframe);
            int maxIdx = -1;
            float max = -1;
            for (int i = 0; i < probabilities.length; i++) {
                if(max<probabilities[i]){
                    maxIdx = i;
                    max = probabilities[i];
                }
            }
            LOGGER.info("Best Match for shot {}: {} with probability {}", shot.getId(), String.join(", ", getNet().getLabels(getNet().getSynSetLabels()[maxIdx])), probabilities[maxIdx]);
            if(probabilities[maxIdx]>0.1){
                LOGGER.info("Actually persisting result");
                String id = UUID.randomUUID().toString();
                persistTuple(classificationWriter.generateTuple(id, shot.getId(), getNet().getSynSetLabels()[maxIdx], probabilities[maxIdx]));
            }

            persist(shot.getId(), new FloatVectorImpl(probabilities));
            LOGGER.trace("NeuralNetFeature.processShot() done in {}",
                    TimeHelper.toc());
        }
        LOGGER.traceExit();
    }

    private void persistTuple(PersistentTuple tuple) {
        classificationWriter.persist(tuple);
    }

    /**
     * Checks if labels have been specified. If no labels have been specified, takes the query image.
     * Might perform knn on the 1k-vector in the future.
     * It's also not clear yet if we could combine labels and input image
     */
    @Override
    public List<ScoreElement> getSimilar(SegmentContainer sc, ReadableQueryConfig qc) {
        return getSimilar(sc, qc, classificationSelector, cutoff);
    }

    /**
     * Classifies an Image with the given neural net.
     * Performs 3 Classifications with different croppings, maxpools the vectors on each dimension to get hits
     */
    private float[] classifyImage(BufferedImage img) {
        float[] probs = new float[1000];
        Arrays.fill(probs, 0f);
        Position[] positions = new Position[3];
        positions[0] = Positions.CENTER;
        if (img.getHeight() > img.getWidth()) {
            positions[1] = Positions.TOP_RIGHT;
            positions[2] = Positions.BOTTOM_RIGHT;
        } else {
            positions[1] = Positions.CENTER_RIGHT;
            positions[2] = Positions.CENTER_LEFT;
        }

        float[] curr;
        for (Position pos : positions) {
            try {
                curr = getNet().classify(Thumbnails.of(img).size(224, 224).crop(pos).asBufferedImage());
                probs = NeuralNetUtil.maxpool(curr, probs);
            } catch (IOException e) {
                LOGGER.error(e);
            }
        }
        return probs;
    }

    @Override
    protected NeuralNet getNet() {
        if(cachedNet == null){
            cachedNet = (VGG16Net) factory.get();
        }
        return cachedNet;
    }

    @Override
    public void init(DBSelectorSupplier selectorSupplier) {
        super.init(selectorSupplier);
        this.classificationSelector = selectorSupplier.get();
        this.classificationSelector.open(generatedLabelsTableName);
    }

    @Override
    public void init(PersistencyWriterSupplier phandlerSupply) {
        super.init(phandlerSupply);
        classificationWriter = phandlerSupply.get();
        classificationWriter.open(generatedLabelsTableName);
        classificationWriter.setFieldNames("id", "segmentid", getWnLabelColName(), "probability");
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

    /**
     * Table 1: segmentid | wnLabel | confidence (ex. 4014 | n203843 | 0.4) - Stores specific hits from the Neuralnet
     */
    @Override
    public void initalizePersistentLayer(Supplier<EntityCreator> supply) {
        super.initalizePersistentLayer(supply);
        createLabelsTable(supply, generatedLabelsTableName);
    }
}
