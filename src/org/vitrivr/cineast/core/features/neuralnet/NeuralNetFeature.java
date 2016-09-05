package org.vitrivr.cineast.core.features.neuralnet;

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
import org.vitrivr.cineast.core.features.abstracts.AbstractFeatureModule;
import org.vitrivr.cineast.core.features.neuralnet.classification.NeuralNet;
import org.vitrivr.cineast.core.features.neuralnet.classification.NeuralNetFactory;
import org.vitrivr.cineast.core.features.neuralnet.label.ConceptReader;
import org.vitrivr.cineast.core.setup.EntityCreator;
import org.vitrivr.cineast.core.util.TimeHelper;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.*;
import java.util.function.Supplier;

public class NeuralNetFeature extends AbstractFeatureModule {

    private static final Logger LOGGER = LogManager.getLogger();

    private NeuralNet net;
    private PersistencyWriter<?> classificationWriter;
    private PersistencyWriter<?> classWriter;
    private DBSelector classificationSelector;
    private DBSelector classSelector;
    private static final String fullVectorTableName =       "features_neuralnet_fullvector";
    private static final String generatedLabelsTableName =  "features_neuralnet_labels";
    private static final String classTableName =            "features_neuralnet_classlabels";
    private float cutoff = 0.2f;

    public NeuralNetFeature(NeuralNetFactory factory) {
        this(1f, factory);
    }

    /**
     * TODO At querytime, where do you set the neuralnet in a smooth way?
     */
    public NeuralNetFeature(){
        this(1f);
    }
    /**
     * TODO I think this is the proper way to handle compability with the extractionRunner
     * Needs to be public.
     * IMO This does not destroy the self-contained nature of the feature-modules.
     */
    public NeuralNetFeature(com.eclipsesource.json.JsonObject config){
        super(fullVectorTableName, 1f);
        NeuralNetConfig parsedConfig = NeuralNetConfig.parse(config);
        this.cutoff = parsedConfig.getCutoff();
        this.net = parsedConfig.getNeuralNetFactory().get();
    }
    protected NeuralNetFeature(float maxDist) {
        super(fullVectorTableName, maxDist);
    }

    protected NeuralNetFeature(float maxDist, NeuralNetFactory factory) {
        this(maxDist, factory.get());
    }

    private NeuralNetFeature(float maxDist, NeuralNet net) {
        super(fullVectorTableName, maxDist);
        this.net = net;
    }

    public void setCutoff(float cutoff) {
        this.cutoff = cutoff;
    }

    public static String getClassTableName() {
        return classTableName;
    }

    @Override
    public void init(DBSelectorSupplier selectorSupplier) {
        super.init(selectorSupplier);
        this.classificationSelector = selectorSupplier.get();
        this.classSelector = selectorSupplier.get();

        this.classificationSelector.open(generatedLabelsTableName);
        this.classSelector.open(classTableName);
    }


    /**
     * TODO This method needs heavy refactoring because creating entities this way is not really pretty, we're relying on the AdamGRPC
     * <p>
     * Create tables that aren't created by super.
     * <p>
     * Currently Objectid is a string. That is because we get a unique id which has the shape n+....
     * <p>
     * Schema:
     * Table 0: shotid | classification - vector - super
     * Table 1: shotid | objectid | confidence (ex. 4014 | n203843 | 0.4) - generated labels
     * <p>
     * Table 2 is only touched for API-Calls about available labels and at init-time - not during extraction
     * Table 2: objectid | label or concept
     */
    @Override
    public void initalizePersistentLayer(Supplier<EntityCreator> supply) {
        super.initalizePersistentLayer(supply);
        EntityCreator ec = supply.get();
        //TODO Set pk / Create idx -> Logic in the ecCreator
        AdamGrpc.AttributeDefinitionMessage.Builder attrBuilder = AdamGrpc.AttributeDefinitionMessage.newBuilder();
        //TODO Shotid is a string here is that correct?
        ec.createIdEntity(generatedLabelsTableName, new EntityCreator.AttributeDefinition("shotid", AdamGrpc.AttributeType.STRING), new EntityCreator.AttributeDefinition("objectid", AdamGrpc.AttributeType.STRING), new EntityCreator.AttributeDefinition("probability", AdamGrpc.AttributeType.DOUBLE));
        ec.createIdEntity(classTableName, new EntityCreator.AttributeDefinition("objectid", AdamGrpc.AttributeType.STRING), new EntityCreator.AttributeDefinition("label", AdamGrpc.AttributeType.STRING));
        ec.close();
    }


