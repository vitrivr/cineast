package org.vitrivr.cineast.standalone.cli;

import com.github.rvesse.airline.annotations.Cli;
import com.github.rvesse.airline.help.Help;

@Cli(name = "cineast-api", description = "The CLI provided by the Cineast API.", commands = {CreateIndicesCommand.class, OptimizeEntitiesCommand.class, CodebookCommand.class, DatabaseSetupCommand.class, EvaluationCommand.class, ExtractionCommand.class, ImportCommand.class, ThreeDeeTestCommand.class, RetrieveCommand.class, MetadataCommand.class, Help.class, SingleObjRetrievalCommand.class, TextRetrievalTest.class})
public class CineastCli {

}
