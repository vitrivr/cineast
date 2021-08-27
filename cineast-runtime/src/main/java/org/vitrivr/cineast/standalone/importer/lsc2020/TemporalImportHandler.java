package org.vitrivr.cineast.standalone.importer.lsc2020;

import com.opencsv.exceptions.CsvException;
import java.io.IOException;
import java.nio.file.Path;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.data.entities.MediaSegmentDescriptor;
import org.vitrivr.cineast.standalone.importer.handlers.DataImportHandler;

/**
 * Handler of temporal imports.
 * Basically uses file (i.e. segment id) information to populate segmentstart and segmentend fields
 */
public class TemporalImportHandler extends DataImportHandler {

  public static final Logger LOGGER = LogManager.getLogger(TemporalImportHandler.class);

  /**
   * Constructor; creates a new DataImportHandler with specified number of threads and batchsize.
   *
   * @param threads   Number of threads to use for data import.
   * @param batchsize Size of data batches that are sent to the persistence layer.
   */
  public TemporalImportHandler(int threads, int batchsize) {
    super(threads, batchsize);
  }


  @Override
  public void doImport(final Path path){
    try{
      LSCUtilities.create(path);
    }catch (IOException | CsvException e){
      LOGGER.fatal("Error during initialisation:", e);
      LOGGER.fatal("Stopping immediately.");
      return;
    }
    LOGGER.info("Starting LSC Temporal import: Populate segmentstart and segmentend from folder {}", path);
    this.futures.add(this.service.submit(new DataImportRunner(new TemporalImporter(path),
        MediaSegmentDescriptor.ENTITY, "lsc-temporal", false)));
  }
}
