package org.vitrivr.cineast.standalone.importer.handlers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.util.LogHelper;
import org.vitrivr.cineast.standalone.importer.LIREImporter;

public class LIREImportHandler extends DataImportHandler {
    /** */
    private static final Logger LOGGER = LogManager.getLogger();

    /**
     * Default constructor for {@link LIREImportHandler}.
     *
     * @param threads Number if threads to use
     * @param batchSize Batch size to use for import.
     */
    public LIREImportHandler(int threads, int batchSize) {
        super(threads, batchSize);
    }

    @Override
    public void doImport(Path path) {
        try {
            LOGGER.info("Starting data import with LIRE files in: {}", path.toString());
            if (!Files.isDirectory(path)) {
                LOGGER.error("Could not start data import process with path '{}', because it is not a directory.", path);
                return;
            }

            final String name = path.getFileName().toString();
            Files.walk(path, 2).filter(p -> p.toString().toLowerCase().endsWith(name)).forEach(p -> {
                final String filename = p.getFileName().toString();
                final String suffix = filename.substring(filename.lastIndexOf("."));
                try {
                    this.futures.add(this.service.submit(new DataImportRunner(new LIREImporter(p.toFile()), filename.replace(suffix, ""), "lire_" + filename.replace(suffix, ""))));
                } catch (IOException e) {
                    LOGGER.error("Could not start data import for file '{}'. Skipping...?", p.toString());
                }
            });
        } catch (IOException e) {
            LOGGER.error("Could not start data import process with path '{}' due to an IOException: {}. Aborting...", path.toString(), LogHelper.getStackTrace(e));
        }
    }
}
