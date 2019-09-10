package org.vitrivr.cineast.api;


import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.vitrivr.cineast.api.grpc.CineastExtractionService;
import org.vitrivr.cineast.api.grpc.CineastQueryService;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class GRPCEndpoint {

    private static Server server;

    public static void start() {

        int port = 4569; //TODO config

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
