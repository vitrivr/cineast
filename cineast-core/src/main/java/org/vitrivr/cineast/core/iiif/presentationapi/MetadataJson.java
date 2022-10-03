package org.vitrivr.cineast.core.iiif.presentationapi;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MetadataJson {

  private static final Logger LOGGER = LogManager.getLogger();

  public final String description;
  public final String attribution;
  public final List<Object> metadata;
  public final List<String> images;

  public MetadataJson(Manifest manifest) {
    description = manifest.getSummary();
    attribution = manifest.getRequiredStatement();
    metadata = manifest.getMetadata();
    images = manifest.getImageUrls();
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
}
