package org.vitrivr.cineast.standalone.importer.vbs2019.v3c1analysis;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.features.SegmentTags;
import org.vitrivr.cineast.core.features.TagsFtSearch;
import org.vitrivr.cineast.core.util.LogHelper;
import org.vitrivr.cineast.standalone.importer.handlers.DataImportHandler;

public class ClassificationsImportHandler extends DataImportHandler {

  private static final Logger LOGGER = LogManager.getLogger();

  public ClassificationsImportHandler(int threads, int batchsize) {
    super(threads, batchsize);
  }

  @Override
  public void doImport(Path root) {
    try {
      LOGGER.info("Starting data import for classification files in: {} with {} threads and {} batchsize", root.toString(), this.numberOfThreads, this.batchsize);
      Path synset = root.resolve("synset.txt");
      Files.walk(root.resolve("aggregate"), 2).filter(p -> p.toString().toLowerCase().endsWith(".json")).forEach(p -> {
        try {
          // TODO ? this.futures.add(this.service.submit(new DataImportRunner(new ClassificationsImporter(p, true), TagReader.TAG_ENTITY_NAME, "classification import synset tags")));
          //this.futures.add(this.service.submit(new DataImportRunner(new ClassificationsImporter(p, synset, false, false), SegmentTags.SEGMENT_TAGS_TABLE_NAME, "classification import classifications")));
          this.futures.add(this.service.submit(new DataImportRunner(new ClassificationsImporter(p, synset, false, true), TagsFtSearch.TAGS_FT_TABLE_NAME, "classification import classifications")));
        } catch (IOException e) {
          LOGGER.fatal("Failed to open path at {} ", p);
          throw new RuntimeException(e);
        }
      });
      this.waitForCompletion();
      LOGGER.info("Completed data import with classification Import files in: {}", root.toString());
    } catch (IOException e) {
      LOGGER.error("Could not start data import process with path '{}' due to an IOException: {}. Aborting...", root.toString(), LogHelper.getStackTrace(e));
    }
  }
}
