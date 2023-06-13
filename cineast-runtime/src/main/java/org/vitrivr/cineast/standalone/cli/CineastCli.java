package org.vitrivr.cineast.standalone.cli;

import com.github.rvesse.airline.annotations.Cli;
import com.github.rvesse.airline.annotations.Parser;
import com.github.rvesse.airline.help.Help;
import com.github.rvesse.airline.parser.errors.handlers.CollectAll;
import org.vitrivr.cineast.standalone.cli.db.DropTableCommand;
import org.vitrivr.cineast.standalone.cli.db.PolyphenyBenchmarkCommand;


@Cli(name = "cineast-api", description = "The CLI provided by the Cineast API.", commands = {
        DropTableCommand.class, TagRetrievalCommand.class, OptimizeEntitiesCommand.class,
        CodebookCommand.class, DatabaseSetupCommand.class, ExtractionCommand.class, ImportCommand.class,
        ThreeDeeTestCommand.class, ThreeDeeTextureTestCommand.class, RetrieveCommand.class, Help.class, SingleObjRetrievalCommand.class,
        TextRetrievalCommand.class, DistinctColumnApiCommand.class,
        PolyphenyBenchmarkCommand.class}, defaultCommand = Help.class, parserConfiguration = @Parser(errorHandler = CollectAll.class)
)
public class CineastCli {

}
