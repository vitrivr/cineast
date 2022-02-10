package org.vitrivr.cineast.api;

import org.vitrivr.cineast.standalone.cli.CineastCli;
import org.vitrivr.cineast.standalone.config.Config;
import org.vitrivr.cineast.standalone.monitoring.PrometheusServer;
import org.vitrivr.cineast.standalone.util.CLI;

public class Main {

  /**
   * Entrypoint for Cineast API application.
   *
   * @param args Program arguments.
   */
  public static void main(String[] args) {
    /* (Force) load application config. */
    if (args.length == 0) {
      System.out.println("No config path given, loading default config cineast.json");
      Config.loadConfig("cineast.json");
    }

    /* (Force) load application config. */
    if (args.length != 0) {
      if (Config.loadConfig(args[0]) == null) {
        System.err.println("Failed to load Cineast configuration from '" + args[0] + "'. Cineast API will shutdown...");
        System.exit(1);
      }
    }

    try {
      /* Start API endpoint. */
      APIEndpoint.getInstance().start();

      /* Start gRPC endpoint. */
      GRPCEndpoint.start();

      /* Initialize Monitoring */
      PrometheusServer.initialize();
    } catch (Throwable e) {
      System.err.println("Failed to initialize some Cineast components due to an exception. Some functionality may be unavailable: " + e.getMessage());
    }

    /* Start Cineast CLI in interactive mode (blocking). */
    CLI.start(CineastCli.class);

    /* This part is only reached when user enters exit/quit: Stops the Cineast API endpoint. */
    APIEndpoint.stop();
    GRPCEndpoint.stop();
    PrometheusServer.stopServer();
  }
}
