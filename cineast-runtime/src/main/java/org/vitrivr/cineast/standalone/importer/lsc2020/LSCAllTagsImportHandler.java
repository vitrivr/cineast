package org.vitrivr.cineast.standalone.importer.lsc2020;

import com.opencsv.exceptions.CsvException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.db.dao.reader.TagReader;
import org.vitrivr.cineast.core.features.SegmentTags;
import org.vitrivr.cineast.standalone.importer.handlers.DataImportHandler;

import java.io.IOException;
import java.nio.file.Path;

public class LSCAllTagsImportHandler extends DataImportHandler {

    private static final Logger LOGGER = LogManager.getLogger();

    public static final String TASK_NAME = "lsc-tags-all";
    public static final String TASK_NAME_LOOKUP_SUFFIX = "-lookup";
    public static final String TASK_NAME_LOOKUP = TASK_NAME+TASK_NAME_LOOKUP_SUFFIX;

    private final boolean clean;

    /**
     * Constructor; creates a new DataImportHandler with specified number of threads and batchsize.
     *  @param threads   Number of threads to use for data import.
     * @param batchsize Size of data batches that are sent to the persistence layer.
     */
    public LSCAllTagsImportHandler(int threads, int batchsize, boolean clean) {
        super(threads, batchsize);
        this.clean = clean;
    }

    @Override
    public void doImport(Path path) {
        LOGGER.info("Starting {} import in {}",TASK_NAME, path);
        if(clean){
            DataImportHandler.cleanOnDemand(TagReader.TAG_ENTITY_NAME, TASK_NAME_LOOKUP);
            DataImportHandler.cleanOnDemand(SegmentTags.SEGMENT_TAGS_TABLE_NAME, TASK_NAME);
        }

        try {
            LSCUtilities.create(path);
            LSCUtilities.getInstance().initMetadata();
        } catch (IOException | CsvException e) {
            LOGGER.error("Cannot do import as initialization failed.", e);
            return;
        }
        // Meta As Tag
        this.futures.add(this.service.submit(new DataImportRunner(new ProcessingMetaImporter(path, ProcessingMetaImporter.Type.TAG_LOOKUP), TagReader.TAG_ENTITY_NAME, "lsc-metaAsTagsLookup")));
        this.futures.add(this.service.submit(new DataImportRunner(new ProcessingMetaImporter(path, ProcessingMetaImporter.Type.TAG), SegmentTags.SEGMENT_TAGS_TABLE_NAME, "lsc-metaAsTags")));
        // Official Concepts
        this.futures.add(this.service.submit(new DataImportRunner(new VisualConceptTagImporter(path, true), TagReader.TAG_ENTITY_NAME, "lsc-visualConceptsTagsLookup")));
        this.futures.add(this.service.submit(new DataImportRunner(new VisualConceptTagImporter(path), SegmentTags.SEGMENT_TAGS_TABLE_NAME, "lsc-visualConceptsTags")));
    }
}
