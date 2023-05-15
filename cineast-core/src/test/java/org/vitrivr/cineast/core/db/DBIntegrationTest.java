package org.vitrivr.cineast.core.db;

import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.vitrivr.cineast.core.util.CineastConstants.DB_DISTANCE_VALUE_QUALIFIER;

import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.vitrivr.cineast.core.config.QueryConfig;
import org.vitrivr.cineast.core.config.ReadableQueryConfig;
import org.vitrivr.cineast.core.config.ReadableQueryConfig.Distance;
import org.vitrivr.cineast.core.data.distance.SegmentDistanceElement;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;
import org.vitrivr.cineast.core.data.providers.primitive.StringTypeProvider;
import org.vitrivr.cineast.core.db.setup.AttributeDefinition;
import org.vitrivr.cineast.core.db.setup.AttributeDefinition.AttributeType;
import org.vitrivr.cineast.core.db.setup.EntityCreator;
import org.vitrivr.cineast.core.util.CineastIOUtils;

/**
 * Performs Inserts and Retrieves Elements. Verifies that both writing and reading is performed correctly.
 *
 * @param <R> The type of Element that can be persisted to this database
 */
@TestInstance(Lifecycle.PER_CLASS)
public abstract class DBIntegrationTest<R> {

  protected static final String ID_COL_NAME = "id";
  protected static final int VECTOR_ELEMENT_COUNT = 110;
  protected static final int MAX_VECTOR_ID = VECTOR_ELEMENT_COUNT - 1;
  protected static final int TEXT_ELEMENT_COUNT = 8;
  protected static final int MAX_TEXT_ID = TEXT_ELEMENT_COUNT - 1;
  private static final int HELLO_WORLD_ID = MAX_TEXT_ID - 7;
  private static final int HELLA_WORLD_ID = MAX_TEXT_ID - 6;
  private static final int SINGLE_ID = MAX_TEXT_ID - 5;
  private static final int DOUBLE_ID = MAX_TEXT_ID - 4;
  private static final int DUPLICATE_ID = MAX_TEXT_ID - 4;
  private static final int HELLO_ID = MAX_TEXT_ID - 3;
  private static final int WORLD_ID = MAX_TEXT_ID - 2;
  private static final int HELLO_WORLD_MY_NAME_IS_CINEAST_ID = MAX_TEXT_ID - 1;
  /**
   * This is not called "feature" by design as it avoid the storage-layers doing optimization by col name
   */
  protected static final String FEATURE_VECTOR_COL_NAME = "vector";
  protected static final String TEXT_COL_NAME = "text";
  protected static final Logger LOGGER = LogManager.getLogger();
  protected DBSelector selector;
  protected String testTextTableName;
  protected String testVectorTableName;
  protected PersistencyWriter<R> writer;
  protected EntityCreator ec;
  protected QueryConfig queryConfig;
  private IntegrationDBProvider<R> provider;

  @BeforeAll
  void checkConnection() {
    provider = provider();
    assumeTrue(provider != null);
    selector = provider.getSelector();
    LOGGER.info("Trying to establish connection to Database");
    assumeTrue(selector.ping(), "Connection to database could not be established");
    LOGGER.info("Connection to database established");
    writer = provider.getPersistencyWriter();
    ec = provider.getEntityCreator();
    testTextTableName = getTestTextTableName();
    testVectorTableName = getTestVectorTableName();
    queryConfig = getQueryConfig();
  }

  @BeforeEach
  void setupTest() {
    assumeTrue(provider != null);
    dropTables();
    createTables();
    fillVectorData();
    fillTextData();
    finishSetup();
  }

  @AfterEach
  void tearDownTest() {
    dropTables();
  }

  @AfterAll
  void tearDownAll() {
    CineastIOUtils.closeQuietly(this.provider);
  }

  protected abstract void finishSetup();

  protected QueryConfig getQueryConfig() {
    QueryConfig qc = new QueryConfig("test-" + RandomStringUtils.randomNumeric(4), new ArrayList<>());
    qc.setDistanceIfEmpty(Distance.euclidean);
    return qc;
  }

