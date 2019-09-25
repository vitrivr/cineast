package org.vitrivr.cineast.api;


import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.vitrivr.cineast.api.grpc.CineastExtractionService;
import org.vitrivr.cineast.api.grpc.CineastQueryService;
import org.vitrivr.cineast.standalone.config.Config;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class GRPCEndpoint {

    private static Server server;

    public static void start() {

        if (!Config.sharedConfig().getApi().getEnableGRPC()) {
            return;
        }

        int port = 4569; Config.sharedConfig().getApi().getGrpcPort();
        server = ServerBuilder.forPort(port).addService(new CineastQueryService()).addService(new CineastExtractionService()).build();

        try {
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void stop() {
        if (server == null || server.isShutdown()){
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
