package org.vitrivr.cineast.core.db.cottontaildb;

import java.nio.file.Paths;
import java.util.function.Supplier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.config.DatabaseConfig;
import org.vitrivr.cottontail.CottontailKt;
import org.vitrivr.cottontail.config.CacheConfig;
import org.vitrivr.cottontail.config.Config;
import org.vitrivr.cottontail.config.ExecutionConfig;
import org.vitrivr.cottontail.config.MapDBConfig;
import org.vitrivr.cottontail.config.ServerConfig;
import org.vitrivr.cottontail.server.grpc.CottontailGrpcServer;

/**
 * Embedded cottontaildb instance for testing purpose as a singleton
 */
public class EmbeddedCottontailDB {

  private static final Logger LOGGER = LogManager.getLogger(EmbeddedCottontailDB.class);

  public static EmbeddedCottontailDB getInstance(){
    if(instance == null){
      instance = new EmbeddedCottontailDB();
    }
    return instance;
  }

  private static EmbeddedCottontailDB instance = null;

  static final Config COTTONTAIL_TEST_CONFIG = new Config(
      Paths.get("./build/data/cottontaildb-data/"),
      false,
      false,
      null,
      new MapDBConfig(),
      new ServerConfig(),
      new ExecutionConfig(),
      new CacheConfig()
  );

  static final Supplier<DatabaseConfig> WRAPPER_CONFIG_PROVIDER = () -> {
    DatabaseConfig config = new DatabaseConfig();
    config.setHost("localhost");
    config.setPort(COTTONTAIL_TEST_CONFIG.getServer().getPort());
    return config;
  };

  private CottontailGrpcServer embedded;
  private final Thread dbThread;
  private final CottontailWrapper wrapper;

  private EmbeddedCottontailDB(){
    dbThread = new Thread(() -> {
      embedded = CottontailKt.embedded(COTTONTAIL_TEST_CONFIG);
    });
    dbThread.setName("Embedded DB Trhead");
    dbThread.setDaemon(true);
    dbThread.start();
    wrapper = new CottontailWrapper(WRAPPER_CONFIG_PROVIDER.get() );
    boolean ping = wrapper.client.ping();
    LOGGER.info("Embedded DB and wrapper started. Ping: "+ping);
  }

  public CottontailWrapper getWrapper(){
    return wrapper;
  }

  public void stop(){
    this.embedded.stop();
    this.wrapper.client.close();
  }
}
