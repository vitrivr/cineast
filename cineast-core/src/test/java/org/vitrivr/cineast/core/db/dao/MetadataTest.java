package org.vitrivr.cineast.core.db.dao;

import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.vitrivr.cineast.core.config.QueryConfig;
import org.vitrivr.cineast.core.config.ReadableQueryConfig.Distance;
import org.vitrivr.cineast.core.data.entities.MediaObjectMetadataDescriptor;
import org.vitrivr.cineast.core.data.entities.MediaSegmentMetadataDescriptor;
import org.vitrivr.cineast.core.db.DBSelector;
import org.vitrivr.cineast.core.db.IntegrationDBProvider;
import org.vitrivr.cineast.core.db.PersistencyWriter;
import org.vitrivr.cineast.core.db.dao.reader.MediaObjectMetadataReader;
import org.vitrivr.cineast.core.db.dao.reader.MediaSegmentMetadataReader;
import org.vitrivr.cineast.core.db.dao.writer.MediaObjectMetadataWriter;
import org.vitrivr.cineast.core.db.dao.writer.MediaSegmentMetadataWriter;
import org.vitrivr.cineast.core.db.setup.EntityCreator;
import org.vitrivr.cineast.core.util.CineastIOUtils;

@TestInstance(Lifecycle.PER_CLASS)
public abstract class MetadataTest<R> {

  private DBSelector objSelector;
  private DBSelector segSelector;
  private String testObjMetaTableName;
  private String testSegMetaTableName;
  private PersistencyWriter<R> objPersWriter;
  private PersistencyWriter<R> segPersWriter;
  private EntityCreator ec;
  private QueryConfig queryConfig;
  private MediaObjectMetadataWriter objWriter;
  private MediaSegmentMetadataWriter segWriter;
  private MediaObjectMetadataReader objReader;
  private MediaSegmentMetadataReader segReader;
  private static final int ELEMENT_COUNT = 8;
  private static final int METADATA_PER_ELEMENT_COUNT = 7;

  private static final Logger LOGGER = LogManager.getLogger();

  private IntegrationDBProvider<R> provider;

  @BeforeAll
  void initTest() {
    if (ELEMENT_COUNT % 2 != 0) {
      Assertions.fail("ELEMENT_COUNT must be an even number");
    }
    provider = provider();
    objSelector = provider.getSelector();
    segSelector = provider.getSelector();
    LOGGER.info("Trying to establish connection to Database");
    assumeTrue(objSelector.ping(), "Connection to database could not be established");
    assumeTrue(segSelector.ping(), "Connection to database could not be established");
    LOGGER.info("Connection to Database established");
    // write data
    testObjMetaTableName = getTestObjMetaTableName();
    testSegMetaTableName = getTestSegMetaTableName();
    objPersWriter = provider.getPersistencyWriter();
    segPersWriter = provider.getPersistencyWriter();
    objWriter = new MediaObjectMetadataWriter(objPersWriter, 1000, testObjMetaTableName);
    segWriter = new MediaSegmentMetadataWriter(segPersWriter, 1000, testSegMetaTableName);
    ec = provider.getEntityCreator();
    dropTables();
    createTables();
    fillMetadata();
    finishInitialSetup();
    CineastIOUtils.closeQuietly(objPersWriter, segPersWriter, objWriter, segWriter);
  }

  @BeforeEach
  void setupTest() {
    provider = provider();
    objSelector = provider.getSelector();
    segSelector = provider.getSelector();
    assumeTrue(objSelector.ping(), "Connection to database could not be established");
    assumeTrue(segSelector.ping(), "Connection to database could not be established");
    testObjMetaTableName = getTestObjMetaTableName();
    testSegMetaTableName = getTestSegMetaTableName();
    objReader = new MediaObjectMetadataReader(objSelector, testObjMetaTableName);
    segReader = new MediaSegmentMetadataReader(segSelector, testSegMetaTableName);
    queryConfig = getQueryConfig();
  }

