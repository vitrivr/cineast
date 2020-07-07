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
    private final boolean metaAsTable;

    /**
     * Constructor; creates a new DataImportHandler with specified number of threads and batchsize.
     *
     * @param threads   Number of threads to use for data import.
     * @param batchsize Size of data batches that are sent to the persistence layer.
     */
    public ProcessingMetaImportHandler(int threads, int batchsize, boolean metaAsTable) {
        super(threads, batchsize);
        this.metaAsTable = metaAsTable;
    }

    @Override
    public void doImport(Path path) {
        LOGGER.info("Starting "+(metaAsTable ? "meta-as-table" : "meta-as-tag")+" import in {}", path);
        try {
            LSCUtilities.create(path);
            LSCUtilities.getInstance().initMetadata();
        } catch (IOException | CsvException e) {
            LOGGER.error("Cannot do import as initialization failed.", e);
            return;
        }
        if (metaAsTable) {
            this.futures.add(this.service.submit(new DataImportRunner(new ProcessingMetaImporter(path, ProcessingMetaImporter.Type.META_AS_TABLE), "features_table_lsc20meta", "lsc-metaAsTable")));
        } else {
            this.futures.add(this.service.submit(new DataImportRunner(new ProcessingMetaImporter(path, ProcessingMetaImporter.Type.TAG_LOOKUP), TagReader.TAG_ENTITY_NAME, "lsc-metaAsTagsLookup")));
            this.futures.add(this.service.submit(new DataImportRunner(new ProcessingMetaImporter(path, ProcessingMetaImporter.Type.TAG), SegmentTags.SEGMENT_TAGS_TABLE_NAME, "lsc-metaAsTags")));
        }
    }
}
