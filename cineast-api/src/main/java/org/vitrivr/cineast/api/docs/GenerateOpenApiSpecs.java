package org.vitrivr.cineast.api.docs;

import java.io.IOException;
import org.vitrivr.cineast.api.APIEndpoint;
import org.vitrivr.cineast.api.rest.OpenApiCompatHelper;
import org.vitrivr.cineast.standalone.config.Config;

/**
 * Main class for standalone persistent openapi specs generation.
 *
 * @author loris.sauter
 */
public class GenerateOpenApiSpecs {

  /**
   * Entrypoint for Cineast API application.
   *
   * @param args Program arguments.
   */
  public static void main(String[] args) {
    /* (Force) load application config. */
    if (args.length < 1) {
      System.err.println("Starting Cineast requires least one argument: the path to the configuration. Cineast API will shutdown...");
      System.exit(1);
    }

    /* (Force) load application config. */
    if (Config.loadConfig(args[0]) == null) {
      System.err.println("Failed to load Cineast configuration from '" + args[0] + "'. Cineast API will shutdown...");
      System.exit(1);
    }

    try {
      OpenApiCompatHelper.writeOpenApiDocPersistently(APIEndpoint.getInstance(), "docs/openapi.json"); // TODO use config?
    } catch (IOException e) {
      e.printStackTrace();
    }

    APIEndpoint.stop();
  }
}
