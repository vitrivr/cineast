package org.vitrivr.cineast.standalone.importer.lsc2020;

import com.opencsv.exceptions.CsvException;
import java.io.IOException;
import java.nio.file.Path;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.db.setup.EntityCreator;
import org.vitrivr.cineast.core.features.SpatialDistance;
import org.vitrivr.cineast.standalone.config.Config;
import org.vitrivr.cineast.standalone.importer.handlers.DataImportHandler;

/**
 * Handler of spatial imports.
 */
public class SpatialImportHandler extends DataImportHandler {

  public static final Logger LOGGER = LogManager.getLogger(SpatialImportHandler.class);


  public SpatialImportHandler(int threads, int batchSize){
    super(threads, batchSize);
  }

  @Override
  public void doImport(Path path) {
    try {
      LSCUtilities.create(path);
    } catch (IOException | CsvException e) {
      LOGGER.fatal("Error in initialisation", e);
      LOGGER.fatal("Crashing now");
      return;
    }
    LOGGER.info("Starting LSC Spatial metadata as feature import from folder {}", path);
    this.futures.add(this.service.submit(new DataImportRunner(new SpatialImporter(path),
        "features_SpatialDistance"/*SpatialDistance.ENTITY*/, "lsc-spatialfeature", false)));
  }
}
