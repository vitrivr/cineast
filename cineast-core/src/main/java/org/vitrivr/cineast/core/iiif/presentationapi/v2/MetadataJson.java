package org.vitrivr.cineast.core.iiif.presentationapi.v2;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.iiif.presentationapi.v2.models.Canvas;
import org.vitrivr.cineast.core.iiif.presentationapi.v2.models.Manifest;
import org.vitrivr.cineast.core.iiif.presentationapi.v2.models.Metadata;
import org.vitrivr.cineast.core.iiif.presentationapi.v2.models.Sequence;

/**
 * @author singaltanmay
 * @version 1.0
 * @created 27.06.21
 */
public class MetadataJson {

  private static final Logger LOGGER = LogManager.getLogger();

  public final String description;
  public final String attribution;
  public final List<Metadata> metadata;
  public final List<ImagePair> images = new LinkedList<>();

  public MetadataJson(Manifest manifest) {
    this.description = manifest.getDescription();
    this.attribution = manifest.getAttribution();
    this.metadata = manifest.getMetadata();
    List<Sequence> sequences = manifest.getSequences();
    if (sequences != null && sequences.size() != 0) {
      for (Sequence sequence : sequences) {
        List<Canvas> canvases = sequence.getCanvases();
        if (canvases != null && canvases.size() != 0) {
          for (Canvas canvas : canvases) {
            canvas.getImages().stream().map(image -> new ImagePair(canvas.getLabel(), image.getAtId(), canvas.getHeight(), canvas.getWidth())).forEach(images::add);
          }
        }
      }
    }
  }

  public String toJsonString() throws JsonProcessingException {
    ObjectMapper objectMapper = new ObjectMapper();
    return objectMapper.writeValueAsString(this);
  }

  public void saveToFile(String filePath, String fileName) throws IOException {
    File file = new File(filePath + "/" + fileName + ".json");
    FileOutputStream fileOutputStream = new FileOutputStream(file);
    BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
    //write byte array to file
    bufferedOutputStream.write(toJsonString().getBytes());
    bufferedOutputStream.close();
    fileOutputStream.close();
    LOGGER.debug("Metadata associated with this manifest written to file successfully");
  }

  public static class ImagePair {

    public final String label;
    public final String url;
    public final long height;
    public final long width;

    public ImagePair(String label, String url, long height, long width) {
      this.label = label;
      this.url = url;
      this.height = height;
      this.width = width;
    }
  }

}