  protected abstract void finishInitialSetup();

  protected QueryConfig getQueryConfig() {
    QueryConfig qc = new QueryConfig("test-" + RandomStringUtils.randomNumeric(4), new ArrayList<>());
    qc.setDistanceIfEmpty(Distance.euclidean);
    return qc;
  }

  protected abstract IntegrationDBProvider<R> provider();

  @AfterEach
  void tearDownTest() {
    CineastIOUtils.closeQuietly(objSelector, segSelector, objReader, segReader);
  }

  @AfterAll
  void finalTearDownTest() {
    dropTables();
    CineastIOUtils.closeQuietly(ec);
  }

  protected void fillMetadata() {
    for (int i = 0; i < ELEMENT_COUNT; i++) {
      objWriter.write(objMetadataForID(i));
      segWriter.write(segMetadataForID(i));
    }
    objWriter.flush();
    segWriter.flush();
  }

  protected void createTables() {
    ec.createMetadataEntity(testObjMetaTableName);
    ec.createSegmentMetadataEntity(testSegMetaTableName);
  }

  protected void dropTables() {
    if (ec != null) {
      if (ec.existsEntity(testObjMetaTableName)) {
        ec.dropEntity(testObjMetaTableName);
      } else {
        LOGGER.trace("not dropping {} because it does not exist", testObjMetaTableName);
      }
      if (ec.existsEntity(testSegMetaTableName)) {
        ec.dropEntity(testSegMetaTableName);
      } else {
        LOGGER.trace("not dropping {} because it does not exist", testSegMetaTableName);
      }
    }
  }

  protected String getTestObjMetaTableName() {
    return "test_metadata_object";
  }

  protected String getTestSegMetaTableName() {
    return "test_metadata_segment";
  }

  @Test
  @DisplayName("Ping the database")
  void ping() {
    Assertions.assertTrue(provider.getSelector().ping());
  }

  @Test
  @DisplayName("Verify entity creation")
  void entitiesExist() {
    Assertions.assertTrue(objSelector.existsEntity(testObjMetaTableName));
    Assertions.assertTrue(objSelector.existsEntity(testSegMetaTableName));
  }

  @Test
  @DisplayName("Verify element count")
  void count() {
    objSelector.open(testObjMetaTableName);
    Assertions.assertEquals(ELEMENT_COUNT * METADATA_PER_ELEMENT_COUNT + ELEMENT_COUNT, objSelector.getAll().size());
    objSelector.open(testSegMetaTableName);
    Assertions.assertEquals(ELEMENT_COUNT * METADATA_PER_ELEMENT_COUNT + ELEMENT_COUNT, objSelector.getAll().size());
  }

  List<MediaObjectMetadataDescriptor> objMetadataForID(int i) {
    List<MediaObjectMetadataDescriptor> descriptors = new ArrayList<>();
    for (int j = 0; j < METADATA_PER_ELEMENT_COUNT; j++) {
      descriptors.add(new MediaObjectMetadataDescriptor(String.valueOf(i), i < ELEMENT_COUNT / 2 ? "one" : "two", String.valueOf(j), j, true));
    }
    descriptors.add(new MediaObjectMetadataDescriptor(String.valueOf(i), i < ELEMENT_COUNT / 2 ? "one" : "two", "id", i, true));
    return descriptors;
  }

  List<MediaSegmentMetadataDescriptor> segMetadataForID(int i) {
    List<MediaSegmentMetadataDescriptor> descriptors = new ArrayList<>();
    for (int j = 0; j < METADATA_PER_ELEMENT_COUNT; j++) {
      descriptors.add(new MediaSegmentMetadataDescriptor(String.valueOf(i), i < ELEMENT_COUNT / 2 ? "one" : "two", String.valueOf(j), j, true));
    }
    descriptors.add(new MediaSegmentMetadataDescriptor(String.valueOf(i), i < ELEMENT_COUNT / 2 ? "one" : "two", "id", i, true));
    return descriptors;
  }

