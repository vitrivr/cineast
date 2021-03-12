package org.vitrivr.cineast.core.db.cottontaildb;

import org.vitrivr.cottontail.client.language.ddl.OptimizeEntity;
import org.vitrivr.cottontail.client.language.dml.Insert;
import org.vitrivr.cineast.core.config.DatabaseConfig;
import org.vitrivr.cineast.core.db.DBSelector;
import org.vitrivr.cineast.core.db.DBIntegrationTest;
import org.vitrivr.cineast.core.db.PersistencyWriter;
import org.vitrivr.cineast.core.db.setup.EntityCreator;

public class CottontailIntegrationTest extends DBIntegrationTest<Insert> {

  private final DatabaseConfig config;

  public CottontailIntegrationTest() {
    config = new DatabaseConfig();
    config.setPort(1865);
  }

  @Override
  protected void finishSetup() {
    final CottontailWrapper wrapper = getWrapper();
    final String fqn = wrapper.fqn(this.getTestTextTableName());
    wrapper.client.optimize(new OptimizeEntity(fqn), null);
    wrapper.close();
  }

  private CottontailWrapper getWrapper() {
    return new CottontailWrapper(config, true);
  }

  @Override
  protected PersistencyWriter<Insert> getPersistencyWriter() {
    return new CottontailWriter(getWrapper());
  }

  @Override
  protected DBSelector getSelector() {
    return new CottontailSelector(getWrapper());
  }

  @Override
  public EntityCreator getEntityCreator() {
    return new CottontailEntityCreator(getWrapper());
  }
}
