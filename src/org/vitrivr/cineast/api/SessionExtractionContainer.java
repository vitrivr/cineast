package org.vitrivr.cineast.api;

import io.prometheus.client.Counter;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.config.Config;
import org.vitrivr.cineast.core.config.IngestConfig;
import org.vitrivr.cineast.core.run.ExtractionContextProvider;
import org.vitrivr.cineast.core.run.ExtractionDispatcher;
import org.vitrivr.cineast.core.run.ExtractionItemContainer;
import org.vitrivr.cineast.core.run.path.SessionContainerProvider;
import org.vitrivr.cineast.core.util.json.JacksonJsonProvider;

/**
 * Singleton Structure. Intended to be an access points across multiple sessions. Can be closed and
 * opened if you care about memory leaks
 *
 * @author silvan on 22.01.18.
 */
public class SessionExtractionContainer {

  private static SessionContainerProvider provider;
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
      IngestConfig context = reader.toObject(configFile, IngestConfig.class);
      provider = new SessionContainerProvider();
      if (dispatcher.initialize(provider, context)) {
        dispatcher.registerListener(provider);
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
    getProviderOrExit().close();
    LOGGER.debug("Restarting SessionPathProvider");
    initalizeExtraction();
  }

  public static void close() {
    LOGGER.debug("Closing session");
    getProviderOrExit().close();
    open = false;
  }

  public static void endSession() {
    getProviderOrExit().endSession();
  }

  public static void addPaths(ExtractionItemContainer[] items) {
    getOpenProviderOrExit().addPaths(Arrays.asList(items));
    if (submittedPaths != null) {
      submittedPaths.inc(items.length);
    }
  }

  /**
   * Marks as not closing anymore. Provides a best-effort to synchronize.
   *
   * @return if the underlying provider is closed
   */
  public static boolean keepAliveCheckIfClosed() {
    return getOpenProviderOrExit().keepAliveCheckIfClosed();
  }

  private static SessionContainerProvider getProviderOrExit() {
    if (provider == null) {
      LOGGER
          .fatal("Provider not initalized yet. Please use the --server option in the API. Exiting");
      throw new RuntimeException();
    }
    return provider;
  }

  private static SessionContainerProvider getOpenProviderOrExit() {
    if (!getProviderOrExit().isOpen()) {
      LOGGER.error("Provider is closed. URL will not be submitted, exiting.");
      throw new RuntimeException();
    }
    return provider;
  }
}