    @Override
    public void init(PersistencyWriterSupplier phandlerSupply) {
        super.init(phandlerSupply);
        classificationWriter = phandlerSupply.get();
        classificationWriter.open(generatedLabelsTableName);
        classificationWriter.setFieldNames("id", "shotid", "objectid", "probability");
        classWriter = phandlerSupply.get();
        classWriter.open(classTableName);
        classWriter.setFieldNames("id", "objectid", "label");
    }

    @Override
    public void finish() {
        super.finish();
        if (this.classificationWriter != null) {
            this.classificationWriter.close();
            this.classificationWriter = null;
        }
        if (this.classWriter != null) {
            this.classWriter.close();
            this.classWriter = null;
        }
        if (this.classificationSelector != null) {
            this.classificationSelector.close();
            this.classificationSelector = null;
        }
        if (this.classSelector != null) {
            this.classSelector.close();
            this.classSelector = null;
        }
    }

    /**
     * This assumes that the required entities have been created
     */
    public void fillLabels(String conceptsPath) {
        ConceptReader cr = new ConceptReader(conceptsPath);

        LOGGER.info("Filling Labels");
        int id = 0;
        //Fill Concept map
        List<PersistentTuple> tuples = new ArrayList(10000);

        for (Map.Entry<String, String[]> entry : cr.getConceptMap().entrySet()) {
            //values are n... -values being labeled as entry.getKey()
            for (String label : entry.getValue()) {
                //TODO Terrible idsolution
                PersistentTuple tuple = classWriter.generateTuple(String.valueOf(id), label, entry.getKey());
                //classWriter.persist(tuple);
                tuples.add(tuple);
                id++;
                if (id % 9500 == 0) {
                    LOGGER.info("Index {} key {}, inserting... ", id, entry.getKey());
                    classWriter.persist(tuples);
                    tuples.clear();
                }
            }
        }

        classWriter.persist(tuples);
        tuples.clear();

        LOGGER.info("done 1 {}", id);
        //Fill class names
        for (int i = 0; i < net.getSynSetLabels().length; i++) {
            String[] labels = net.getLabels(net.getSynSetLabels()[i]);
            for (String label : labels) {
                PersistentTuple tuple = classWriter.generateTuple(String.valueOf(id), net.getSynSetLabels()[i], label);
                tuples.add(tuple);
                id++;
            }
        }
        classWriter.persist(tuples);
        tuples.clear();

        LOGGER.info("done 2 {}", id);

        int idx = 0;
        for (PrimitiveTypeProvider typeProvider : classSelector.getAll("label")) {
            LOGGER.info("Retrieved label {}", typeProvider.getString());
            idx++;
            if (idx > 10) {
                System.exit(1);
            }
        }
    }

    /**
     * Set neuralNet to specified net if you have called the default constructor
     */
    public void setNeuralNet(NeuralNet net) {
        this.net = net;
    }

    /**
     * Classifies an Image with the given neural net
     *
     * @return A float array containing the probabilities given by the neural net.
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

        for(Position pos: positions){
            float[] curr = new float[0];
            try {
                curr = net.classify(Thumbnails.of(img).size(224, 224).crop(pos).asBufferedImage());
            } catch (IOException e) {
                LOGGER.error(e);
            }
            probs = maxpool(curr, probs);
        }
        return probs;
    }

    /**
     * Takes the maximum value at each position
     */
    private float[] maxpool(float[] curr, float[] probs) {
        if(curr.length!=probs.length){
            throw new IllegalArgumentException("Float[] need to have the same size");
        }
        float[] _ret = new float[curr.length];
        for(int i = 0; i<curr.length;i++){
            if(curr[i]>probs[i]){
                _ret[i]= curr[i];
            } else{
                _ret[i]=probs[i];
            }
        }
        return _ret;
    }

