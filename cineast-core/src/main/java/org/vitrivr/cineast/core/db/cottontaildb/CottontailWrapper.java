package org.vitrivr.cineast.core.db.cottontaildb;

import static org.vitrivr.cineast.core.db.cottontaildb.CottontailMessageBuilder.entity;

import com.google.protobuf.Empty;
import org.apache.commons.lang3.time.StopWatch;

import org.vitrivr.cottontail.grpc.CottontailGrpc.*;
import io.grpc.ManagedChannel;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.netty.NettyChannelBuilder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.config.DatabaseConfig;
import org.vitrivr.cottontail.grpc.DDLGrpc;
import org.vitrivr.cottontail.grpc.DDLGrpc.DDLBlockingStub;
import org.vitrivr.cottontail.grpc.DMLGrpc;
import org.vitrivr.cottontail.grpc.DMLGrpc.DMLBlockingStub;
import org.vitrivr.cottontail.grpc.DQLGrpc;
import org.vitrivr.cottontail.grpc.DQLGrpc.DQLBlockingStub;

public class CottontailWrapper implements AutoCloseable {

  private static final Logger LOGGER = LogManager.getLogger();

  private final ManagedChannel channel;
  private final HashSet<String> ensuredSchemas = new HashSet<>();

  private static final int maxMessageSize = 10_000_000;
  private static final long MAX_QUERY_CALL_TIMEOUT = 300_000; //TODO expose to config
  private static final long MAX_CALL_TIMEOUT = 5000; //TODO expose to config
  private final boolean closeWrapper;

  private final DDLBlockingStub ddlStub;
  private final DMLBlockingStub dmlStub;
  private final DQLBlockingStub dqlStub;

  public CottontailWrapper(DatabaseConfig config, boolean closeWrapper) {
    StopWatch watch = StopWatch.createStarted();
    this.closeWrapper = closeWrapper;
    NettyChannelBuilder builder = NettyChannelBuilder.forAddress(config.getHost(), config.getPort()).maxInboundMessageSize(maxMessageSize);
    if (config.getPlaintext()) {
      builder = builder.usePlaintext();
    }
    this.channel = builder.build();
    this.ddlStub = DDLGrpc.newBlockingStub(this.channel);
    this.dmlStub = DMLGrpc.newBlockingStub(this.channel);
    this.dqlStub = DQLGrpc.newBlockingStub(this.channel).withDeadlineAfter(MAX_QUERY_CALL_TIMEOUT, TimeUnit.MILLISECONDS);
    watch.stop();
    LOGGER.info("Connected to Cottontail in {} ms at {}:{}", watch.getTime(TimeUnit.MILLISECONDS), config.getHost(), config.getPort());
  }

  /**
   * Creates and schema in Cottontail DB.
   *
   * @param schema The {@link SchemaName}.
   * @return True if entity was created, false otherwise.
   */
  public synchronized boolean createSchema(SchemaName schema) {
    try {
      this.ddlStub.createSchema(CreateSchemaMessage.newBuilder().setSchema(schema).build());
      return true;
    } catch (StatusRuntimeException e) {
      if (e.getStatus().getCode() == Status.ALREADY_EXISTS.getCode()) {
        LOGGER.debug("Schema {} was not created because it already exists.", schema.getName());
      } else {
        LOGGER.error("Error occurred during invocation of CottontailWrapper.createSchema: {}", e.getMessage());
      }
      return false;
    }
  }

  /**
   * Creates and entity in Cottontail DB.
   *
   * @param definition The {@link EntityDefinition}.
   * @return True if entity was created, false otherwise.
   */
  public synchronized boolean createEntity(EntityDefinition definition) {
    try {
      this.ddlStub.createEntity(CreateEntityMessage.newBuilder().setDefinition(definition).build());
      return true;
    } catch (StatusRuntimeException e) {
      if (e.getStatus().getCode() == Status.ALREADY_EXISTS.getCode()) {
        LOGGER.warn("Entity {} was not created because it already exists", definition.getEntity().getName());
      } else if (e.getStatus().getCode() == Status.NOT_FOUND.getCode()) {
        LOGGER.warn("Entity {} was not created because schema does not exist.", definition.getEntity().getName());
      } else {
        LOGGER.error("Error occurred during invocation of CottontailWrapper.createEntity: {}", e.getMessage());
      }
      return false;
    }
  }

  /**
   * Drops an entity in Cottontail DB.
   *
   * @param entity The {@link EntityName}.
   * @return True if entity was dropped, false otherwise.
   */
  public synchronized boolean dropEntity(EntityName entity) {
    try {
      this.ddlStub.dropEntity(DropEntityMessage.newBuilder().setEntity(entity).build());
      return true;
    } catch (StatusRuntimeException e) {
      if (e.getStatus().getCode() == Status.NOT_FOUND.getCode()) {
        LOGGER.debug("Entity {} was not dropped because either it does not exist", entity.getName());
      } else {
        LOGGER.error("Error occurred during invocation of CottontailWrapper.dropEntity: {}", e.getMessage());
      }
      return false;
    }
  }

