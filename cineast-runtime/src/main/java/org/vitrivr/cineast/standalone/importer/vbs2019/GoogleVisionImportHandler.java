package org.vitrivr.cineast.standalone.importer.vbs2019;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.features.TagsFtSearch;
import org.vitrivr.cineast.core.util.LogHelper;
import org.vitrivr.cineast.standalone.importer.handlers.DataImportHandler;
import org.vitrivr.cineast.standalone.importer.vbs2019.gvision.GoogleVisionCategory;

public class GoogleVisionImportHandler extends DataImportHandler {

  private static final Logger LOGGER = LogManager.getLogger();
  private final GoogleVisionCategory category;
  private final boolean importTagsFt;

  public GoogleVisionImportHandler(int threads, int batchsize, GoogleVisionCategory category,
      boolean importTagsFt) {
    super(threads, batchsize);
    this.category = category;
    this.importTagsFt = importTagsFt;
  }

  @Override
  public void doImport(Path root) {
    try {
      LOGGER.info("Starting import on path {}", root.toAbsolutePath());
      Files.walk(root, 2).filter(p -> p.toString().toLowerCase().endsWith(".json")).forEach(p -> {
        try {
          this.futures.add(this.service.submit(
              new DataImportRunner(new GoogleVisionImporter(p, category, importTagsFt),
                  importTagsFt ? TagsFtSearch.TAGS_FT_TABLE_NAME : category.tableName,
                  "gvision-" + category + "-" + importTagsFt + "-file")));
        } catch (IOException e) {
          LOGGER.fatal("Failed to open path at {} ", p);
          throw new RuntimeException(e);
        }
      });
    } catch (IOException e) {
      LOGGER.error(
          "Could not start data import process with path '{}' due to an IOException: {}. Aborting...",
          root.toString(), LogHelper.getStackTrace(e));
    }
  }
}