    @Override
    public void processShot(SegmentContainer shot) {
        LOGGER.entry();
        TimeHelper.tic();
        int id = 0;
        //check if shot has been processed
        if (!phandler.idExists(shot.getId())) {
            BufferedImage keyframe = shot.getMostRepresentativeFrame().getImage().getBufferedImage();

            float[] probs = classifyImage(keyframe);
            //Persist best matches
            for (int i = 0; i < probs.length; i++) {
                int idcounter = 0;
                if (probs[i] > 0.1) {
                    LOGGER.info("Match found for shot {}: {} with probability {}",shot.getId(), String.join(", ", net.getLabels(net.getSynSetLabels()[i])), probs[i]);
                    //TODO Ugly Fix for the ID-Problem
                    String classificationID = shot.getId()+"_"+idcounter++;
                    int finalI = i;
                    PersistentTuple gen = new PersistentTuple() {

                        double prob = probs[finalI];
                        String id = classificationID;
                        String shotid = shot.getId();
                        String objectid = net.getSynSetLabels()[finalI];
                        @Override
                        public Object getPersistentRepresentation() {
                            Map<String, AdamGrpc.DataMessage> values = new HashMap<>();
                            values.put("probability", AdamGrpc.DataMessage.newBuilder().setDoubleData(prob).build());
                            values.put("id", AdamGrpc.DataMessage.newBuilder().setStringData(id).build());
                            values.put("shotid", AdamGrpc.DataMessage.newBuilder().setStringData(shotid).build());
                            values.put("objectid", AdamGrpc.DataMessage.newBuilder().setStringData(objectid).build());

                            return AdamGrpc.InsertMessage.TupleInsertMessage.newBuilder().putAllData(values).build();
                        }
                    };
                    PersistentTuple tuple = classificationWriter.generateTuple(classificationID, shot.getId(), net.getSynSetLabels()[i], (double) probs[i]);
                    classificationWriter.persist(tuple);
                    classificationWriter.persist(gen);

                }
            }
            persist(shot.getId(), new FloatVectorImpl(probs));
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
        NeuralNet _net = null;
        if(this.net!=null){
            _net = this.net;
        }
        if(qc.getNet() != null){
            _net = qc.getNet();
        }
        if(_net == null){
            this.net = Config.getNeuralNetConfig().getNeuralNetFactory().get(); //cache NN
            _net = this.net;
        }
        List<StringDoublePair> _return = new ArrayList();

        if (!sc.getTags().isEmpty()) {
            List<String> wnLabels = new ArrayList();
            for (String label : sc.getTags()) {
                LOGGER.debug("Looking for tag {}", label);
                wnLabels = new ArrayList();
                for (Map<String, PrimitiveTypeProvider> row : classSelector.getRows("label", label)) {
                    wnLabels.add(row.get("objectid").getString());
                }
            }
            Set<String> setLabels = new HashSet<>();
            for(String label: wnLabels){
                setLabels.add(label);
            }

            for (String wnLabel : setLabels) {
                for (Map<String, PrimitiveTypeProvider> row : classificationSelector.getRows("objectid", wnLabel)) {
                    LOGGER.debug("Found hit for query {}: {} {} ", row.get("shotid").getString(), row.get("probability").getDouble(), row.get("objectid").toString());
                    _return.add(new StringDoublePair(row.get("shotid").getString(), row.get("probability").getDouble()));
                }
            }
        } else {
            //TODO Can we just take the most representative frame from the sc? Is that the query image?
            float[] res = _net.classify(sc.getMostRepresentativeFrame().getImage().getBufferedImage());

            for (int i = 0; i < res.length; i++) {
                //TODO This cutoff should be in queryConfig probably
                if (res[i] > cutoff) {
                    for (Map<String, PrimitiveTypeProvider> row : classificationSelector.getRows("objectid", net.getSynSetLabels()[i])) {
                        //TODO Duplicates?
                        LOGGER.debug("Found hit for query {}: {} {} ", row.get("shotid").getString(), row.get("probability").getDouble(), row.get("objectid").toString());
                        _return.add(new StringDoublePair(row.get("shotid").getString(), row.get("probability").getDouble()));
                    }
                }
            }
        }
        //TODO Currently returns mock-result until we get data in the DB
        _return = new ArrayList();
        _return.add(new StringDoublePair("720909", 0.5));
        LOGGER.info("NeuralNetFeature.getSimilar() done in {}",
                TimeHelper.toc());
        return LOGGER.exit(_return);
    }
}
