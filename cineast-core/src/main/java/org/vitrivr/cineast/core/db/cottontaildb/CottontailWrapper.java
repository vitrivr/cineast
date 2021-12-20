package org.vitrivr.cineast.core.db.cottontaildb;

import org.apache.commons.lang3.time.StopWatch;
import io.grpc.ManagedChannel;
import io.grpc.netty.NettyChannelBuilder;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.config.DatabaseConfig;
import org.vitrivr.cottontail.client.SimpleClient;

public final class CottontailWrapper implements AutoCloseable {

  private static final Logger LOGGER = LogManager.getLogger();

  public static final String CINEAST_SCHEMA = "cineast";
  public static final String WARREN_PREFIX = "warren";
  public static final String FQN_CINEAST_SCHEMA = WARREN_PREFIX + "." + CINEAST_SCHEMA;

  /** Internal connection pool to re-use managed channels. */
  private static final Map<String, ManagedChannel> POOL = new ConcurrentHashMap<>(); /* TODO: Reference counting? */

  /**
   * Returns a {@link ManagedChannel} object for the given {@link DatabaseConfig}.
   *
   * @param host Hostname of the Cottontail DB server.
   * @param port Port of the Cottontail DB server.
   * @return {@link ManagedChannel}
   */
  private static ManagedChannel sharedChannel(String host, int port) {
    return POOL.computeIfAbsent((host + ":" + port), (key) -> {
      final StopWatch watch = StopWatch.createStarted();
      LOGGER.debug("Starting to connect to Cottontail DB at {}:{}", host, port);
      final NettyChannelBuilder builder = NettyChannelBuilder.forAddress(host, port).usePlaintext();
      final ManagedChannel channel = builder.build();
      Runtime.getRuntime().addShutdownHook(new Thread(() -> {
        LOGGER.info("Closing connection to Cottontail DB.");
        channel.shutdownNow();
      }));
      watch.stop();
      LOGGER.info("Connected to Cottontail DB in {} ms at {}:{}", watch.getTime(TimeUnit.MILLISECONDS), host, port);
      return channel;
    });
  }
  /**
   * The {@link SimpleClient} instance that facilitates access to Cottontail DB.
   */
  public final SimpleClient client;

  public String fqnInput(String entity) {
    return CINEAST_SCHEMA + "." + entity;
  }

  public String fqnOutput(String entity) {
    return FQN_CINEAST_SCHEMA + "." + entity;
  }

  public CottontailWrapper(String host, int port) {
    this.client = new SimpleClient(sharedChannel(host, port));
  }

  /**
   * Closes this {@link CottontailWrapper}.
   */
  @Override
  public void close() { /* No op. */ }
}
