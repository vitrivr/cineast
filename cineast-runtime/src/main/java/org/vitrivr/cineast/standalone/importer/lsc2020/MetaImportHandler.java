package org.vitrivr.cineast.standalone.importer.lsc2020;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.data.entities.MediaSegmentMetadataDescriptor;
import org.vitrivr.cineast.standalone.importer.handlers.DataImportHandler;

import java.nio.file.Path;

public class MetaImportHandler extends DataImportHandler {

    private static final Logger LOGGER = LogManager.getLogger(MetaImportHandler.class);

    private final boolean clean;

    public MetaImportHandler(int threads, int batchSize, boolean clean){
        super(threads, batchSize);
        this.clean= clean;
    }

    @Override
    public void doImport(Path path) {
        LOGGER.info("Starting LSC metadata import from folder {}", path);
        this.futures.add(this.service.submit(new DataImportRunner(new MetaImporter(path), MediaSegmentMetadataDescriptor.ENTITY, "lsc-metadata", clean)));
    }
}
