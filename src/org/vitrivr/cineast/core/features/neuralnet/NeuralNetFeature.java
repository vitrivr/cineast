package org.vitrivr.cineast.core.features.neuralnet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.adam.grpc.AdamGrpc;
import org.vitrivr.cineast.core.config.Config;
import org.vitrivr.cineast.core.config.QueryConfig;
import org.vitrivr.cineast.core.data.FloatVectorImpl;
import org.vitrivr.cineast.core.data.SegmentContainer;
import org.vitrivr.cineast.core.data.StringDoublePair;
import org.vitrivr.cineast.core.db.DBSelector;
import org.vitrivr.cineast.core.db.PersistencyWriter;
import org.vitrivr.cineast.core.db.PersistentTuple;
import org.vitrivr.cineast.core.features.abstracts.AbstractFeatureModule;
import org.vitrivr.cineast.core.features.neuralnet.label.ConceptReader;
import org.vitrivr.cineast.core.features.neuralnet.classification.NeuralNet;
import org.vitrivr.cineast.core.setup.EntityCreator;
import org.vitrivr.cineast.core.util.TimeHelper;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Map;

/**
 * Mock-class for thinking
 */
public class NeuralNetFeature extends AbstractFeatureModule {

    private static final Logger LOGGER = LogManager.getLogger();

    private final NeuralNet net;
    private PersistencyWriter<?> classificationWriter;
    private PersistencyWriter<?> classWriter;
    private PersistencyWriter<?> conceptWriter;
    private DBSelector classificationSelector;
    private DBSelector classSelector;
    private DBSelector conceptSelector;
    private static final String fullVectorTableName = "features_neuralnet_fullvector";
    private static final String generatedLabelsTableName = "features_neuralnet_labels";
    private static final String classTableName = "features_neuralnet_classlabels";
    private static final String conceptTableName = "features_neuralnet_conceptmapping";

    //TODO What is maxDist here?
    protected NeuralNetFeature(float maxDist) {
        this(maxDist, Config.getNeuralNetConfig().getNeuralNetFactory().generate());
    }

    protected NeuralNetFeature(float maxDist, org.vitrivr.cineast.core.features.neuralnet.classification.NeuralNet net) {
        super(fullVectorTableName, maxDist);
        this.net = net;

        this.classificationSelector.open(generatedLabelsTableName);
        this.classSelector.open(classTableName);
        this.conceptSelector.open(conceptTableName);
    }

    /**
     * Create tables that aren't created by super.
     *
     * Currently Objectid is a string. That is because we get a unique id which has the shape n+....
     * <p>
     * Schema:
     * Table 0: shotid | classification - vector
     * Table 1: shotid | objectid | confidence (ex. 4014 | n203843 | 0.4)
     * Table 2: objectid | labels (ex. n203843 | banana, bananas ) (View Labels as binary storage since it's violating first normal form)
     * Table 3: objectid | concept (ex. n203843 | fruit)
     */
    private void createTables() {
        //create tables TODO Does this belong here
        //TODO Check if entity exists
        EntityCreator ec = new EntityCreator();
        //TODO Set pk / Create idx
        ec.createIdEntity(generatedLabelsTableName, new EntityCreator.AttributeDefinition("shotid", AdamGrpc.AttributeType.LONG), new EntityCreator.AttributeDefinition("objectid", AdamGrpc.AttributeType.STRING), new EntityCreator.AttributeDefinition("certainty", AdamGrpc.AttributeType.FLOAT));
        ec.createIdEntity(classTableName, new EntityCreator.AttributeDefinition("objectid", AdamGrpc.AttributeType.STRING), new EntityCreator.AttributeDefinition("labels", AdamGrpc.AttributeType.STRING));
        ec.createIdEntity(conceptTableName, new EntityCreator.AttributeDefinition("objectid", AdamGrpc.AttributeType.STRING), new EntityCreator.AttributeDefinition("concept", AdamGrpc.AttributeType.STRING));
    }

