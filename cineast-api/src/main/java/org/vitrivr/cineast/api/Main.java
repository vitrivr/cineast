package org.vitrivr.cineast.api;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingDeque;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.render.lwjgl.renderer.RenderJob;
import org.vitrivr.cineast.core.render.lwjgl.renderer.RenderWorker;

import static org.vitrivr.cineast.core.util.CineastConstants.DEFAULT_CONFIG_PATH;

import org.vitrivr.cineast.core.render.lwjgl.util.fsm.abstractworker.JobControlCommand;
import org.vitrivr.cineast.standalone.cli.CineastCli;
import org.vitrivr.cineast.standalone.config.Config;
import org.vitrivr.cineast.standalone.monitoring.PrometheusServer;
import org.vitrivr.cineast.standalone.util.CLI;


public class Main {
    private static final Logger LOGGER = LogManager.getLogger();

    /**
     * Entrypoint for Cineast API application.
     *
     * @param args Program arguments.
     */
    public static void main(String[] args) {
        /* (Force) load application config. */
        if (args.length == 0) {
            System.out.println("No config path given, loading default config '" + DEFAULT_CONFIG_PATH + "'");
            if (Config.loadConfig(DEFAULT_CONFIG_PATH) == null) {
                System.err.println("Failed to load Cineast configuration from '" + DEFAULT_CONFIG_PATH + "'. Cineast API will shutdown...");
                System.exit(1);
            }
        }

        /* (Force) load application config. */
        if (args.length != 0) {
            if (Config.loadConfig(args[0]) == null) {
                System.err.println("Failed to load Cineast configuration from '" + args[0] + "'. Cineast API will shutdown...");
                System.exit(1);
            }
        }

        /* Start API endpoint. */
        try {
            APIEndpoint.getInstance().start();
        } catch (Throwable e) {
            e.printStackTrace();
            System.err.println("Failed to initialize API endpoint due to an exception: " + e.getMessage());
        }

        /* Start gRPC endpoint. */
        try {
            GRPCEndpoint.start();
        } catch (Throwable e) {
            e.printStackTrace();
            System.err.println("Failed to initialize gRPC endpoint due to an exception: " + e.getMessage());
        }

        /* Initialize Monitoring */
        try {
            PrometheusServer.initialize();
        } catch (Throwable e) {
            e.printStackTrace();
            System.err.println("Failed to initialize Monitoring due to an exception: " + e.getMessage());
        }
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down endpoints...");
            APIEndpoint.stop();
            GRPCEndpoint.stop();
            PrometheusServer.stopServer();
            if (RenderWorker.getRenderJobQueue() != null) {
                RenderWorker.getRenderJobQueue().add(new RenderJob(JobControlCommand.SHUTDOWN_WORKER));
            }
            System.out.println("Goodbye!");
        }));

        if (Config.sharedConfig().getExtractor().getEnableRenderWorker()) {
            /* Initialize Renderer */
            var renderThread = new Thread(new RenderWorker(new LinkedBlockingDeque<>()), "RenderWorker");
            renderThread.start();
        }

        if (Config.sharedConfig().getApi().getEnableExternalClip()) {
            /* Startup Clip Python Endpoint */
            // TODO: Make this configurable
            Path condaEnvironmentPath = Path.of( "C:/Users/walten0000/.conda/envs/openclip/python.exe");
            Path scriptPath = Path.of( "./cineast-api/src/main/python/serve_open_clip_lion_text_feature_proxy.py");

            var processParameters = new ArrayList<String>();
            processParameters.add(condaEnvironmentPath.toString());
            processParameters.add(scriptPath.toString());

            var processBuilder = new ProcessBuilder(processParameters);
            //processBuilder.command("python", "./cineast-api/src/main/python/serve_open_clip_lion_text_feature_proxy.py");
            processBuilder.redirectErrorStream(true);

                var processBuilderThread = new Thread(()->{
                    Process process = null;
                    try {
                        process = processBuilder.start();
                        process.info();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    var reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    LOGGER.info("Starting OpenClip Python Endpoint");
                    reader.lines().forEach(l -> LOGGER.info("External Clip: " + l));

                }
            );
            processBuilderThread.start();

        }

        try {
            /* Start Cineast CLI in interactive mode (blocking). */
            if (Config.sharedConfig().getApi().getEnableCli()) {
                CLI.start(CineastCli.class);
            } else {
                while (true) {
                    Thread.sleep(100);
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.exit(0);
    }
}
