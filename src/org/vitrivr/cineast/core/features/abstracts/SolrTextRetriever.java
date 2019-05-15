package org.vitrivr.cineast.core.features.abstracts;

import java.util.*;
import java.util.function.Supplier;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.vitrivr.cineast.core.config.Config;
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
 * retrieval in the future.
 * Expects two fields for a feature: id and feature. this corresponds to {@link SimpleFulltextFeatureDescriptor#FIELDNAMES}
 */
public abstract class SolrTextRetriever implements Retriever, Extractor {

  private static final Logger LOGGER = LogManager.getLogger();

  /**
   * Name of the table/entity used to store the data.
   */
  private final String tableName;

  /**
   * The {@link DBSelector} used for database lookup.
   */
  protected DBSelector selector = null;

  /**
   * The {@link SimpleFulltextFeatureDescriptorWriter} used to persist data.
   */
  protected SimpleFulltextFeatureDescriptorWriter writer;

  /**
   * Constructor for {@link SolrTextRetriever}
   *
   * @param tableName Name of the table/entity used to store the data
   */
  public SolrTextRetriever(String tableName) {
    this.tableName = tableName;
  }

  @Override
  public void init(DBSelectorSupplier selectorSupply) {
    this.selector = selectorSupply.get();
    this.selector.open(this.getEntityName());
  }

  @Override
  public void init(PersistencyWriterSupplier phandlerSupply) {
    this.writer = new SimpleFulltextFeatureDescriptorWriter(phandlerSupply.get(), this.tableName,
        10);
  }

  @Override
  public void processSegment(SegmentContainer shot) {
    throw new UnsupportedOperationException("Not supported by default");
  }

  /**
   * Initializes the persistent layer with two fields: "id" and "feature" both using the Apache Solr
   * storage handler. This corresponds to the Fieldnames of the {@link SimpleFulltextFeatureDescriptor} The "feature" in this context is the full text for the given segment
   */
  @Override
  public void initalizePersistentLayer(Supplier<EntityCreator> supply) {
    final AttributeDefinition[] fields = new AttributeDefinition[2];
    final Map<String, String> hints = new HashMap<>(1);
    hints.put("handler", "solr");
    fields[0] = new AttributeDefinition(SimpleFulltextFeatureDescriptor.FIELDNAMES[0],
        AttributeDefinition.AttributeType.STRING, hints);
    fields[1] = new AttributeDefinition(SimpleFulltextFeatureDescriptor.FIELDNAMES[1],
        AttributeDefinition.AttributeType.TEXT, hints);
    supply.get().createEntity(this.tableName, fields);
  }

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
    LOGGER.error("Similar to shotID is not supported for SolrTextRetriever");
    return new ArrayList<>(0); // currently not supported
  }

  /**
   * Performs a fulltext search using the text specified in {@link SegmentContainer#getText()}. In
   * contrast to convention used in most feature modules, the data used during ingest and retrieval
   * is usually different for {@link SolrTextRetriever} subclasses.
   *
   * <strong>Important:</strong> This implementation is tailored to the Apache Solr storage engine
   * used by ADAMpro. It uses Lucene's fuzzy search functionality.
   */
  @Override
  public List<ScoreElement> getSimilar(SegmentContainer sc, ReadableQueryConfig qc) {
    final String[] terms = generateQuery(sc, qc);
    return getSimilar(qc, terms);
  }

  /**
   * Generate a query term which will then be used for retrieval.
   */
  protected String[] generateQuery(SegmentContainer sc, ReadableQueryConfig qc) {
    return sc.getText().split(" ");
  }
  /**
   * Convenience-Method for implementing classes once they have generated their query terms
   */
  protected List<ScoreElement> getSimilar(ReadableQueryConfig qc, String... terms) {
    final List<Map<String, PrimitiveTypeProvider>> resultList = this.selector
        .getFulltextRows(Config.sharedConfig().getRetriever().getMaxResultsPerModule(), SimpleFulltextFeatureDescriptor.FIELDNAMES[1], terms);

    final CorrespondenceFunction f = CorrespondenceFunction
        .fromFunction(score -> score / terms.length / 10f);
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