    /**
     * Creates entities and fills both class and concept-tables.
     *
     * TODO What is the behavior if init() has been called? Verify if entities exist?
     */
    public void setup() {
        createTables();
        init(Config.getDatabaseConfig().newWriter());
        ConceptReader cr = new ConceptReader(Config.getNeuralNetConfig().getConceptsPath());
        //Fill Concept map
        for(Map.Entry<String, String[]> entry : cr.getConceptMap().entrySet()) {
            for(String label : entry.getValue()){
                PersistentTuple tuple = conceptWriter.generateTuple(label, entry.getKey());
                conceptWriter.persist(tuple);
            }
        }
        //Fill class names
        for(int i = 0; i<net.getSynSetLabels().length;i++){
            PersistentTuple tuple = classWriter.generateTuple(net.getSynSetLabels()[i], net.getLabels()[i]);
            classWriter.persist(tuple);
        }
    }

    /**
     * The handler is passed right through to super. Then our own handlers are opened
     */
    public void init(PersistencyWriter<?> phandler) {
        super.init(phandler);
        classificationWriter = Config.getDatabaseConfig().newWriter();
        classificationWriter.open(generatedLabelsTableName);
        classWriter = Config.getDatabaseConfig().newWriter();
        classWriter.open(classTableName);
        conceptWriter = Config.getDatabaseConfig().newWriter();
        conceptWriter.open(conceptTableName);
    }

    /**
     * Classifies an Image with a neural net
     *
     * @return A float array containing the probabilities given by the neural net.
     */
    private float[] classifyImage(BufferedImage img) {
        return net.classify(img);
    }

    @Override
    public void processShot(SegmentContainer shot) {
        LOGGER.entry();
        TimeHelper.tic();
        if (!phandler.idExists(shot.getId())) {
            BufferedImage keyframe = shot.getMostRepresentativeFrame().getImage().getBufferedImage();
            float[] probs = classifyImage(keyframe);

            //Persist best matches
            for (int i = 0; i < probs.length; i++) {
                if (probs[i] > Config.getNeuralNetConfig().getCutoff()) {
                    PersistentTuple tuple = classificationWriter.generateTuple(shot.getId(), net.getSynSetLabels()[i], probs[i]);
                    classificationWriter.persist(tuple);
                }
            }
            persist(shot.getId(), new FloatVectorImpl(probs));
            LOGGER.debug("NeuralNetFeature.processShot() done in {}",
                    TimeHelper.toc());
        }
        LOGGER.exit();
    }

    /**
     * Checks if labels have been specified. If no labels have been specified, takes the queryimage.
     * Might perform knn on the 1k-vector in the future.
     * It's not clear yet how we calculate the score of an image
     * It's also not clear yet if we could combine labels and input image??
     */
    @Override
    public List<StringDoublePair> getSimilar(SegmentContainer sc, QueryConfig qc) {
        LOGGER.entry();
        TimeHelper.tic();
        List<StringDoublePair> _return = null;

        //TODO Check for label input and then decide what to do
        String[] labels = new String[1];    //Mocklabels

        if(!sc.getTags().isEmpty()){
            for(String label: sc.getTags()){
                LOGGER.debug("Looking for tag: "+label);
            }
        }else{
            //TODO Can we just take the most representative frame from the sc? Is that the query image?
            float[] res = classifyImage(sc.getMostRepresentativeFrame().getImage().getBufferedImage());

            for(int i = 0; i<res.length; i++){
                if(res[i]>Config.getNeuralNetConfig().getCutoff()){
                    //Matching! Wub wub
                    //TODO Compare to DB using classified labels -> BooleanQuerySelector in Ap-impl
                }
            }
        }

        //TODO How do we calculate score?
        LOGGER.debug("NeuralNetFeature.getSimilar() done in {}",
                TimeHelper.toc());
        //TODO Do we just return a StringDoublePair or do we return something else?
        return LOGGER.exit(_return);
    }
}
