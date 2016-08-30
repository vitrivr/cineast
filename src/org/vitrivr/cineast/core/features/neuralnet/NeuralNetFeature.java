package org.vitrivr.cineast.core.features.neuralnet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.adam.grpc.AdamGrpc;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Please use setup as an entrypoint
 */
public class NeuralNetFeature extends AbstractFeatureModule {

    private static final Logger LOGGER = LogManager.getLogger();

    private NeuralNet net;
    private PersistencyWriter<?> classificationWriter;
    private PersistencyWriter<?> classWriter;
    private DBSelector classificationSelector;
    private DBSelector classSelector;
    private static final String fullVectorTableName = "features_neuralnet_fullvector";
    private static final String generatedLabelsTableName = "features_neuralnet_labels";
    private static final String classTableName = "features_neuralnet_classlabels";
    private float cutoff = 0.2f;

    //TODO What is maxDist here?

    /**
     * Careful: This constructor does not initalize the neural net
     */
    protected NeuralNetFeature(float maxDist) {
        super(fullVectorTableName, maxDist);
    }

    protected NeuralNetFeature(float maxDist, NeuralNetFactory factory){
        this(maxDist, factory.get());
    }

    private NeuralNetFeature(float maxDist, NeuralNet net) {
        super(fullVectorTableName, maxDist);
        this.net = net;
    }

    public void setCutoff(float cutoff){
        this.cutoff = cutoff;
    }

    public static String getClassTableName() {
        return classTableName;
    }

    @Override
    public void init(DBSelectorSupplier selectorSupplier){
        super.init(selectorSupplier);
        this.classificationSelector = selectorSupplier.get();
        this.classSelector = selectorSupplier.get();

        this.classificationSelector.open(generatedLabelsTableName);
        this.classSelector.open(classTableName);
    }


    /**
     * TODO This method needs heavy refactoring because creating entities this way is not really pretty, we're relying on the AdamGRPC
     *
     * Create tables that aren't created by super.
     *
     * Currently Objectid is a string. That is because we get a unique id which has the shape n+....
     * <p>
     * Schema:
     * Table 0: shotid | classification - vector - super
     * Table 1: shotid | objectid | confidence (ex. 4014 | n203843 | 0.4) - generated labels
     *
     * Table 2 is only touched for API-Calls about available labels and at init-time - not during extraction
     * Table 2: objectid | label or concept
     */
    @Override
    public void initalizePersistentLayer(Supplier<EntityCreator> supply) {
        EntityCreator ec = supply.get();
        //TODO Set pk / Create idx -> Logic in the ecCreator
        AdamGrpc.AttributeDefinitionMessage.Builder attrBuilder = AdamGrpc.AttributeDefinitionMessage.newBuilder();
        if(!ec.existsEntity(generatedLabelsTableName)){
            //TODO Shotid is a string here is that correct?
            ec.createIdEntity(generatedLabelsTableName, new EntityCreator.AttributeDefinition("shotid", AdamGrpc.AttributeType.STRING), new EntityCreator.AttributeDefinition("objectid", AdamGrpc.AttributeType.STRING), new EntityCreator.AttributeDefinition("probability", AdamGrpc.AttributeType.DOUBLE));
        }
        if(!ec.existsEntity(classTableName)){
            ec.createIdEntity(classTableName, new EntityCreator.AttributeDefinition("objectid", AdamGrpc.AttributeType.STRING), new EntityCreator.AttributeDefinition("label", AdamGrpc.AttributeType.STRING));
        }
        ec.close();
    }


    @Override
    public void init(PersistencyWriterSupplier phandlerSupply) {
        super.init(phandlerSupply);
        classificationWriter = phandlerSupply.get();
        classificationWriter.open(generatedLabelsTableName);
        classWriter = phandlerSupply.get();
        classWriter.open(classTableName);
    }

    @Override
    public void finish(){
        super.finish();
        if(this.classificationWriter!= null){
            this.classificationWriter.close();
            this.classificationWriter = null;
        }
        if(this.classWriter!=null){
            this.classWriter.close();
            this.classWriter = null;
        }
        if(this.classificationSelector!=null){
            this.classificationSelector.close();
            this.classificationSelector = null;
        }
        if(this.classSelector!=null){
            this.classSelector.close();
            this.classSelector = null;
        }
    }

