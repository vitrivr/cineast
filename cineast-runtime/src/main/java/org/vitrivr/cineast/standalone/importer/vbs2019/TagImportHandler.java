package org.vitrivr.cineast.standalone.importer.vbs2019;

import java.io.IOException;
import java.nio.file.Path;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.db.dao.reader.TagReader;
import org.vitrivr.cineast.core.features.SegmentTags;
import org.vitrivr.cineast.core.util.LogHelper;
import org.vitrivr.cineast.standalone.importer.handlers.DataImportHandler;

public class TagImportHandler extends DataImportHandler {

  private static final Logger LOGGER = LogManager.getLogger();

  public TagImportHandler(int threads, int batchsize) {
    super(threads, batchsize);
  }

  @Override
  public void doImport(Path root) {
    try {
      LOGGER.info("Starting data import for tag files in: {}", root.toString());

      this.futures.add(this.service.submit(new DataImportRunner(new TagImporter(root.resolve("tagDetails.tsv"), TagReader.TAG_ID_COLUMNNAME, TagReader.TAG_NAME_COLUMNNAME, null, TagReader.TAG_DESCRIPTION_COLUMNNAME), TagReader.TAG_ENTITY_NAME, "tags")));
      this.futures.add(this.service.submit(new DataImportRunner(new TagImporter(root.resolve("uniqueInstances.tsv"), "id", "tagid", "score"), SegmentTags.SEGMENT_TAGS_TABLE_NAME, "unique mappings from segments and tags")));
      LOGGER.info("Completed data import with Tag Import files in: {}", root.toString());
    } catch (IOException e) {
      LOGGER.error("Could not start data import process with path '{}' due to an IOException: {}. Aborting...", root.toString(), LogHelper.getStackTrace(e));
    }
  }
}
