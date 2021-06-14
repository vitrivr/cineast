package org.vitrivr.cineast.core.iiif.imageapi.v3;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;
import org.vitrivr.cineast.core.iiif.imageapi.ImageInformationRequest;

/**
 * @author singaltanmay
 * @version 1.0
 * @created 14.06.21
 */
public class ImageInformationRequest_v3 implements ImageInformationRequest {

  private static final Logger LOGGER = LogManager.getLogger();

  private final String url;
  private final String imageInformation;

  public ImageInformationRequest_v3(String url) throws IOException {
    this.url = url;
    this.imageInformation = this.fetchImageInformation(url);
  }

  @Nullable
  @Override
  public ImageInformation_v3 parseImageInformation(String response) {
    ImageInformation_v3 imageInformation = null;
    if (response == null || response.isEmpty()) {
      response = this.imageInformation;
    }
    try {
      imageInformation = new ObjectMapper().readValue(response, ImageInformation_v3.class);
    } catch (IOException e) {
      e.printStackTrace();
    }
    return imageInformation;
  }

  @Override
  public void saveToFile(String filePath, String fileName) throws IOException {
    File file = new File(filePath + "/" + fileName + ".json");
    FileOutputStream fileOutputStream = new FileOutputStream(file);
    BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
    //write byte array to file
    bufferedOutputStream.write(imageInformation.getBytes());
    bufferedOutputStream.close();
    fileOutputStream.close();
    LOGGER.debug("Image information request's json response data written to file successfully. Request url:\t" + url);
  }
}
