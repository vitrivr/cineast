package org.vitrivr.cineast.api;

import org.vitrivr.cineast.standalone.cli.CineastCli;
import org.vitrivr.cineast.standalone.config.Config;
import org.vitrivr.cineast.standalone.util.CLI;

public class Main {

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
    APIEndpoint.getInstance().start();

    /* Start Cineast CLI in interactive mode (blocking). */
    CLI.start(CineastCli.class);

    /* This part is only reached when user enters exit/quit: Stops the Cineast API endpoint. */
    APIEndpoint.stop();
  }
}
