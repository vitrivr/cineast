package org.vitrivr.cineast.standalone.importer.vbs2019.v3c1analysis;

import java.io.IOException;
import java.nio.file.Path;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.data.entities.MediaSegmentMetadataDescriptor;
import org.vitrivr.cineast.standalone.importer.handlers.DataImportHandler;

public class FacesImportHandler extends DataImportHandler {

  private static final Logger LOGGER = LogManager.getLogger();

  public FacesImportHandler(int threads, int batchsize) {
    super(threads, batchsize);
  }

  @Override
  public void doImport(Path root) {
    LOGGER.info("Starting data import for face files in: {} with {} threads and {} batchsize", root.toString(), this.numberOfThreads, this.batchsize);
    try {
      SequenceIdLookupService lookupService = new SequenceIdLookupService(root.resolve("msb-allshots.txt"));
      this.futures.add(this.service.submit(new DataImportRunner(new FacesImporter(root.resolve("1face.txt"), 1, lookupService), MediaSegmentMetadataDescriptor.ENTITY, "importing for one face")));
      this.futures.add(this.service.submit(new DataImportRunner(new FacesImporter(root.resolve("2faces.txt"), 2, lookupService), MediaSegmentMetadataDescriptor.ENTITY, "importing for two faces")));
      this.futures.add(this.service.submit(new DataImportRunner(new FacesImporter(root.resolve("3faces.txt"), 3, lookupService), MediaSegmentMetadataDescriptor.ENTITY, "importing for three faces")));
      this.futures.add(this.service.submit(new DataImportRunner(new FacesImporter(root.resolve("4faces.txt"), 4, lookupService), MediaSegmentMetadataDescriptor.ENTITY, "importing for four faces")));
      this.futures.add(this.service.submit(new DataImportRunner(new FacesImporter(root.resolve("manyfaces.txt"), 5, lookupService), MediaSegmentMetadataDescriptor.ENTITY, "importing for many faces")));
      this.futures.add(this.service.submit(new DataImportRunner(new FacesImporter(root.resolve("nofaces.txt"), 0, lookupService), MediaSegmentMetadataDescriptor.ENTITY, "importing for zero faces")));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    this.waitForCompletion();
    LOGGER.info("Completed data import with classification Import files in: {}", root.toString());
  }
}
