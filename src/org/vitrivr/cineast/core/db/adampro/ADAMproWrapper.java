package org.vitrivr.cineast.core.db.adampro;

import java.util.concurrent.ExecutionException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.adampro.grpc.AdamDefinitionGrpc;
import org.vitrivr.adampro.grpc.AdamDefinitionGrpc.AdamDefinitionFutureStub;
import org.vitrivr.adampro.grpc.AdamGrpc;
import org.vitrivr.adampro.grpc.AdamGrpc.AckMessage;
import org.vitrivr.adampro.grpc.AdamGrpc.BatchedQueryMessage;
import org.vitrivr.adampro.grpc.AdamGrpc.CreateEntityMessage;
import org.vitrivr.adampro.grpc.AdamGrpc.EntityNameMessage;
import org.vitrivr.adampro.grpc.AdamGrpc.EntityPropertiesMessage;
import org.vitrivr.adampro.grpc.AdamGrpc.ExistsMessage;
import org.vitrivr.adampro.grpc.AdamGrpc.InsertMessage;
import org.vitrivr.adampro.grpc.AdamGrpc.PreviewMessage;
import org.vitrivr.adampro.grpc.AdamGrpc.PropertiesMessage;
import org.vitrivr.adampro.grpc.AdamGrpc.QueryMessage;
import org.vitrivr.adampro.grpc.AdamGrpc.QueryResultsMessage;
import org.vitrivr.adampro.grpc.AdamSearchGrpc;
import org.vitrivr.adampro.grpc.AdamSearchGrpc.AdamSearchFutureStub;
import org.vitrivr.cineast.core.config.Config;
import org.vitrivr.cineast.core.config.DatabaseConfig;
import org.vitrivr.cineast.core.util.LogHelper;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;

import io.grpc.ManagedChannel;
import io.grpc.netty.NettyChannelBuilder;
import io.grpc.stub.StreamObserver;

public class ADAMproWrapper implements AutoCloseable {

  private static final Logger LOGGER = LogManager.getLogger();

  private static final AckMessage INTERRUPTED_ACK_MESSAGE = AckMessage.newBuilder()
      .setCode(AckMessage.Code.ERROR).setMessage("Interrupted on client side").build();

  private static final PropertiesMessage INTERRUPTED_PROPERTIES_MESSAGE = PropertiesMessage
      .newBuilder().setAck(INTERRUPTED_ACK_MESSAGE).build();

  private ManagedChannel channel;
  private final AdamDefinitionFutureStub definitionStub;
  private final AdamSearchFutureStub searchStub;

  private static final int maxMessageSize = 10_000_000;
  
  public ADAMproWrapper() {
    DatabaseConfig config = Config.sharedConfig().getDatabase();
    this.channel = NettyChannelBuilder.forAddress(config.getHost(), config.getPort())
        .maxMessageSize(maxMessageSize).usePlaintext(config.getPlaintext()).build();
    this.definitionStub = AdamDefinitionGrpc.newFutureStub(channel);
    this.searchStub = AdamSearchGrpc.newFutureStub(channel);
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
    return standardQuery(message);
  }

   public ListenableFuture<QueryResultsMessage> standardQuery(QueryMessage message) {
    synchronized (this.searchStub) {
      return this.searchStub.doQuery(message);
    }
  }

  public ListenableFuture<AdamGrpc.BatchedQueryResultsMessage> batchedQuery(BatchedQueryMessage message) {
    synchronized (this.searchStub) {
      return this.searchStub.doBatchQuery(message);
    }
  }


  public ListenableFuture<QueryResultsMessage> previewEntity(PreviewMessage message) {
    ListenableFuture<QueryResultsMessage> future;
    synchronized (this.searchStub) {
      future = this.searchStub.preview(message);
    }
    return future;
  }

  public ListenableFuture<PropertiesMessage> getProperties(EntityPropertiesMessage message) {
    ListenableFuture<PropertiesMessage> future;
    synchronized (this.searchStub) {
      future = this.definitionStub.getEntityProperties(message);
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

  class LastObserver<T> implements StreamObserver<T> {

    private final SettableFuture<T> future;
    private T last = null;

    LastObserver(final SettableFuture<T> future) {
      this.future = future;
    }

    @Override
    public void onCompleted() {
      future.set(this.last);
    }

    @Override
    public void onError(Throwable e) {
      LOGGER.error(LogHelper.getStackTrace(e));
      future.setException(e);
    }

    @Override
    public void onNext(T t) {
      this.last = t;
    }

  }

  class LastAckStreamObserver extends LastObserver<AckMessage> {

    LastAckStreamObserver(SettableFuture<AckMessage> future) {
      super(future);
    }
  }

  class LastQueryResponseStreamObserver extends LastObserver<QueryResultsMessage> {

    LastQueryResponseStreamObserver(SettableFuture<QueryResultsMessage> future) {
      super(future);
    }
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

}
