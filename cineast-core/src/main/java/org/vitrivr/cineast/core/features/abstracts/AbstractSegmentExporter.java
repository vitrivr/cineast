package org.vitrivr.cineast.core.features.abstracts;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.HashMap;
import java.util.function.Supplier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.data.segments.SegmentContainer;
import org.vitrivr.cineast.core.db.PersistencyWriterSupplier;
import org.vitrivr.cineast.core.db.setup.EntityCreator;
import org.vitrivr.cineast.core.features.extractor.Extractor;
import org.vitrivr.cineast.core.util.LogHelper;

abstract public class AbstractSegmentExporter implements Extractor {

  protected static final Logger LOGGER = LogManager.getLogger();

  private static final String PROPERTY_NAME_DESTINATION = "destination";

  /**
   * Destination path for the audio-segment.
   */
  private final Path destination;

  /**
   * Returns the file extension for the exported data.
   *
   * @return String containing the file extension without the dot.
   */
  protected abstract String getFileExtension();

  /**
   * Returns the data-url prefix for the exported data.
   *
   * @return String containing the data-url prefix.
   */
  protected abstract String getDataUrlPrefix();

  /**
   * Default constructor
   */
  public AbstractSegmentExporter() {
    this(new HashMap<>());
  }

  /**
   * Default constructor. A segment exporter can be configured via named properties in the provided HashMap.
   * <p>
   * Supported parameters:
   *
   * <ol>
   *      <li>destination: Path where files should be stored.</li>
   * </ol>
   *
   * @param properties HashMap containing named properties
   */
  public AbstractSegmentExporter(HashMap<String, String> properties) {
    this.destination = Path.of(properties.getOrDefault(PROPERTY_NAME_DESTINATION, "./export"));
  }


  /**
   * Exports the data of a segment container to an output stream.
   *
   * @param sc  SegmentContainer to export.
   * @param stream OutputStream to write to.
   */
  abstract public void exportToStream(SegmentContainer sc, OutputStream stream);

  /**
   * Determines whether a segment can be exported or not, i.e. if there is enough data to create an export. For example, a segment without audio cannot be exported as audio. Does not check if the segment is already exported.
   *
   * @param sc The segment to be checked.
   */
  abstract public boolean isExportable(SegmentContainer sc);

  @Override
  public void processSegment(SegmentContainer shot) {
    if (!isExportable(shot)) {
      return;
    }
    try {
      /* Prepare folder and OutputStream. */
      Path folder = this.destination.resolve(shot.getSuperId());
      if (!folder.toFile().exists()) {
        folder.toFile().mkdirs();
      }
      Path path = folder.resolve(shot.getId() +'.' + this.getFileExtension());
      OutputStream os = Files.newOutputStream(path);

      exportToStream(shot, os);

      /* Close OutputStream. */
      os.close();
    } catch (Exception e) {
      LOGGER.error("Could not write data to file for segment {} due to {}.", shot.getId(), LogHelper.getStackTrace(e));
    }
  }

  /**
   * Exports a segment to a data-url.
   *
   * @param shot The segment to be exported.
   * @return A String containing the data-url.
   */
  public String exportToDataUrl(SegmentContainer shot) throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    exportToStream(shot, baos);
    byte[] bytes = baos.toByteArray();
    baos.close();
    String base64 = Base64.getEncoder().encodeToString(bytes);
    return this.getDataUrlPrefix() + base64;
  }

  /**
   * Exports a segment to a byte array.
   *
   * @param shot The segment to be exported.
   * @return A byte array containing the exported data.
   */
  public byte[] exportToBinary(SegmentContainer shot) {
    // create a ByteArrayOutputStream
    ByteArrayOutputStream baos = new ByteArrayOutputStream();

    // call the exportToStream method with the ByteArrayOutputStream
    exportToStream(shot, baos);

    // convert the ByteArrayOutputStream's data to a byte array
    return baos.toByteArray();
  }

  @Override
  public void init(PersistencyWriterSupplier phandlerSupply) { /* Nothing to init. */ }

  @Override
  public void finish() {  /* Nothing to finish. */}

  @Override
  public void initalizePersistentLayer(Supplier<EntityCreator> supply) { /* Nothing to init. */ }

  @Override
  public void dropPersistentLayer(Supplier<EntityCreator> supply) { /* Nothing to drop. */}

}
