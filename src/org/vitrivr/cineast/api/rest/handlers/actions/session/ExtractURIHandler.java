package org.vitrivr.cineast.api.rest.handlers.actions.session;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.api.SessionExtractionContainer;
import org.vitrivr.cineast.api.rest.exceptions.ActionHandlerException;
import org.vitrivr.cineast.api.rest.exceptions.MethodNotSupportedException;
import org.vitrivr.cineast.api.rest.handlers.abstracts.ParsingActionHandler;
import org.vitrivr.cineast.core.data.messages.session.SessionState;
import org.vitrivr.cineast.core.data.messages.session.UriList;

/**
 * @author silvan on 19.01.18.
 */
public class ExtractURIHandler extends ParsingActionHandler<UriList> {

  private static final Logger LOGGER = LogManager.getLogger();

  @Override
  public Object doGet(Map<String, String> parameters) throws ActionHandlerException {
    throw new MethodNotSupportedException("HTTP GET is not supported for ExtractPathHandler");

  }

  @Override
  public Object doPost(UriList context, Map<String, String> parameters)
      throws ActionHandlerException {
    SessionState state = ValidateSessionHandler.validateSession(parameters); //TODO Use State

    LOGGER.debug("Received uris {}", Arrays.toString(context.getUris()));
    List<Path> paths = Arrays.stream(context.getUris()).map(uriString -> {
          Optional<Path> path;
          try {
            URI uri = new URI(uriString);
            path = Optional.of(Paths.get(uri));
          } catch (URISyntaxException | IllegalArgumentException | FileSystemNotFoundException | SecurityException e) {
            path = Optional.empty();
          }
          return path;
        }
    ).filter(Optional::isPresent).map(Optional::get).collect(Collectors.toList());
    SessionExtractionContainer.addPaths(paths);
    LOGGER.debug("Submitted for extraction");
    return state;
  }

  @Override
  public Class<UriList> inClass() {
    return UriList.class;
  }
}
