package org.vitrivr.cineast.core.db.cottontaildb;

import io.grpc.ConnectivityState;
import io.grpc.ManagedChannel;
import io.grpc.netty.NettyChannelBuilder;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import kotlin.jvm.Synchronized;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cottontail.client.SimpleClient;

public final class CottontailWrapper implements AutoCloseable {

  public static final String CINEAST_SCHEMA = "cineast";
  public static final String WARREN_PREFIX = "warren";
  public static final String FQN_CINEAST_SCHEMA = WARREN_PREFIX + "." + CINEAST_SCHEMA;
  private static final Logger LOGGER = LogManager.getLogger();
  /**
   * Internal connection pool to re-use managed channels.
   */
  private static final Map<String, ManagedChannel> POOL = new HashMap<>();
  /**
   * The {@link SimpleClient} instance that facilitates access to Cottontail DB.
   */
  public final SimpleClient client;

  public CottontailWrapper(String host, int port) {
    StopWatch watch = StopWatch.createStarted();
    this.client = new SimpleClient(sharedChannel(host, port));
    boolean pingSuccessful = this.client.ping();
    watch.stop();
    if (pingSuccessful) {
      LOGGER.info("Connected to Cottontail DB in {} ms at {}:{}", watch.getTime(TimeUnit.MILLISECONDS), host, port);
    } else {
      LOGGER.warn("Could not ping Cottontail DB instance at {}:{}", host, port);
    }
  }

  /**
   * Returns a {@link ManagedChannel} object for the given database configuration.
   * <p>
   * Tries to re-use existing {@link ManagedChannel} objects. Currently, {@link ManagedChannel} are kept alive as long as Cineast runs.
   *
   * @param host Hostname of the Cottontail DB server.
   * @param port Port of the Cottontail DB server.
   * @return {@link ManagedChannel}
   */
  @Synchronized
  private static ManagedChannel sharedChannel(String host, int port) {
    final String key = host + ":" + port;
    ManagedChannel channel = POOL.get(key);
    if (channel != null) {
      final ConnectivityState state = channel.getState(true);
      if (state == ConnectivityState.TRANSIENT_FAILURE || state == ConnectivityState.SHUTDOWN) {
        channel.shutdownNow(); /* Close old channel. */
        channel = createChannel(host, port);
        POOL.put(key, channel);
      }
    } else {
      channel = createChannel(host, port);
      POOL.put(key, channel);
    }
    return channel;
  }

  /**
   * Returns a new {@link ManagedChannel} object for the  given database configuration.
   *
   * @param host Hostname of the Cottontail DB server.
   * @param port Port of the Cottontail DB server.
   * @return {@link ManagedChannel}
   */
  private static ManagedChannel createChannel(String host, int port) {
    LOGGER.debug("Starting to connect to Cottontail DB at {}:{}", host, port);
    final NettyChannelBuilder builder = NettyChannelBuilder.forAddress(host, port).usePlaintext();
    final ManagedChannel channel = builder.build();
    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      LOGGER.info("Closing connection to Cottontail DB.");
      channel.shutdownNow();
    }));
    return channel;
  }

  public String fqnInput(String entity) {
    return CINEAST_SCHEMA + "." + entity;
  }

  public String fqnOutput(String entity) {
    return FQN_CINEAST_SCHEMA + "." + entity;
  }

  /**
   * Closes this {@link CottontailWrapper}.
   */
  @Override
  public void close() { /* No op. */ }
}
