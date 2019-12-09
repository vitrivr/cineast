package org.vitrivr.cineast.core.db.cottontaildb;

import ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Tuple;
import org.vitrivr.cineast.core.config.DatabaseConfig;
import org.vitrivr.cineast.core.db.DBSelector;
import org.vitrivr.cineast.core.db.DBSelectorTest;
import org.vitrivr.cineast.core.db.PersistencyWriter;
import org.vitrivr.cineast.core.db.setup.EntityCreator;

public class CottontailSelectorTest extends DBSelectorTest<Tuple> {

  private final DatabaseConfig config;

  public CottontailSelectorTest() {
    config = new DatabaseConfig();
    config.setPort(1865);
  }

  @Override
  protected void finishSetup() {
    CottontailWrapper wrapper = getWrapper();
    wrapper.optimizeEntityBlocking(CottontailMessageBuilder.entity(this.getTestTextTableName()));
    wrapper.close();
  }

  private CottontailWrapper getWrapper() {
    return new CottontailWrapper(config, true);
  }

  @Override
  protected PersistencyWriter<Tuple> getPersistencyWriter() {
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
