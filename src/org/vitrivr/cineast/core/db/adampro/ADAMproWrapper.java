package org.vitrivr.cineast.core.db.adampro;

import com.google.common.util.concurrent.Futures;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.adampro.grpc.AdamDefinitionGrpc;
import org.vitrivr.adampro.grpc.AdamDefinitionGrpc.AdamDefinitionFutureStub;
import org.vitrivr.adampro.grpc.AdamGrpc;
import org.vitrivr.adampro.grpc.AdamGrpc.AckMessage;
import org.vitrivr.adampro.grpc.AdamGrpc.BatchedQueryMessage;
import org.vitrivr.adampro.grpc.AdamGrpc.CreateEntityMessage;
import org.vitrivr.adampro.grpc.AdamGrpc.EmptyMessage;
import org.vitrivr.adampro.grpc.AdamGrpc.EntityNameMessage;
import org.vitrivr.adampro.grpc.AdamGrpc.EntityPropertiesMessage;
import org.vitrivr.adampro.grpc.AdamGrpc.ExistsMessage;
import org.vitrivr.adampro.grpc.AdamGrpc.IndexNameMessage;
import org.vitrivr.adampro.grpc.AdamGrpc.InsertMessage;
import org.vitrivr.adampro.grpc.AdamGrpc.PreviewMessage;
import org.vitrivr.adampro.grpc.AdamGrpc.PropertiesMessage;
import org.vitrivr.adampro.grpc.AdamGrpc.QueryMessage;
import org.vitrivr.adampro.grpc.AdamGrpc.QueryResultsMessage;
import org.vitrivr.adampro.grpc.AdamSearchGrpc;
import org.vitrivr.adampro.grpc.AdamSearchGrpc.AdamSearchFutureStub;
import org.vitrivr.adampro.grpc.AdamSearchGrpc.AdamSearchStub;
import org.vitrivr.cineast.core.config.Config;
import org.vitrivr.cineast.core.config.DatabaseConfig;
import org.vitrivr.cineast.core.util.LogHelper;

import com.google.common.util.concurrent.ListenableFuture;

import io.grpc.ManagedChannel;
import io.grpc.netty.NettyChannelBuilder;
import io.grpc.stub.StreamObserver;
import sun.awt.EventListenerAggregate;

public class ADAMproWrapper implements AutoCloseable {

  private static final Logger LOGGER = LogManager.getLogger();

  private static final AckMessage INTERRUPTED_ACK_MESSAGE = AckMessage.newBuilder()
      .setCode(AckMessage.Code.ERROR).setMessage("Interrupted on client side").build();

  private static final PropertiesMessage INTERRUPTED_PROPERTIES_MESSAGE = PropertiesMessage
      .newBuilder().setAck(INTERRUPTED_ACK_MESSAGE).build();

  private static final ScheduledExecutorService timeoutService = Executors.newSingleThreadScheduledExecutor();

  private ManagedChannel channel;
  private final AdamDefinitionFutureStub definitionStub;
  private final AdamSearchFutureStub searchStub;
  private final AdamSearchStub streamSearchStub;

  private static final int maxMessageSize = 10_000_000;
  private static final long maxCallTimeOutMs = 300_000; //TODO expose to config
  
  public ADAMproWrapper() {
    DatabaseConfig config = Config.sharedConfig().getDatabase();
    this.channel = NettyChannelBuilder.forAddress(config.getHost(), config.getPort())
        .maxMessageSize(maxMessageSize).usePlaintext(config.getPlaintext()).build();
    this.definitionStub = AdamDefinitionGrpc.newFutureStub(channel);
    this.searchStub = AdamSearchGrpc.newFutureStub(channel);
    this.streamSearchStub = AdamSearchGrpc.newStub(channel);
  }

  public synchronized ListenableFuture<AckMessage> createEntity(CreateEntityMessage message) {
    return this.definitionStub.createEntity(message);
  }

  public AckMessage createEntityBlocking(CreateEntityMessage message) {
    ListenableFuture<AckMessage> future = this.createEntity(message);
    try {
      return future.get();
    } catch (InterruptedException | ExecutionException e) {
      LOGGER.error("error in createEntityBlocking: {}", LogHelper.getStackTrace(e));
      return INTERRUPTED_ACK_MESSAGE;
    }
  }

