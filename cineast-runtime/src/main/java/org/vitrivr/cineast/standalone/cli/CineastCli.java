package org.vitrivr.cineast.standalone.cli;

import com.github.rvesse.airline.annotations.Cli;
import com.github.rvesse.airline.help.Help;
import org.vitrivr.cineast.standalone.cli.db.DropTableCommand;
import org.vitrivr.cineast.standalone.cli.db.LSC21TemporalUpdateCommand;

@Cli(name = "cineast-api", description = "The CLI provided by the Cineast API.", commands = {
    DropTableCommand.class, TagRetrievalCommand.class, OptimizeEntitiesCommand.class,
    CodebookCommand.class, DatabaseSetupCommand.class, ExtractionCommand.class, ImportCommand.class,
    ThreeDeeTestCommand.class, RetrieveCommand.class, Help.class, SingleObjRetrievalCommand.class,
    TextRetrievalCommand.class, DistinctColumnApiCommand.class,
    LSC21TemporalUpdateCommand.class}, defaultCommand = Help.class)
public class CineastCli {

}
