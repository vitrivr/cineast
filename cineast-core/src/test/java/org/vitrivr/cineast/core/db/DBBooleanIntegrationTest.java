package org.vitrivr.cineast.core.db;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.vitrivr.cineast.core.config.QueryConfig;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;
import org.vitrivr.cineast.core.db.cottontaildb.CottontailBooleanIntegrationTest;
import org.vitrivr.cineast.core.db.setup.AttributeDefinition;
import org.vitrivr.cineast.core.db.setup.AttributeDefinition.AttributeType;
import org.vitrivr.cineast.core.db.setup.EntityCreator;
import org.vitrivr.cineast.core.util.CineastIOUtils;

/**
 * Creates a table in cottontail with {@link #TABLE_CARD} columns of layout:
 * <p>
 * id | data_1_string_data_id | data_2_id_negative | data_3_id_plus_card
 * <p>
 * example columns for cardinality 20:
 * <br>
 * 1 | "1" | -1 | 21
 * <br>
 * 2 | "2" | -2 | 22
 * <p>
 * A DBSelector which would like to be tested simply needs to extend this class. Have a look at {@link CottontailBooleanIntegrationTest} for an example.
 *
 * @param <R> Type of the {@link PersistencyWriter} of the underlying DB
 */
@TestInstance(Lifecycle.PER_CLASS)
public abstract class DBBooleanIntegrationTest<R> {

  private static final int TABLE_CARD = 20;
  private DBSelector selector;
  private String testTableName;
  private PersistencyWriter<R> writer;
  private EntityCreator ec;
  private QueryConfig queryConfig;

  private static final String ID_COL_NAME = "id";
  private static final String DATA_COL_NAME_1 = "data_1_string_data_id";
  private static final String DATA_COL_NAME_2 = "data_2_id_negative";
  private static final String DATA_COL_NAME_3 = "data_3_id_plus_card";

  private IntegrationDBProvider<R> provider;

  private static final Logger LOGGER = LogManager.getLogger();

  @BeforeAll
  void checkConnection() {
    provider = provider();
    selector = provider.getSelector();
    LOGGER.info("Trying to establish connection to Database");
    assumeTrue(selector.ping(), "Connection to database could not be established");
    LOGGER.info("Connection to Database established");
  }

  @BeforeEach
  void setupTest() {
    provider = provider();
    selector = provider.getSelector();
    assumeTrue(selector.ping(), "Connection to database could not be established");
    writer = provider.getPersistencyWriter();
    ec = provider.getEntityCreator();
    testTableName = getTestTableName();
    queryConfig = getQueryConfig();
    dropTables();
    createTables();
    fillData();
    finishSetup();
  }

  @AfterEach
  void tearDownTest() {
    CineastIOUtils.closeQuietly(writer, selector);
    dropTables();
    CineastIOUtils.closeQuietly(ec);
  }

  protected String getTestTableName() {
    return "test_boolean_table";
  }

  protected abstract void finishSetup();

  protected abstract IntegrationDBProvider<R> provider();

  protected QueryConfig getQueryConfig() {
    QueryConfig qc = new QueryConfig("test-" + RandomStringUtils.randomNumeric(4), new ArrayList<>());
    return qc;
  }

  protected void dropTables() {
    if (ec != null) {
      if (ec.existsEntity(testTableName)) {
        ec.dropEntity(testTableName);
      } else {
        LOGGER.trace("not dropping {} because it does not exist", testTableName);
      }
    }
  }

  /**
   * Create table for boolean retrieval
   */
  protected void createTables() {
    // TODO Text retrieval fails on windows
    ec.createEntity(testTableName, new AttributeDefinition(ID_COL_NAME, AttributeType.INT), new AttributeDefinition(DATA_COL_NAME_1, AttributeType.TEXT), new AttributeDefinition(DATA_COL_NAME_2, AttributeType.INT), new AttributeDefinition(DATA_COL_NAME_3, AttributeType.INT));
  }

  protected void fillData() {
    writer.open(testTableName);
    writer.setFieldNames(ID_COL_NAME, DATA_COL_NAME_1, DATA_COL_NAME_2, DATA_COL_NAME_3);
    List<PersistentTuple> vectors = new ArrayList<>();

    for (int i = 0; i < TABLE_CARD; i++) {
      vectors.add(writer.generateTuple(i, "string-data-" + i, -i, i + TABLE_CARD));
      //vectors.add(writer.generateTuple(i, i, -i, i + TABLE_CARD));
    }
    writer.persist(vectors);
  }

