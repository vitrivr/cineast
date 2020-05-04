package org.vitrivr.cineast.standalone.importer.vbs2019;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.features.DescriptionTextSearch;
import org.vitrivr.cineast.core.features.ObjectInstances;
import org.vitrivr.cineast.core.util.LogHelper;
import org.vitrivr.cineast.standalone.importer.handlers.DataImportHandler;

public class MLTFeaturesImportHandler extends DataImportHandler {

  private static final Logger LOGGER = LogManager.getLogger();

  public MLTFeaturesImportHandler(int threads, int batchsize) {
    super(threads, batchsize);
  }

  @Override
  public void doImport(Path root) {
    try {
      LOGGER.info("Starting data import for mlt feature files in: {}", root.toString());
      Files.walk(root, 2).filter(p -> p.toString().toLowerCase().endsWith(".json")).forEach(p -> {
        try {
          this.futures.add(this.service.submit(new DataImportRunner(new MLTFeatureImporter(p), ObjectInstances.TABLE_NAME, "mlt feature file")));
        } catch (IOException e) {
          LOGGER.fatal("Failed to open path at {} ", p);
          throw new RuntimeException(e);
        }
      });
      this.waitForCompletion();
      LOGGER.info("Completed data import with mlt feature Import files in: {}", root.toString());
    } catch (IOException e) {
      LOGGER.error("Could not start data import process with path '{}' due to an IOException: {}. Aborting...", root.toString(), LogHelper.getStackTrace(e));
    }
  }
}
