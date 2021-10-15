package org.vitrivr.cineast.standalone.importer.vbs2019.v3c1analysis;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.data.entities.MediaSegmentMetadataDescriptor;
import org.vitrivr.cineast.core.util.LogHelper;
import org.vitrivr.cineast.standalone.importer.handlers.DataImportHandler;

public class ColorlabelImportHandler extends DataImportHandler {

  private static final Logger LOGGER = LogManager.getLogger();

  public ColorlabelImportHandler(int threads, int batchsize) {
    super(threads, batchsize);
  }

  @Override
  public void doImport(Path root) {
    try {
      LOGGER.info("Starting data import for colorlabel files in: {} with {} threads and {} batchsize", root.toString(), this.numberOfThreads, this.batchsize);
      Files.walk(root.resolve("colorlabels/"), 2).filter(p -> p.toString().toLowerCase().endsWith(".txt")).forEach(p -> {
        try {
          String color = p.getFileName().toString().split("\\.")[0];
          this.futures.add(this.service.submit(new DataImportRunner(new ColorlabelImporter(p, color), MediaSegmentMetadataDescriptor.ENTITY, "import colorlabels " + color)));
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      });
    } catch (IOException e) {
      LOGGER.error("Could not start data import process with path '{}' due to an IOException: {}. Aborting...", root.toString(), LogHelper.getStackTrace(e));
    }
  }
}
