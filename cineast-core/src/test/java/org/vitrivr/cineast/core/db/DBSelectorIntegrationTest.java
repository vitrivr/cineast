package org.vitrivr.cineast.core.db;

import static org.junit.jupiter.api.Assumptions.assumeTrue;

import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.vitrivr.cineast.core.config.QueryConfig;
import org.vitrivr.cineast.core.config.ReadableQueryConfig;
import org.vitrivr.cineast.core.config.ReadableQueryConfig.Distance;
import org.vitrivr.cineast.core.data.distance.SegmentDistanceElement;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;
import org.vitrivr.cineast.core.db.setup.AttributeDefinition;
import org.vitrivr.cineast.core.db.setup.AttributeDefinition.AttributeType;
import org.vitrivr.cineast.core.db.setup.EntityCreator;

public abstract class DBSelectorIntegrationTest<R> {

  private DBSelector selector;
  private String testTextTableName;
  private String testVectorTableName;
  private PersistencyWriter<R> writer;
  private EntityCreator ec;
  private QueryConfig queryConfig;
  private static final String ID_COL_NAME = "id";
  /**
   * This is not called "feature" by design as it avoid the storage-layers doing optimization by col name
   */
  private static final String FEATURE_VECTOR_COL_NAME = "vector";
  private static final String TEXT_COL_NAME = "text";

  @BeforeEach
  void setupTest() {
    selector = getSelector();
    assumeTrue(selector.ping(), "Connection to database could not be established");
    writer = getPersistencyWriter();
    ec = getEntityCreator();
    testTextTableName = getTestTextTableName();
    testVectorTableName = getTestVectorTableName();
    queryConfig = getQueryConfig();
    dropTables();
    createTables();
    fillVectorData();
    fillTextData();
    finishSetup();
  }

  protected QueryConfig getQueryConfig() {
    QueryConfig qc = new QueryConfig(RandomStringUtils.random(10), new ArrayList<>());
    qc.setDistanceIfEmpty(Distance.euclidean);
    return qc;
  }

  protected abstract void finishSetup();

  @AfterEach
  void tearDownTest() {
    if (writer != null) {
      writer.close();
    }
    if (selector != null) {
      selector.close();
    }
    dropTables();
    if (ec != null) {
      ec.close();
    }
  }

  protected void fillTextData() {
    writer.open(testTextTableName);
    writer.setFieldNames("id", "text");
    List<PersistentTuple> vectors = new ArrayList<>();
    vectors.add(writer.generateTuple(1, "hello world"));
    vectors.add(writer.generateTuple(2, "hella world"));
    vectors.add(writer.generateTuple(3, "single"));
    vectors.add(writer.generateTuple(4, "double"));
    vectors.add(writer.generateTuple(5, "hello"));
    vectors.add(writer.generateTuple(4, "duplicate"));
    vectors.add(writer.generateTuple(6, "world"));
    vectors.add(writer.generateTuple(7, "hello world my name is cineast"));
    writer.persist(vectors);
  }

  protected void fillVectorData() {
    writer.open(testVectorTableName);
    writer.setFieldNames(ID_COL_NAME, FEATURE_VECTOR_COL_NAME);
    List<PersistentTuple> vectors = new ArrayList<>();

    for (int i = 0; i < 10; i++) {
      float[] vector = new float[3];
      vector[0] = i;
      vector[1] = 1;
      vector[2] = 0;
      vectors.add(writer.generateTuple(i, vector));
    }
    vectors.add(writer.generateTuple(0, new float[]{0, 0, 0}));
    writer.persist(vectors);
  }

  protected abstract PersistencyWriter<R> getPersistencyWriter();

  protected void createTables() {
    ec.createEntity(testTextTableName, new AttributeDefinition(ID_COL_NAME, AttributeType.INT), new AttributeDefinition(TEXT_COL_NAME, AttributeType.TEXT));
    ec.createEntity(testVectorTableName, new AttributeDefinition(ID_COL_NAME, AttributeType.LONG), new AttributeDefinition(FEATURE_VECTOR_COL_NAME, AttributeType.VECTOR, 3));
  }

