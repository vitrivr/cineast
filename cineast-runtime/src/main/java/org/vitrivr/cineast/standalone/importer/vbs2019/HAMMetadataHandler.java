package org.vitrivr.cineast.standalone.importer.vbs2019;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;
import org.vitrivr.cineast.core.db.cottontaildb.CottontailEntityCreator;
import org.vitrivr.cineast.core.db.cottontaildb.CottontailSelector;
import org.vitrivr.cineast.core.db.setup.AttributeDefinition;
import org.vitrivr.cineast.core.db.setup.EntityCreator;
import org.vitrivr.cineast.core.db.setup.EntityDefinition;
import org.vitrivr.cineast.standalone.config.Config;
import org.vitrivr.cineast.standalone.importer.handlers.DataImportHandler;
import org.vitrivr.cottontail.grpc.CottontailGrpc;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HAMMetadataHandler extends DataImportHandler {


    private static final Logger LOGGER = LogManager.getLogger();
    private static final String TABLE_NAME = "him_table"; // Features is misleading, in future LSC / VBS use different one
    private static final EntityDefinition METADATA_TABLE = new EntityDefinition.EntityDefinitionBuilder(
            TABLE_NAME)
            .withAttributes(
                    new AttributeDefinition("lesion_id", AttributeDefinition.AttributeType.STRING,
                            createHintsForId()),
                    new AttributeDefinition("id", AttributeDefinition.AttributeType.STRING),
                    new AttributeDefinition("dx", AttributeDefinition.AttributeType.STRING),
                    new AttributeDefinition("dx_type", AttributeDefinition.AttributeType.STRING),
                    new AttributeDefinition("age", AttributeDefinition.AttributeType.STRING),
                    new AttributeDefinition("sex", AttributeDefinition.AttributeType.STRING),
                    new AttributeDefinition("localization", AttributeDefinition.AttributeType.STRING),
                    new AttributeDefinition("dataset", AttributeDefinition.AttributeType.STRING)
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
    public HAMMetadataHandler(int threads, int batchsize, boolean clean) {
        super(threads, batchsize);
        //metaAsTable = true;
        if (clean) {
            createEntityForced();
        }
    }

    /**
     * Constructor; creates a new DataImportHandler with specified number of threads and batchsize.
     *
     * @param threads   Number of threads to use for data import.
     * @param batchsize Size of data batches that are sent to the persistence layer.
     */
    public HAMMetadataHandler(int threads, int batchsize) {
        super(threads, batchsize);
    }


    public static void executeSimpleSelect() {
        String entity = "him_table";
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
            LOGGER.info("Starting data import for caption files in: {}", root.toString());
            try {
                    this.futures.add(this.service.submit(new DataImportHandler.DataImportRunner(new HAMMetadataImporter(root), "him_table", "metadata_new")));
            } catch (IOException e) {
                LOGGER.fatal("Failed to open path at {} ", root);
                throw new RuntimeException(e);
            }
            this.waitForCompletion();
            LOGGER.info("Completed data import with Metadata files in: {}", root.toString());
    }
    //executeSimpleSelect();
}