  protected abstract IntegrationDBProvider<R> provider();

  protected void fillTextData() {
    writer.open(testTextTableName);
    writer.setFieldNames("id", "text");
    List<PersistentTuple> vectors = new ArrayList<>();
    vectors.add(writer.generateTuple(String.valueOf(HELLO_WORLD_ID), "hello world"));
    vectors.add(writer.generateTuple(String.valueOf(HELLA_WORLD_ID), "hella world"));
    vectors.add(writer.generateTuple(String.valueOf(SINGLE_ID), "single"));
    vectors.add(writer.generateTuple(String.valueOf(DOUBLE_ID), "double"));
    vectors.add(writer.generateTuple(String.valueOf(HELLO_ID), "hello"));
    vectors.add(writer.generateTuple(String.valueOf(DUPLICATE_ID), "duplicate"));
    vectors.add(writer.generateTuple(String.valueOf(WORLD_ID), "world"));
    vectors.add(writer.generateTuple(String.valueOf(HELLO_WORLD_MY_NAME_IS_CINEAST_ID), "hello world my name is cineast"));
    writer.persist(vectors);
  }

  /**
   * Writes a set of vectors into the underlying database
   */
  protected void fillVectorData() {
    writer.open(testVectorTableName);
    writer.setFieldNames(ID_COL_NAME, FEATURE_VECTOR_COL_NAME);
    List<PersistentTuple> vectors = new ArrayList<>();

    for (int i = 0; i < MAX_VECTOR_ID; i++) {
      float[] vector = new float[3];
      vector[0] = i;
      vector[1] = 1;
      vector[2] = 0;
      vectors.add(writer.generateTuple(toId(i), vector));
    }
    /* We write a second vector with the same id in the db */
    vectors.add(writer.generateTuple(toId(0), new float[]{0, 0, 0}));
    writer.persist(vectors);
  }

  private String toId(int id) {
    return String.format("%05d", id);
  }

  /**
   * Create both a table for vector retrieval & text retrieval
   */
  protected void createTables() {
    this.ec.createEntity(testTextTableName, new AttributeDefinition(ID_COL_NAME, AttributeType.STRING), new AttributeDefinition(TEXT_COL_NAME, AttributeType.TEXT));
    this.ec.createEntity(testVectorTableName, new AttributeDefinition(ID_COL_NAME, AttributeType.STRING), new AttributeDefinition(FEATURE_VECTOR_COL_NAME, AttributeType.VECTOR, 3));
  }

  protected void dropTables() {
    if (ec != null) {
      if (ec.existsEntity(testTextTableName)) {
        ec.dropEntity(testTextTableName);
      } else {
        LOGGER.trace("not dropping {} because it does not exist", testTextTableName);
      }
      if (ec.existsEntity(testVectorTableName)) {
        ec.dropEntity(testVectorTableName);
      } else {
        LOGGER.trace("not dropping {} because it does not exist", testVectorTableName);
      }
    }
  }

  protected String getTestTextTableName() {
    return "test_feature_text";
  }

  protected String getTestVectorTableName() {
    return "test_feature_vector";
  }

  @Test
  @DisplayName("Ping the database")
  void ping() {
    Assertions.assertTrue(provider.getSelector().ping());
  }

  @Test
  @DisplayName("Verify entity creation")
  void entitiesExist() {
    Assertions.assertTrue(selector.existsEntity(testVectorTableName));
    Assertions.assertTrue(selector.existsEntity(testTextTableName));
  }

  @Test
  @DisplayName("Verify element count using getAll()")
  void countGetAll() {
    selector.open(testVectorTableName);
    Assertions.assertEquals(VECTOR_ELEMENT_COUNT, selector.getAll().size());
    selector.open(testTextTableName);
    Assertions.assertEquals(TEXT_ELEMENT_COUNT, selector.getAll().size());
  }

