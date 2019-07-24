package org.vitrivr.cineast.standalone.monitoring;

import io.prometheus.client.Summary;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.standalone.config.Config;

/**
 * So we don't clutter the {@link org.vitrivr.cineast.standalone.runtime.ExtractionTask} code.
 * Singleton where you can register an extraction time of features
 *
 * @author silvan on 25.01.18.
 */
public class PrometheusExtractionTaskMonitor extends ImportTaskMonitor {

  private static boolean initalized = false;
  private static Summary extractionTime;
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
    LOGGER.info("Initalizing Prometheus Extraction Task Monitor");
    extractionTime = Summary.build().name("cineast_feature_process_time")
        .help("Time for an ExtractionTask to process the Shot")
        .labelNames("Extractor").quantile(0.5, 0.05).quantile(0.9, 0.01).register();
  }

  /**
   * You can call this method without worrying if prometheus support is enabled
   */
  public static void reportExecutionTime(String name, long miliseconds) {
    if (extractionTime != null) {
      extractionTime.labels(name).observe(miliseconds);
    }
  }

}
