package org.vitrivr.cineast.core.db.cottontaildb;

import static org.vitrivr.cineast.core.db.cottontaildb.CottontailMessageBuilder.CINEAST_SCHEMA;
import static org.vitrivr.cineast.core.db.cottontaildb.CottontailMessageBuilder.entity;

import com.google.protobuf.Empty;
import org.apache.commons.lang3.time.StopWatch;

import org.vitrivr.cottontail.grpc.CottontailGrpc;
import org.vitrivr.cottontail.grpc.CottontailGrpc.*;
import com.google.common.util.concurrent.ListenableFuture;
import io.grpc.ManagedChannel;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.netty.NettyChannelBuilder;
import io.grpc.stub.StreamObserver;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.config.DatabaseConfig;
import org.vitrivr.cineast.core.util.LogHelper;
import org.vitrivr.cottontail.grpc.DDLGrpc;
import org.vitrivr.cottontail.grpc.DDLGrpc.DDLBlockingStub;
import org.vitrivr.cottontail.grpc.DDLGrpc.DDLFutureStub;
import org.vitrivr.cottontail.grpc.DMLGrpc;
import org.vitrivr.cottontail.grpc.DMLGrpc.DMLStub;
import org.vitrivr.cottontail.grpc.DQLGrpc;
import org.vitrivr.cottontail.grpc.DQLGrpc.DQLBlockingStub;

public class CottontailWrapper implements AutoCloseable {

  private static final Logger LOGGER = LogManager.getLogger();

  private final ManagedChannel channel;
  private final DDLFutureStub definitionFutureStub;
  private final DMLStub managementStub;
  private final DMLStub insertStub;
  private final HashSet<String> ensuredSchemas = new HashSet<>();

  private static final int maxMessageSize = 10_000_000;
  private static final long MAX_QUERY_CALL_TIMEOUT = 300_000; //TODO expose to config
  private static final long MAX_CALL_TIMEOUT = 5000; //TODO expose to config
  private final boolean closeWrapper;

  public CottontailWrapper(DatabaseConfig config, boolean closeWrapper) {
    StopWatch watch = StopWatch.createStarted();
    this.closeWrapper = closeWrapper;
    NettyChannelBuilder builder = NettyChannelBuilder.forAddress(config.getHost(), config.getPort()).maxInboundMessageSize(maxMessageSize);
    if (config.getPlaintext()) {
      builder = builder.usePlaintext();
    }
    this.channel = builder.build();
    this.definitionFutureStub = DDLGrpc.newFutureStub(channel);
    this.managementStub = DMLGrpc.newStub(channel);
    this.insertStub = DMLGrpc.newStub(channel);
    watch.stop();
    LOGGER.info("Connected to Cottontail in {} ms at {}:{}", watch.getTime(TimeUnit.MILLISECONDS), config.getHost(), config.getPort());
  }

  public synchronized ListenableFuture<CottontailGrpc.QueryResponseMessage> createEntity(EntityDefinition definition) {
    final DDLFutureStub stub = DDLGrpc.newFutureStub(this.channel);
    return stub.createEntity(CreateEntityMessage.newBuilder().setDefinition(definition).build());
  }

  public synchronized boolean createEntityBlocking(EntityDefinition defintion) {
    final DDLBlockingStub stub = DDLGrpc.newBlockingStub(this.channel);
    try {
      stub.createEntity(CreateEntityMessage.newBuilder().setDefinition(defintion).build());
      return true;
    } catch (StatusRuntimeException e) {
      if (e.getStatus().getCode() == Status.ALREADY_EXISTS.getCode()) {
        LOGGER.warn("Entity {} was not created because it already exists", defintion.getEntity().getName());
      } else {
        e.printStackTrace();
      }
    }
    return false;
  }

  public synchronized boolean createIndexBlocking(IndexDefinition definition) {
    final DDLBlockingStub stub = DDLGrpc.newBlockingStub(this.channel);
    try {
      stub.createIndex(CreateIndexMessage.newBuilder().setDefinition(definition).build());
      return true;
    } catch (StatusRuntimeException e) {
      if (e.getStatus().getCode() == Status.ALREADY_EXISTS.getCode()) {
        LOGGER.warn("Index on {} was not created because it already exists.", definition.getName().getName());
        return false;
      }
      e.printStackTrace();
    }
    return false;
  }

  public synchronized boolean dropIndexBlocking(IndexName index) {
    final DDLBlockingStub stub = DDLGrpc.newBlockingStub(this.channel);
    try {
      stub.dropIndex(DropIndexMessage.newBuilder().setIndex(index).build());
      return true;
    } catch (StatusRuntimeException e) {
      if (e.getStatus() == Status.NOT_FOUND) {
        LOGGER.warn("Index {} was not dropped because it does not exist", index.getName());
        return false;
      }
      e.printStackTrace();
    }
    return false;
  }

