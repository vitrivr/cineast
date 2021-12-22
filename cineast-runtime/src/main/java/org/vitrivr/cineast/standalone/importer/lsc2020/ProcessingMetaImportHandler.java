package org.vitrivr.cineast.standalone.importer.lsc2020;

import com.opencsv.exceptions.CsvException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.db.cottontaildb.CottontailEntityCreator;
import org.vitrivr.cineast.core.db.dao.reader.TagReader;
import org.vitrivr.cineast.core.db.setup.AttributeDefinition;
import org.vitrivr.cineast.core.db.setup.EntityCreator;
import org.vitrivr.cineast.core.db.setup.EntityDefinition;
import org.vitrivr.cineast.core.features.SegmentTags;
import org.vitrivr.cineast.standalone.config.Config;
import org.vitrivr.cineast.standalone.importer.handlers.DataImportHandler;
import org.vitrivr.cottontail.grpc.CottontailGrpc;

public class ProcessingMetaImportHandler extends DataImportHandler {

  private static final Logger LOGGER = LogManager.getLogger();
  private static final String TABLE_NAME = "features_table_lsc20meta"; // Features is misleading, in future LSC / VBS use different one
  private static final EntityDefinition METADATA_TABLE = new EntityDefinition.EntityDefinitionBuilder(
      TABLE_NAME)
      .withAttributes(
          new AttributeDefinition("id", AttributeDefinition.AttributeType.STRING,
              createHintsForId()),
          new AttributeDefinition("minute_id", AttributeDefinition.AttributeType.STRING),
          new AttributeDefinition("utc_time", AttributeDefinition.AttributeType.STRING),
          new AttributeDefinition("local_time", AttributeDefinition.AttributeType.STRING),
          new AttributeDefinition("timezone", AttributeDefinition.AttributeType.STRING),
          new AttributeDefinition("lat", AttributeDefinition.AttributeType.DOUBLE),
          new AttributeDefinition("lon", AttributeDefinition.AttributeType.DOUBLE),
          new AttributeDefinition("semantic_name", AttributeDefinition.AttributeType.STRING),
          new AttributeDefinition("elevation", AttributeDefinition.AttributeType.STRING),
          new AttributeDefinition("speed", AttributeDefinition.AttributeType.STRING),
          new AttributeDefinition("heart", AttributeDefinition.AttributeType.STRING),
          new AttributeDefinition("calories", AttributeDefinition.AttributeType.STRING),
          new AttributeDefinition("activity_type", AttributeDefinition.AttributeType.STRING),
          new AttributeDefinition("steps", AttributeDefinition.AttributeType.STRING),
          new AttributeDefinition("p_utc_standard", AttributeDefinition.AttributeType.STRING),
          new AttributeDefinition("p_local_standard", AttributeDefinition.AttributeType.STRING),
          new AttributeDefinition("p_day_of_week", AttributeDefinition.AttributeType.STRING),
          new AttributeDefinition("p_phase_of_day", AttributeDefinition.AttributeType.STRING),
          new AttributeDefinition("p_year", AttributeDefinition.AttributeType.INT),
          new AttributeDefinition("p_month", AttributeDefinition.AttributeType.STRING),
          new AttributeDefinition("p_hour", AttributeDefinition.AttributeType.INT),
          new AttributeDefinition("p_datetime", AttributeDefinition.AttributeType.STRING),
          new AttributeDefinition("p_day", AttributeDefinition.AttributeType.INT)
      )
      .build();
  private final boolean metaAsTable;

  /**
   * Constructor; creates a new DataImportHandler with specified number of threads and batchsize.
   *
   * @param threads   Number of threads to use for data import.
   * @param batchsize Size of data batches that are sent to the persistence layer.
   * @param mode      The mode of the import
   * @param clean     Whether to clean the data before import. has no impact on tag import
   */
  public ProcessingMetaImportHandler(int threads, int batchsize, Mode mode, boolean clean) {
    super(threads, batchsize);
    if (mode == Mode.TABLE) {
      metaAsTable = true;
    } else {
      metaAsTable = false;
    }
    if (clean) {
      createEntityForced();
    }
  }

  private static Map<String, String> createHintsForId() {
    Map<String, String> map = new HashMap<>();
    map.put(CottontailEntityCreator.INDEX_HINT, CottontailGrpc.IndexType.HASH.name());
    return map;
  }

  public static void createEntityForced() {
    createEntity(true);
  }

  public static void createEntity(boolean deleteBeforeCreate) {
    final EntityCreator ec = Config.sharedConfig().getDatabase().getEntityCreatorSupplier().get();
    if (ec != null) {
      if (deleteBeforeCreate && ec.existsEntity(TABLE_NAME)) {
        ec.dropEntity(TABLE_NAME);
      }
      ec.createEntity(METADATA_TABLE);
    }
  }

  @Override
  public void doImport(Path path) {
    LOGGER.info("Starting " + (metaAsTable ? "meta-as-table" : "meta-as-tag") + " import in {}",
        path);
    try {
      LSCUtilities.create(path);
      LSCUtilities.getInstance().initMetadata();
    } catch (IOException | CsvException e) {
      LOGGER.error("Cannot do import as initialization failed.", e);
      return;
    }
    if (metaAsTable) {
      this.futures.add(this.service.submit(new DataImportRunner(
          new ProcessingMetaImporter(path, ProcessingMetaImporter.Type.META_AS_TABLE),
          "features_table_lsc20meta", "lsc-metaAsTable")));
    } else {
      this.futures.add(this.service.submit(new DataImportRunner(
          new ProcessingMetaImporter(path, ProcessingMetaImporter.Type.TAG_LOOKUP),
          TagReader.TAG_ENTITY_NAME, "lsc-metaAsTagsLookup")));
      this.futures.add(this.service.submit(
          new DataImportRunner(new ProcessingMetaImporter(path, ProcessingMetaImporter.Type.TAG),
              SegmentTags.SEGMENT_TAGS_TABLE_NAME, "lsc-metaAsTags")));
    }
  }

  /**
   * The mode specifies how the {@link ProcessingMetaImporter} actually imports the data
   */
  public enum Mode {
    /**
     * Required for boolean retrieval on the data, it stores the data in a -dedicated- entity
     */
    TABLE,
    /**
     * Imports tags
     */
    TAGS
  }
}
