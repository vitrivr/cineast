package org.vitrivr.cineast.standalone.importer.lsc2020;

import com.opencsv.exceptions.CsvException;
import java.io.IOException;
import java.nio.file.Path;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.features.OCRSearch;
import org.vitrivr.cineast.standalone.importer.handlers.DataImportHandler;

public class OCRImportHandler extends DataImportHandler {

  private static final Logger LOGGER = LogManager.getLogger(OCRImportHandler.class);
  private final boolean clean;

  /**
   * Constructor; creates a new DataImportHandler with specified number of threads and batchsize.
   *
   * @param threads   Number of threads to use for data import.
   * @param batchsize Size of data batches that are sent to the persistence layer.
   */
  public OCRImportHandler(int threads, int batchsize, boolean clean) {
    super(threads, batchsize);
    this.clean = clean;
  }

  @Override
  public void doImport(Path path) {
    try {
      LSCUtilities.create(path);
    } catch (IOException | CsvException e) {
      LOGGER.fatal("Error in init", e);
      return;
    }
    LOGGER.info("Starting LSC OCR import from {}", path);
    this.futures.add(this.service.submit(new DataImportRunner(new OCRImporter(path), OCRSearch.OCR_TABLE_NAME, "lsc-ocr", clean)));
  }
}
