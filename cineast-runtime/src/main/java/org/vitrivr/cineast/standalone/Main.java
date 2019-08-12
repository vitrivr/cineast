package org.vitrivr.cineast.standalone;

import java.util.Arrays;
import org.vitrivr.cineast.standalone.cli.CineastCli;
import org.vitrivr.cineast.standalone.config.Config;
import org.vitrivr.cineast.standalone.util.CLI;

public class Main {
    /**
     * Entrypoint for Cineast standalone application.
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

        /* Either start Cineast in interactive mode OR execute command directly. */
        if (args[1].equals("interactive")) {
            CLI.start(CineastCli.class);
        } else {
            com.github.rvesse.airline.Cli<Runnable> cli = new com.github.rvesse.airline.Cli<>(CineastCli.class);
            final Runnable command = cli.parse(Arrays.copyOfRange(args, 1, args.length));
            command.run();
        }
    }
}
