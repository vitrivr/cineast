package org.vitrivr.cineast.standalone.importer.vbs2019;

import com.opencsv.exceptions.CsvException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.data.entities.MediaObjectMetadataDescriptor;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;
import org.vitrivr.cineast.core.db.DBSelector;
import org.vitrivr.cineast.core.db.DBSelectorSupplier;
import org.vitrivr.cineast.core.db.cottontaildb.CottontailEntityCreator;
import org.vitrivr.cineast.core.db.cottontaildb.CottontailSelector;
import org.vitrivr.cineast.core.db.dao.reader.TagReader;
import org.vitrivr.cineast.core.db.setup.AttributeDefinition;
import org.vitrivr.cineast.core.db.setup.EntityCreator;
import org.vitrivr.cineast.core.db.setup.EntityDefinition;
import org.vitrivr.cineast.core.features.SegmentTags;
import org.vitrivr.cineast.core.util.LogHelper;
import org.vitrivr.cineast.standalone.config.Config;
import org.vitrivr.cineast.standalone.importer.handlers.DataImportHandler;
import org.vitrivr.cineast.standalone.importer.lsc2020.LSCUtilities;
import org.vitrivr.cineast.standalone.importer.lsc2020.ProcessingMetaImportHandler;
import org.vitrivr.cineast.standalone.importer.lsc2020.ProcessingMetaImporter;
import org.vitrivr.cottontail.grpc.CottontailGrpc;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 Used for the new boolean model
 */



public class BooleanDataHandler extends DataImportHandler {


    private static final Logger LOGGER = LogManager.getLogger();
    private static final String TABLE_NAME = "test_table"; // Features is misleading, in future LSC / VBS use different one
    private static final EntityDefinition METADATA_TABLE = new EntityDefinition.EntityDefinitionBuilder(
            TABLE_NAME)
            .withAttributes(
                    new AttributeDefinition("id", AttributeDefinition.AttributeType.STRING,
                            createHintsForId()),
                    new AttributeDefinition("domain", AttributeDefinition.AttributeType.STRING),
                    new AttributeDefinition("key", AttributeDefinition.AttributeType.INT),
                    new AttributeDefinition("value", AttributeDefinition.AttributeType.INT)
            )
            .build();
    private final boolean metaAsTable = true;

    /**
     * Constructor; creates a new DataImportHandler with specified number of threads and batchsize.
     *
     * @param threads   Number of threads to use for data import.
     * @param batchsize Size of data batches that are sent to the persistence layer.
     * @param clean     Whether to clean the data before import. has no impact on tag import
     */
    public BooleanDataHandler(int threads, int batchsize, boolean clean) {
        super(threads, batchsize);
            //metaAsTable = true;
        if(clean){
            createEntityForced();
        }
    }

    /**
     * Constructor; creates a new DataImportHandler with specified number of threads and batchsize.
     *
     * @param threads   Number of threads to use for data import.
     * @param batchsize Size of data batches that are sent to the persistence layer.
     */
    public BooleanDataHandler(int threads, int batchsize) {
        super(threads, batchsize);
    }


    public static void executeSimpleSelect() {
        String entity = "test_table";
        final CottontailSelector selector = (CottontailSelector) Config.sharedConfig().getDatabase().getSelectorSupplier().get();
        selector.open(entity);
        System.out.println(selector.existsEntity(entity));
        List<PrimitiveTypeProvider> z = selector.getAll("id");
        System.out.println(selector.getAll("key"));
        System.out.println("hello");
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
    public void doImport(Path root) {
        try {
            LOGGER.info("Starting data import for caption files in: {}", root.toString());
            Files.walk(root, 2).filter(p -> p.toString().toLowerCase().endsWith(".json")).forEach(p -> {
                try {
                    this.futures.add(this.service.submit(new DataImportRunner(new BooleanData(p), "test_table", "metadata_new")));
                } catch (IOException e) {
                    LOGGER.fatal("Failed to open path at {} ", p);
                    throw new RuntimeException(e);
                }
            });
            this.waitForCompletion();
            LOGGER.info("Completed data import with Metadata files in: {}", root.toString());
        } catch (IOException e) {
            LOGGER.error("Could not start data import process with path '{}' due to an IOException: {}. Aborting...", root.toString(), LogHelper.getStackTrace(e));
        }
        executeSimpleSelect();
    }


}
