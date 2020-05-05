package org.vitrivr.cineast.core.db.cottontaildb;

import org.vitrivr.cottontail.grpc.CottonDDLGrpc;
import org.vitrivr.cottontail.grpc.CottonDDLGrpc.CottonDDLBlockingStub;
import org.vitrivr.cottontail.grpc.CottonDDLGrpc.CottonDDLFutureStub;
import org.vitrivr.cottontail.grpc.CottonDMLGrpc;
import org.vitrivr.cottontail.grpc.CottonDMLGrpc.CottonDMLStub;
import org.vitrivr.cottontail.grpc.CottonDQLGrpc;
import org.vitrivr.cottontail.grpc.CottonDQLGrpc.CottonDQLBlockingStub;
import org.vitrivr.cottontail.grpc.CottontailGrpc.BatchedQueryMessage;
import org.vitrivr.cottontail.grpc.CottontailGrpc.CreateIndexMessage;
import org.vitrivr.cottontail.grpc.CottontailGrpc.Empty;
import org.vitrivr.cottontail.grpc.CottontailGrpc.Entity;
import org.vitrivr.cottontail.grpc.CottontailGrpc.EntityDefinition;
import org.vitrivr.cottontail.grpc.CottontailGrpc.InsertMessage;
import org.vitrivr.cottontail.grpc.CottontailGrpc.InsertStatus;
import org.vitrivr.cottontail.grpc.CottontailGrpc.QueryMessage;
import org.vitrivr.cottontail.grpc.CottontailGrpc.QueryResponseMessage;
import org.vitrivr.cottontail.grpc.CottontailGrpc.Schema;
import org.vitrivr.cottontail.grpc.CottontailGrpc.SuccessStatus;
import com.google.common.util.concurrent.ListenableFuture;
import io.grpc.ManagedChannel;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.netty.NettyChannelBuilder;
import io.grpc.stub.StreamObserver;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.config.DatabaseConfig;
import org.vitrivr.cineast.core.util.LogHelper;

public class CottontailWrapper implements AutoCloseable {

  private static final Logger LOGGER = LogManager.getLogger();

  private static final InsertStatus INTERRUPTED_INSERT = InsertStatus.newBuilder().setSuccess(false).build();

  private final ManagedChannel channel;
  private final CottonDDLFutureStub definitionFutureStub;
  private final CottonDMLStub managementStub;
  private final CottonDMLStub insertStub;

  private static final int maxMessageSize = 10_000_000;
  private static final long MAX_QUERY_CALL_TIMEOUT = 300_000; //TODO expose to config
  private static final long MAX_CALL_TIMEOUT = 5000; //TODO expose to config
  private final boolean closeWrapper;

  public CottontailWrapper(DatabaseConfig config, boolean closeWrapper) {
    this.closeWrapper = closeWrapper;
    NettyChannelBuilder builder = NettyChannelBuilder.forAddress(config.getHost(), config.getPort()).maxInboundMessageSize(maxMessageSize);
    if (config.getPlaintext()) {
      builder = builder.usePlaintext();
    }
    this.channel = builder.build();
    LOGGER.info("Connected to Cottontail at {}:{}", config.getHost(), config.getPort());
    this.definitionFutureStub = CottonDDLGrpc.newFutureStub(channel);
    this.managementStub = CottonDMLGrpc.newStub(channel);
    this.insertStub = CottonDMLGrpc.newStub(channel);
  }

  public synchronized ListenableFuture<SuccessStatus> createEntity(EntityDefinition createMessage) {
    final CottonDDLFutureStub stub = CottonDDLGrpc.newFutureStub(this.channel);
    return stub.createEntity(createMessage);
  }

  public synchronized void createEntityBlocking(EntityDefinition createMessage) {
    final CottonDDLBlockingStub stub = CottonDDLGrpc.newBlockingStub(this.channel);
    try {
      stub.createEntity(createMessage);
    } catch (StatusRuntimeException e) {
      if (e.getStatus().getCode() == Status.ALREADY_EXISTS.getCode()) {
        LOGGER.warn("Entity {} was not created because it already exists", createMessage.getEntity().getName());
      } else {
        e.printStackTrace();
      }
    }
  }

  public synchronized void createIndexBlocking(CreateIndexMessage createMessage) {
    final CottonDDLBlockingStub stub = CottonDDLGrpc.newBlockingStub(this.channel);
    try {
      stub.createIndex(createMessage);
    } catch (StatusRuntimeException e) {
      if (e.getStatus().getCode() == Status.ALREADY_EXISTS.getCode()) {
        LOGGER.warn("Index on {}.{} was not created because it already exists", createMessage.getIndex().getEntity().getName(), createMessage.getColumnsList().toString());
        return;
      }
      e.printStackTrace();
    }
  }

  public synchronized void optimizeEntityBlocking(Entity entity) {
    final CottonDDLBlockingStub stub = CottonDDLGrpc.newBlockingStub(this.channel);
    try {
      stub.optimizeEntity(entity);
    } catch (StatusRuntimeException e) {
      e.printStackTrace();
    }
  }

  public synchronized void dropEntityBlocking(Entity entity) {
    final CottonDDLBlockingStub stub = CottonDDLGrpc.newBlockingStub(this.channel);
    try {
      stub.dropEntity(entity);
    } catch (StatusRuntimeException e) {
      if (e.getStatus().getCode() == Status.NOT_FOUND.getCode()) {
        LOGGER.debug("entity {} was not dropped because it does not exist", entity.getName());
      } else {
        e.printStackTrace();
      }
    }
  }

