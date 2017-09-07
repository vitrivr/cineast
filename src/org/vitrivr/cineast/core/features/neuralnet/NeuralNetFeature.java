package org.vitrivr.cineast.core.features.neuralnet;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.config.ReadableQueryConfig;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;
import org.vitrivr.cineast.core.data.score.ScoreElement;
import org.vitrivr.cineast.core.data.score.SegmentScoreElement;
import org.vitrivr.cineast.core.data.segments.SegmentContainer;
import org.vitrivr.cineast.core.db.DBSelector;
import org.vitrivr.cineast.core.db.DBSelectorSupplier;
import org.vitrivr.cineast.core.db.PersistencyWriter;
import org.vitrivr.cineast.core.db.PersistencyWriterSupplier;
import org.vitrivr.cineast.core.db.PersistentTuple;
import org.vitrivr.cineast.core.features.abstracts.AbstractFeatureModule;
import org.vitrivr.cineast.core.features.neuralnet.classification.NeuralNet;
import org.vitrivr.cineast.core.features.neuralnet.label.ConceptReader;
import org.vitrivr.cineast.core.setup.AttributeDefinition;
import org.vitrivr.cineast.core.setup.AttributeDefinition.AttributeType;
import org.vitrivr.cineast.core.setup.EntityCreator;
import org.vitrivr.cineast.core.util.TimeHelper;

/**
 * NeuralNet Feature modules should extend this class
 * It provides a table where neuralnets can access wordNet-labels and human labels
 */
@Deprecated
public abstract class NeuralNetFeature extends AbstractFeatureModule {

    private static final Logger LOGGER = LogManager.getLogger();

    private PersistencyWriter<?> classWriter;
    private DBSelector classSelector;

    /**
     * Table-name where the labels are stored
     */
    private static final String classTableName = "features_neuralnet_classlabels";
    /**
     * Column-name for the WordNet labels
     */
    private static final String wnLabel = "wnlabel";
    /**
     * Column-name for the human language label corresponding to a WordNet label
     */
    private static final String humanLabelColName = "humanlabel";

    /**
     * Just passes the tableName to super
     **/
    public NeuralNetFeature(String tableName) {
        super(tableName, 1f);
    }

    /**
     * Returns the Table-name where the labels are stored
     */
    public static String getClassTableName() {
        return classTableName;
    }

    /**
     * Checks if labels have been specified. If no labels have been specified, takes the query image.
     * Might perform knn on the 1k-vector in the future.
     * It's also not clear yet if we could combine labels and input image
     */
    public List<ScoreElement> getSimilar(SegmentContainer sc, ReadableQueryConfig qc, DBSelector classificationSelector, float defaultCutoff) {
        LOGGER.traceEntry();
        TimeHelper.tic();
        List<ScoreElement> _return = new ArrayList<>();
        if (!sc.getTags().isEmpty()) {
            Set<String> wnLabels = new HashSet<>();
            wnLabels.addAll(getClassSelector().getRows(getHumanLabelColName(), sc.getTags().toArray(new String[sc.getTags().size()])).stream().map(row -> row.get(getWnLabelColName()).getString()).collect(Collectors.toList()));

            LOGGER.debug("Looking for labels: {}", String.join(", ",wnLabels.toArray(new String[wnLabels.size()])));
            for (Map<String, PrimitiveTypeProvider> row :
                    classificationSelector.getRows(getWnLabelColName(), wnLabels.toArray(new String[wnLabels.size()]))) {
                String segmentId = row.get("segmentid").getString();
                float probability = row.get("probability").getFloat();
                LOGGER.debug("Found hit for query {}: {} {} ",
                    segmentId, probability, row.get(getWnLabelColName()).toString());
                _return.add(new SegmentScoreElement(segmentId, probability));
            }
        } else {
            LOGGER.debug("Starting Sketch-based lookup");
            NeuralNet _net = null;
//            if (qc.getNet().isPresent()) {
//                _net = qc.getNet().get();
//            }
            if (_net == null) {
                _net = getNet();
            }

            float[] classified = _net.classify(sc.getMostRepresentativeFrame().getImage().getBufferedImage());
            List<String> hits = new ArrayList<>();
            for (int i = 0; i < classified.length; i++) {
                if (classified[i] > /*qc.getCutoff().orElse(*/defaultCutoff/*)*/) {
                    hits.add(_net.getSynSetLabels()[i]);
                }
            }
            for (Map<String, PrimitiveTypeProvider> row : classificationSelector.getRows(getWnLabelColName(), hits.toArray(new String[hits.size()]))) {
                String segmentId = row.get("segmentid").getString();
                float probability = row.get("probability").getFloat();
                LOGGER.debug("Found hit for query {}: {} {} ",
                    segmentId, probability, row.get(getWnLabelColName()).toString());
                _return.add(new SegmentScoreElement(segmentId, probability));
            }
        }
        _return = ScoreElement.filterMaximumScores(_return.stream());
        LOGGER.trace("NeuralNetFeature.getSimilar() done in {}",
                TimeHelper.toc());
        return LOGGER.traceExit(_return);
    }

