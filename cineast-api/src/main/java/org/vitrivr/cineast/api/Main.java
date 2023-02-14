package org.vitrivr.cineast.api;

import org.vitrivr.cineast.standalone.cli.CineastCli;
import org.vitrivr.cineast.standalone.config.Config;
import org.vitrivr.cineast.standalone.monitoring.PrometheusServer;
import org.vitrivr.cineast.standalone.util.CLI;

public class Main {

  public static final String DEFAULT_PATH = "cineast.json";

  /**
   * Entrypoint for Cineast API application.
   *
   * @param args Program arguments.
   */
  public static void main(String[] args) {
    /* (Force) load application config. */
    if (args.length == 0) {
      System.out.println("No config path given, loading default config '" + DEFAULT_PATH + "'");
      if (Config.loadConfig(DEFAULT_PATH) == null) {
        System.err.println("Failed to load Cineast configuration from '" + DEFAULT_PATH + "'. Cineast API will shutdown...");
        System.exit(1);
      }
    }

    /* (Force) load application config. */
    if (args.length != 0) {
      if (Config.loadConfig(args[0]) == null) {
        System.err.println("Failed to load Cineast configuration from '" + args[0] + "'. Cineast API will shutdown...");
        System.exit(1);
      }
    }

    /* Start API endpoint. */
    try {
      APIEndpoint.getInstance().start();
    } catch (Throwable e) {
      e.printStackTrace();
      System.err.println("Failed to initialize API endpoint due to an exception: " + e.getMessage());
    }

    /* Start gRPC endpoint. */
    try {
      GRPCEndpoint.start();
    } catch (Throwable e) {
      e.printStackTrace();
      System.err.println("Failed to initialize gRPC endpoint due to an exception: " + e.getMessage());
    }

    /* Initialize Monitoring */
    try {
      PrometheusServer.initialize();
    } catch (Throwable e) {
      e.printStackTrace();
      System.err.println("Failed to initialize Monitoring due to an exception: " + e.getMessage());
    }
    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      System.out.println("Shutting down endpoints...");
      APIEndpoint.stop();
      GRPCEndpoint.stop();
      PrometheusServer.stopServer();
      System.out.println("Goodbye!");
    }));
    try {
      /* Start Cineast CLI in interactive mode (blocking). */
      if (Config.sharedConfig().getApi().getEnableCli()) {
        CLI.start(CineastCli.class);
      } else {
        while (true) {
          Thread.sleep(100);
        }
      }
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    System.exit(0);
  }
}