  @Test
  @DisplayName("Verify element count using rowCount()")
  void countRowCount() {
    selector.open(testVectorTableName);
    Assertions.assertEquals(VECTOR_ELEMENT_COUNT, selector.rowCount());
    selector.open(testTextTableName);
    Assertions.assertEquals(TEXT_ELEMENT_COUNT, selector.rowCount());
  }

  @Test
  @DisplayName("Verify elements exist by id")
  void entriesExistById() {
    this.writer.open(testVectorTableName);
    for (int i = 0; i < MAX_VECTOR_ID; i++) {
      Assertions.assertTrue(writer.idExists(toId(i)));
    }
    this.writer.open(testTextTableName);
    for (int i = 0; i < MAX_TEXT_ID; i++) {
      Assertions.assertTrue(writer.idExists(String.valueOf(i)));
    }
  }


  @Test
  @DisplayName("get multiple feature vectors")
  void getFeatureVectors() {
    this.selector.open(testVectorTableName);
    final List<PrimitiveTypeProvider> vectors = this.selector.getFeatures(ID_COL_NAME, new StringTypeProvider(toId(0)), FEATURE_VECTOR_COL_NAME, queryConfig);
    Assertions.assertTrue((Arrays.equals(PrimitiveTypeProvider.getSafeFloatArray(vectors.get(0)), new float[]{0, 0, 0}) | Arrays.equals(PrimitiveTypeProvider.getSafeFloatArray(vectors.get(0)), new float[]{0, 1, 0})));
    Assertions.assertTrue((Arrays.equals(PrimitiveTypeProvider.getSafeFloatArray(vectors.get(1)), new float[]{0, 0, 0}) | Arrays.equals(PrimitiveTypeProvider.getSafeFloatArray(vectors.get(1)), new float[]{0, 1, 0})));
  }

  @Test
  @DisplayName("Simple KNN search")
  void knnSearch() {
    selector.open(testVectorTableName);
    List<SegmentDistanceElement> result = selector.getNearestNeighboursGeneric(3, new float[]{1, 1, 0}, FEATURE_VECTOR_COL_NAME, SegmentDistanceElement.class, queryConfig);
    Assertions.assertEquals(3, result.size());
    Assertions.assertEquals(toId(1), result.get(0).getSegmentId());
    Assertions.assertTrue(result.get(1).getSegmentId().equals(toId(2)) || result.get(2).getSegmentId().equals(toId(2)));
    Assertions.assertTrue(result.get(1).getSegmentId().equals(toId(0)) || result.get(2).getSegmentId().equals(toId(0)));
  }

  @Test
  @DisplayName("Get all skip limit")
  void getAllSkip() {
    selector.open(testVectorTableName);
    int limit = 10;
    IntStream.range(0, 3).forEach(i -> {
      List<Map<String, PrimitiveTypeProvider>> all = selector.getAll(ID_COL_NAME, i, limit);
      Assertions.assertEquals(all.size(), limit);
      all.forEach(el -> {
        Assertions.assertTrue(Integer.parseInt(el.get(ID_COL_NAME).getString()) >= i - 1, "id " + el.get(ID_COL_NAME).getString() + " was smaller than " + i);
        // we use <= for 0 because there are two elements with id 0 in the table
        if (i == 0 || i == 1) {
          Assertions.assertTrue(Integer.parseInt(el.get(ID_COL_NAME).getString()) <= i + limit, "id " + el.get(ID_COL_NAME).getString() + " was larger than " + (i + limit));
        } else {
          Assertions.assertTrue(Integer.parseInt(el.get(ID_COL_NAME).getString()) < i + limit, "id " + el.get(ID_COL_NAME).getString() + " was larger than " + (i + limit));
        }
      });
    });
  }

