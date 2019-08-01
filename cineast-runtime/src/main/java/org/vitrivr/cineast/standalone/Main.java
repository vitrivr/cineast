package org.vitrivr.cineast.standalone;

import com.github.rvesse.airline.annotations.Cli;
import org.vitrivr.cineast.standalone.cli.*;

@Cli(name = "cineast", description = "Provides a basic example CLI", commands = { CodebookCli.class, DatabaseSetupCli.class, EvaluationCli.class, ExtractionCli.class, ImportCli.class, ThreeDeeTestCli.class, RetrieveCli.class, MetadataCli.class, InteractiveCli.class})
public class Main {

    /**
     * Entrypoint for Cineast standalone application.
     *
     * @param args Program arguments.
     */
    public static void main(String[] args) {
        com.github.rvesse.airline.Cli<Runnable> cli = new com.github.rvesse.airline.Cli<>(Main.class);
        final Runnable command = cli.parse(args);
        command.run();
    }
}
