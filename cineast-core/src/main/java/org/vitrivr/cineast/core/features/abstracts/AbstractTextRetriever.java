package org.vitrivr.cineast.core.features.abstracts;

import static org.vitrivr.cineast.core.util.CineastConstants.DB_DISTANCE_VALUE_QUALIFIER;
import static org.vitrivr.cineast.core.util.CineastConstants.FEATURE_COLUMN_QUALIFIER;
import static org.vitrivr.cineast.core.util.CineastConstants.GENERIC_ID_COLUMN_QUALIFIER;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.config.ReadableQueryConfig;
import org.vitrivr.cineast.core.data.CorrespondenceFunction;
import org.vitrivr.cineast.core.data.entities.SimpleFulltextFeatureDescriptor;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;
import org.vitrivr.cineast.core.data.providers.primitive.StringTypeProvider;
import org.vitrivr.cineast.core.data.score.ScoreElement;
import org.vitrivr.cineast.core.data.score.SegmentScoreElement;
import org.vitrivr.cineast.core.data.segments.SegmentContainer;
import org.vitrivr.cineast.core.db.DBSelector;
import org.vitrivr.cineast.core.db.DBSelectorSupplier;
import org.vitrivr.cineast.core.db.PersistencyWriterSupplier;
import org.vitrivr.cineast.core.db.dao.writer.SimpleFulltextFeatureDescriptorWriter;
import org.vitrivr.cineast.core.db.setup.AttributeDefinition;
import org.vitrivr.cineast.core.db.setup.EntityCreator;
import org.vitrivr.cineast.core.features.extractor.Extractor;
import org.vitrivr.cineast.core.features.retriever.Retriever;

/**
 * Expects two fields for a feature: id and feature. this corresponds to {@link SimpleFulltextFeatureDescriptor#FIELDNAMES}
 */
public abstract class AbstractTextRetriever implements Retriever, Extractor {

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

  @Override
  public List<String> getTableNames() {
    return Collections.singletonList(tableName);
  }

  /**
   * Constructor for {@link AbstractTextRetriever}
   *
   * @param tableName Name of the table/entity used to store the data
   */
  public AbstractTextRetriever(String tableName) {
    this.tableName = tableName;
  }

  @Override
  public void init(DBSelectorSupplier selectorSupply) {
    this.selector = selectorSupply.get();
    this.selector.open(this.getEntityName());
  }

  @Override
  public void init(PersistencyWriterSupplier phandlerSupply, int batchSize) {
    this.writer = new SimpleFulltextFeatureDescriptorWriter(phandlerSupply.get(), this.tableName, batchSize);
  }

  @Override
  public void processSegment(SegmentContainer shot) {
    throw new UnsupportedOperationException("Not supported by default");
  }

  /**
   * Initializes the persistent layer with two fields: GENERIC_ID_COLUMN_QUALIFIER and FEATURE_COLUMN_QUALIFIER both using the Apache Solr storage handler.
   *
   * This corresponds to the Fieldnames of the {@link SimpleFulltextFeatureDescriptor} The FEATURE_COLUMN_QUALIFIER in this context is the full text for the given segment
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
  public List<ScoreElement> getSimilar(String segmentId, ReadableQueryConfig qc) {
    List<Map<String, PrimitiveTypeProvider>> rows = this.selector.getRows(GENERIC_ID_COLUMN_QUALIFIER, new StringTypeProvider(segmentId));
    if (rows.isEmpty()) {
      LOGGER.debug("No rows with segment id {}", segmentId);
      return Collections.emptyList();
    }
    List<String> terms = new ArrayList<>();
    rows.forEach(row -> terms.add(row.get(FEATURE_COLUMN_QUALIFIER).getString()));
    return this.getSimilar(qc, terms.toArray(new String[]{}));
  }

  @Override
  public List<ScoreElement> getSimilar(List<String> segmentIds, ReadableQueryConfig qc) {
    List<String> list = this.selector.getRows(GENERIC_ID_COLUMN_QUALIFIER, segmentIds).stream().map(map -> map.get(FEATURE_COLUMN_QUALIFIER).getString()).collect(Collectors.toList());
    if (list.isEmpty()) {
      LOGGER.debug("No rows with segment id {}", segmentIds);
      return Collections.emptyList();
    }
    return this.getSimilar(qc, list.toArray(new String[]{}));
  }

  /**
   * Performs a fulltext search using the text specified in {@link SegmentContainer#getText()}. In contrast to convention used in most feature modules, the data used during ingest and retrieval is usually different for {@link AbstractTextRetriever} subclasses.
   *
   * <strong>Important:</strong> This implementation is tailored to the Apache Solr storage engine
   * used by ADAMpro. It uses Lucene's fuzzy search functionality.
   */
  @Override
  public List<ScoreElement> getSimilar(SegmentContainer sc, ReadableQueryConfig qc) {
    final String[] terms = generateQuery(sc, qc);
    return this.getSimilar(qc, terms);
  }

  /**
   * Generate a query term which will then be used for retrieval.
   */
  private static final Pattern regex = Pattern.compile("([^\"]\\S*|\".+?\")\\s*");

  protected String[] generateQuery(SegmentContainer sc, ReadableQueryConfig qc) {

    Matcher m = regex.matcher(sc.getText());
    ArrayList<String> matches = new ArrayList<>();

    while (m.find()) {
      String match = m.group(1).trim();
      if (!match.isEmpty()) {
        matches.add(enrichQueryTerm(match));
      }
    }

    return matches.toArray(new String[matches.size()]);
  }

  /**
   * Implementing features can transform individual query terms. By default, nothing happens
   */
  protected String enrichQueryTerm(String queryTerm) {
    return queryTerm;
  }

  /**
   * Convenience-Method for implementing classes once they have generated their query terms.
   *
   * If there are multiple scores per segment (e.g. a segment has "hello" and "hello world" which produces two hits, does maxpooling
   */
  protected List<ScoreElement> getSimilar(ReadableQueryConfig qc, String... terms) {
    final List<Map<String, PrimitiveTypeProvider>> resultList = this.selector.getFulltextRows(qc.getResultsPerModule(), SimpleFulltextFeatureDescriptor.FIELDNAMES[1], qc, terms);

    LOGGER.trace("Retrieved {} results for terms {}", resultList.size(), Arrays.toString(terms));

    final CorrespondenceFunction f = CorrespondenceFunction
        .fromFunction(score -> score / terms.length / 10f);
    final List<ScoreElement> scoreElements = new ArrayList<>(resultList.size());
    final Map<String, Float> scoreMap = new HashMap<>();

    for (Map<String, PrimitiveTypeProvider> result : resultList) {
      String id = result.get(GENERIC_ID_COLUMN_QUALIFIER).getString();
      scoreMap.putIfAbsent(id, 0f);
      // There is no way to trace from a result to which of the terms it matched. While this is regrettable behavior, averaging is preferable to maxpooling
      scoreMap.compute(id, (key, val) -> val += result.get(DB_DISTANCE_VALUE_QUALIFIER).getFloat() / terms.length);
    }
    scoreMap.forEach((key, value) -> {
      double score = f.applyAsDouble(scoreMap.get(key));
      scoreElements.add(new SegmentScoreElement(key, score));
    });
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
