package org.vitrivr.cineast.api.docs;

import java.io.IOException;
import org.vitrivr.cineast.api.APIEndpoint;
import org.vitrivr.cineast.standalone.cli.CineastCli;
import org.vitrivr.cineast.standalone.config.Config;
import org.vitrivr.cineast.standalone.util.CLI;

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

    /* Start Cineast API endpoint. */
    try {
      APIEndpoint.getInstance().writeOpenApiDocPersistently("docs/swagger.json"); // TODO use config?
    } catch (IOException e) {
      e.printStackTrace();
    }

    /* This part is only reached when user enters exit/quit: Stops the Cineast API endpoint. */
    APIEndpoint.stop();
  }
}