  public synchronized ListenableFuture<SuccessStatus> createSchema(String schama) {
    final CottonDDLFutureStub stub = CottonDDLGrpc.newFutureStub(this.channel);
    return stub.createSchema(CottontailMessageBuilder.schema(schama));
  }

  public synchronized void createSchemaBlocking(String schema) {
    ListenableFuture<SuccessStatus> future = this.createSchema(schema);
    try {
      future.get();
    } catch (InterruptedException | ExecutionException e) {
      LOGGER.error("error in createSchemaBlocking: {}", LogHelper.getStackTrace(e));
    }
  }

  public boolean insert(List<InsertMessage> messages) {

    final boolean[] status = {false, false}; /* {done, error}. */
    final StreamObserver<InsertStatus> observer = new StreamObserver<InsertStatus>() {

      @Override
      public void onNext(InsertStatus value) {
        LOGGER.trace("Tuple received: {}", value.getTimestamp());
      }

      @Override
      public void onError(Throwable t) {
        status[0] = true;
        status[1] = true;
        LOGGER.error("Error during insert. Everything was rolled back: {}", t.getMessage());
      }

      @Override
      public void onCompleted() {
        status[0] = true;
        LOGGER.trace("Insert successful. Changes were committed!");
      }
    };

    /* Start data transfer. */
    final StreamObserver<InsertMessage> sink = this.managementStub.insert(observer);
    for (InsertMessage message : messages) {
      sink.onNext(message);
    }
    sink.onCompleted(); /* Send commit message. */

    while (!status[0]) {
      Thread.yield();
    }
    return !status[1];
  }

  /**
   * Issues a single query to the Cottontail DB endpoint in a blocking fashion.
   *
   * @return The query results (unprocessed).
   */
  public List<QueryResponseMessage> query(QueryMessage query) {
    final ArrayList<QueryResponseMessage> results = new ArrayList<>();
    final CottonDQLBlockingStub stub = CottonDQLGrpc.newBlockingStub(this.channel).withDeadlineAfter(MAX_QUERY_CALL_TIMEOUT, TimeUnit.MILLISECONDS);
    try {
      stub.query(query).forEachRemaining(results::add);
    } catch (StatusRuntimeException e) {
      if (e.getStatus() == Status.DEADLINE_EXCEEDED) {
        LOGGER.error("CottontailWrapper.query has timed out (timeout = {}ms).", MAX_QUERY_CALL_TIMEOUT);
      } else {
        LOGGER.error("Error occurred during invocation of CottontailWrapper.query: {}", e.getMessage());
      }
    }
    return results;
  }

  /**
   * Issues a batched query to the Cottontail DB endpoint in a blocking fashion.
   *
   * @return The query results (unprocessed).
   */
  public List<QueryResponseMessage> batchedQuery(BatchedQueryMessage query) {
    final ArrayList<QueryResponseMessage> results = new ArrayList<>();
    final CottonDQLBlockingStub stub = CottonDQLGrpc.newBlockingStub(this.channel).withDeadlineAfter(MAX_QUERY_CALL_TIMEOUT, TimeUnit.MILLISECONDS);
    try {
      stub.batchedQuery(query).forEachRemaining(results::add);
    } catch (StatusRuntimeException e) {
      if (e.getStatus() == Status.DEADLINE_EXCEEDED) {
        LOGGER.error("CottontailWrapper.batchedQuery has timed out (timeout = {}ms).", MAX_QUERY_CALL_TIMEOUT);
      } else {
        LOGGER.error("Error occurred during invocation of CottontailWrapper.batchedQuery: {}", e.getMessage());
      }
    }
    return results;
  }

  /**
   * Pings the Cottontail DB endpoint and returns true on success and false otherwise.
   *
   * @return True on success, false otherwise.
   */
  public boolean ping() {
    final CottonDQLBlockingStub stub = CottonDQLGrpc.newBlockingStub(this.channel).withDeadlineAfter(MAX_CALL_TIMEOUT, TimeUnit.MILLISECONDS);
    try {
      final SuccessStatus status = stub.ping(Empty.getDefaultInstance());
      return true;
    } catch (StatusRuntimeException e) {
      if (e.getStatus() == Status.DEADLINE_EXCEEDED) {
        LOGGER.error("CottontailWrapper.ping has timed out.");
      } else {
        LOGGER.error("Error occurred during invocation of CottontailWrapper.ping: {}", e.getMessage());

      }
      return false;
    }
  }

  /**
   * Uses the Cottontail DB endpoint to list all entities.
   *
   * @param schema Schema for which to list entities.
   * @return List of entities.
   */
  public List<Entity> listEntities(Schema schema) {
    ArrayList<Entity> entities = new ArrayList<>();
    final CottonDDLBlockingStub stub = CottonDDLGrpc.newBlockingStub(this.channel).withDeadlineAfter(MAX_CALL_TIMEOUT, TimeUnit.MILLISECONDS);
    try {
      stub.listEntities(schema).forEachRemaining(entities::add);
    } catch (StatusRuntimeException e) {
      if (e.getStatus() == Status.DEADLINE_EXCEEDED) {
        LOGGER.error("CottontailWrapper.listEntities has timed out (timeout = {}ms).", MAX_CALL_TIMEOUT);
      } else {
        LOGGER.error("Error occurred during invocation of CottontailWrapper.listEntities: {}", e.getMessage());
      }
    }
    return entities;
  }

  /**
   *
   */
  @Override
  public void close() {
    if (closeWrapper) {
      LOGGER.info("Closing connection to cottontail");
      this.channel.shutdown();
    }
  }
}
