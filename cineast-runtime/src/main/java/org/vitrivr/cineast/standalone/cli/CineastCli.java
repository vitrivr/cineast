package org.vitrivr.cineast.standalone.cli;

import com.github.rvesse.airline.annotations.Cli;
import com.github.rvesse.airline.help.Help;

@Cli(name = "cineast-api", description = "The CLI provided by the Cineast API.", commands = { CodebookCommand.class, DatabaseSetupCommand.class, EvaluationCommand.class, ExtractionCommand.class, ImportCommand.class, ThreeDeeTestCommand.class, RetrieveCommand.class, MetadataCommand.class, Help.class})
public class CineastCli {}