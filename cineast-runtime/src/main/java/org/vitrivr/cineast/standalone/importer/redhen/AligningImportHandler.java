package org.vitrivr.cineast.standalone.importer.redhen;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.data.entities.MediaSegmentDescriptor;
import org.vitrivr.cineast.core.db.DBSelectorSupplier;
import org.vitrivr.cineast.core.db.dao.reader.MediaObjectReader;
import org.vitrivr.cineast.core.db.dao.reader.MediaSegmentReader;
import org.vitrivr.cineast.core.util.LogHelper;
import org.vitrivr.cineast.standalone.config.Config;
import org.vitrivr.cineast.standalone.importer.handlers.DataImportHandler;

abstract class AligningImportHandler extends DataImportHandler {

  private static final Logger LOGGER = LogManager.getLogger();
  private MediaObjectReader objectReader;
  private MediaSegmentReader segmentReader;

  /**
   * Constructor; creates a new {@link AligningImportHandler} with specified number of threads and batchsize.
   *
   * @param threads   Number of threads to use for data import.
   * @param batchsize Size of data batches that are sent to the persistence layer.
   */
  public AligningImportHandler(int threads, int batchsize) {
    super(threads, batchsize);
  }

  protected abstract String getExtension();

  protected abstract void addFileJobs(String fullPath, Path input);

  protected List<MediaSegmentDescriptor> getObjectDescriptorsByPath(String objectPath) {
    final String objectId = objectReader.lookUpObjectByName(objectPath).getObjectId();
    return getObjectDescriptorsById(objectId);
  }

  protected List<MediaSegmentDescriptor> getObjectDescriptorsById(String objectId) {
    return segmentReader.lookUpSegmentsOfObject(objectId);
  }

  @Override
  public void doImport(Path root) {
    final DBSelectorSupplier readerSupplier = Config.sharedConfig().getDatabase().getSelectorSupplier();
    if (!readerSupplier.get().ping()) {
      LOGGER.fatal("Database reader unreachable. Aborting...");
      return;
    }
    this.objectReader = new MediaObjectReader(readerSupplier.get());
    this.segmentReader = new MediaSegmentReader(readerSupplier.get());
    Path rootDir = Files.isDirectory(root) ? root : root.getParent();
    try {
      LOGGER.info("Starting import on path {}", root.toAbsolutePath());
      Files.walk(root, 2).filter(
          path -> path.toString().toLowerCase().endsWith(this.getExtension())
      ).forEach(path -> {
        String fullPath = rootDir.relativize(path).toString();
        addFileJobs(fullPath, path);
      });
      LOGGER.info("Waiting for completion");
      this.waitForCompletion();
    } catch (IOException e) {
      LOGGER.error("Could not start data import process with path '{}' due to an IOException: {}. Aborting...", root.toString(), LogHelper
          .getStackTrace(e));
    }
  }
}
