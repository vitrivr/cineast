package org.vitrivr.cineast.core.db;

import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.vitrivr.cineast.core.config.QueryConfig;
import org.vitrivr.cineast.core.config.ReadableQueryConfig.Distance;
import org.vitrivr.cineast.core.data.distance.SegmentDistanceElement;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;
import org.vitrivr.cineast.core.db.setup.AttributeDefinition;
import org.vitrivr.cineast.core.db.setup.AttributeDefinition.AttributeType;
import org.vitrivr.cineast.core.db.setup.EntityCreator;

public abstract class DBSelectorTest<R> {

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
    writer.close();
    selector.close();
    dropTables();
    ec.close();
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
    writer.persist(vectors);
  }

  protected abstract PersistencyWriter<R> getPersistencyWriter();

  protected void createTables() {
    ec.createEntity(testTextTableName, new AttributeDefinition(ID_COL_NAME, AttributeType.INT), new AttributeDefinition(TEXT_COL_NAME, AttributeType.TEXT));
    ec.createEntity(testVectorTableName, new AttributeDefinition(ID_COL_NAME, AttributeType.LONG), new AttributeDefinition(FEATURE_VECTOR_COL_NAME, AttributeType.VECTOR, 3));
  }

  protected void dropTables() {
    ec.dropEntity(testTextTableName);
    ec.dropEntity(testVectorTableName);
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
    Assertions.assertEquals(10, selector.getAll().size());
    selector.open(testTextTableName);
    Assertions.assertEquals(7, selector.getAll().size());
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

  @Test
  @DisplayName("Text: One el, LIKE")
  void textRetrievalSingleLike() {
    selector.open(testTextTableName);
    List<Map<String, PrimitiveTypeProvider>> results = selector.getFulltextRows(10, TEXT_COL_NAME, "hello");
    Assertions.assertEquals(3, results.size());
    checkContains(results, ID_COL_NAME, val -> val.getInt() == 1);
    checkContains(results, ID_COL_NAME, val -> val.getInt() == 5);
    checkContains(results, ID_COL_NAME, val -> val.getInt() == 7);
  }

  @Test
  @DisplayName("Text: One el (two words), LIKE")
  void textRetrievalSingleTwoWordsLike() {
    selector.open(testTextTableName);
    List<Map<String, PrimitiveTypeProvider>> results = selector.getFulltextRows(10, TEXT_COL_NAME, "\"hello world\"");
    Assertions.assertEquals(2, results.size());
    checkContains(results, ID_COL_NAME, val -> val.getInt() == 1);
    checkContains(results, ID_COL_NAME, val -> val.getInt() == 7);
  }

  @Test
  @DisplayName("Text: One el, Fuzzy")
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


  @Test
  @DisplayName("Text: Two els")
  void testRetrievalTwo() {
    selector.open(testTextTableName);
    List<Map<String, PrimitiveTypeProvider>> results = selector.getFulltextRows(10, TEXT_COL_NAME, "single", "double");
    Assertions.assertEquals(2, results.size());
    checkContains(results, ID_COL_NAME, val -> val.getInt() == 3);
    checkContains(results, ID_COL_NAME, val -> val.getInt() == 4);
  }

  @Test
  @DisplayName("Text: Three els")
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