  @Test
  @DisplayName("Ping the database")
  void ping() {
    Assertions.assertTrue(provider.getSelector().ping());
  }

  @Test
  @DisplayName("Verify entity creation")
  void entitiesExist() {
    Assertions.assertTrue(selector.existsEntity(testTableName));
  }

  @Test
  @DisplayName("Verify element count")
  void count() {
    selector.open(testTableName);
    Assertions.assertEquals(TABLE_CARD, selector.getAll().size());
    Assertions.assertEquals(TABLE_CARD, selector.getAll(DATA_COL_NAME_1).size());
  }

  @Test
  @DisplayName("Verify elements exist by id")
  void entriesExistById() {
    writer.open(testTableName);
    for (int i = 0; i < TABLE_CARD; i++) {
      Assertions.assertTrue(writer.idExists(String.valueOf(i)));
    }
  }

  @Test
  @DisplayName("Verify unique value count")
  void uniqueValueCountCorrect() {
    selector.open(testTableName);
    Assertions.assertEquals(TABLE_CARD, selector.getUniqueValues(ID_COL_NAME).size());
    Assertions.assertEquals(TABLE_CARD, selector.getUniqueValues(DATA_COL_NAME_1).size());
    Assertions.assertEquals(TABLE_CARD, selector.getUniqueValues(DATA_COL_NAME_2).size());
    Assertions.assertEquals(TABLE_CARD, selector.getUniqueValues(DATA_COL_NAME_3).size());
  }

  @Test
  @DisplayName("test fulltext query")
  void testFulltextQuery() {
    selector.open(testTableName);
    // test latest entry
    int idToCheck = TABLE_CARD - 1;
    List<Map<String, PrimitiveTypeProvider>> result = selector.getFulltextRows(1, DATA_COL_NAME_1, queryConfig, "string-data-" + idToCheck);
    Assertions.assertEquals(result.get(0).get(DATA_COL_NAME_1).getString(), "string-data-" + idToCheck);
    Assertions.assertEquals(result.get(0).get(DATA_COL_NAME_2).getInt(), -idToCheck);
    Assertions.assertEquals(result.get(0).get(DATA_COL_NAME_3).getInt(), (idToCheck + TABLE_CARD));
  }

  @Test
  @DisplayName("test IN() query")
  void testInQuery() {
    selector.open(testTableName);
    // test latest entry
    int idToCheck = TABLE_CARD - 1;
    List<PrimitiveTypeProvider> values = new ArrayList<>();
    // check column two (e.g. -18 and -19 if cardinality is 20)
    values.add(PrimitiveTypeProvider.fromObject(-idToCheck));
    values.add(PrimitiveTypeProvider.fromObject(-(idToCheck - 1)));
    List<Map<String, PrimitiveTypeProvider>> result = selector.getRows(DATA_COL_NAME_2, values);
    MatcherAssert.assertThat(result.stream().map(el -> el.get(ID_COL_NAME).getString()).collect(Collectors.toList()), hasItem(String.valueOf(idToCheck)));
    MatcherAssert.assertThat(result.stream().map(el -> el.get(ID_COL_NAME).getString()).collect(Collectors.toList()), hasItem(String.valueOf(idToCheck - 1)));
  }

  @Test
  @DisplayName("test BETWEEN() query")
  void testBetweenQuery() {
    selector.open(testTableName);
    int idToCheck = TABLE_CARD - 1;
    List<PrimitiveTypeProvider> values = new ArrayList<>();
    values.add(PrimitiveTypeProvider.fromObject(-idToCheck));
    values.add(PrimitiveTypeProvider.fromObject(-(idToCheck - 1)));
    List<Map<String, PrimitiveTypeProvider>> result = selector.getRows(DATA_COL_NAME_2, RelationalOperator.BETWEEN, values);
    MatcherAssert.assertThat(result.stream().map(el -> el.get(ID_COL_NAME).getString()).collect(Collectors.toList()), hasItem(String.valueOf(idToCheck)));
    MatcherAssert.assertThat(result.stream().map(el -> el.get(ID_COL_NAME).getString()).collect(Collectors.toList()), hasItem(String.valueOf(idToCheck - 1)));
  }

