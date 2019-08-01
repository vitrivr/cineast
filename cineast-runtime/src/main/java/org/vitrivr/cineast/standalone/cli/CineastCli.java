package org.vitrivr.cineast.standalone.cli;

import com.github.rvesse.airline.annotations.Cli;

@Cli(name = "cineast-api", description = "The CLI provided by the Cineast API.", commands = { CodebookCommand.class, DatabaseSetupCommand.class, EvaluationCommand.class, ExtractionCommand.class, ImportCommand.class, ThreeDeeTestCommand.class, RetrieveCommand.class, MetadataCommand.class})
public class CineastCli {}