  protected void dropTables() {
    if (ec != null) {
      ec.dropEntity(testTextTableName);
      ec.dropEntity(testVectorTableName);
    }
  }

  /**
   * As implementing test, you are responsible on whether this returns a new selector instance or a previously reused selector instance.
   */
  protected abstract DBSelector getSelector();

  public abstract EntityCreator getEntityCreator();

  protected String getTestTextTableName() {
    return "test_feature_text";
  }

  protected String getTestVectorTableName() {
    return "test_feature_vector";
  }

  @Test
  @DisplayName("Ping the database")
  void ping() {
    Assertions.assertTrue(getSelector().ping());
  }

  @Test
  @DisplayName("Verify entity creation")
  void entitiesExist() {
    Assertions.assertTrue(selector.existsEntity(testVectorTableName));
    Assertions.assertTrue(selector.existsEntity(testTextTableName));
  }

  @Test
  @DisplayName("Verify element count")
  void count() {
    selector.open(testVectorTableName);
    Assertions.assertEquals(11, selector.getAll().size());
    selector.open(testTextTableName);
    Assertions.assertEquals(8, selector.getAll().size());
  }

  @Test
  @DisplayName("get multiple feature vectors")
  void getFeatureVectors() {
    selector.open(testVectorTableName);
    List<PrimitiveTypeProvider> vectors = selector.getFeatureVectorsGeneric(ID_COL_NAME, "0", FEATURE_VECTOR_COL_NAME);
    Assertions.assertTrue((Arrays.equals(PrimitiveTypeProvider.getSafeFloatArray(vectors.get(0)), new float[]{0, 0, 0}) | Arrays.equals(PrimitiveTypeProvider.getSafeFloatArray(vectors.get(0)), new float[]{0, 1, 0})));
    Assertions.assertTrue((Arrays.equals(PrimitiveTypeProvider.getSafeFloatArray(vectors.get(1)), new float[]{0, 0, 0}) | Arrays.equals(PrimitiveTypeProvider.getSafeFloatArray(vectors.get(1)), new float[]{0, 1, 0})));
  }

  @Test
  @DisplayName("Simple KNN search")
  void knnSearch() {
    selector.open(testVectorTableName);
    List<SegmentDistanceElement> result = selector.getNearestNeighboursGeneric(3, new float[]{1, 1, 0}, FEATURE_VECTOR_COL_NAME, SegmentDistanceElement.class, queryConfig);
    Assertions.assertEquals(3, result.size());
    Assertions.assertEquals("1", result.get(0).getSegmentId());
    Assertions.assertTrue(result.get(1).getSegmentId().equals("2") || result.get(2).getSegmentId().equals("2"));
    Assertions.assertTrue(result.get(1).getSegmentId().equals("0") || result.get(2).getSegmentId().equals("0"));
  }

  @Test
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
  void textRetrievalSingleLike() {
    selector.open(testTextTableName);
    List<Map<String, PrimitiveTypeProvider>> results = selector.getFulltextRows(10, TEXT_COL_NAME, "hello");
    Assertions.assertEquals(3, results.size());
    checkContains(results, ID_COL_NAME, val -> val.getInt() == 1);
    checkContains(results, ID_COL_NAME, val -> val.getInt() == 5);
    checkContains(results, ID_COL_NAME, val -> val.getInt() == 7);
  }

  /**
   * This test verifies that a simple "hello" query retrieves exact and partial matches, but no fuzziness
   */
  @Test
  @DisplayName("Text: two words, inverted, no quotes")
  void textRetrievalSingleTwoWordsLike() {
    selector.open(testTextTableName);
    List<Map<String, PrimitiveTypeProvider>> results = selector.getFulltextRows(10, TEXT_COL_NAME, "name my");
    Assertions.assertEquals(1, results.size());
    checkContains(results, ID_COL_NAME, val -> val.getInt() == 7);
  }