    protected abstract NeuralNet getNet();

    @Override
    public void init(DBSelectorSupplier selectorSupplier) {
        super.init(selectorSupplier);
        this.classSelector = selectorSupplier.get();
        this.classSelector.open(classTableName);
    }


    /**
     * TODO This method needs heavy refactoring along with the entitycode because creating entities this way is not really pretty, we're relying on the AdamGRPC
     * Currently wnLabel is a string. That is because we get a unique id which has the shape n+....
     * Schema:
     * Table 0: segmentid | classificationvector - handled by super
     * Table 1: wnLabel | humanlabel
     * Table 1 is only touched for API-Calls about available labels and at init-time - not during extraction
     * It is also used at querytime for the nn-features to determine the wnLabel associated with the concepts they should query for.
     */
    @Override
    public void initalizePersistentLayer(Supplier<EntityCreator> supply) {
        super.initalizePersistentLayer(supply);
        EntityCreator ec = supply.get();
        //TODO Set pk / Create idx -> Logic in the ecCreator
        ec.createIdEntity(classTableName, new AttributeDefinition(wnLabel, AttributeType.STRING), new AttributeDefinition(getHumanLabelColName(), AttributeType.STRING));
        ec.close();
    }

    /**
     * Table 1: segmentid | wnLabel | confidence (ex. 4014 | n203843 | 0.4) - Stores specific hits from the Neuralnet
     */
    public void createLabelsTable(Supplier<EntityCreator> supply, String tableName){
        EntityCreator ec = supply.get();
        //TODO Set pk / Create idx -> Logic in the ecCreator
        ec.createIdEntity(tableName, new AttributeDefinition("segmentid", AttributeType.STRING), new AttributeDefinition(getWnLabelColName(), AttributeType.STRING), new AttributeDefinition("probability", AttributeType.FLOAT));
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
     * Fills general concepts into the DB. Concepts are provided at conceptsPath and parsed by the ConceptReader
     * NeuralNets must fill their own labels into the DB. If their labels are not generally applicable, they should use their own table.
     */
    public void fillConcepts(String conceptsPath) {
        ConceptReader cr = new ConceptReader(conceptsPath);

        LOGGER.info("Filling Labels");
        //Fill Concept map with batch-inserting
        List<PersistentTuple> tuples = new ArrayList<>(10000);

        for (Map.Entry<String, String[]> entry : cr.getConceptMap().entrySet()) {
            for (String label : entry.getValue()) {
                String id = UUID.randomUUID().toString();
                PersistentTuple tuple = classWriter.generateTuple(id, label, entry.getKey());
                tuples.add(tuple);
                if (tuples.size() == 9500) {
                    LOGGER.debug("Batch-inserting");
                    classWriter.persist(tuples);
                    tuples.clear();
                }
            }
        }
        classWriter.persist(tuples);
        tuples.clear();
    }

    /**
     * Fills Labels of the neuralNet
     *
     * @param options pass Options to the neuralNet such as filepath
     */
    public abstract void fillLabels(Map<String, String> options);

    /**
     * Use this writer to fill in your own labels into the table which is available to all neural nets
     */
    protected PersistencyWriter<?> getClassWriter() {
        return this.classWriter;
    }

    /**
     * Get wnLabels associated with concepts from this selector
     */
    protected DBSelector getClassSelector() {
        return this.classSelector;
    }

    /**
     * Column name for the wnLabel. Example: n2348
     */
    protected String getWnLabelColName() {
        return wnLabel;
    }

    /**
     * Column name for the concept/human label column
     */
    public static String getHumanLabelColName() {
        return humanLabelColName;
    }

    /**
     * @return The table name where generated data is stored (not the full 1k-vector)
     */
    abstract public String getClassificationTable();
}
