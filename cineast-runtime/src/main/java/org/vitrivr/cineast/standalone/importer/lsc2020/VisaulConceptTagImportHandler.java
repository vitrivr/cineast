package org.vitrivr.cineast.standalone.importer.lsc2020;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.db.dao.reader.TagReader;
import org.vitrivr.cineast.core.features.SegmentTags;
import org.vitrivr.cineast.standalone.importer.handlers.DataImportHandler;

import java.nio.file.Path;

public class VisaulConceptTagImportHandler extends DataImportHandler {

    private static final Logger LOGGER = LogManager.getLogger();

    /**
     * Constructor; creates a new DataImportHandler with specified number of threads and batchsize.
     *
     * @param threads   Number of threads to use for data import.
     * @param batchsize Size of data batches that are sent to the persistence layer.
     */
    public VisaulConceptTagImportHandler(int threads, int batchsize) {
        super(threads, batchsize);
    }

    @Override
    public void doImport(Path path) {
        LOGGER.info("Starting visual concept import for tags in {}", path);
        this.futures.add(this.service.submit(new DataImportRunner(new VisualConceptTagImporter(path, true), TagReader.TAG_ENTITY_NAME, "lsc-uniqueTags")));
        this.futures.add(this.service.submit(new DataImportRunner(new VisualConceptTagImporter(path), SegmentTags.SEGMENT_TAGS_TABLE_NAME, "lsc-visualConceptsTags")));
    }
}
