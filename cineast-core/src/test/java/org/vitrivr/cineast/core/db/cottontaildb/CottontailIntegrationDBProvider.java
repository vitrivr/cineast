package org.vitrivr.cineast.core.db.cottontaildb;

import org.vitrivr.cineast.core.config.DatabaseConfig;
import org.vitrivr.cineast.core.db.DBSelector;
import org.vitrivr.cineast.core.db.IntegrationDBProvider;
import org.vitrivr.cineast.core.db.PersistencyWriter;
import org.vitrivr.cineast.core.db.setup.EntityCreator;
import org.vitrivr.cottontail.client.language.dml.Insert;

public class CottontailIntegrationDBProvider implements IntegrationDBProvider<Insert> {


  private final DatabaseConfig config;

  public CottontailIntegrationDBProvider() {
    config = new DatabaseConfig();
    config.setPort(1865);
  }


  CottontailWrapper getWrapper() {
    return new CottontailWrapper(config, true);
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
