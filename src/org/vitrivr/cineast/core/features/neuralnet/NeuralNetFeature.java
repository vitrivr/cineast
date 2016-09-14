package org.vitrivr.cineast.core.features.neuralnet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.adam.grpc.AdamGrpc;
import org.vitrivr.cineast.core.db.*;
import org.vitrivr.cineast.core.features.abstracts.AbstractFeatureModule;
import org.vitrivr.cineast.core.features.neuralnet.label.ConceptReader;
import org.vitrivr.cineast.core.setup.EntityCreator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * TODO Make this abstract and specify specific NN-Featuremodules with their own nets
 */
public abstract class NeuralNetFeature extends AbstractFeatureModule {

    private static final Logger LOGGER = LogManager.getLogger();

    private PersistencyWriter<?> classWriter;
    private DBSelector classSelector;
    private static final String classTableName =            "features_neuralnet_classlabels";
    private float cutoff = 0.2f;
    private static final String wnLabel = "wnlabel";
    private static final String humanLabelColName = "humanlabel";

    public NeuralNetFeature(String tableName){
        super(tableName, 1f);
    }

    public static String getClassTableName() {
        return classTableName;
    }

    @Override
    public void init(DBSelectorSupplier selectorSupplier) {
        super.init(selectorSupplier);
        this.classSelector = selectorSupplier.get();
        this.classSelector.open(classTableName);
    }


    /**
     * TODO This method needs heavy refactoring because creating entities this way is not really pretty, we're relying on the AdamGRPC
     * Currently wnLabel is a string. That is because we get a unique id which has the shape n+....
     * Schema:
     * Table 0: segmentid | classificationvector - done by super
     * Table 1: wnLabel | humanlabel
     * Table 1 is only touched for API-Calls about available labels and at init-time - not during extraction
     */
    @Override
    public void initalizePersistentLayer(Supplier<EntityCreator> supply) {
        super.initalizePersistentLayer(supply);
        EntityCreator ec = supply.get();
        //TODO Set pk / Create idx -> Logic in the ecCreator
        AdamGrpc.AttributeDefinitionMessage.Builder attrBuilder = AdamGrpc.AttributeDefinitionMessage.newBuilder();
        ec.createIdEntity(classTableName, new EntityCreator.AttributeDefinition(wnLabel, AdamGrpc.AttributeType.STRING), new EntityCreator.AttributeDefinition(getHumanLabelColName(), AdamGrpc.AttributeType.STRING));
        ec.close();
    }


    @Override
    public void init(PersistencyWriterSupplier phandlerSupply) {
        super.init(phandlerSupply);
        classWriter = phandlerSupply.get();
        classWriter.open(classTableName);
        classWriter.setFieldNames("id", wnLabel, getHumanLabelColName());
    }

    @Override
    public void finish() {
        super.finish();
        if (this.classWriter != null) {
            this.classWriter.close();
            this.classWriter = null;
        }
        if (this.classSelector != null) {
            this.classSelector.close();
            this.classSelector = null;
        }
    }

    /**
     * Fills concepts into the DB. Concepts are provided at conceptsPath and parsed by a reader
     * NeuralNets must fill their own labels into the DB
     */
    public void fillConcepts(String conceptsPath) {
        ConceptReader cr = new ConceptReader(conceptsPath);

        LOGGER.info("Filling Labels");
        //Fill Concept map with batch-inserting
        List<PersistentTuple> tuples = new ArrayList(10000);

        for (Map.Entry<String, String[]> entry : cr.getConceptMap().entrySet()) {
            for (String label : entry.getValue()) {
                String id = UUID.randomUUID().toString();
                PersistentTuple tuple = classWriter.generateTuple(id, label, entry.getKey());
                tuples.add(tuple);
                if (tuples.size()==9500) {
                    LOGGER.debug("Batch-inserting");
                    classWriter.persist(tuples);
                    tuples.clear();
                }
            }
        }
        classWriter.persist(tuples);
        tuples.clear();
    }

    public abstract void fillLabels();

    protected PersistencyWriter getClassWriter(){
        return this.classWriter;
    }

    protected  DBSelector getClassSelector(){
        return this.classSelector;
    }

    public String getWnLabelColName(){
        return wnLabel;
    }

    public static String getHumanLabelColName() {
        return humanLabelColName;
    }

    /**
     * @return The table name where classification data is stored
     */
    abstract public String getClassificationTable();
}
