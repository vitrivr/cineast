package org.vitrivr.cineast.standalone.cli.db;


import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.Option;
import com.github.rvesse.airline.annotations.restrictions.Required;
import org.vitrivr.cineast.core.db.setup.EntityCreator;
import org.vitrivr.cineast.standalone.cli.AbstractCineastCommand;
import org.vitrivr.cineast.standalone.config.Config;

/**
 * A CLI command that can be used to drop a table
 */
@Command(name = "drop-table", description = "Drop a specific table")
public class DropTableCommand extends AbstractCineastCommand {

  @Option(name = {"--table"}, description = "Name of the table to drop.")
  @Required
  private String tableName;

  public static void dropTable(String tableName) {
    final EntityCreator ec = Config.sharedConfig().getDatabase().getEntityCreatorSupplier().get();
    if (ec != null) {
      System.out.println("Dropping " + tableName);

      ec.dropEntity(tableName);
      System.out.println("...done");
      /* Closes the EntityCreator. */
      ec.close();
    }
  }

  @Override
  protected void execute() {
    dropTable(tableName);
  }
}
