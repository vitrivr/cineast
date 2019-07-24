package org.vitrivr.cineast.standalone.importer.handlers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.util.LogHelper;
import org.vitrivr.cineast.standalone.importer.JsonObjectImporter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class JsonDataImportHandler extends DataImportHandler {
    /** */
    private static final Logger LOGGER = LogManager.getLogger();

    /**
     *
     * @param threads
     * @param batchsize
     */
    public JsonDataImportHandler(int threads, int batchsize) {
        super(threads, batchsize);
    }

    /**
     * Starts data import process using JSON files. The method can either be used with a single file or a folder containing JSON files.
     *
     * @param path Path to the JSON file or a folder containing JSON files.
     */
    @Override
    public void doImport(Path path) {
        try {
            LOGGER.info("Starting data import with JSON files in: {}", path.toString());
            Files.walk(path, 2).filter(p -> p.toString().toLowerCase().endsWith(".json")).forEach(p -> {
                final String filename = p.getFileName().toString();
                final String suffix = filename.substring(filename.lastIndexOf("."));
                try {
                  this.futures.add(this.service.submit(new DataImportRunner(new JsonObjectImporter(p.toFile()), filename.replace(suffix, ""), "json_" + filename.replace(suffix, ""))));
                } catch (IOException e) {
                    LOGGER.error("Could not start data import for file '{}'. Skipping...?", p.toString());
                }
            });
            this.waitForCompletion();
            LOGGER.info("Completed data import with JSON files in: {}", path.toString());
        } catch (IOException e) {
            LOGGER.error("Could not start data import process with path '{}' due to an IOException: {}. Aborting...", path.toString(), LogHelper.getStackTrace(e));
        }
    }
}
