package org.vitrivr.cineast.standalone.cli;

import org.vitrivr.cottontail.grpc.CottontailGrpc.Schema;
import com.github.rvesse.airline.annotations.Command;
import org.vitrivr.cineast.core.config.DatabaseConfig.Selector;
import org.vitrivr.cineast.core.config.DatabaseConfig.Writer;
import org.vitrivr.cineast.core.db.cottontaildb.CottontailWrapper;
import org.vitrivr.cineast.standalone.config.Config;

@Command(name = "optimize", description = "Optimize all entities for the cineast schema. This command is only compatible with the cottontail database")
public class OptimizeEntitiesCommand implements Runnable {

  @Override
  public void run() {
    optimizeAllCottontailEntities();
  }

  public static void optimizeAllCottontailEntities() {
    if (Config.sharedConfig().getDatabase().getSelector() != Selector.COTTONTAIL || Config.sharedConfig().getDatabase().getWriter() != Writer.COTTONTAIL) {
      System.err.println("Cottontail is not both selector & writer in the config. exiting");
      return;
    }
    System.out.println("Optimizing all entities for schema cineast in Cottontail");
    CottontailWrapper wrapper = new CottontailWrapper(Config.sharedConfig().getDatabase(), true);
    wrapper.listEntities(Schema.newBuilder().setName("cineast").build()).forEach(entity -> {
      System.out.println("Optimizing entity " + entity);
      wrapper.optimizeEntityBlocking(entity);
    });
    System.out.println("Finished optimizing all entities");
  }
}
