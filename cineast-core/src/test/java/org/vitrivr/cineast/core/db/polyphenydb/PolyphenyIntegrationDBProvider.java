package org.vitrivr.cineast.core.db.polyphenydb;

import java.sql.PreparedStatement;
import java.util.function.Supplier;
import org.vitrivr.cineast.core.config.DatabaseConfig;
import org.vitrivr.cineast.core.db.DBSelector;
import org.vitrivr.cineast.core.db.IntegrationDBProvider;
import org.vitrivr.cineast.core.db.PersistencyWriter;
import org.vitrivr.cineast.core.db.polypheny.PolyphenyEntityCreator;
import org.vitrivr.cineast.core.db.polypheny.PolyphenySelector;
import org.vitrivr.cineast.core.db.polypheny.PolyphenyWrapper;
import org.vitrivr.cineast.core.db.polypheny.PolyphenyWriter;
import org.vitrivr.cineast.core.db.setup.EntityCreator;

public class PolyphenyIntegrationDBProvider implements IntegrationDBProvider<PreparedStatement> {

  private static final Supplier<DatabaseConfig> WRAPPER_CONFIG_PROVIDER = () -> {
    DatabaseConfig config = new DatabaseConfig();
    config.setHost("localhost");
    config.setPort(20591);
    return config;
  };

  private final PolyphenyWrapper wrapper;

  /**
   * The {@link DatabaseConfig} to use to run this test.
   */
  private final DatabaseConfig config = WRAPPER_CONFIG_PROVIDER.get();

  /**
   * Constructor.
   */
  public PolyphenyIntegrationDBProvider() {
    this.wrapper = new PolyphenyWrapper(this.config.getHost(), this.config.getPort());
  }

  PolyphenyWrapper getWrapper() {
    return this.wrapper;
  }

  @Override
  public PersistencyWriter<PreparedStatement> getPersistencyWriter() {
    return new PolyphenyWriter(getWrapper(), this.config.getBatchsize());
  }

  @Override
  public DBSelector getSelector() {
    return new PolyphenySelector(getWrapper());
  }

  @Override
  public EntityCreator getEntityCreator() {
    return new PolyphenyEntityCreator(getWrapper());
  }

  @Override
  public void close() {
    this.wrapper.close();
  }
}
