package org.vitrivr.cineast.core.db.cottontaildb;

import org.vitrivr.cineast.core.config.DatabaseConfig;
import org.vitrivr.cineast.core.db.DBSelector;
import org.vitrivr.cineast.core.db.IntegrationDBProvider;
import org.vitrivr.cineast.core.db.PersistencyWriter;
import org.vitrivr.cineast.core.db.setup.EntityCreator;
import org.vitrivr.cottontail.CottontailKt;
import org.vitrivr.cottontail.client.language.dml.Insert;
import org.vitrivr.cottontail.server.grpc.CottontailGrpcServer;

public class CottontailIntegrationDBProvider implements IntegrationDBProvider<Insert> {


  private final EmbeddedCottontailDB db;

  private final CottontailWrapper wrapper;

  public CottontailIntegrationDBProvider() {
    db = EmbeddedCottontailDB.getInstance();
  }


  CottontailWrapper getWrapper() {
    return db.getWrapper();
  }

  @Override
  public PersistencyWriter<Insert> getPersistencyWriter() {
    return new CottontailWriter(getWrapper());
  }

  @Override
  public DBSelector getSelector() {
    return new CottontailSelector(getWrapper());
  }

  @Override
  public EntityCreator getEntityCreator() {
    return new CottontailEntityCreator(getWrapper());
  }


}