  /**
   * Optimizes an entity in Cottontail DB.
   *
   * @param entity The {@link EntityName}.
   * @return True if entity was optimized, false otherwise.
   */
  public synchronized boolean optimizeEntity(EntityName entity) {
    try {
      this.ddlStub.optimizeEntity(OptimizeEntityMessage.newBuilder().setEntity(entity).build());
      return true;
    } catch (StatusRuntimeException e) {
      if (e.getStatus().getCode() == Status.NOT_FOUND.getCode()) {
        LOGGER.debug("Entity {} was not optimized because it does not exist", entity.getName());
      } else {
        LOGGER.error("Error occurred during invocation of CottontailWrapper.optimizeIndex: {}", e.getMessage());
      }
      return false;
    }
  }

  /**
   * Creates an index in Cottontail DB in a blocking fashion.
   *
   * @param definition The {@link IndexDefinition}.
   * @return True if index was created, false otherwise.
   */
  public synchronized boolean createIndex(IndexDefinition definition) {
    try {
      this.ddlStub.createIndex(CreateIndexMessage.newBuilder().setDefinition(definition).build());
      return true;
    } catch (StatusRuntimeException e) {
      if (e.getStatus().getCode() == Status.ALREADY_EXISTS.getCode()) {
        LOGGER.warn("Index on {} was not created because it already exists.", definition.getName().getName());
      } else if (e.getStatus().getCode() == Status.NOT_FOUND.getCode()) {
        LOGGER.warn("Index on {} was not created because either entity or schema does not exist.", definition.getName().getName());
      } else {
        LOGGER.error("Error occurred during invocation of CottontailWrapper.createIndex: {}", e.getMessage());
      }
      return false;
    }
  }

  /**
   * Drops an index in Cottontail DB in a blocking fashion.
   *
   * @param index The {@link IndexName}.
   * @return True if index was created, false otherwise.
   */
  public synchronized boolean dropIndex(IndexName index) {
    try {
      this.ddlStub.dropIndex(DropIndexMessage.newBuilder().setIndex(index).build());
      return true;
    } catch (StatusRuntimeException e) {
      if (e.getStatus() == Status.NOT_FOUND) {
        LOGGER.warn("Index {} was not dropped because it does not exist", index.getName());
      } else {
        LOGGER.error("Error occurred during invocation of CottontailWrapper.dropIndex: {}", e.getMessage());
      }
      return false;
    }
  }

  /**
   * Ensures existence of schema with given name.
   *
   * @param schema Name of the schema.
   */
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
      this.createSchema(SchemaName.newBuilder().setName(schema).build());
    }
    this.ensuredSchemas.add(schema);
  }

  /**
   * Issues a single query to the Cottontail DB endpoint in a blocking fashion.
   *
   * @return The query results (unprocessed).
   */
  public List<QueryResponseMessage> query(QueryMessage query) {
    StopWatch watch = StopWatch.createStarted();
    final ArrayList<QueryResponseMessage> results = new ArrayList<>();
    try {
      this.dqlStub.query(query).forEachRemaining(results::add);
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
   * Inserts a list of {@link InsertMessage}s into Cottontail DB.
   *
   * TODO: Currently, each insert is committed immediately. One could use Transaction support here.
   *
   * @param messages List of {@link InsertMessage}
   * @return true upon success.
   */
  public boolean insert(List<InsertMessage> messages) {
    try {
      for (final InsertMessage m : messages) {
        final QueryResponseMessage result = this.dmlStub.insert(m);
      }
      return true;
    } catch (StatusRuntimeException e) {
      if (e.getStatus() == Status.NOT_FOUND) {
        LOGGER.warn("Insert failed because specified entity does not exist.");
      } else {
        LOGGER.error("Error occurred during invocation of CottontailWrapper.insert: {}", e.getMessage());
      }
      return false;
    }
  }

  /**
   * Pings the Cottontail DB endpoint and returns true on success and false otherwise.
   *
   * @return True on success, false otherwise.
   */
  public boolean ping() {
    try {
      this.dqlStub.ping(Empty.getDefaultInstance());
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
    try {
      this.ddlStub.listEntities(ListEntityMessage.newBuilder().setSchema(schema).build()).forEachRemaining(
          r -> r.getTuplesList().forEach(t -> entities.add(entity(t.getData(0).getStringData().split("\\.")[2])))
      );
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
   * Checks if entity with the given name exists.
   *
   * @param name {@link EntityName} to check.
   * @return List of entities.
   */
  public boolean existsEntity(EntityName name) {
    final EntityDetailsMessage message = EntityDetailsMessage.newBuilder().setEntity(name).build();
    try {
      final QueryResponseMessage responseMessage = this.ddlStub.entityDetails(message);
      return responseMessage.getTuplesCount() > 0;
    } catch (StatusRuntimeException e) {
      if (e.getStatus() != Status.NOT_FOUND) {
        LOGGER.error("Error occurred during invocation of CottontailWrapper.entityDetails: {}", e.getMessage());
      }
      return false;
    }
  }

  /**
   * Closes this {@link CottontailWrapper}.
   */
  @Override
  public void close() {
    if (this.closeWrapper) {
      LOGGER.info("Closing connection to cottontail");
      this.channel.shutdown();
    }
  }
}
