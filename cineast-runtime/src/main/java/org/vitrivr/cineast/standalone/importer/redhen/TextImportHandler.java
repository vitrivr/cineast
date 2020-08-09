package org.vitrivr.cineast.standalone.importer.redhen;

import java.util.Optional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.features.pose.PoseKeypoints;
import java.nio.file.Path;
import org.vitrivr.cineast.standalone.importer.redhen.TextImporter.HeaderInfo;
import sun.jvm.hotspot.memory.HeapBlock.Header;


abstract public class TextImportHandler extends AligningImportHandler {
  private static final Logger LOGGER = LogManager.getLogger();

  /**
   * Constructor; creates a new {@link TextImportHandler} with specified number of threads and batchsize.
   *
   * @param threads   Number of threads to use for data import.
   * @param batchsize Size of data batches that are sent to the persistence layer.
   */
  public TextImportHandler(int threads, int batchsize) {
    super(threads, batchsize);
  }

  @Override
  protected void addFileJobs(String fullPath, Path input) {
    TextImporter importer = this.mkImporter(input);
    Optional<HeaderInfo> headerInfo = importer.getHeaderInfo();
    if (!headerInfo.isPresent()) {
      throw new RuntimeException("Could not parse header");
    }
    HeaderInfo headerInfoGot = headerInfo.get();
    importer.setSegments(getObjectDescriptors(headerInfoGot.name));
    this.futures.add(this.service.submit(new DataImportRunner(
        importer,
        this.getTableName(),
        this.getTaskName(input)
    )));
  }


  protected abstract String getTaskName(Path input);

  protected abstract String getTableName();

  abstract protected TextImporter mkImporter(Path input);
}
