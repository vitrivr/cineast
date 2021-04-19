package org.vitrivr.cineast.standalone.importer.lsc2020;

import java.nio.file.Path;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.db.dao.reader.TagReader;
import org.vitrivr.cineast.core.features.SegmentTags;
import org.vitrivr.cineast.standalone.importer.handlers.DataImportHandler;
import org.vitrivr.cineast.standalone.importer.lsc2020.MyscealTagImporter;

public class MyscealTagImportHandler extends DataImportHandler {

  private static final Logger LOGGER = LogManager.getLogger();

  public MyscealTagImportHandler(int threads, int batchsize){
    super(threads, batchsize);
  }

  @Override
  public void doImport(Path path) {
    LOGGER.info("Starting mysceal tag import from {}", path);
    this.futures.add(this.service.submit(new DataImportRunner(new MyscealTagImporter(path, true),
        TagReader.TAG_ENTITY_NAME, "lsc-mysceal-tags-unique")));
    this.futures.add(this.service.submit(new DataImportRunner(new MyscealTagImporter(path, false),
        SegmentTags.SEGMENT_TAGS_TABLE_NAME, "lsc-mysceal-tags-segment")));
  }
}