  public synchronized ListenableFuture<AckMessage> insert(InsertMessage message) {
    return this.definitionStub.insert(message);
  }

  public AckMessage insertBlocking(InsertMessage message) {
    ListenableFuture<AckMessage> future = this.insert(message);
    try {
      return future.get();
    } catch (InterruptedException | ExecutionException e) {
      LOGGER.error("error in insertOneBlocking: {}", LogHelper.getStackTrace(e));
      return INTERRUPTED_ACK_MESSAGE;
    }
  }

  public synchronized ListenableFuture<AckMessage> ping() {
    return this.searchStub.ping(EmptyMessage.getDefaultInstance());
  }

  public AckMessage pingBlocking() {
    ListenableFuture<AckMessage> future = this.ping();
    try {
      return future.get();
    } catch (InterruptedException | ExecutionException e) {
      LOGGER.error("error in pingBlocking: {}", LogHelper.getStackTrace(e));
      return INTERRUPTED_ACK_MESSAGE;
    }
  }

  public ListenableFuture<ExistsMessage> existsEntity(String eName) {
    return this.definitionStub.existsEntity(
        EntityNameMessage.newBuilder().setEntity(eName).build());
  }
  
  public boolean existsEntityBlocking(String eName) {
    ListenableFuture<ExistsMessage> future = existsEntity(eName);
    try {
      return future.get().getExists();
    } catch (InterruptedException | ExecutionException e) {
      LOGGER.error("error in existsEntityBlocking: {}", LogHelper.getStackTrace(e));
      return false;
    }
  }

  public AckMessage dropEntityBlocking(EntityNameMessage message) {
    ListenableFuture<AckMessage> future = this.definitionStub.dropEntity(message);
    try {
      return future.get();
    } catch (InterruptedException | ExecutionException e) {
      LOGGER.error("error in dropEntityBlocking: {}", LogHelper.getStackTrace(e));
      return INTERRUPTED_ACK_MESSAGE;
    }
  }

  public ListenableFuture<QueryResultsMessage> booleanQuery(QueryMessage message) {
    return Futures.withTimeout(standardQuery(message), maxCallTimeOutMs, TimeUnit.MILLISECONDS, timeoutService);
  }

   public ListenableFuture<QueryResultsMessage> standardQuery(QueryMessage message) {
    synchronized (this.searchStub) {
      return Futures.withTimeout(this.searchStub.doQuery(message), maxCallTimeOutMs, TimeUnit.MILLISECONDS, timeoutService);
    }
  }

  public ArrayList<QueryResultsMessage> streamingStandardQuery(QueryMessage message) {

    ArrayList<QueryResultsMessage> results = new ArrayList<>();
    Semaphore semaphore = new Semaphore(1);

    StreamObserver<QueryResultsMessage> resultsMessageStreamObserver = new StreamObserver<QueryResultsMessage>() {
      @Override
      public void onNext(QueryResultsMessage value) {
        results.add(value);
      }

      @Override
      public void onError(Throwable t) {
        LOGGER.error("Error in ADAMproWrapper.streamingStandardQuery: {}", LogHelper.getStackTrace(t));
      }

      @Override
      public void onCompleted() {
        semaphore.release();
      }
    };
    synchronized (this.streamSearchStub) {
      StreamObserver<QueryMessage> queryMessageStreamObserver = this.streamSearchStub
          .doStreamingQuery(resultsMessageStreamObserver);
      try {
        semaphore.acquire();
      } catch (InterruptedException e) {
        //ignore
      }
      queryMessageStreamObserver.onNext(message);
      queryMessageStreamObserver.onCompleted();

      try {
        semaphore.tryAcquire(maxCallTimeOutMs, TimeUnit.MILLISECONDS);
      } catch (InterruptedException e) {
        LOGGER.warn("Waiting for response in ADAMproWrapper.streamingStandardQuery has been interrupted: {}", LogHelper.getStackTrace(e));
      }
      return results;
    }

  }

