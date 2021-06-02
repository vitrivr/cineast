package org.vitrivr.cineast.core.iiif.imageapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.IOUtils;
import org.vitrivr.cineast.core.iiif.imageapi.ImageInformation.ProfileItem;

/**
 * @author singaltanmay
 * @version 1.0
 * @created 02.06.21
 */
public class ImageInformationRequest {

  public static void main(String[] args) throws IOException {
    ImageInformation imageInformation = fetchImageInformation("https://libimages.princeton.edu/loris/pudl0001%2F5138415%2F00000011.jp2/info.json");
    System.out.println(imageInformation);
//    String level = (String) imageInformation.profile.get(0);
//    ProfileItem profileItem = (ProfileItem) imageInformation.profile.get(1);
//    System.out.println(level);
//    System.out.println(profileItem);
  }

  private static ImageInformation fetchImageInformation(String url) throws IOException {
    HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
    connection.setRequestProperty("accept", "application/json");
    InputStream responseStream = connection.getInputStream();

    ObjectMapper mapper = new ObjectMapper();
    String response = IOUtils.toString(responseStream, StandardCharsets.UTF_8);

    ImageInformation imageInformation = null;
    try {
      imageInformation = mapper.readValue(response, ImageInformation.class);
    } catch (IOException e) {
      e.printStackTrace();
    }
    return imageInformation;
  }
}
