package org.vitrivr.cineast.monitoring;

import io.prometheus.client.exporter.MetricsServlet;
import io.prometheus.client.hotspot.DefaultExports;
import java.util.Optional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.vitrivr.cineast.core.config.Config;

/**
 * Singleton which starts a reporting endpoint for Prometheus
 *
 * @author silvan on 25.01.18.
 */
public class PrometheusServer {

  private static boolean initalized = false;
  private static final Logger LOGGER = LogManager.getLogger();
  private static Optional<Server> server;

  public static synchronized void initialize() {
    if (initalized) {
      LOGGER.info("Prometheus already initalized");
      return;
    }
    if (!Config.sharedConfig().getMonitoring().enablePrometheus) {
      LOGGER.info("Prometheus monitoring not enabled");
      return;
    }
    DefaultExports.initialize();
    Integer port = Config.sharedConfig().getMonitoring().prometheusPort;
    LOGGER.info("Initalizing Prometheus endpoint at port {}", port);
    server = Optional.of(new Server(port));
    ServletContextHandler context = new ServletContextHandler();
    context.setContextPath("/");
    server.get().setHandler(context);
    context.addServlet(new ServletHolder(new MetricsServlet()), "/metrics");
    PrometheusExtractionTaskMonitor.init();
    try {
      server.get().start();
    } catch (Exception e) {
      e.printStackTrace();
    }
    initalized = true;
  }

  public static void stopServer() {
    if (server.isPresent()) {
      try {
        server.get().stop();
      } catch (Exception e) {
        LOGGER.error(e);
      }
    }
  }

  public static Optional<Server> getServer() {
    return server;
  }

  private PrometheusServer() {
  }

}
