package org.vitrivr.cineast.api;

import org.vitrivr.cineast.standalone.cli.CineastCli;
import org.vitrivr.cineast.standalone.config.Config;
import org.vitrivr.cineast.standalone.util.CLI;

import java.util.Arrays;

public class Main {
    /**
     * Entrypoint for Cineast API application.
     *
     * @param args Program arguments.
     */
    public static void main(String[] args) {
        /* (Force) load application config. */
        if (args.length < 2) {
            System.err.println("Starting Cineast requires at least two arguments: the path to the configuration file and a command (cineast <config> <command>. Cineast will shutdown...");
            System.exit(1);
        }

        /* (Force) load application config. */
        if (Config.loadConfig(args[0]) == null) {
            System.err.println("Failed to load Cineast configuration from '" + args[0] + "'. Cineast will shutdown...");
            System.exit(1);
        }

        /* Start Cineast API endpoint. */
        APIEndpoint.start();

        /* Either start Cineast in interactive mode. */
        CLI.start(CineastCli.class);
    }
}
