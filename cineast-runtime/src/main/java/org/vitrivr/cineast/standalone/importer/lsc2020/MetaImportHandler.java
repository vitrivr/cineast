package org.vitrivr.cineast.standalone.importer.lsc2020;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.data.entities.MediaObjectMetadataDescriptor;
import org.vitrivr.cineast.standalone.importer.handlers.DataImportHandler;

import java.nio.file.Path;

public class MetaImportHandler extends DataImportHandler {

    private static final Logger LOGGER = LogManager.getLogger(MetaImportHandler.class);

    public MetaImportHandler(int threads, int batchSize){
        super(threads, batchSize);
    }

    @Override
    public void doImport(Path path) {
        LOGGER.info("Starting LSC metadata import from folder {}", path);
        this.futures.add(this.service.submit(new DataImportRunner(new MetaImporter(path), MediaObjectMetadataDescriptor.ENTITY, "lsc-metadata")));
    }
}
