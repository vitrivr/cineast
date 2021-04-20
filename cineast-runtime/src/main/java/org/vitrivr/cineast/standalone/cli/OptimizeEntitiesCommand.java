package org.vitrivr.cineast.standalone.cli;

import com.github.rvesse.airline.annotations.Command;

import org.vitrivr.cineast.core.config.DatabaseConfig.Selector;
import org.vitrivr.cineast.core.config.DatabaseConfig.Writer;
import org.vitrivr.cineast.core.db.cottontaildb.CottontailWrapper;
import org.vitrivr.cineast.standalone.config.Config;
import org.vitrivr.cottontail.client.language.ddl.ListEntities;
import org.vitrivr.cottontail.client.language.ddl.OptimizeEntity;

@Command(name = "optimize", description = "Optimize all entities for the Cineast schema. This command is only compatible with the Cottontail DB database.")
public class OptimizeEntitiesCommand implements Runnable {

  @Override
  public void run() {
    optimizeAllCottontailEntities();
  }

  public static void optimizeAllCottontailEntities() {
    if (Config.sharedConfig().getDatabase().getSelector() != Selector.COTTONTAIL || Config.sharedConfig().getDatabase().getWriter() != Writer.COTTONTAIL) {
      System.err.println("Cottontail DB is not both selector & writer in the config. exiting");
      return;
    }
    try (final CottontailWrapper wrapper = new CottontailWrapper(Config.sharedConfig().getDatabase(), false)) {
      System.out.println("Optimizing all entities for schema '" + CottontailWrapper.CINEAST_SCHEMA + "' in Cottontail");
      wrapper.client.list(new ListEntities(CottontailWrapper.CINEAST_SCHEMA), null).forEachRemaining(entity -> {
        System.out.println("Optimizing entity " + entity);
        final String name = entity.asString("dbo").replace("warren.", "");
        wrapper.client.optimize(new OptimizeEntity(name), null);
      });
      System.out.println("Finished optimizing all entities");
    }
  }
}
