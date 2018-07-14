package org.vitrivr.cineast.api.rest.handlers.actions;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.vitrivr.cineast.api.SessionExtractionContainer;
import org.vitrivr.cineast.core.data.messages.session.ExtractionContainerMessage;
import org.vitrivr.cineast.core.run.ExtractionItemContainer;
import spark.Route;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import spark.Request;
import spark.Response;
import javax.servlet.MultipartConfigElement;
import javax.servlet.http.Part;
import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

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

    request.attribute("org.eclipse.jetty.multipartConfig", new MultipartConfigElement("/temp"));

    ExtractionContainerMessage extractionContainerMessage = null;

    for (Part part: request.raw().getParts()) {
      String fileName = getFileName(part);
      if (fileName == null) fileName = String.valueOf(System.currentTimeMillis());

      Path filePath = new File(fileName).toPath();

      try (InputStream input = part.getInputStream()) {
          // Use the input stream to create a file
          Files.copy(input, filePath, StandardCopyOption.REPLACE_EXISTING);
      }
    }

    File extractConfig = new File("extract.json");
    if (extractConfig.exists()) {
        extractionContainerMessage = MAPPER.readValue(extractConfig, ExtractionContainerMessage.class);

        for (ExtractionItemContainer container: extractionContainerMessage.getItems()) {
            container.setPath(Paths.get(URI.create("file://" + new File(container.getObject().getPath()).getAbsolutePath())));
        }
    }

    if (extractionContainerMessage != null) {
        //deserialize and add to extraction pipeline
        SessionExtractionContainer.addPaths(extractionContainerMessage.getItems());
        return "Files submitted for extraction";
    } else return "extract.json not uploaded";
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
}
