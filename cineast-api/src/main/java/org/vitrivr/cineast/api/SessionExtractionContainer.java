package org.vitrivr.cineast.api;

import java.io.File;
import java.io.IOException;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.util.json.JacksonJsonProvider;
import org.vitrivr.cineast.standalone.config.Config;
import org.vitrivr.cineast.standalone.config.IngestConfig;
import org.vitrivr.cineast.standalone.run.ExtractionDispatcher;
import org.vitrivr.cineast.standalone.run.ExtractionItemContainer;
import org.vitrivr.cineast.standalone.run.path.SessionContainerProvider;

public class SessionExtractionContainer {

  private static final Logger LOGGER = LogManager.getLogger();
  private static SessionContainerProvider provider;
  private static IngestConfig context;

  private static void loadConfig() {
    String path = Config.sharedConfig().getApi().getSessionExtractionConfigLocation();
    File configFile = new File(path);
    JacksonJsonProvider reader = new JacksonJsonProvider();
    context = reader.toObject(configFile, IngestConfig.class);
  }

  public static boolean startExtraction() {
    if (provider != null && provider.isOpen()) {
      // Already running.
      return false;
    }
    if (!Config.sharedConfig().getApi().getAllowExtraction()) {
      LOGGER.info("Extraction disallowed in config, not starting extraction provider");
    }

    ExtractionDispatcher dispatcher = new ExtractionDispatcher();

    try {
      loadConfig();

      // Create new provider and go.
      provider = new SessionContainerProvider();
      dispatcher.initialize(provider, context);
      dispatcher.registerListener(provider);
      dispatcher.start(); // Runs as long as the provider is open.
    } catch (IOException e) {
      LOGGER.error("Failed to start new ExtractionDispatcher for session!");
      return false;
    }

    return true;
  }

  public static void stopExtraction() {
    provider.endSession();
    provider = null;
    context = null;
  }

  public static Boolean isOpen() {
    return provider != null && provider.isOpen();
  }

  public static void addPaths(List<ExtractionItemContainer> items) {
    provider.addPaths(items);
  }

}