  /**
   * TODO: Currently not supported in Cottontail DB v0.12.0. Re-activate, once support is back.
   */
  @Test
  @Disabled
  @DisplayName("Batched KNN search")
  void batchedKnnSearch() {
    selector.open(testVectorTableName);
    List<float[]> queries = new ArrayList<>();
    queries.add(new float[]{0.001f, 1, 0});
    queries.add(new float[]{3.1f, 1, 0});
    queries.add(new float[]{4.8f, 1, 0});
    queryConfig.setDistanceIfEmpty(Distance.manhattan);
    List<ReadableQueryConfig> configs = queries.stream().map(el -> new ReadableQueryConfig(queryConfig)).collect(Collectors.toList());
    List<SegmentDistanceElement> result = selector.getBatchedNearestNeighbours(1, queries, FEATURE_VECTOR_COL_NAME, SegmentDistanceElement.class, configs);
    Assertions.assertEquals(3, result.size());
    Assertions.assertEquals("0", result.get(0).getSegmentId());
    Assertions.assertEquals(0.001, result.get(0).getDistance(), 0.0001);
    Assertions.assertEquals("3", result.get(1).getSegmentId());
    Assertions.assertEquals(0.1, result.get(1).getDistance(), 0.0001);
    Assertions.assertEquals("5", result.get(2).getSegmentId());
    Assertions.assertEquals(0.2, result.get(2).getDistance(), 0.0001);
  }


  /**
   * Verify that a resultSet satisfies a certain condition for a given column
   *
   * @param results  the full resultset
   * @param col      column for which the provided function should be executed
   * @param function function to be executed on the {@link PrimitiveTypeProvider} value at the column
   */
  private void checkContains(List<Map<String, PrimitiveTypeProvider>> results, String col, Function<PrimitiveTypeProvider, Boolean> function) {
    AtomicBoolean match = new AtomicBoolean(false);
    results.forEach(map -> {
      map.forEach((key, value) -> {
        if (key.equals(col) && function.apply(value)) {
          match.set(true);
        }
      });
    });
    if (match.get()) {
      return;
    }
    Assertions.fail("element not found in results: \n" + new Gson().toJson(results));
  }

  /**
   * This test verifies that a simple "hello" query retrieves exact and partial matches, but no fuzziness
   */
  @Test
  @DisplayName("Text: One el, no quotes")
  public void textRetrievalSingleLike() {
    selector.open(testTextTableName);
    List<Map<String, PrimitiveTypeProvider>> results = selector.getFulltextRows(10, TEXT_COL_NAME, queryConfig, "hello");
    Assertions.assertEquals(3, results.size());
    checkContains(results, ID_COL_NAME, val -> val.getString().equals(String.valueOf(HELLO_WORLD_ID)));
    checkContains(results, ID_COL_NAME, val -> val.getString().equals(String.valueOf(HELLO_ID)));
    checkContains(results, ID_COL_NAME, val -> val.getString().equals(String.valueOf(HELLO_WORLD_MY_NAME_IS_CINEAST_ID)));
  }

  /**
   * This test verifies that a simple "hello" query retrieves exact and partial matches, but no fuzziness
   */
  @Test
  @DisplayName("Text: two words, inverted, no quotes")
  public void textRetrievalSingleTwoWordsLike() {
    selector.open(testTextTableName);
    List<Map<String, PrimitiveTypeProvider>> results = selector.getFulltextRows(10, TEXT_COL_NAME, queryConfig, "name my");
    Assertions.assertEquals(1, results.size());
    checkContains(results, ID_COL_NAME, val -> val.getString().equals(String.valueOf(HELLO_WORLD_MY_NAME_IS_CINEAST_ID)));
  }


  /**
   * This test verifies that a quoted query only retrieves results which contain the full query term
   */
  @Test
  @DisplayName("Text: One el (two words), quotes")
  public void textRetrievalSingleTwoWordsQuotedLike() {
    selector.open(testTextTableName);
    List<Map<String, PrimitiveTypeProvider>> results = selector.getFulltextRows(10, TEXT_COL_NAME, queryConfig, "\"hello world\"");
    Assertions.assertEquals(2, results.size());
    checkContains(results, ID_COL_NAME, val -> val.getString().equals(String.valueOf(HELLO_WORLD_ID)));
    checkContains(results, ID_COL_NAME, val -> val.getString().equals(String.valueOf(HELLO_WORLD_MY_NAME_IS_CINEAST_ID)));
  }