  @Test
  @DisplayName("test Greater() query")
  void testGreaterQuery() {
    selector.open(testTableName);
    // query for greater than second-highest value
    int idToCheck = TABLE_CARD - 2;
    // cardinality should be higher than 2
    if (idToCheck < -1) {
      Assertions.fail();
    }
    List<PrimitiveTypeProvider> values = new ArrayList<>();
    values.add(PrimitiveTypeProvider.fromObject(idToCheck));
    List<Map<String, PrimitiveTypeProvider>> result = selector.getRows(ID_COL_NAME, RelationalOperator.GREATER, values);
    MatcherAssert.assertThat(result.stream().map(el -> el.get(ID_COL_NAME).getString()).collect(Collectors.toList()), hasItem(String.valueOf(TABLE_CARD - 1)));
  }

  @Test
  @DisplayName("test Less() query")
  void testLessQuery() {
    selector.open(testTableName);
    int idToCheck = 1;
    List<PrimitiveTypeProvider> values = new ArrayList<>();
    values.add(PrimitiveTypeProvider.fromObject(idToCheck));
    List<Map<String, PrimitiveTypeProvider>> result = selector.getRows(ID_COL_NAME, RelationalOperator.LESS, values);
    MatcherAssert.assertThat(result.stream().map(el -> el.get(ID_COL_NAME).getString()).collect(Collectors.toList()), hasItem(String.valueOf(0)));
  }


  @Test
  @DisplayName("test BETWEEN() AND BETWEEN() query")
  void testBetweenANDBetweenQuery() {
    selector.open(testTableName);
    int idToCheck = TABLE_CARD / 2;

    //The result range is between x and x+1
    List<PrimitiveTypeProvider> values1 = new ArrayList<>();
    values1.add(PrimitiveTypeProvider.fromObject(idToCheck));
    values1.add(PrimitiveTypeProvider.fromObject(idToCheck + 1));
    Triple<String, RelationalOperator, List<PrimitiveTypeProvider>> element1 = new ImmutableTriple<>(ID_COL_NAME, RelationalOperator.BETWEEN, values1);

    //The result range is between x+1 and x+2
    List<PrimitiveTypeProvider> values2 = new ArrayList<>();
    values2.add(PrimitiveTypeProvider.fromObject(idToCheck + 1));
    values2.add(PrimitiveTypeProvider.fromObject(idToCheck + 2));
    Triple<String, RelationalOperator, List<PrimitiveTypeProvider>> element2 = new ImmutableTriple<>(ID_COL_NAME, RelationalOperator.BETWEEN, values2);

    List<Map<String, PrimitiveTypeProvider>> result = selector.getRowsAND(asList(element1, element2), ID_COL_NAME, asList(ID_COL_NAME), null);

    MatcherAssert.assertThat(result.stream().map(el -> el.get(ID_COL_NAME).getString()).collect(Collectors.toList()), hasItem(String.valueOf(idToCheck + 1)));
    Assertions.assertEquals(1, result.size());
  }


  @Test
  @DisplayName("test BETWEEN() AND IN() query")
  void testBetweenANDInQuery() {
    selector.open(testTableName);
    int idToCheck = TABLE_CARD / 2;

    //The result is between x-2 and x+2
    List<PrimitiveTypeProvider> values1 = new ArrayList<>();
    values1.add(PrimitiveTypeProvider.fromObject(idToCheck + 2));
    values1.add(PrimitiveTypeProvider.fromObject(idToCheck - 2));
    Triple<String, RelationalOperator, List<PrimitiveTypeProvider>> element1 = new ImmutableTriple<>(ID_COL_NAME, RelationalOperator.BETWEEN, values1);

    //The result range is either x+1 or x-1
    List<PrimitiveTypeProvider> values2 = new ArrayList<>();
    values2.add(PrimitiveTypeProvider.fromObject(idToCheck + 1));
    values2.add(PrimitiveTypeProvider.fromObject(idToCheck - 1));
    Triple<String, RelationalOperator, List<PrimitiveTypeProvider>> element2 = new ImmutableTriple<>(ID_COL_NAME, RelationalOperator.IN, values2);

    List<Map<String, PrimitiveTypeProvider>> result = selector.getRowsAND(asList(element1, element2), ID_COL_NAME, asList(ID_COL_NAME), null);

    MatcherAssert.assertThat(result.stream().map(el -> el.get(ID_COL_NAME).getString()).collect(Collectors.toList()), hasItem(String.valueOf(idToCheck - 1)));
    MatcherAssert.assertThat(result.stream().map(el -> el.get(ID_COL_NAME).getString()).collect(Collectors.toList()), hasItem(String.valueOf(idToCheck + 1)));
    Assertions.assertEquals(2, result.size());
  }

}
