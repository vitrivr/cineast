package org.vitrivr.cineast.core.importer.handlers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.importer.VbsMetaImporter;
import org.vitrivr.cineast.core.util.LogHelper;

import java.io.IOException;
import java.nio.file.Path;

public class VbsMetaDataImportHandler extends DataImportHandler {
    /** */
    private static final Logger LOGGER = LogManager.getLogger();

    /**
     *
     * @param threads
     * @param batchsize
     */
    public VbsMetaDataImportHandler(int threads, int batchsize) {
        super(threads, batchsize);
    }

    /**
     *
     * @param path
     */
    @Override
    public void doImport(Path path) {
        try {
            this.futures.add(this.service.submit(new DataImportHandler.DataImportRunner(new VbsMetaImporter(path), "features_meta")));
        } catch (IOException e) {
            LOGGER.error("Could not start data import process with path '{}' due to an IOException: {}. Aborting...", path.toString(), LogHelper.getStackTrace(e));
        }
    }
}