  public ArrayList<QueryResultsMessage> streamingStandardQuery(Collection<QueryMessage> messages) {

    if(messages == null || messages.isEmpty()){
      return new ArrayList<>(0);
    }

    ArrayList<QueryResultsMessage> results = new ArrayList<>();
    Semaphore semaphore = new Semaphore(1);

    StreamObserver<QueryResultsMessage> resultsMessageStreamObserver = new StreamObserver<QueryResultsMessage>() {
      @Override
      public void onNext(QueryResultsMessage value) {
        results.add(value);
      }

      @Override
      public void onError(Throwable t) {
        LOGGER.error("Error in ADAMproWrapper.streamingStandardQuery: {}", LogHelper.getStackTrace(t));
      }

      @Override
      public void onCompleted() {
        semaphore.release();
      }
    };
    synchronized (this.streamSearchStub) {
      StreamObserver<QueryMessage> queryMessageStreamObserver = this.streamSearchStub
          .doStreamingQuery(resultsMessageStreamObserver);
      try {
        semaphore.acquire();
      } catch (InterruptedException e) {
        //ignore
      }
      for(QueryMessage message : messages){
        queryMessageStreamObserver.onNext(message);
      }
      queryMessageStreamObserver.onCompleted();

      try {
        semaphore.tryAcquire(maxCallTimeOutMs, TimeUnit.MILLISECONDS);
      } catch (InterruptedException e) {
        LOGGER.warn("Waiting for response in ADAMproWrapper.streamingStandardQuery has been interrupted: {}", LogHelper.getStackTrace(e));
      }
      return results;
    }

  }

  public ListenableFuture<AdamGrpc.BatchedQueryResultsMessage> batchedQuery(BatchedQueryMessage message) {
    synchronized (this.searchStub) {
      return Futures.withTimeout(this.searchStub.doBatchQuery(message), maxCallTimeOutMs, TimeUnit.MILLISECONDS, timeoutService);
    }
  }


  public ListenableFuture<QueryResultsMessage> previewEntity(PreviewMessage message) {
    ListenableFuture<QueryResultsMessage> future;
    synchronized (this.searchStub) {
      future = Futures.withTimeout(this.searchStub.preview(message), maxCallTimeOutMs, TimeUnit.MILLISECONDS, timeoutService);
    }
    return future;
  }

  public ListenableFuture<PropertiesMessage> getProperties(EntityPropertiesMessage message) {
    ListenableFuture<PropertiesMessage> future;
    synchronized (this.searchStub) {
      future = Futures.withTimeout(this.definitionStub.getEntityProperties(message), maxCallTimeOutMs, TimeUnit.MILLISECONDS, timeoutService);
    }
    return future;
  }

  public PropertiesMessage getPropertiesBlocking(EntityPropertiesMessage message) {
    ListenableFuture<PropertiesMessage> future = getProperties(message);
    try {
      return future.get();
    } catch (InterruptedException | ExecutionException e) {
      LOGGER.error("error in getPropertiesBlocking: {}", LogHelper.getStackTrace(e));
      return INTERRUPTED_PROPERTIES_MESSAGE;
    }
  }

  @Override
  public void close() {
    this.channel.shutdown();
  }

  @Override
  protected void finalize() throws Throwable {
    this.close();
    super.finalize();
  }

  public ListenableFuture<AckMessage> dropEntity(String entityName){
    return this.definitionStub.dropEntity(EntityNameMessage.newBuilder().setEntity(entityName).build());
  }

  public boolean dropEntityBlocking(String entityName) {
    ListenableFuture<AckMessage> future = this.dropEntity(entityName);
    try {
      return future.get().getCode() == AckMessage.Code.OK;
    } catch (InterruptedException | ExecutionException e) {
      LOGGER.error("error in dropEntityBlocking: {}", LogHelper.getStackTrace(e));
      return false;
    }
  }

  public ListenableFuture<AckMessage> dropIndex(String indexName){
    return this.definitionStub.dropIndex(IndexNameMessage.newBuilder().setIndex(indexName).build());
  }

  public boolean dropIndexBlocking(String indexName){
    ListenableFuture<AckMessage> future = this.dropIndex(indexName);
    try {
      return future.get().getCode() == AckMessage.Code.OK;
    } catch (InterruptedException | ExecutionException e) {
      LOGGER.error("error in dropIndexBlocking: {}", LogHelper.getStackTrace(e));
      return false;
    }
  }

}
