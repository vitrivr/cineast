package org.vitrivr.cineast.core.features.neuralnet.classification.tf;

import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.geometry.Position;
import net.coobird.thumbnailator.geometry.Positions;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.adam.grpc.AdamGrpc;
import org.vitrivr.cineast.core.config.Config;
import org.vitrivr.cineast.core.config.NeuralNetConfig;
import org.vitrivr.cineast.core.config.QueryConfig;
import org.vitrivr.cineast.core.data.FloatVectorImpl;
import org.vitrivr.cineast.core.data.SegmentContainer;
import org.vitrivr.cineast.core.data.StringDoublePair;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;
import org.vitrivr.cineast.core.db.*;
import org.vitrivr.cineast.core.features.neuralnet.NeuralNetFeature;
import org.vitrivr.cineast.core.features.neuralnet.classification.NeuralNet;
import org.vitrivr.cineast.core.setup.EntityCreator;
import org.vitrivr.cineast.core.util.NeuralNetUtil;
import org.vitrivr.cineast.core.util.TimeHelper;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * VGG16-Feature
 *
 * Created by silvan on 09.09.16.
 */
public class NeuralNetVGG16Feature extends NeuralNetFeature {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final String fullVectorTableName = "features_neuralnet_vgg16_fullvector";
    private static final String generatedLabelsTableName = "features_neuralnet_vgg16_classifiedlabels";

    private VGG16Net net;
    private float cutoff = 0.2f;

    private DBSelector classificationSelector;
    private PersistencyWriter<?> classificationWriter;

    /**
     * Needs to be public so the extraction runner has access with a config-object
     */
    @SuppressWarnings("unused")
    public NeuralNetVGG16Feature(com.eclipsesource.json.JsonObject config){
        super(fullVectorTableName);
        NeuralNetConfig parsedConfig = NeuralNetConfig.parse(config);
        this.cutoff = parsedConfig.getCutoff();
        this.net = (VGG16Net) parsedConfig.getNeuralNetFactory().get();
    }

    /**
     * Also needs to be public since the retriever config needs access
     */
    @SuppressWarnings("unused")
    public NeuralNetVGG16Feature(){
        this(Config.getNeuralNetConfig());
    }

    public NeuralNetVGG16Feature(NeuralNetConfig neuralNetConfig) {
        super(fullVectorTableName);
        this.cutoff = neuralNetConfig.getCutoff();
        this.net = (VGG16Net) neuralNetConfig.getNeuralNetFactory().get();
    }

    @Override
    public void fillLabels(){
        LOGGER.debug("filling labels");
        List<PersistentTuple> tuples = new ArrayList<>(1000);
        for (int i = 0; i < net.getSynSetLabels().length; i++) {
            String[] labels = net.getLabels(net.getSynSetLabels()[i]);
            for (String label : labels) {
                PersistentTuple tuple = getClassWriter().generateTuple(UUID.randomUUID().toString(), net.getSynSetLabels()[i], label);
                tuples.add(tuple);
            }
        }
        getClassWriter().persist(tuples);
    }

    @Override
    public void processShot(SegmentContainer shot) {
        LOGGER.entry();
        TimeHelper.tic();
        if (!phandler.idExists(shot.getId())) {
            BufferedImage keyframe = shot.getMostRepresentativeFrame().getImage().getBufferedImage();
            float[] probabilities = classifyImage(keyframe);
            for (int i = 0; i < probabilities.length; i++) {
                if (probabilities[i] > 0.1) {
                    LOGGER.info("Match found for shot {}: {} with probability {}",shot.getId(), String.join(", ", net.getLabels(net.getSynSetLabels()[i])), probabilities[i]);
                    String id = UUID.randomUUID().toString();
                    PersistentTuple tuple = classificationWriter.generateTuple(id, shot.getId(), net.getSynSetLabels()[i], probabilities[i]);
                    classificationWriter.persist(tuple);
                }
            }
            persist(shot.getId(), new FloatVectorImpl(probabilities));
            LOGGER.debug("NeuralNetFeature.processShot() done in {}",
                    TimeHelper.toc());
        }
        LOGGER.exit();
    }

