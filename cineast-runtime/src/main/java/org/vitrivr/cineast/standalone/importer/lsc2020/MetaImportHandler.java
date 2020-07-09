package org.vitrivr.cineast.standalone.importer.lsc2020;

import com.opencsv.exceptions.CsvException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.data.entities.MediaSegmentMetadataDescriptor;
import org.vitrivr.cineast.core.db.setup.EntityCreator;
import org.vitrivr.cineast.standalone.config.Config;
import org.vitrivr.cineast.standalone.importer.handlers.DataImportHandler;

import java.io.IOException;
import java.nio.file.Path;

public class MetaImportHandler extends DataImportHandler {

    private static final Logger LOGGER = LogManager.getLogger(MetaImportHandler.class);

    private final boolean clean;

    public MetaImportHandler(int threads, int batchSize, boolean clean){
        super(threads, batchSize);
        this.clean= clean;
        final EntityCreator ec = Config.sharedConfig().getDatabase().getEntityCreatorSupplier().get();
        /* Beware, this drops the table */
        if (clean) {
            LOGGER.info("Dropping table ...");
            ec.dropSegmentMetadataEntity();
            LOGGER.info("Finished dropping table for entity ");
            ec.createSegmentMetadataEntity();
            LOGGER.info("Re-created SegmentMetaData entity");
            ec.close();
        }
    }

    @Override
    public void doImport(Path path) {
        try {
            LSCUtilities.create(path);
        } catch (IOException | CsvException e) {
            LOGGER.fatal("Error in initialisation", e);
            LOGGER.fatal("Crashing now");
            return;
        }
        LOGGER.info("Starting LSC metadata import from folder {}", path);
        this.futures.add(this.service.submit(new DataImportRunner(new MetaImporter(path), MediaSegmentMetadataDescriptor.ENTITY, "lsc-metadata", clean)));
    }
}
