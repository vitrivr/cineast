package org.vitrivr.cineast.standalone.importer.lsc2020;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.features.DescriptionTextSearch;
import org.vitrivr.cineast.standalone.importer.handlers.DataImportHandler;

import java.nio.file.Path;

public class CaptionImportHandler extends DataImportHandler {

    private static final Logger LOGGER = LogManager.getLogger(CaptionImportHandler.class);

    /**
     * Constructor; creates a new DataImportHandler with specified number of threads and batchsize.
     *
     * @param threads   Number of threads to use for data import.
     * @param batchsize Size of data batches that are sent to the persistence layer.
     */
    public CaptionImportHandler(int threads, int batchsize) {
        super(threads, batchsize);
    }

    @Override
    public void doImport(Path path) {
        LOGGER.info("Starting caption import");
        this.futures.add(this.service.submit(new DataImportRunner(new CaptionImporter(path), DescriptionTextSearch.DESCRIPTION_TEXT_TABLE_NAME, "lsc-caption")));
    }
}
