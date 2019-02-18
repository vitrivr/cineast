package org.vitrivr.cineast.core.db.cottontaildb;

import ch.unibas.dmi.dbis.cottontail.grpc.CottonDDLGrpc;
import ch.unibas.dmi.dbis.cottontail.grpc.CottonDDLGrpc.CottonDDLFutureStub;
import ch.unibas.dmi.dbis.cottontail.grpc.CottonDMLGrpc;
import ch.unibas.dmi.dbis.cottontail.grpc.CottonDMLGrpc.CottonDMLFutureStub;
import ch.unibas.dmi.dbis.cottontail.grpc.CottonDQLGrpc;
import ch.unibas.dmi.dbis.cottontail.grpc.CottonDQLGrpc.CottonDQLFutureStub;
import ch.unibas.dmi.dbis.cottontail.grpc.CottonDQLGrpc.CottonDQLStub;
import ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.CreateEntityMessage;
import ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Entity;
import ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.InsertMessage;
import ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.InsertStatus;
import ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Query;
import ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.QueryMessage;
import ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.QueryResponseMessage;
import ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Schema;
import ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.SuccessStatus;
import com.google.common.util.concurrent.ListenableFuture;
import io.grpc.ManagedChannel;
import io.grpc.netty.NettyChannelBuilder;
import io.grpc.stub.StreamObserver;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.config.Config;
import org.vitrivr.cineast.core.config.DatabaseConfig;
import org.vitrivr.cineast.core.util.LogHelper;

public class CottontailWrapper implements AutoCloseable{

  private static final Logger LOGGER = LogManager.getLogger();

  private static final InsertStatus INTERRUPTED_INSERT = InsertStatus.newBuilder().setSuccess(false).build();

  private ManagedChannel channel;
  private final CottonDDLFutureStub definitionStub;
  private final CottonDMLFutureStub managementStub;
  private final CottonDQLStub queryStub;

  private static final int maxMessageSize = 10_000_000;
  private static final long maxCallTimeOutMs = 300_000; //TODO expose to config

  public CottontailWrapper(){
    DatabaseConfig config = Config.sharedConfig().getDatabase();
    this.channel = NettyChannelBuilder.forAddress(config.getHost(), config.getPort()).usePlaintext(config.getPlaintext()).maxInboundMessageSize(maxMessageSize).build();
    this.definitionStub = CottonDDLGrpc.newFutureStub(channel);
    this.managementStub = CottonDMLGrpc.newFutureStub(channel);
    this.queryStub = CottonDQLGrpc.newStub(channel);
  }

  public synchronized ListenableFuture<SuccessStatus> createEntity(CreateEntityMessage createMessage){
    return this.definitionStub.createEntity(createMessage);
  }

  public synchronized void createEntityBlocking(CreateEntityMessage createMessage){
    ListenableFuture<SuccessStatus> future = this.createEntity(createMessage);
    try {
      future.get();
    } catch (InterruptedException | ExecutionException e) {
      LOGGER.error("error in createEntityBlocking: {}", LogHelper.getStackTrace(e));
    }
  }

  public synchronized ListenableFuture<SuccessStatus> createSchema(String schama){
    return this.definitionStub.createSchema(CottontailMessageBuilder.schema(schama));
  }

  public synchronized void createSchemaBlocking(String schema){
    ListenableFuture<SuccessStatus> future = this.createSchema(schema);
    try {
      future.get();
    } catch (InterruptedException | ExecutionException e) {
      LOGGER.error("error in createSchemaBlocking: {}", LogHelper.getStackTrace(e));
    }
  }

  public ListenableFuture<InsertStatus> insert(InsertMessage message){
    return this.managementStub.insert(message);
  }

  public InsertStatus insertBlocking(InsertMessage message){
    ListenableFuture<InsertStatus> future = this.insert(message);
    try {
      return future.get();
    } catch (InterruptedException | ExecutionException e) {
      LOGGER.error("error in insertBlocking: {}", LogHelper.getStackTrace(e));
      return INTERRUPTED_INSERT;
    }
  }

  @Override
  public void close() {
    this.channel.shutdown();
  }

  public List<QueryResponseMessage> query(QueryMessage query) {

    ArrayList<QueryResponseMessage> results = new ArrayList<>();
    Semaphore semaphore = new Semaphore(1);

    StreamObserver<QueryResponseMessage> observer = new StreamObserver<QueryResponseMessage>() {
      @Override
      public void onNext(QueryResponseMessage value) {
        results.add(value);
      }

      @Override
      public void onError(Throwable t) {
        LOGGER.error("error in CottonDQL.Query: {}", LogHelper.getStackTrace(t));
      }

      @Override
      public void onCompleted() {
        semaphore.release();
      }
    };

    this.queryStub.query(query, observer);

    try {
      semaphore.tryAcquire(maxCallTimeOutMs, TimeUnit.MILLISECONDS);
    } catch (InterruptedException e) {
      LOGGER.warn("Waiting for response in CottontailWrapper.query has been interrupted: {}", LogHelper.getStackTrace(e));
    }
    return results;

  }
}
