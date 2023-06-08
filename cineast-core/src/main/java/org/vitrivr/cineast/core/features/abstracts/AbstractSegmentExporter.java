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
  private Path destination;

  protected String fileExtension;

  protected String dataUrlPrefix;

  /**
   * Default constructor
   */
  public AbstractSegmentExporter() {
    this(new HashMap<>());
  }

  /**
   * Default constructor. The AudioSegmentExport can be configured via named properties in the provided HashMap.
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
    this.destination = Path.of(properties.getOrDefault(PROPERTY_NAME_DESTINATION, "."));
  }

  abstract public void exportToStream(SegmentContainer sc, OutputStream stream);

  @Override
  public void processSegment(SegmentContainer shot) {
    try {
      /* Prepare folder and OutputStream. */
      Path folder = this.destination.resolve(shot.getSuperId());
      if (!folder.toFile().exists()) {
        folder.toFile().mkdirs();
      }
      Path path = folder.resolve(shot.getId() + this.fileExtension);
      OutputStream os = Files.newOutputStream(path);

      /* Write audio data to OutputStream. */
      exportToStream(shot, os);

      /* Close OutputStream. */
      os.close();
    } catch (Exception e) {
      LOGGER.error("Could not write audio data to file for segment {} due to {}.", shot.getId(), LogHelper.getStackTrace(e));
    }
  }

  public String exportToDataUrl(SegmentContainer shot) throws IOException {
    // create a ByteArrayOutputStream
    ByteArrayOutputStream baos = new ByteArrayOutputStream();

    // call the exportToStream method with the ByteArrayOutputStream
    exportToStream(shot, baos);

    // convert the ByteArrayOutputStream's data to a byte array
    byte[] bytes = baos.toByteArray();

    // encode the byte array to a Base64 string
    String base64 = Base64.getEncoder().encodeToString(bytes);

    // concatenate the data URL prefix and the Base64 string
    return this.dataUrlPrefix + base64;
  }

  public byte[] exportToBinary(SegmentContainer shot) throws IOException {
    // create a ByteArrayOutputStream
    ByteArrayOutputStream baos = new ByteArrayOutputStream();

    // call the exportToStream method with the ByteArrayOutputStream
    exportToStream(shot, baos);

    // convert the ByteArrayOutputStream's data to a byte array
    byte[] bytes = baos.toByteArray();

    return bytes;
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
