package org.vitrivr.cineast.api;


import io.grpc.Server;
import io.grpc.ServerBuilder;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.api.grpc.CineastExtractionService;
import org.vitrivr.cineast.api.grpc.CineastManagementService;
import org.vitrivr.cineast.api.grpc.CineastQueryService;
import org.vitrivr.cineast.standalone.config.Config;

public class GRPCEndpoint {

  private static Server server;

  private static final Logger LOGGER = LogManager.getLogger();

  public static void start() {

    if (!Config.sharedConfig().getApi().getEnableGRPC()) {
      return;
    }

    int port = Config.sharedConfig().getApi().getGrpcPort();

    LOGGER.info("Starting GRPC Endpoint at port {}", port);

    server = ServerBuilder.forPort(port).addService(new CineastQueryService(APIEndpoint.retrievalLogic)) //FIXME this should come from a more reasonable location
        .addService(new CineastExtractionService()).addService(new CineastManagementService()).build();

    try {
      server.start();
    } catch (IOException e) {
      e.printStackTrace();
    }

  }

  public static void stop() {
    if (server == null || server.isShutdown()) {
      return;
    }

    server.shutdown();
    try {
      server.awaitTermination(10, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    server.shutdownNow();
  }

}
