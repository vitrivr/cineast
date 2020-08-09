package org.vitrivr.cineast.standalone.importer.redhen;

import java.nio.file.Path;
import org.vitrivr.cineast.core.features.SubtitleFulltextSearch;

public class SubtitleImportHandler extends TextImportHandler {
  /**
   * Constructor; creates a new {@link SubtitleImportHandler} with specified number of threads and
   * batchsize.
   *
   * @param threads   Number of threads to use for data import.
   * @param batchsize Size of data batches that are sent to the persistence layer.
   */
  public SubtitleImportHandler(int threads, int batchsize) {
    super(threads, batchsize);
  }

  @Override
  protected String getTaskName(Path input) {
    return "sub-import-" + input.getFileName();
  }

  @Override
  protected String getTableName() {
    return SubtitleFulltextSearch.ASR_TABLE_NAME;
  }

  @Override
  protected TextImporter mkImporter(Path input) {
    return new TextImporter(input, "CC1");
  }

  @Override
  protected String getExtension() {
    return ".txt";
  }
}
