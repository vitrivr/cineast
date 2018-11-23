package org.vitrivr.cineast.core.importer.handlers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.features.DescriptionTextSearch;
import org.vitrivr.cineast.core.importer.vbs2019.CaptionTextImporter;
import org.vitrivr.cineast.core.importer.vbs2019.GoogleVisionCategory;
import org.vitrivr.cineast.core.importer.vbs2019.GoogleVisionImporter;
import org.vitrivr.cineast.core.util.LogHelper;

public class GoogleVisionImportHandler extends DataImportHandler {

  private static final Logger LOGGER = LogManager.getLogger();
  private final GoogleVisionCategory category;
  private final boolean importTags;

  public GoogleVisionImportHandler(int threads, int batchsize, GoogleVisionCategory category, boolean importTags) {
    super(threads, batchsize);
    this.category = category;
    this.importTags = importTags;
  }

  @Override
  public void doImport(Path root) {
    try {
      Files.walk(root, 2).filter(p -> p.toString().toLowerCase().endsWith(".json")).forEach(p -> {
        try {
          this.futures.add(this.service.submit(new DataImportRunner(new GoogleVisionImporter(p, category, importTags), category.tableName, "gvision-" + category + "-" + importTags)));
        } catch (IOException e) {
          LOGGER.fatal("Failed to open path at {} ", p);
          throw new RuntimeException(e);
        }
      });
    } catch (IOException e) {
      LOGGER.error("Could not start data import process with path '{}' due to an IOException: {}. Aborting...", root.toString(), LogHelper.getStackTrace(e));
    }
  }
}
