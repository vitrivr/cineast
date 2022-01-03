package org.vitrivr.cineast.core.db.cottontaildb;

import java.nio.file.Paths;
import java.util.function.Supplier;
import org.vitrivr.cineast.core.config.DatabaseConfig;
import org.vitrivr.cineast.core.db.DBSelector;
import org.vitrivr.cineast.core.db.IntegrationDBProvider;
import org.vitrivr.cineast.core.db.PersistencyWriter;
import org.vitrivr.cineast.core.db.setup.EntityCreator;
import org.vitrivr.cottontail.CottontailKt;
import org.vitrivr.cottontail.client.language.dml.Insert;
import org.vitrivr.cottontail.config.CacheConfig;
import org.vitrivr.cottontail.config.Config;
import org.vitrivr.cottontail.config.ExecutionConfig;
import org.vitrivr.cottontail.config.MapDBConfig;
import org.vitrivr.cottontail.config.ServerConfig;
import org.vitrivr.cottontail.server.grpc.CottontailGrpcServer;

public class CottontailIntegrationDBProvider implements IntegrationDBProvider<Insert> {


  private static final Config COTTONTAIL_TEST_CONFIG = new Config(
      Paths.get("./build/data/cottontaildb-data/"),
      false,
      false,
      null,
      new MapDBConfig(),
      new ServerConfig(),
      new ExecutionConfig(),
      new CacheConfig()
  );

  private static final Supplier<DatabaseConfig> WRAPPER_CONFIG_PROVIDER = () -> {
    DatabaseConfig config = new DatabaseConfig();
    config.setHost("localhost");
    config.setPort(COTTONTAIL_TEST_CONFIG.getServer().getPort());
    return config;
  };


  /** The embedded {@link CottontailGrpcServer} used by this {@link CottontailIntegrationDBProvider}. */
  private final CottontailGrpcServer embedded;

  /** The {@link CottontailWrapper} used to establish a database connection. */
  private final CottontailWrapper wrapper;

  /**
   * Constructor.
   */
  public CottontailIntegrationDBProvider() {
    this.embedded = CottontailKt.embedded(COTTONTAIL_TEST_CONFIG);
    try {
        this.wrapper = new CottontailWrapper(WRAPPER_CONFIG_PROVIDER.get().getHost(), WRAPPER_CONFIG_PROVIDER.get().getPort());
    } catch (Throwable e) {
      this.embedded.stop(); /* Stop server to relinquish locks on catalogue. */
      throw e;
    }
  }


  CottontailWrapper getWrapper() {
    return this.wrapper;
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

  @Override
  public void close() {
    this.wrapper.close();
    if (this.embedded.isRunning()) {
      this.embedded.stop();
    }
  }
}
