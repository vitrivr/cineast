package org.vitrivr.cineast.standalone.cli;

import ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Schema;
import com.github.rvesse.airline.annotations.Command;
import org.vitrivr.cineast.core.db.cottontaildb.CottontailWrapper;
import org.vitrivr.cineast.standalone.config.Config;

@Command(name = "optimize", description = "Optimize all entities for the cineast schema")
public class OptimizeEntitiesCommand implements Runnable {

  @Override
  public void run() {
    System.out.println("Optimizing all entities for schema cineast in Cottontail");
    CottontailWrapper wrapper = new CottontailWrapper(Config.sharedConfig().getDatabase());
    wrapper.listEntities(Schema.newBuilder().setName("cineast").build()).forEach(entity -> {
      System.out.println("Optimizing entity " + entity);
      wrapper.optimizeEntityBlocking(entity);
    });
    System.out.println("Finished optimizing all entities");
  }
}
