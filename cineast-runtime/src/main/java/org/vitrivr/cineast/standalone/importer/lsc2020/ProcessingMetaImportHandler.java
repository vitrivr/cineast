package org.vitrivr.cineast.standalone.importer.lsc2020;

import com.opencsv.exceptions.CsvException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.db.dao.reader.TagReader;
import org.vitrivr.cineast.core.features.SegmentTags;
import org.vitrivr.cineast.standalone.importer.handlers.DataImportHandler;

import java.io.IOException;
import java.nio.file.Path;

public class ProcessingMetaImportHandler extends DataImportHandler {

    private static final Logger LOGGER = LogManager.getLogger();

    /**
     * Constructor; creates a new DataImportHandler with specified number of threads and batchsize.
     *
     * @param threads   Number of threads to use for data import.
     * @param batchsize Size of data batches that are sent to the persistence layer.
     */
    public ProcessingMetaImportHandler(int threads, int batchsize) {
        super(threads, batchsize);
    }

    @Override
    public void doImport(Path path) {
        LOGGER.info("Starting meta as tag import for tags in {}", path);
        try {
            LSCUtilities.create(path);
        } catch (IOException | CsvException e) {
            LOGGER.error("Cannot do import as initialization failed.", e);
            return;
        }
        this.futures.add(this.service.submit(new DataImportRunner(new ProcessingMetaImporter(path, ProcessingMetaImporter.Type.TAG), SegmentTags.SEGMENT_TAGS_TABLE_NAME, "lsc-metaAsTags")));
        this.futures.add(this.service.submit(new DataImportRunner(new ProcessingMetaImporter(path, ProcessingMetaImporter.Type.TAG), TagReader.TAG_ENTITY_NAME, "lsc-metaAsTagsLookup")));
    }
}
