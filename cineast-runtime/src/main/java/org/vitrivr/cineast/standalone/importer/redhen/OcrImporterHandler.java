package org.vitrivr.cineast.standalone.importer.redhen;

import java.nio.file.Path;
import org.vitrivr.cineast.core.features.OCRSearch;

public class OcrImporterHandler extends TextImportHandler {
  /**
   * Constructor; creates a new {@link OcrImporterHandler} with specified number of threads and
   * batchsize.
   *
   * @param threads   Number of threads to use for data import.
   * @param batchsize Size of data batches that are sent to the persistence layer.
   */
  public OcrImporterHandler(int threads, int batchsize) {
    super(threads, batchsize);
  }

  @Override
  protected String getTaskName(Path input) {
    return "ocr-import-" + input.getFileName();
  }

  @Override
  protected String getTableName() {
    return OCRSearch.OCR_TABLE_NAME;
  }

  @Override
  protected TextImporter mkImporter(Path input) {
    return new TextImporter(input, "OCR1");
  }

  @Override
  protected String getExtension() {
    return ".ocr";
  }
}
