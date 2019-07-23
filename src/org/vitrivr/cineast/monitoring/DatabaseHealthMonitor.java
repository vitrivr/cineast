package org.vitrivr.cineast.monitoring;

import io.prometheus.client.Summary;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.config.Config;
import org.vitrivr.cineast.core.db.DBSelector;

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
    new Thread(() -> {
      while (running) {
        long start = System.currentTimeMillis();
        boolean ping = selector.ping();
        if (!ping) {
          LOGGER.error("Connection issue, waiting for 1 minute");
          try {
            Thread.sleep(60_000);
          } catch (InterruptedException e) {
            LOGGER.error(e);
          }
        } else {
          long stop = System.currentTimeMillis();
          LOGGER.trace("Ping of {} ms", stop - start);
          executionTime.observe(stop - start);
        }
        try {
          Thread.sleep(PING_INTERVAL);
        } catch (InterruptedException e) {
          LOGGER.error(e);
        }
      }
    }).start();
  }

  public static void stop() {
    if (!initalized) {
      LOGGER.error("Not running, cannot stop.");
    }
    LOGGER.debug("Stopping DB Health Monitor");
    running = false;
  }


}