  /**
   * Verifies that ~1 means levenshtein 1
   */
  @Test
  @DisplayName("Text: One el, one word, Fuzzy")
  public void testRetrievalSingleFuzzy() {
    selector.open(testTextTableName);
    List<Map<String, PrimitiveTypeProvider>> results = selector.getFulltextRows(10, TEXT_COL_NAME, queryConfig, "hello~1");
    Assertions.assertEquals(4, results.size());
    checkContains(results, ID_COL_NAME, val -> val.getString().equals(String.valueOf(HELLO_WORLD_ID)));
    checkContains(results, ID_COL_NAME, val -> val.getString().equals(String.valueOf(HELLO_ID)));
    checkContains(results, ID_COL_NAME, val -> val.getString().equals(String.valueOf(HELLO_WORLD_MY_NAME_IS_CINEAST_ID)));
  }

  /**
   * TODO: Fuzzy search on whole phrases is currently not supported.
   * <p>
   * Something like "hello world"~1 would need to be implemented as either hello~1 AND world~1, but that is not implemented in the DBSelector / cottontail.
   * The cottontail implementation in december 19 parses hello world~1 as hello .. world~1, which is not what we're looking for
   * Therefore, this test serves as a note that this functionality is lacking.
   */
  @Test
  @DisplayName("Text: One el (Two words), Fuzzy")
  @Disabled
  void testRetrievalSingleTwoWordsFuzzy() {
    Assertions.fail();
  }

  /**
   * Verify that searching for two terms retrieves both individual results
   */
  @Test
  @DisplayName("Text: Two elements w/ single word")
  public void testRetrievalTwo() {
    selector.open(testTextTableName);
    List<Map<String, PrimitiveTypeProvider>> results = selector.getFulltextRows(10, TEXT_COL_NAME, queryConfig, "single", "double");
    Assertions.assertEquals(2, results.size());
    checkContains(results, ID_COL_NAME, val -> val.getString().equals(String.valueOf(SINGLE_ID)));
    checkContains(results, ID_COL_NAME, val -> val.getString().equals(String.valueOf(DOUBLE_ID)));
  }

  @Test
  @DisplayName("Text: Three elements, two are a match for the same id")
  public void testRetrievalThreeDouble() {
    selector.open(testTextTableName);
    List<Map<String, PrimitiveTypeProvider>> results = selector.getFulltextRows(10, TEXT_COL_NAME, queryConfig, "double", "single", "duplicate");
    Assertions.assertEquals(3, results.size());
    checkContains(results, ID_COL_NAME, val -> val.getString().equals(String.valueOf(DOUBLE_ID)));
    checkContains(results, ID_COL_NAME, val -> val.getString().equals(String.valueOf(SINGLE_ID)));
    float score = results.get(0).get(DB_DISTANCE_VALUE_QUALIFIER).getFloat();
    Assertions.assertEquals(score, results.get(1).get(DB_DISTANCE_VALUE_QUALIFIER).getFloat(), 0.01);
    Assertions.assertEquals(score, results.get(2).get(DB_DISTANCE_VALUE_QUALIFIER).getFloat(), 0.01);
  }

  @Test
  @DisplayName("Text: Three els, one of those with quotes")
  public void testRetrievalThree() {
    selector.open(testTextTableName);
    List<Map<String, PrimitiveTypeProvider>> results = selector.getFulltextRows(10, TEXT_COL_NAME, queryConfig, "single", "double", "\"hello world\"");
    Assertions.assertEquals(4, results.size());
    checkContains(results, ID_COL_NAME, val -> val.getString().equals(String.valueOf(SINGLE_ID)));
    checkContains(results, ID_COL_NAME, val -> val.getString().equals(String.valueOf(DOUBLE_ID)));
    checkContains(results, ID_COL_NAME, val -> val.getString().equals(String.valueOf(HELLO_WORLD_ID)));
    checkContains(results, ID_COL_NAME, val -> val.getString().equals(String.valueOf(HELLO_WORLD_MY_NAME_IS_CINEAST_ID)));
  }

}
