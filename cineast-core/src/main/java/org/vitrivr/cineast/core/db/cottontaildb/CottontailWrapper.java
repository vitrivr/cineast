package org.vitrivr.cineast.core.db.cottontaildb;

import org.apache.commons.lang3.time.StopWatch;
import org.vitrivr.cottontail.client.BatchInsertClient;
import org.vitrivr.cottontail.client.stub.SimpleClient;
import io.grpc.ManagedChannel;
import io.grpc.netty.NettyChannelBuilder;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.config.DatabaseConfig;

public final class CottontailWrapper implements AutoCloseable {

  private static final Logger LOGGER = LogManager.getLogger();

  public static final String CINEAST_SCHEMA = "cineast";

  /** The {@link ManagedChannel} used for communication. */
  public final ManagedChannel channel;

  /** The {@link SimpleClient} instance that facilitates access to Cottontail DB. */
  public final SimpleClient client;

  /** The {@link BatchInsertClient} instance that facilitates batch inserts to Cottontail DB. */
  public final BatchInsertClient batchClient;

  /** Flag indicating that his {@link CottontailWrapper}'s {@link ManagedChannel} should be kept open. */
  public final boolean keepOpen;

  public String fqn(String entity) {
    return CINEAST_SCHEMA + "." + entity;
  }

  public CottontailWrapper(DatabaseConfig config, boolean keepOpen) {
    StopWatch watch = StopWatch.createStarted();
    this.keepOpen = keepOpen;
    LOGGER.debug("Starting to connect to cottontail at {}:{}", config.getHost(), config.getPort());
    final NettyChannelBuilder builder = NettyChannelBuilder.forAddress(config.getHost(), config.getPort());
    if (config.getPlaintext()) {
      builder.usePlaintext();
    }
    this.channel = builder.build();
    this.client = new SimpleClient(this.channel);
    this.batchClient = new BatchInsertClient(config.getHost(), config.getPort());
    watch.stop();
    LOGGER.info("Connected to Cottontail in {} ms at {}:{}", watch.getTime(TimeUnit.MILLISECONDS), config.getHost(), config.getPort());
  }

  /**
   * Closes this {@link CottontailWrapper}.
   */
  @Override
  public void close() {
    if (!this.keepOpen) {
      LOGGER.info("Closing connection to Cottontail DB.");
      try {
        this.channel.shutdown();
        this.channel.awaitTermination(5000, TimeUnit.MILLISECONDS);
      } catch (InterruptedException e) {
        LOGGER.warn("Thread was interrupted while waiting for gRPC channel to close (timeout = 5s).");
      }
    }
  }
}