    /**
     * This assumes that the required entities have been created
     */
    public void fillLabels(String conceptsPath) {
        ConceptReader cr = new ConceptReader(conceptsPath);

        //Fill Concept map
        for(Map.Entry<String, String[]> entry : cr.getConceptMap().entrySet()) {
            //values are n... -values being labeled as entry.getKey()
            for(String label : entry.getValue()){
                PersistentTuple tuple = classWriter.generateTuple(label, entry.getKey());
                classWriter.persist(tuple);
            }
        }

        //Fill class names
        for(int i = 0; i<net.getSynSetLabels().length;i++){
            String[] labels = net.getLabels(net.getSynSetLabels()[i]);
            for(String label: labels){
                PersistentTuple tuple = classWriter.generateTuple(net.getSynSetLabels()[i], label);
                classWriter.persist(tuple);
            }
        }
    }

    /**
     * Set neuralNet to specified net if you have called the default constructor
     */
    public void setNeuralNet(NeuralNet net){
        this.net = net;
    }

    /**
     * Classifies an Image with the given neural net
     * @return A float array containing the probabilities given by the neural net.
     */
    private float[] classifyImage(BufferedImage img) {
        return net.classify(img);
    }

    @Override
    public void processShot(SegmentContainer shot) {
        LOGGER.entry();
        TimeHelper.tic();
        //check if shot has been processed
        if (!phandler.idExists(shot.getId())) {
            BufferedImage keyframe = shot.getMostRepresentativeFrame().getImage().getBufferedImage();
            float[] probs = classifyImage(keyframe);

            //Persist best matches
            for (int i = 0; i < probs.length; i++) {
                if (probs[i] > cutoff) {
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
     *
     * TODO How do we calculate score?
     * Checks if labels have been specified. If no labels have been specified, takes the queryimage.
     * Might perform knn on the 1k-vector in the future.
     * It's also not clear yet if we could combine labels and input image??
     */
    @Override
    public List<StringDoublePair> getSimilar(SegmentContainer sc, QueryConfig qc) {
        LOGGER.entry();
        TimeHelper.tic();
        List<StringDoublePair> _return = new ArrayList();

        if(!sc.getTags().isEmpty()){
            List<String> wnLabels = new ArrayList();
            for(String label: sc.getTags()){
                LOGGER.debug("Looking for tag: "+label);
                wnLabels = new ArrayList();
                for(Map<String, PrimitiveTypeProvider> row : classSelector.getRows("label", label)){
                    //TODO is this the proper way to get info from the row?
                    wnLabels.add(row.get("label").getString());
                }
            }

            //TODO Eliminate Duplicates from wnLabels
            for(String wnLabel : wnLabels){
                for(Map<String, PrimitiveTypeProvider> row : classificationSelector.getRows("objectid", wnLabel)){
                    LOGGER.debug("Found hit for query: "+row.get("shotid").getString(), row.get("probability").getDouble(), row.get("objectid").toString());
                    _return.add(new StringDoublePair(row.get("shotid").getString(),row.get("probability").getDouble()));
                }
            }
        }else{
            //TODO Can we just take the most representative frame from the sc? Is that the query image?
            float[] res = classifyImage(sc.getMostRepresentativeFrame().getImage().getBufferedImage());

            for(int i = 0; i<res.length; i++){
                //TODO This cutoff should be in queryConfig probably
                if(res[i]>cutoff){
                    //Matching! Wub wub

                    for(Map<String, PrimitiveTypeProvider> row : classificationSelector.getRows("objectid", net.getSynSetLabels()[i])){
                        //TODO Duplicates?
                        LOGGER.debug("Found hit for query: "+row.get("shotid").getString(), row.get("probability").getDouble(), row.get("objectid").toString());
                        _return.add(new StringDoublePair(row.get("shotid").getString(),row.get("probability").getDouble()));
                    }
                }
            }
        }
        //TODO Currently returns mock-result until we get data in the DB
        _return = new ArrayList();
        _return.add(new StringDoublePair("125", 0.5));
        LOGGER.debug("NeuralNetFeature.getSimilar() done in {}",
                TimeHelper.toc());
        return LOGGER.exit(_return);
    }
}
