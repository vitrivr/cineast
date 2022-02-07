package org.vitrivr.cineast.core.db.cottontaildb;

import java.util.function.Supplier;
import org.vitrivr.cineast.core.config.DatabaseConfig;
import org.vitrivr.cineast.core.db.DBSelector;
import org.vitrivr.cineast.core.db.IntegrationDBProvider;
import org.vitrivr.cineast.core.db.PersistencyWriter;
import org.vitrivr.cineast.core.db.setup.EntityCreator;
import org.vitrivr.cottontail.client.language.dml.Insert;

public class CottontailIntegrationDBProvider implements IntegrationDBProvider<Insert> {

  private static final Supplier<DatabaseConfig> WRAPPER_CONFIG_PROVIDER = () -> {
    DatabaseConfig config = new DatabaseConfig();
    config.setHost("localhost");
    config.setPort(1865);
    return config;
  };

  /** The {@link CottontailWrapper} used to establish a database connection. */
  private final CottontailWrapper wrapper;

  /** The {@link DatabaseConfig} to use to run this test.*/
  private final DatabaseConfig config = WRAPPER_CONFIG_PROVIDER.get();

  /**
   * Constructor.
   */
  public CottontailIntegrationDBProvider() {
    this.wrapper = new CottontailWrapper(this.config.getHost(), this.config.getPort());
  }

  CottontailWrapper getWrapper() {
    return this.wrapper;
  }

  @Override
  public PersistencyWriter<Insert> getPersistencyWriter() {
    return new CottontailWriter(getWrapper(), this.config.getBatchsize());
  }

  @Override
  public DBSelector getSelector() {
    return new CottontailSelector(getWrapper());
  }

  @Override
  public EntityCreator getEntityCreator() {
    return new CottontailEntityCreator(getWrapper());
  }

  @Override
  public void close() {
    /* No op. */
  }
}