    /**
     * TODO How do we calculate score?
     * Checks if labels have been specified. If no labels have been specified, takes the queryimage.
     * Might perform knn on the 1k-vector in the future.
     * It's also not clear yet if we could combine labels and input image??
     */
    @Override
    public List<StringDoublePair> getSimilar(SegmentContainer sc, QueryConfig qc) {
        LOGGER.entry();
        TimeHelper.tic();
        List<StringDoublePair> _return = new ArrayList<>();

        LOGGER.debug("No tags found, classifying");
        NeuralNet _net = null;
        if(this.net!=null){
            _net = this.net;
        }
        if(qc.getNet().isPresent()){
            _net = qc.getNet().get();
        }
        if(_net == null){
            this.net = (VGG16Net) Config.getNeuralNetConfig().getNeuralNetFactory().get(); //cache NN
            _net = this.net;
        }

        if (!sc.getTags().isEmpty()) {
            Set<String> wnLabels = new HashSet<>();

            //TODO Use distinct
            for (String label : sc.getTags()) {
                LOGGER.debug("Looking for tag {}", label);
                wnLabels.addAll(getClassSelector().getRows("label", label).stream().map(row -> row.get("objectid").getString()).collect(Collectors.toList()));
            }
                for (Map<String, PrimitiveTypeProvider> row :
                        classificationSelector.getRows("objectid", wnLabels.toArray(new String[wnLabels.size()]))) {
                    LOGGER.debug("Found hit for query {}: {} {} ", row.get("shotid").getString(), row.get("probability").getDouble(), row.get("objectid").toString());
                    _return.add(new StringDoublePair(row.get("shotid").getString(), row.get("probability").getDouble()));
                }
        } else {
            //TODO Can we just take the most representative frame from the sc? Is that the query image?
            float[] res = _net.classify(sc.getMostRepresentativeFrame().getImage().getBufferedImage());
            List<String> hits = new ArrayList<>();
            for (int i = 0; i < res.length; i++) {
                if (res[i] > qc.getCutoff().orElse(cutoff)) {
                    hits.add(net.getSynSetLabels()[i]);
                }
            }
            for (Map<String, PrimitiveTypeProvider> row : classificationSelector.getRows("objectid", hits.toArray(new String[hits.size()]))) {
                //TODO Handle Duplicates -> Wait for Maxpool-updates
                //TODO How do we tell the user why we matched
                LOGGER.debug("Found hit for query {}: {} {} ", row.get("shotid").getString(), row.get("probability").getDouble(), row.get("objectid").toString());
                _return.add(new StringDoublePair(row.get("shotid").getString(), row.get("probability").getDouble()));
            }
        }
        if(_return.size()==0){
            _return = getSimilar(_net.classify(sc.getMostRepresentativeFrame().getImage().getBufferedImage()), qc);
        }
        LOGGER.info("NeuralNetFeature.getSimilar() done in {}",
                TimeHelper.toc());
        return LOGGER.exit(_return);
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
        if(img.getHeight()>img.getWidth()){
            positions[1] = Positions.TOP_RIGHT;
            positions[2] = Positions.BOTTOM_RIGHT;
        }else{
            positions[1] = Positions.CENTER_RIGHT;
            positions[2] = Positions.CENTER_LEFT;
        }

        float[] curr;
        for(Position pos: positions){
            try {
                curr = net.classify(Thumbnails.of(img).size(224, 224).crop(pos).asBufferedImage());
                probs = NeuralNetUtil.maxpool(curr, probs);
            } catch (IOException e) {
                LOGGER.error(e);
            }
        }
        return probs;
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
        classificationWriter.setFieldNames("id", "shotid", "objectid", "probability");
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
     * Table 1: shotid | objectid | confidence (ex. 4014 | n203843 | 0.4) - generated labels
     */
    @Override
    public void initalizePersistentLayer(Supplier<EntityCreator> supply) {
        super.initalizePersistentLayer(supply);
        EntityCreator ec = supply.get();
        //TODO Set pk / Create idx -> Logic in the ecCreator
        ec.createIdEntity(generatedLabelsTableName, new EntityCreator.AttributeDefinition("shotid", AdamGrpc.AttributeType.STRING), new EntityCreator.AttributeDefinition("objectid", AdamGrpc.AttributeType.STRING), new EntityCreator.AttributeDefinition("probability", AdamGrpc.AttributeType.FLOAT));
        ec.close();
    }
}