  public synchronized boolean optimizeEntityBlocking(EntityName entity) {
    final DDLBlockingStub stub = DDLGrpc.newBlockingStub(this.channel);
    try {
      stub.optimizeEntity(OptimizeEntityMessage.newBuilder().setEntity(entity).build());
      return true;
    } catch (StatusRuntimeException e) {
      e.printStackTrace();
    }
    return false;
  }

  public synchronized boolean dropEntityBlocking(EntityName entity) {
    final DDLBlockingStub stub = DDLGrpc.newBlockingStub(this.channel);
    try {
      stub.dropEntity(DropEntityMessage.newBuilder().setEntity(entity).build());
      return true;
    } catch (StatusRuntimeException e) {
      if (e.getStatus().getCode() == Status.NOT_FOUND.getCode()) {
        LOGGER.debug("entity {} was not dropped because it does not exist", entity.getName());
      } else {
        e.printStackTrace();
      }
    }
    return false;
  }

  public synchronized boolean createSchemaBlocking(SchemaName schama) {
    final DDLBlockingStub stub = DDLGrpc.newBlockingStub(this.channel);
    try {
      stub.createSchema(CreateSchemaMessage.newBuilder().setSchema(schama).build());
      return true;
    } catch (StatusRuntimeException e) {
      if (e.getStatus().getCode() == Status.ALREADY_EXISTS.getCode()) {
        LOGGER.debug("Schema {} was not created because it already exists.", schama.getName());
      } else {
        e.printStackTrace();
      }
    }
    return false;
  }

  public synchronized void ensureSchemaBlocking(String schema) {
    if (this.ensuredSchemas.contains(schema)) {
      return;
    }
    final DDLBlockingStub stub = DDLGrpc.newBlockingStub(this.channel);
    final Iterator<QueryResponseMessage> existingSchemas = stub.listSchemas(ListSchemaMessage.newBuilder().build());
    final boolean[] schemaExists = new boolean[] { false };
    existingSchemas.forEachRemaining(s -> {
      s.getTuplesList().forEach(t -> {
        if (t.getData(0).getStringData().split("\\.")[1].equals(schema)) {
          schemaExists[0] = true;
        }
      });
    });
    if (!schemaExists[0]) {
      this.createSchemaBlocking(SchemaName.newBuilder().setName(schema).build());
    }
    this.ensuredSchemas.add(schema);
  }

  public boolean insert(List<InsertMessage> messages) {
    return false;
  }

  /**
   * Issues a single query to the Cottontail DB endpoint in a blocking fashion.
   *
   * @return The query results (unprocessed).
   */
  public List<QueryResponseMessage> query(QueryMessage query) {
    StopWatch watch = StopWatch.createStarted();
    final ArrayList<QueryResponseMessage> results = new ArrayList<>();
    final DQLBlockingStub stub = DQLGrpc.newBlockingStub(this.channel).withDeadlineAfter(MAX_QUERY_CALL_TIMEOUT, TimeUnit.MILLISECONDS);
    try {
      stub.query(query).forEachRemaining(results::add);
    } catch (StatusRuntimeException e) {
      if (e.getStatus() == Status.DEADLINE_EXCEEDED) {
        LOGGER.error("CottontailWrapper.query has timed out (timeout = {}ms).", MAX_QUERY_CALL_TIMEOUT);
      } else {
        LOGGER.error("Error occurred during invocation of CottontailWrapper.query: {}", e.getMessage());
      }
    }
    LOGGER.trace("Wall time for query {} is {} ms", query.getTxId().getQueryId(), watch.getTime());
    return results;
  }

  /**
   * Pings the Cottontail DB endpoint and returns true on success and false otherwise.
   *
   * @return True on success, false otherwise.
   */
  public boolean ping() {
    final DQLBlockingStub stub = DQLGrpc.newBlockingStub(this.channel).withDeadlineAfter(MAX_CALL_TIMEOUT, TimeUnit.MILLISECONDS);
    try {
      stub.ping(Empty.getDefaultInstance());
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
  public List<EntityName> listEntities(SchemaName schema) {
    final ArrayList<EntityName> entities = new ArrayList<>();
    final DDLBlockingStub stub = DDLGrpc.newBlockingStub(this.channel).withDeadlineAfter(MAX_CALL_TIMEOUT, TimeUnit.MILLISECONDS);
    try {
      stub.listEntities(ListEntityMessage.newBuilder().setSchema(schema).build()).forEachRemaining(r -> r.getTuplesList().forEach(t -> entities.add(entity(t.getData(0).getStringData()))));
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

  public boolean existsEntity(String name) {
    final List<EntityName> entities = this.listEntities(CINEAST_SCHEMA);
    for (EntityName entity : entities) {
      if (entity.getName().equals(name)) {
        return true;
      }
    }
    return false;
  }
}