  /**
   * Retrieves all descriptors and compares them to the result of {@link #objMetadataForID(int)}
   */
  @Test
  @DisplayName("Simple Select")
  void existAll() {
    List<String> ids = new ArrayList<>();
    List<MediaObjectMetadataDescriptor> objDescriptors = new ArrayList<>();
    List<MediaSegmentMetadataDescriptor> segDescriptors = new ArrayList<>();
    for (int i = 0; i < ELEMENT_COUNT; i++) {
      String id = String.valueOf(i);
      ids.add(id);
      List<MediaObjectMetadataDescriptor> objDescriptor = objMetadataForID(i);
      objDescriptors.addAll(objDescriptor);
      List<MediaSegmentMetadataDescriptor> segDescriptor = segMetadataForID(i);
      segDescriptors.addAll(segDescriptor);
      Assertions.assertEquals(objDescriptor, objReader.lookupMultimediaMetadata(id));
      Assertions.assertEquals(segDescriptor, segReader.lookupMultimediaMetadata(id));
    }
    Assertions.assertEquals(objDescriptors, objReader.lookupMultimediaMetadata(ids));
    Assertions.assertEquals(segDescriptors, segReader.lookupMultimediaMetadata(ids));
  }

  @Test
  @DisplayName("Find by domain")
  void findByDomain() {
    MetadataAccessSpecification objSpec = new MetadataAccessSpecification(MetadataType.OBJECT, "one", "*");
    MetadataAccessSpecification segSpec = new MetadataAccessSpecification(MetadataType.SEGMENT, "one", "*");
    List<MediaObjectMetadataDescriptor> oneDomainObject = objReader.findBySpec(objSpec);
    List<MediaSegmentMetadataDescriptor> oneDomainSegment = segReader.findBySpec(segSpec);
    List<MediaObjectMetadataDescriptor> objDescriptors = new ArrayList<>();
    List<MediaSegmentMetadataDescriptor> segDescriptors = new ArrayList<>();
    for (int i = 0; i < ELEMENT_COUNT / 2; i++) {
      objDescriptors.addAll(objMetadataForID(i));
      segDescriptors.addAll(segMetadataForID(i));
    }
    Assertions.assertEquals(objDescriptors, oneDomainObject);
    Assertions.assertEquals(segDescriptors, oneDomainSegment);
  }

  @Test
  @DisplayName("Find by key")
  void findByKey() {
    int key = 5;
    MetadataAccessSpecification objSpec = new MetadataAccessSpecification(MetadataType.OBJECT, "*", String.valueOf(key));
    MetadataAccessSpecification segSpec = new MetadataAccessSpecification(MetadataType.SEGMENT, "*", String.valueOf(key));
    Set<MediaObjectMetadataDescriptor> oneDomainObject = new HashSet<>(objReader.findBySpec(objSpec));
    Set<MediaSegmentMetadataDescriptor> oneDomainSegment = new HashSet<>(segReader.findBySpec(segSpec));
    Set<MediaObjectMetadataDescriptor> objDescriptors = new HashSet<>();
    Set<MediaSegmentMetadataDescriptor> segDescriptors = new HashSet<>();
    for (int i = 0; i < ELEMENT_COUNT; i++) {
      objDescriptors.add(objMetadataForID(i).get(key));
      segDescriptors.add(segMetadataForID(i).get(key));
    }
    Assertions.assertEquals(objDescriptors, oneDomainObject);
    Assertions.assertEquals(segDescriptors, oneDomainSegment);
  }

