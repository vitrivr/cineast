package org.vitrivr.cineast.standalone;

import static org.vitrivr.cineast.core.util.CineastConstants.DEFAULT_CONFIG_PATH;

import com.github.rvesse.airline.parser.ParseResult;
import com.github.rvesse.airline.parser.errors.ParseException;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.LinkedBlockingDeque;

import org.vitrivr.cineast.core.render.lwjgl.renderer.RenderJob;
import org.vitrivr.cineast.core.render.lwjgl.renderer.RenderWorker;
import org.vitrivr.cineast.core.render.lwjgl.util.fsm.abstractworker.JobControlCommand;
import org.vitrivr.cineast.standalone.cli.CineastCli;
import org.vitrivr.cineast.standalone.config.Config;
import org.vitrivr.cineast.standalone.monitoring.PrometheusServer;
import org.vitrivr.cineast.standalone.util.CLI;

public class Main {

  /**
   * Entrypoint for Cineast standalone application.
   *
   * @param args Program arguments.
   */
  public static void main(String[] args) {
    /* (Force) load application config. */
    if (args.length == 0) {
      System.out.println("No config path given, loading default config '" + DEFAULT_CONFIG_PATH + "'");
      if (Config.loadConfig(DEFAULT_CONFIG_PATH) == null) {
        System.err.println("Failed to load Cineast configuration from '" + DEFAULT_CONFIG_PATH + "'. Cineast API will shutdown...");
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
    /* Initialize Monitoring */
    try {
      PrometheusServer.initialize();
    } catch (Throwable e) {
      System.err.println("Failed to initialize Monitoring due to an exception: " + e.getMessage());
    }

    if (Config.sharedConfig().getApi().getEnableRenderWorker()) {
      /* Initialize Renderer */
      var renderThread = new Thread(new RenderWorker(new LinkedBlockingDeque<>()), "RenderWorker");
      renderThread.start();
    }

    if (args.length == 1) {
      CLI.start(CineastCli.class);
    }
    /* Either start Cineast in interactive mode OR execute command directly. */
    if (args.length == 2 && args[1].equals("interactive")) {
      CLI.start(CineastCli.class);
    } else {
      com.github.rvesse.airline.Cli<Runnable> cli = new com.github.rvesse.airline.Cli<>(CineastCli.class);
      final String[] theArgs = Arrays.copyOfRange(args, 1, args.length);
      // Adopted from https://rvesse.github.io/airline/guide/help/index.html
      // Parse with a result to allow us to inspect the results of parsing
      ParseResult<Runnable> result = cli.parseWithResult(theArgs);
      if (result.wasSuccessful()) {
        // Parsed successfully, so just run the command and exit
        result.getCommand().run();
      } else {
        // Parsing failed
        // Display errors and then the help information
        System.err.println(String.format("%d errors encountered:", result.getErrors().size()));
        int i = 1;
        for (ParseException e : result.getErrors()) {
          System.err.println(String.format("Error %d: %s", i, e.getMessage()));
          i++;
        }

        System.err.println();

        try {
          com.github.rvesse.airline.help.Help.<Runnable>help(cli.getMetadata(), Arrays.asList(theArgs), System.out); // Is it appropriate to use System.out here?
        } catch (IOException e) {
          // Something seriously went wrong, as we could not display the help message.
          e.printStackTrace();
        }
      }
      PrometheusServer.stopServer();
      RenderWorker.getRenderJobQueue().add(new RenderJob(JobControlCommand.SHUTDOWN_WORKER));
    }
  }
}
