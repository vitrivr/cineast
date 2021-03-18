package org.vitrivr.cineast.core.db;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.vitrivr.cineast.core.config.QueryConfig;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;
import org.vitrivr.cineast.core.db.setup.AttributeDefinition;
import org.vitrivr.cineast.core.db.setup.AttributeDefinition.AttributeType;
import org.vitrivr.cineast.core.db.setup.EntityCreator;

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

  private String getTestTableName() {
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
     int idToCheck = TABLE_CARD - 1;
     List<Map<String, PrimitiveTypeProvider>> result = selector.getFulltextRows(1, DATA_COL_NAME_1, queryConfig, "string-data-" + idToCheck);
     Assertions.assertEquals(result.get(0).get(DATA_COL_NAME_1).getString(), "string-data-" + idToCheck);
     Assertions.assertEquals(result.get(0).get(DATA_COL_NAME_2).getInt(), -idToCheck);
     Assertions.assertEquals(result.get(0).get(DATA_COL_NAME_3).getInt(), -idToCheck + TABLE_CARD);
  }

  @Test
  @DisplayName("test IN() query")
  void testInQuery() {
    selector.open(testTableName);
    int idToCheck = TABLE_CARD - 1;
    List<PrimitiveTypeProvider> values = new ArrayList<>();
    values.add(PrimitiveTypeProvider.fromObject(-idToCheck));
    values.add(PrimitiveTypeProvider.fromObject(-(idToCheck - 1)));
    List<Map<String, PrimitiveTypeProvider>> result = selector.getRows(DATA_COL_NAME_2, values);
    MatcherAssert.assertThat(result.stream().map(el -> el.get(ID_COL_NAME).getString()).collect(Collectors.toList()), hasItem(String.valueOf(idToCheck)));
    MatcherAssert.assertThat(result.stream().map(el -> el.get(ID_COL_NAME).getString()).collect(Collectors.toList()), hasItem(String.valueOf(idToCheck-1)));
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

}