  @Test
  @DisplayName("Find by multiple keys")
  void findByKeys() {
    int[] keys = new int[]{1, 5, 6};
    Set<MediaObjectMetadataDescriptor> objDescriptors = new HashSet<>();
    Set<MediaSegmentMetadataDescriptor> segDescriptors = new HashSet<>();
    List<MetadataAccessSpecification> objSpec = new ArrayList<>();
    List<MetadataAccessSpecification> segSpec = new ArrayList<>();
    for (int key : keys) {
      objSpec.add(new MetadataAccessSpecification(MetadataType.OBJECT, "*", String.valueOf(key)));
      segSpec.add(new MetadataAccessSpecification(MetadataType.SEGMENT, "*", String.valueOf(key)));
      for (int i = 0; i < ELEMENT_COUNT; i++) {
        objDescriptors.add(objMetadataForID(i).get(key));
        segDescriptors.add(segMetadataForID(i).get(key));
      }
    }
    Assertions.assertEquals(objDescriptors, new HashSet<>(objReader.findBySpec(objSpec)));
    Assertions.assertEquals(segDescriptors, new HashSet<>(segReader.findBySpec(segSpec)));
  }

  @Test
  @DisplayName("Find by multiple keys and domain")
  void findByKeysAndDomain() {
    int[] keys = new int[]{1, 5, 6};
    Set<MediaObjectMetadataDescriptor> objDescriptors = new HashSet<>();
    Set<MediaSegmentMetadataDescriptor> segDescriptors = new HashSet<>();
    List<MetadataAccessSpecification> objSpec = new ArrayList<>();
    List<MetadataAccessSpecification> segSpec = new ArrayList<>();
    for (int key : keys) {
      objSpec.add(new MetadataAccessSpecification(MetadataType.OBJECT, "one", String.valueOf(key)));
      segSpec.add(new MetadataAccessSpecification(MetadataType.SEGMENT, "one", String.valueOf(key)));
      for (int i = 0; i < ELEMENT_COUNT/2; i++) {
        objDescriptors.add(objMetadataForID(i).get(key));
        segDescriptors.add(segMetadataForID(i).get(key));
      }
    }
    Assertions.assertEquals(objDescriptors, new HashSet<>(objReader.findBySpec(objSpec)));
    Assertions.assertEquals(segDescriptors, new HashSet<>(segReader.findBySpec(segSpec)));
  }

  @Test
  @DisplayName("Find by key and single id")
  void findByKeyAndId() {
    int key = 5;
    int id = 1;
    MetadataAccessSpecification objSpec = new MetadataAccessSpecification(MetadataType.OBJECT, "*", String.valueOf(key));
    MetadataAccessSpecification segSpec = new MetadataAccessSpecification(MetadataType.SEGMENT, "*", String.valueOf(key));
    Assertions.assertEquals(objMetadataForID(id).get(key), objReader.findBySpec(String.valueOf(id), objSpec).get(0));
    Assertions.assertEquals(segMetadataForID(id).get(key), segReader.findBySpec(String.valueOf(id), segSpec).get(0));
  }

  @Test
  @DisplayName("Find all by spec")
  void findAllBySpec() {
    List<String> ids = new ArrayList<>();
    List<MediaObjectMetadataDescriptor> objDescriptors = new ArrayList<>();
    List<MediaSegmentMetadataDescriptor> segDescriptors = new ArrayList<>();
    MetadataAccessSpecification objSpec = new MetadataAccessSpecification(MetadataType.OBJECT, "*", "*");
    MetadataAccessSpecification segSpec = new MetadataAccessSpecification(MetadataType.SEGMENT, "*", "*");
    for (int i = 0; i < ELEMENT_COUNT; i++) {
      String id = String.valueOf(i);
      ids.add(id);
      List<MediaObjectMetadataDescriptor> objDescriptor = objMetadataForID(i);
      objDescriptors.addAll(objDescriptor);
      List<MediaSegmentMetadataDescriptor> segDescriptor = segMetadataForID(i);
      segDescriptors.addAll(segDescriptor);
    }
    Assertions.assertEquals(objDescriptors, objReader.findBySpec(ids, objSpec));
    Assertions.assertEquals(segDescriptors, segReader.findBySpec(ids, segSpec));
  }


}
