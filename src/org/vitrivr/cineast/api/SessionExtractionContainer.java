package org.vitrivr.cineast.api;

import io.prometheus.client.Counter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.config.Config;
import org.vitrivr.cineast.core.config.IngestConfig;
import org.vitrivr.cineast.core.run.ExtractionContextProvider;
import org.vitrivr.cineast.core.run.ExtractionDispatcher;
import org.vitrivr.cineast.core.run.ExtractionPathProvider;
import org.vitrivr.cineast.core.run.path.SessionPathProvider;
import org.vitrivr.cineast.core.util.json.JacksonJsonProvider;

/**
 * Singleton Structure. Intended to be an access points across multiple sessions. Can be closed and
 * opened if you care about memory leaks
 *
 * @author silvan on 22.01.18.
 */
public class SessionExtractionContainer {

  private static ExtractionPathProvider provider;
  private static final Logger LOGGER = LogManager.getLogger();
  private static boolean open = false;
  private static Counter submittedPaths;
  private static File configFile;

  /**
   * @param configFile where the config is located
   */
  public static synchronized void open(File configFile) {
    if (open) {
      LOGGER.error("Already initialized...");
      return;
    }
    SessionExtractionContainer.configFile = configFile;
    initalizeExtraction();
    if (Config.sharedConfig().getMonitoring().enablePrometheus) {
      LOGGER.debug("Initalizing Prometheus monitoring for submitted paths");
      submittedPaths = Counter.build().name("cineast_submitted_paths")
          .help("Submitted Paths for this instance").register();
    }
    open = true;
  }

  private static void initalizeExtraction() {
    ExtractionDispatcher dispatcher = new ExtractionDispatcher();
    try {
      JacksonJsonProvider reader = new JacksonJsonProvider();
      ExtractionContextProvider context = reader.toObject(configFile, IngestConfig.class);
      provider = new SessionPathProvider();
      if (dispatcher.initialize(provider, context)) {
        dispatcher.start();
      } else {
        System.err.println(String.format(
            "Could not start session with configuration file '%s'. Does the file exist?",
            configFile.toString()));
      }
      dispatcher.start();
    } catch (IOException e) {
      System.err.println(String.format(
          "Could not start session with configuration file '%s' due to a IO error.",
          configFile.toString()));
      e.printStackTrace();
    }
  }

  /**
   * Re-initalizes extraction. Counter for submitted paths remains
   */
  public static void restartExceptCounter() {
    provider.close();
    initalizeExtraction();
  }

  public static void close() {
    provider.close();
    open = false;
  }

  public static boolean isOpen() {
    return open;
  }

  public static void addPaths(List<Path> pathList) {
    if (provider == null) {
      LOGGER
          .fatal("Provider not initalized yet. Please use the --server option in the API. Exiting");
      throw new RuntimeException();
    }
    if (!provider.isOpen()) {
      LOGGER.error("Provider is closed. URL will not be submitted, exiting.");
      throw new RuntimeException();
    }
    if (submittedPaths != null) {
      submittedPaths.inc(pathList.size());
    }
    provider.addPaths(pathList);
  }

}
