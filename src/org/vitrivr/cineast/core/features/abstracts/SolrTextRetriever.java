package org.vitrivr.cineast.core.features.abstracts;

import java.util.*;
import java.util.function.Supplier;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.vitrivr.cineast.core.config.ReadableQueryConfig;
import org.vitrivr.cineast.core.data.CorrespondenceFunction;
import org.vitrivr.cineast.core.data.entities.SimpleFulltextFeatureDescriptor;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;
import org.vitrivr.cineast.core.data.score.ScoreElement;
import org.vitrivr.cineast.core.data.score.SegmentScoreElement;
import org.vitrivr.cineast.core.data.segments.SegmentContainer;

import org.vitrivr.cineast.core.db.DBSelector;
import org.vitrivr.cineast.core.db.DBSelectorSupplier;
import org.vitrivr.cineast.core.db.PersistencyWriterSupplier;
import org.vitrivr.cineast.core.db.dao.writer.SimpleFulltextFeatureDescriptorWriter;
import org.vitrivr.cineast.core.features.extractor.Extractor;
import org.vitrivr.cineast.core.features.retriever.Retriever;
import org.vitrivr.cineast.core.setup.AttributeDefinition;
import org.vitrivr.cineast.core.setup.EntityCreator;

/**
 * This is a proof of concept class and will probably be replaced by a more general solution to text
 * retrieval in the future
 */
public abstract class SolrTextRetriever implements Retriever, Extractor {

    private static final Logger LOGGER = LogManager.getLogger();

    /** Name of the table/entity used to store the data. */
    private final String tableName;

    /** The {@link DBSelector} used for database lookup. */
    protected DBSelector selector = null;

    /** The {@link SimpleFulltextFeatureDescriptorWriter} used to persist data. */
    protected SimpleFulltextFeatureDescriptorWriter writer;

    /**
     * Constructor for {@link SolrTextRetriever}
     *
     * @param tableName Name of the table/entity used to store the data
     */
    public SolrTextRetriever(String tableName) {
        this.tableName = tableName;
    }

    /**
     * Initializes the database selector.
     *
     * @param selectorSupply
     */
    @Override
    public void init(DBSelectorSupplier selectorSupply) {
        this.selector = selectorSupply.get();
        this.selector.open(this.getEntityName());
    }

    /**
     * Initializes the persistency writer.
     *
     * @param phandlerSupply
     */
    @Override
    public void init(PersistencyWriterSupplier phandlerSupply) {
        this.writer = new SimpleFulltextFeatureDescriptorWriter(phandlerSupply.get(), this.tableName, 10);
    }

    /**
     * Initializes the persistent layer with two fields: "id" and "feature" both using the Apache Solr storage handler.
     *
     * @param supply A supplier for {@link EntityCreator} instances.
     */
    @Override
    public void initalizePersistentLayer(Supplier<EntityCreator> supply) {
        final AttributeDefinition[] fields = new AttributeDefinition[2];
        final Map<String,String> hints = new HashMap<>(1);
        hints.put("handler", "solr");
        fields[0] = new AttributeDefinition(SimpleFulltextFeatureDescriptor.FIELDNAMES[0], AttributeDefinition.AttributeType.STRING, hints);
        fields[1] = new AttributeDefinition(SimpleFulltextFeatureDescriptor.FIELDNAMES[1], AttributeDefinition.AttributeType.TEXT, hints);
        supply.get().createEntity(this.tableName, fields);
    }

    /**
     *
     * @param supply
     */
    @Override
    public void dropPersistentLayer(Supplier<EntityCreator> supply) {
        supply.get().dropEntity(this.tableName);
    }

    /**
     * Returns the name of the entity used to store the data.
     *
     * @return Name of the entity.
     */
    public String getEntityName() {
        return this.tableName;
    }

    @Override
    public List<ScoreElement> getSimilar(String shotId, ReadableQueryConfig qc) {
        return new ArrayList<>(0); // currently not supported
    }

    /**
     * Performs a fulltext search using the text specified in {@link SegmentContainer#getText()}. In contrast to convention used in most
     * feature modules, the data used during ingest and retrieval is usually different for {@link SolrTextRetriever} subclasses.
     *
     * <strong>Important:</strong> This implementation is tailored to the Apache Solr storage engine used by ADAMpro. It uses Lucene's
     * fuzzy search functionality.
     *
     * TODO: The class should probably be generalized in the future.
     *
     * @param sc The {@link SegmentContainer} used for lookup.
     * @param qc The {@link ReadableQueryConfig} used to configure the query.
     * @return List of {@link ScoreElement}s.
     */
    @Override
    public List<ScoreElement> getSimilar(SegmentContainer sc, ReadableQueryConfig qc) {
        final String[] terms = Arrays.stream(sc.getText().split("\\s")).map(s -> s + "~0.5").toArray(String[]::new);
        final List<Map<String, PrimitiveTypeProvider>> resultList = this.selector.getFulltextRows(500, SimpleFulltextFeatureDescriptor.FIELDNAMES[1], terms);
        final CorrespondenceFunction f = CorrespondenceFunction.fromFunction(score -> score / terms.length / 10f);
        final List<ScoreElement> scoreElements = new ArrayList<>(resultList.size());
        for (Map<String, PrimitiveTypeProvider> result : resultList) {
            String id = result.get("id").getString();
            double score = f.applyAsDouble(result.get("ap_score").getFloat());
            scoreElements.add(new SegmentScoreElement(id, score));
        }
        return scoreElements;
    }

    @Override
    public void finish() {
        if (this.selector != null) {
            this.selector.close();
            this.selector = null;
        }
        if (this.writer != null) {
            this.writer.close();
            this.writer = null;
        }
    }
}