  /**
   * This test verifies that a quoted query only retrieves results which contain the full query term
   */
  @Test
  @DisplayName("Text: One el (two words), quotes")
  void textRetrievalSingleTwoWordsQuotedLike() {
    selector.open(testTextTableName);
    List<Map<String, PrimitiveTypeProvider>> results = selector.getFulltextRows(10, TEXT_COL_NAME, "\"hello world\"");
    Assertions.assertEquals(2, results.size());
    checkContains(results, ID_COL_NAME, val -> val.getInt() == 1);
    checkContains(results, ID_COL_NAME, val -> val.getInt() == 7);
  }

  /**
   * Verifies that ~1 means levenshtein 1
   */
  @Test
  @DisplayName("Text: One el, one word, Fuzzy")
  void testRetrievalSingleFuzzy() {
    selector.open(testTextTableName);
    List<Map<String, PrimitiveTypeProvider>> results = selector.getFulltextRows(10, TEXT_COL_NAME, "hello~1");
    Assertions.assertEquals(4, results.size());
    checkContains(results, ID_COL_NAME, val -> val.getInt() == 1);
    checkContains(results, ID_COL_NAME, val -> val.getInt() == 2);
    checkContains(results, ID_COL_NAME, val -> val.getInt() == 5);
    checkContains(results, ID_COL_NAME, val -> val.getInt() == 7);
  }

  @Test
  @DisplayName("Text: One el (Two words), Fuzzy")
  void testRetrievalSingleTwoWordsFuzzy() {
    /*
     * Fuzzy search on whole phrases is currently not supported.
     * Something like "hello world"~1 would need to be implemented as either hello~1 AND world~1, but that is not implemented in the DBSelector / cottontail.
     * The cottontail implementation in december 19 parses hello world~1 as hello .. world~1, which is not what we're looking for
     * Therefore, this test serves as a note that this functionality is lacking.
     */
    return;
  }


  /**
   * Verify that searching for two terms retrieves both individual results
   */
  @Test
  @DisplayName("Text: Two elements w/ single word")
  void testRetrievalTwo() {
    selector.open(testTextTableName);
    List<Map<String, PrimitiveTypeProvider>> results = selector.getFulltextRows(10, TEXT_COL_NAME, "single", "double");
    Assertions.assertEquals(2, results.size());
    checkContains(results, ID_COL_NAME, val -> val.getInt() == 3);
    checkContains(results, ID_COL_NAME, val -> val.getInt() == 4);
  }

  @Test
  @DisplayName("Text: Three elements, two are a match for the same id")
  void testRetrievalThreeDouble() {
    selector.open(testTextTableName);
    List<Map<String, PrimitiveTypeProvider>> results = selector.getFulltextRows(10, TEXT_COL_NAME, "double", "single", "duplicate");
    Assertions.assertEquals(3, results.size());
    checkContains(results, ID_COL_NAME, val -> val.getInt() == 4);
    checkContains(results, ID_COL_NAME, val -> val.getInt() == 3);
    float score = results.get(0).get("ap_score").getFloat();
    Assertions.assertEquals(score, results.get(1).get("ap_score").getFloat(), 0.01);
    Assertions.assertEquals(score, results.get(2).get("ap_score").getFloat(), 0.01);
  }

  @Test
  @DisplayName("Text: Three els, one of those with quotes")
  void testRetrievalThree() {
    selector.open(testTextTableName);
    List<Map<String, PrimitiveTypeProvider>> results = selector.getFulltextRows(10, TEXT_COL_NAME, "single", "double", "\"hello world\"");
    Assertions.assertEquals(4, results.size());
    checkContains(results, ID_COL_NAME, val -> val.getInt() == 3);
    checkContains(results, ID_COL_NAME, val -> val.getInt() == 4);
    checkContains(results, ID_COL_NAME, val -> val.getInt() == 1);
    checkContains(results, ID_COL_NAME, val -> val.getInt() == 7);
  }

}
