package org.vitrivr.cineast.standalone.monitoring;

import io.prometheus.client.Summary;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.standalone.config.Config;


public class RetrievalTaskMonitor {

  private static boolean initalized = false;
  private static Summary executionTime;
  private static final Logger LOGGER = LogManager.getLogger();

  public static void init() {
    if (initalized) {
      LOGGER.info("Already initalized, Returning");
      return;
    }
    if (!Config.sharedConfig().getMonitoring().enablePrometheus) {
      LOGGER.warn("Prometheus not enabled, returning");
      return;
    }
    LOGGER.info("Initalizing Prometheus Retrieval Task Monitor");
    executionTime = Summary.build().name("cineast_retrievaltask")
        .help("Time for one query item")
        .labelNames("Retriever").quantile(0.5, 0.05).quantile(0.9, 0.01).register();
  }

  /**
   * You can call this method without worrying if prometheus support is enabled
   */
  public static void reportExecutionTime(String name, long miliseconds) {
    if (executionTime != null) {
      executionTime.labels(name).observe(miliseconds);
    }
  }

}
