package org.vitrivr.cineast.api.rest.handlers.actions;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.vitrivr.cineast.api.SessionExtractionContainer;
import org.vitrivr.cineast.core.data.messages.session.ExtractionContainerMessage;
import org.vitrivr.cineast.api.SessionExtractionContainer;
import org.vitrivr.cineast.core.run.ExtractionItemContainer;
import org.vitrivr.cineast.core.data.entities.MultimediaObjectDescriptor;
import org.vitrivr.cineast.core.data.MediaType;
import org.vitrivr.cineast.core.config.Config;
import spark.Route;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import spark.Request;
import spark.Response;
import javax.servlet.MultipartConfigElement;
import javax.servlet.http.Part;
import java.io.File;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * @author iammvaibhav on 06.07.18
 * @version 1.0
 *
 * This handler is used to let the users upload media files to be extracted along with a extract.json
 * file which will be deserialized to ExtractionContainerMessage and will be passed to extraction pipeline for
 * media extraction.
 *
 * File are uploaded using the enctype multipart/form-data.
 */
public class FileExtractionHandler implements Route {

  private static final Logger LOGGER = LogManager.getLogger();

  /**
   * Jackson ObjectMapper used to map to/from objects.
   */
  private static final ObjectMapper MAPPER = new ObjectMapper();

  /* Can be used to setup the ObjectMapper.  */
  static {
      MAPPER.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
      MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
      MAPPER.configure(DeserializationFeature.READ_ENUMS_USING_TO_STRING, true);
  }


  @Override
  public Object handle(Request request, Response response) throws Exception {

    request.raw().setAttribute("org.eclipse.jetty.multipartConfig", new MultipartConfigElement("/temp"));

    ExtractionContainerMessage extractionContainerMessage = null;

    Part config = request.raw().getPart("extract_config");
    if (config != null) {
      extractionContainerMessage = MAPPER.readValue(config.getInputStream(), ExtractionContainerMessage.class);
    } else return "extract_config key not found";

    if (extractionContainerMessage == null)
      return "malformed extraction config message";

    MediaType mediaType;
    try {
        mediaType = MediaType.valueOf(request.headers("media_type"));
    } catch (Exception e) {
      return "error occured in decoding media_type value";
    }

    /**
     * Get all the files from HTTP multipart request and store them in the
     * appropriate media folders in the object location
     */
    for (ExtractionItemContainer itemContainer: extractionContainerMessage.getItems()) {
      String fileName = itemContainer.getObject().getName();

      File inputFile = getFile(fileName, extractionContainerMessage);
      Path filePath = inputFile.toPath();

      itemContainer.setPath(Paths.get(inputFile.toURI()));

      if (request.raw().getPart(fileName) != null) {
        try (InputStream input = request.raw().getPart(fileName).getInputStream()) {
            // Use the input stream to create a file
            Files.copy(input, filePath, StandardCopyOption.REPLACE_EXISTING);
        }
      }
    }

    // start the session
    if (SessionExtractionContainer.isProviderNull()) {
      SessionExtractionContainer.open(SessionExtractionContainer.getConfigFileForMediaType(mediaType));
    } else if (!SessionExtractionContainer.isProviderNull() && SessionExtractionContainer.keepAliveCheckIfClosed()) {
      SessionExtractionContainer.startSessionFor(mediaType);
    }

    // add to extraction pipeline
    SessionExtractionContainer.addPaths(extractionContainerMessage.getItems());

    // end the session
    SessionExtractionContainer.endSession();

    return "Files submitted for extraction";
  }

  /**
   * given Part object, this method return the file name
   * @param part part of request describing a file
   * @return file name
   */
  private String getFileName(Part part) {
    for (String cd : part.getHeader("content-disposition").split(";")) {
      if (cd.trim().startsWith("filename")) {
        return cd.substring(cd.indexOf('=') + 1).trim().replace("\"", "");
      }
    }
    return null;
  }

  /**
   * given a file name and ExtractionContainerMessage, returns the File object
   * representing object location according to media type
   */
  private File getFile(String fileName, ExtractionContainerMessage extractionContainerMessage) {

    for (ExtractionItemContainer itemContainer: extractionContainerMessage.getItems()) {
      if (fileName.equals(itemContainer.getObject().getName())) {
        int mediatypeId = itemContainer.getObject().getMediatypeId();

        String mediaType = null;

        switch (mediatypeId) {
          case 0: mediaType = "video"; break;
          case 1: mediaType = "image"; break;
          case 2: mediaType = "audio"; break;
          case 3: mediaType = "model3d"; break;
          default: mediaType = null;
        }

        if (mediaType == null) {
          return null;
        }

        File mediaFolder = new File(Config.sharedConfig().getApi().getObjectLocation(), mediaType);
        mediaFolder.mkdirs();
        return new File(mediaFolder, fileName);
      }
    }
    return null;
  }
}
