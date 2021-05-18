package org.vitrivr.cineast.standalone.monitoring;

import io.prometheus.client.Summary;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.db.DBSelector;
import org.vitrivr.cineast.standalone.config.Config;

/**
 * @author silvan on 19.11.18.
 */
public class DatabaseHealthMonitor {

  private static boolean initalized = false;
  private static Summary executionTime;
  private static final Logger LOGGER = LogManager.getLogger();
  private static volatile boolean running = false;
  private static DBSelector selector;
  private static final long PING_INTERVAL = 2_000;
  private static Thread monitorThread;

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
    executionTime = Summary.build().name("cineast_dbping")
        .help("Time until a ping is returned").quantile(0.5, 0.05).quantile(0.9, 0.01).register();
    running = true;
    selector = Config.sharedConfig().getDatabase().getSelectorSupplier().get();
    monitorThread = new Thread(() -> {
      while (running) {
        long start = System.currentTimeMillis();
        boolean ping = selector.ping();
        try {
          if (!ping) {
            LOGGER.trace("Connection issue, waiting for 1 minute");
            Thread.sleep(60_000);
          } else {
            long stop = System.currentTimeMillis();
            executionTime.observe(stop - start);
            Thread.sleep(PING_INTERVAL);
          }
        } catch (InterruptedException e) {
          if (!running) {
            /* Thread was interrupted due to shutdown, no need to log exception */
            break;
          }
          LOGGER.error(e);
        }
      }
    });
    monitorThread.start();

    initalized = true;
  }

  public static void stop() {
    if (!initalized) {
      LOGGER.error("DB Health Monitoring not running, cannot stop.");
      return;
    }
    LOGGER.debug("Stopping DB Health Monitor");
    running = false;
    try {
      monitorThread.interrupt();
    } catch (Exception e) {
      LOGGER.error("Encountered exception during DB Health Monitor shutdown: " + e);
    }
  }
}
