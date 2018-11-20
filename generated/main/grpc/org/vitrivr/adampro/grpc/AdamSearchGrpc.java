package org.vitrivr.adampro.grpc;

import static io.grpc.stub.ClientCalls.asyncUnaryCall;
import static io.grpc.stub.ClientCalls.asyncServerStreamingCall;
import static io.grpc.stub.ClientCalls.asyncClientStreamingCall;
import static io.grpc.stub.ClientCalls.asyncBidiStreamingCall;
import static io.grpc.stub.ClientCalls.blockingUnaryCall;
import static io.grpc.stub.ClientCalls.blockingServerStreamingCall;
import static io.grpc.stub.ClientCalls.futureUnaryCall;
import static io.grpc.MethodDescriptor.generateFullMethodName;
import static io.grpc.stub.ServerCalls.asyncUnaryCall;
import static io.grpc.stub.ServerCalls.asyncServerStreamingCall;
import static io.grpc.stub.ServerCalls.asyncClientStreamingCall;
import static io.grpc.stub.ServerCalls.asyncBidiStreamingCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedStreamingCall;

/**
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.0.1)",
    comments = "Source: grpc.proto")
public class AdamSearchGrpc {

  private AdamSearchGrpc() {}

  public static final String SERVICE_NAME = "AdamSearch";

  // Static method descriptors that strictly reflect the proto.
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<org.vitrivr.adampro.grpc.AdamGrpc.IndexNameMessage,
      org.vitrivr.adampro.grpc.AdamGrpc.AckMessage> METHOD_CACHE_INDEX =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "AdamSearch", "CacheIndex"),
          io.grpc.protobuf.ProtoUtils.marshaller(org.vitrivr.adampro.grpc.AdamGrpc.IndexNameMessage.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(org.vitrivr.adampro.grpc.AdamGrpc.AckMessage.getDefaultInstance()));
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<org.vitrivr.adampro.grpc.AdamGrpc.EntityNameMessage,
      org.vitrivr.adampro.grpc.AdamGrpc.AckMessage> METHOD_CACHE_ENTITY =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "AdamSearch", "CacheEntity"),
          io.grpc.protobuf.ProtoUtils.marshaller(org.vitrivr.adampro.grpc.AdamGrpc.EntityNameMessage.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(org.vitrivr.adampro.grpc.AdamGrpc.AckMessage.getDefaultInstance()));
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<org.vitrivr.adampro.grpc.AdamGrpc.PreviewMessage,
      org.vitrivr.adampro.grpc.AdamGrpc.QueryResultsMessage> METHOD_PREVIEW =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "AdamSearch", "Preview"),
          io.grpc.protobuf.ProtoUtils.marshaller(org.vitrivr.adampro.grpc.AdamGrpc.PreviewMessage.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(org.vitrivr.adampro.grpc.AdamGrpc.QueryResultsMessage.getDefaultInstance()));
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<org.vitrivr.adampro.grpc.AdamGrpc.QueryMessage,
      org.vitrivr.adampro.grpc.AdamGrpc.QueryResultsMessage> METHOD_DO_QUERY =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "AdamSearch", "DoQuery"),
          io.grpc.protobuf.ProtoUtils.marshaller(org.vitrivr.adampro.grpc.AdamGrpc.QueryMessage.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(org.vitrivr.adampro.grpc.AdamGrpc.QueryResultsMessage.getDefaultInstance()));
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<org.vitrivr.adampro.grpc.AdamGrpc.QueryMessage,
      org.vitrivr.adampro.grpc.AdamGrpc.QueryResultsMessage> METHOD_DO_STREAMING_QUERY =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.BIDI_STREAMING,
          generateFullMethodName(
              "AdamSearch", "DoStreamingQuery"),
          io.grpc.protobuf.ProtoUtils.marshaller(org.vitrivr.adampro.grpc.AdamGrpc.QueryMessage.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(org.vitrivr.adampro.grpc.AdamGrpc.QueryResultsMessage.getDefaultInstance()));
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<org.vitrivr.adampro.grpc.AdamGrpc.BatchedQueryMessage,
      org.vitrivr.adampro.grpc.AdamGrpc.BatchedQueryResultsMessage> METHOD_DO_BATCH_QUERY =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "AdamSearch", "DoBatchQuery"),
          io.grpc.protobuf.ProtoUtils.marshaller(org.vitrivr.adampro.grpc.AdamGrpc.BatchedQueryMessage.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(org.vitrivr.adampro.grpc.AdamGrpc.BatchedQueryResultsMessage.getDefaultInstance()));
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<org.vitrivr.adampro.grpc.AdamGrpc.QueryMessage,
      org.vitrivr.adampro.grpc.AdamGrpc.QueryResultsMessage> METHOD_DO_PARALLEL_QUERY =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING,
          generateFullMethodName(
              "AdamSearch", "DoParallelQuery"),
          io.grpc.protobuf.ProtoUtils.marshaller(org.vitrivr.adampro.grpc.AdamGrpc.QueryMessage.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(org.vitrivr.adampro.grpc.AdamGrpc.QueryResultsMessage.getDefaultInstance()));
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<org.vitrivr.adampro.grpc.AdamGrpc.QueryMessage,
      org.vitrivr.adampro.grpc.AdamGrpc.QueryResultsMessage> METHOD_DO_PROGRESSIVE_QUERY =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING,
          generateFullMethodName(
              "AdamSearch", "DoProgressiveQuery"),
          io.grpc.protobuf.ProtoUtils.marshaller(org.vitrivr.adampro.grpc.AdamGrpc.QueryMessage.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(org.vitrivr.adampro.grpc.AdamGrpc.QueryResultsMessage.getDefaultInstance()));
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<org.vitrivr.adampro.grpc.AdamGrpc.CachedResultsMessage,
      org.vitrivr.adampro.grpc.AdamGrpc.QueryResultsMessage> METHOD_GET_CACHED_RESULTS =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "AdamSearch", "GetCachedResults"),
          io.grpc.protobuf.ProtoUtils.marshaller(org.vitrivr.adampro.grpc.AdamGrpc.CachedResultsMessage.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(org.vitrivr.adampro.grpc.AdamGrpc.QueryResultsMessage.getDefaultInstance()));
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<org.vitrivr.adampro.grpc.AdamGrpc.QuerySimulationMessage,
      org.vitrivr.adampro.grpc.AdamGrpc.ScoredExecutionPathsMessage> METHOD_GET_SCORED_EXECUTION_PATH =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "AdamSearch", "GetScoredExecutionPath"),
          io.grpc.protobuf.ProtoUtils.marshaller(org.vitrivr.adampro.grpc.AdamGrpc.QuerySimulationMessage.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(org.vitrivr.adampro.grpc.AdamGrpc.ScoredExecutionPathsMessage.getDefaultInstance()));
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<org.vitrivr.adampro.grpc.AdamGrpc.StopQueryMessage,
      org.vitrivr.adampro.grpc.AdamGrpc.AckMessage> METHOD_STOP_QUERY =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "AdamSearch", "StopQuery"),
          io.grpc.protobuf.ProtoUtils.marshaller(org.vitrivr.adampro.grpc.AdamGrpc.StopQueryMessage.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(org.vitrivr.adampro.grpc.AdamGrpc.AckMessage.getDefaultInstance()));
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<org.vitrivr.adampro.grpc.AdamGrpc.EmptyMessage,
      org.vitrivr.adampro.grpc.AdamGrpc.AckMessage> METHOD_STOP_ALL_QUERIES =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "AdamSearch", "StopAllQueries"),
          io.grpc.protobuf.ProtoUtils.marshaller(org.vitrivr.adampro.grpc.AdamGrpc.EmptyMessage.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(org.vitrivr.adampro.grpc.AdamGrpc.AckMessage.getDefaultInstance()));
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<org.vitrivr.adampro.grpc.AdamGrpc.EmptyMessage,
      org.vitrivr.adampro.grpc.AdamGrpc.AckMessage> METHOD_PING =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "AdamSearch", "Ping"),
          io.grpc.protobuf.ProtoUtils.marshaller(org.vitrivr.adampro.grpc.AdamGrpc.EmptyMessage.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(org.vitrivr.adampro.grpc.AdamGrpc.AckMessage.getDefaultInstance()));

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static AdamSearchStub newStub(io.grpc.Channel channel) {
    return new AdamSearchStub(channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static AdamSearchBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    return new AdamSearchBlockingStub(channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary and streaming output calls on the service
   */
  public static AdamSearchFutureStub newFutureStub(
      io.grpc.Channel channel) {
    return new AdamSearchFutureStub(channel);
  }

  /**
   */
  public static abstract class AdamSearchImplBase implements io.grpc.BindableService {

    /**
     * <pre>
     *caches an index explicitly (before performing a query to speed up retrieval time)
     * </pre>
     */
    public void cacheIndex(org.vitrivr.adampro.grpc.AdamGrpc.IndexNameMessage request,
        io.grpc.stub.StreamObserver<org.vitrivr.adampro.grpc.AdamGrpc.AckMessage> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_CACHE_INDEX, responseObserver);
    }

    /**
     */
    public void cacheEntity(org.vitrivr.adampro.grpc.AdamGrpc.EntityNameMessage request,
        io.grpc.stub.StreamObserver<org.vitrivr.adampro.grpc.AdamGrpc.AckMessage> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_CACHE_ENTITY, responseObserver);
    }

    /**
     */
    public void preview(org.vitrivr.adampro.grpc.AdamGrpc.PreviewMessage request,
        io.grpc.stub.StreamObserver<org.vitrivr.adampro.grpc.AdamGrpc.QueryResultsMessage> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_PREVIEW, responseObserver);
    }

    /**
     * <pre>
     *performs a query on an entity with hints on which search method to use
     *(if no hint is specified a fallback is used)
     * </pre>
     */
    public void doQuery(org.vitrivr.adampro.grpc.AdamGrpc.QueryMessage request,
        io.grpc.stub.StreamObserver<org.vitrivr.adampro.grpc.AdamGrpc.QueryResultsMessage> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_DO_QUERY, responseObserver);
    }

    /**
     */
    public io.grpc.stub.StreamObserver<org.vitrivr.adampro.grpc.AdamGrpc.QueryMessage> doStreamingQuery(
        io.grpc.stub.StreamObserver<org.vitrivr.adampro.grpc.AdamGrpc.QueryResultsMessage> responseObserver) {
      return asyncUnimplementedStreamingCall(METHOD_DO_STREAMING_QUERY, responseObserver);
    }

    /**
     */
    public void doBatchQuery(org.vitrivr.adampro.grpc.AdamGrpc.BatchedQueryMessage request,
        io.grpc.stub.StreamObserver<org.vitrivr.adampro.grpc.AdamGrpc.BatchedQueryResultsMessage> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_DO_BATCH_QUERY, responseObserver);
    }

    /**
     */
    public void doParallelQuery(org.vitrivr.adampro.grpc.AdamGrpc.QueryMessage request,
        io.grpc.stub.StreamObserver<org.vitrivr.adampro.grpc.AdamGrpc.QueryResultsMessage> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_DO_PARALLEL_QUERY, responseObserver);
    }

    /**
     */
    public void doProgressiveQuery(org.vitrivr.adampro.grpc.AdamGrpc.QueryMessage request,
        io.grpc.stub.StreamObserver<org.vitrivr.adampro.grpc.AdamGrpc.QueryResultsMessage> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_DO_PROGRESSIVE_QUERY, responseObserver);
    }

    /**
     * <pre>
     *return cached results
     * </pre>
     */
    public void getCachedResults(org.vitrivr.adampro.grpc.AdamGrpc.CachedResultsMessage request,
        io.grpc.stub.StreamObserver<org.vitrivr.adampro.grpc.AdamGrpc.QueryResultsMessage> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_GET_CACHED_RESULTS, responseObserver);
    }

    /**
     * <pre>
     *get scores for execution paths under empirical querying
     * </pre>
     */
    public void getScoredExecutionPath(org.vitrivr.adampro.grpc.AdamGrpc.QuerySimulationMessage request,
        io.grpc.stub.StreamObserver<org.vitrivr.adampro.grpc.AdamGrpc.ScoredExecutionPathsMessage> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_GET_SCORED_EXECUTION_PATH, responseObserver);
    }

    /**
     */
    public void stopQuery(org.vitrivr.adampro.grpc.AdamGrpc.StopQueryMessage request,
        io.grpc.stub.StreamObserver<org.vitrivr.adampro.grpc.AdamGrpc.AckMessage> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_STOP_QUERY, responseObserver);
    }

    /**
     */
    public void stopAllQueries(org.vitrivr.adampro.grpc.AdamGrpc.EmptyMessage request,
        io.grpc.stub.StreamObserver<org.vitrivr.adampro.grpc.AdamGrpc.AckMessage> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_STOP_ALL_QUERIES, responseObserver);
    }

    /**
     */
    public void ping(org.vitrivr.adampro.grpc.AdamGrpc.EmptyMessage request,
        io.grpc.stub.StreamObserver<org.vitrivr.adampro.grpc.AdamGrpc.AckMessage> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_PING, responseObserver);
    }

    @java.lang.Override public io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
          .addMethod(
            METHOD_CACHE_INDEX,
            asyncUnaryCall(
              new MethodHandlers<
                org.vitrivr.adampro.grpc.AdamGrpc.IndexNameMessage,
                org.vitrivr.adampro.grpc.AdamGrpc.AckMessage>(
                  this, METHODID_CACHE_INDEX)))
          .addMethod(
            METHOD_CACHE_ENTITY,
            asyncUnaryCall(
              new MethodHandlers<
                org.vitrivr.adampro.grpc.AdamGrpc.EntityNameMessage,
                org.vitrivr.adampro.grpc.AdamGrpc.AckMessage>(
                  this, METHODID_CACHE_ENTITY)))
          .addMethod(
            METHOD_PREVIEW,
            asyncUnaryCall(
              new MethodHandlers<
                org.vitrivr.adampro.grpc.AdamGrpc.PreviewMessage,
                org.vitrivr.adampro.grpc.AdamGrpc.QueryResultsMessage>(
                  this, METHODID_PREVIEW)))
          .addMethod(
            METHOD_DO_QUERY,
            asyncUnaryCall(
              new MethodHandlers<
                org.vitrivr.adampro.grpc.AdamGrpc.QueryMessage,
                org.vitrivr.adampro.grpc.AdamGrpc.QueryResultsMessage>(
                  this, METHODID_DO_QUERY)))
          .addMethod(
            METHOD_DO_STREAMING_QUERY,
            asyncBidiStreamingCall(
              new MethodHandlers<
                org.vitrivr.adampro.grpc.AdamGrpc.QueryMessage,
                org.vitrivr.adampro.grpc.AdamGrpc.QueryResultsMessage>(
                  this, METHODID_DO_STREAMING_QUERY)))
          .addMethod(
            METHOD_DO_BATCH_QUERY,
            asyncUnaryCall(
              new MethodHandlers<
                org.vitrivr.adampro.grpc.AdamGrpc.BatchedQueryMessage,
                org.vitrivr.adampro.grpc.AdamGrpc.BatchedQueryResultsMessage>(
                  this, METHODID_DO_BATCH_QUERY)))
          .addMethod(
            METHOD_DO_PARALLEL_QUERY,
            asyncServerStreamingCall(
              new MethodHandlers<
                org.vitrivr.adampro.grpc.AdamGrpc.QueryMessage,
                org.vitrivr.adampro.grpc.AdamGrpc.QueryResultsMessage>(
                  this, METHODID_DO_PARALLEL_QUERY)))
          .addMethod(
            METHOD_DO_PROGRESSIVE_QUERY,
            asyncServerStreamingCall(
              new MethodHandlers<
                org.vitrivr.adampro.grpc.AdamGrpc.QueryMessage,
                org.vitrivr.adampro.grpc.AdamGrpc.QueryResultsMessage>(
                  this, METHODID_DO_PROGRESSIVE_QUERY)))
          .addMethod(
            METHOD_GET_CACHED_RESULTS,
            asyncUnaryCall(
              new MethodHandlers<
                org.vitrivr.adampro.grpc.AdamGrpc.CachedResultsMessage,
                org.vitrivr.adampro.grpc.AdamGrpc.QueryResultsMessage>(
                  this, METHODID_GET_CACHED_RESULTS)))
          .addMethod(
            METHOD_GET_SCORED_EXECUTION_PATH,
            asyncUnaryCall(
              new MethodHandlers<
                org.vitrivr.adampro.grpc.AdamGrpc.QuerySimulationMessage,
                org.vitrivr.adampro.grpc.AdamGrpc.ScoredExecutionPathsMessage>(
                  this, METHODID_GET_SCORED_EXECUTION_PATH)))
          .addMethod(
              METHOD_STOP_QUERY,
              asyncUnaryCall(
                  new MethodHandlers<
                      org.vitrivr.adampro.grpc.AdamGrpc.StopQueryMessage,
                      org.vitrivr.adampro.grpc.AdamGrpc.AckMessage>(
                      this, METHODID_STOP_QUERY)))
          .addMethod(
              METHOD_STOP_ALL_QUERIES,
              asyncUnaryCall(
                  new MethodHandlers<
                      org.vitrivr.adampro.grpc.AdamGrpc.EmptyMessage,
                      org.vitrivr.adampro.grpc.AdamGrpc.AckMessage>(
                      this, METHODID_STOP_ALL_QUERIES)))
          .addMethod(
              METHOD_PING,
              asyncUnaryCall(
                  new MethodHandlers<
                      org.vitrivr.adampro.grpc.AdamGrpc.EmptyMessage,
                      org.vitrivr.adampro.grpc.AdamGrpc.AckMessage>(
                      this, METHODID_PING)))
          .build();
    }
  }

  /**
   */
  public static final class AdamSearchStub extends io.grpc.stub.AbstractStub<AdamSearchStub> {
    private AdamSearchStub(io.grpc.Channel channel) {
      super(channel);
    }

    private AdamSearchStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected AdamSearchStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new AdamSearchStub(channel, callOptions);
    }

    /**
     * <pre>
     *caches an index explicitly (before performing a query to speed up retrieval time)
     * </pre>
     */
    public void cacheIndex(org.vitrivr.adampro.grpc.AdamGrpc.IndexNameMessage request,
        io.grpc.stub.StreamObserver<org.vitrivr.adampro.grpc.AdamGrpc.AckMessage> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_CACHE_INDEX, getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void cacheEntity(org.vitrivr.adampro.grpc.AdamGrpc.EntityNameMessage request,
        io.grpc.stub.StreamObserver<org.vitrivr.adampro.grpc.AdamGrpc.AckMessage> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_CACHE_ENTITY, getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void preview(org.vitrivr.adampro.grpc.AdamGrpc.PreviewMessage request,
        io.grpc.stub.StreamObserver<org.vitrivr.adampro.grpc.AdamGrpc.QueryResultsMessage> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_PREVIEW, getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     *performs a query on an entity with hints on which search method to use
     *(if no hint is specified a fallback is used)
     * </pre>
     */
    public void doQuery(org.vitrivr.adampro.grpc.AdamGrpc.QueryMessage request,
        io.grpc.stub.StreamObserver<org.vitrivr.adampro.grpc.AdamGrpc.QueryResultsMessage> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_DO_QUERY, getCallOptions()), request, responseObserver);
    }

    /**
     */
    public io.grpc.stub.StreamObserver<org.vitrivr.adampro.grpc.AdamGrpc.QueryMessage> doStreamingQuery(
        io.grpc.stub.StreamObserver<org.vitrivr.adampro.grpc.AdamGrpc.QueryResultsMessage> responseObserver) {
      return asyncBidiStreamingCall(
          getChannel().newCall(METHOD_DO_STREAMING_QUERY, getCallOptions()), responseObserver);
    }

    /**
     */
    public void doBatchQuery(org.vitrivr.adampro.grpc.AdamGrpc.BatchedQueryMessage request,
        io.grpc.stub.StreamObserver<org.vitrivr.adampro.grpc.AdamGrpc.BatchedQueryResultsMessage> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_DO_BATCH_QUERY, getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void doParallelQuery(org.vitrivr.adampro.grpc.AdamGrpc.QueryMessage request,
        io.grpc.stub.StreamObserver<org.vitrivr.adampro.grpc.AdamGrpc.QueryResultsMessage> responseObserver) {
      asyncServerStreamingCall(
          getChannel().newCall(METHOD_DO_PARALLEL_QUERY, getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void doProgressiveQuery(org.vitrivr.adampro.grpc.AdamGrpc.QueryMessage request,
        io.grpc.stub.StreamObserver<org.vitrivr.adampro.grpc.AdamGrpc.QueryResultsMessage> responseObserver) {
      asyncServerStreamingCall(
          getChannel().newCall(METHOD_DO_PROGRESSIVE_QUERY, getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     *return cached results
     * </pre>
     */
    public void getCachedResults(org.vitrivr.adampro.grpc.AdamGrpc.CachedResultsMessage request,
        io.grpc.stub.StreamObserver<org.vitrivr.adampro.grpc.AdamGrpc.QueryResultsMessage> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_GET_CACHED_RESULTS, getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     *get scores for execution paths under empirical querying
     * </pre>
     */
    public void getScoredExecutionPath(org.vitrivr.adampro.grpc.AdamGrpc.QuerySimulationMessage request,
        io.grpc.stub.StreamObserver<org.vitrivr.adampro.grpc.AdamGrpc.ScoredExecutionPathsMessage> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_GET_SCORED_EXECUTION_PATH, getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void stopQuery(org.vitrivr.adampro.grpc.AdamGrpc.StopQueryMessage request,
        io.grpc.stub.StreamObserver<org.vitrivr.adampro.grpc.AdamGrpc.AckMessage> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_STOP_QUERY, getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void stopAllQueries(org.vitrivr.adampro.grpc.AdamGrpc.EmptyMessage request,
        io.grpc.stub.StreamObserver<org.vitrivr.adampro.grpc.AdamGrpc.AckMessage> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_STOP_ALL_QUERIES, getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void ping(org.vitrivr.adampro.grpc.AdamGrpc.EmptyMessage request,
        io.grpc.stub.StreamObserver<org.vitrivr.adampro.grpc.AdamGrpc.AckMessage> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_PING, getCallOptions()), request, responseObserver);
    }
  }

  /**
   */
  public static final class AdamSearchBlockingStub extends io.grpc.stub.AbstractStub<AdamSearchBlockingStub> {
    private AdamSearchBlockingStub(io.grpc.Channel channel) {
      super(channel);
    }

    private AdamSearchBlockingStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected AdamSearchBlockingStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new AdamSearchBlockingStub(channel, callOptions);
    }

    /**
     * <pre>
     *caches an index explicitly (before performing a query to speed up retrieval time)
     * </pre>
     */
    public org.vitrivr.adampro.grpc.AdamGrpc.AckMessage cacheIndex(org.vitrivr.adampro.grpc.AdamGrpc.IndexNameMessage request) {
      return blockingUnaryCall(
          getChannel(), METHOD_CACHE_INDEX, getCallOptions(), request);
    }

    /**
     */
    public org.vitrivr.adampro.grpc.AdamGrpc.AckMessage cacheEntity(org.vitrivr.adampro.grpc.AdamGrpc.EntityNameMessage request) {
      return blockingUnaryCall(
          getChannel(), METHOD_CACHE_ENTITY, getCallOptions(), request);
    }

    /**
     */
    public org.vitrivr.adampro.grpc.AdamGrpc.QueryResultsMessage preview(org.vitrivr.adampro.grpc.AdamGrpc.PreviewMessage request) {
      return blockingUnaryCall(
          getChannel(), METHOD_PREVIEW, getCallOptions(), request);
    }

    /**
     * <pre>
     *performs a query on an entity with hints on which search method to use
     *(if no hint is specified a fallback is used)
     * </pre>
     */
    public org.vitrivr.adampro.grpc.AdamGrpc.QueryResultsMessage doQuery(org.vitrivr.adampro.grpc.AdamGrpc.QueryMessage request) {
      return blockingUnaryCall(
          getChannel(), METHOD_DO_QUERY, getCallOptions(), request);
    }

    /**
     */
    public org.vitrivr.adampro.grpc.AdamGrpc.BatchedQueryResultsMessage doBatchQuery(org.vitrivr.adampro.grpc.AdamGrpc.BatchedQueryMessage request) {
      return blockingUnaryCall(
          getChannel(), METHOD_DO_BATCH_QUERY, getCallOptions(), request);
    }

    /**
     */
    public java.util.Iterator<org.vitrivr.adampro.grpc.AdamGrpc.QueryResultsMessage> doParallelQuery(
        org.vitrivr.adampro.grpc.AdamGrpc.QueryMessage request) {
      return blockingServerStreamingCall(
          getChannel(), METHOD_DO_PARALLEL_QUERY, getCallOptions(), request);
    }

    /**
     */
    public java.util.Iterator<org.vitrivr.adampro.grpc.AdamGrpc.QueryResultsMessage> doProgressiveQuery(
        org.vitrivr.adampro.grpc.AdamGrpc.QueryMessage request) {
      return blockingServerStreamingCall(
          getChannel(), METHOD_DO_PROGRESSIVE_QUERY, getCallOptions(), request);
    }

    /**
     * <pre>
     *return cached results
     * </pre>
     */
    public org.vitrivr.adampro.grpc.AdamGrpc.QueryResultsMessage getCachedResults(org.vitrivr.adampro.grpc.AdamGrpc.CachedResultsMessage request) {
      return blockingUnaryCall(
          getChannel(), METHOD_GET_CACHED_RESULTS, getCallOptions(), request);
    }

    /**
     * <pre>
     *get scores for execution paths under empirical querying
     * </pre>
     */
    public org.vitrivr.adampro.grpc.AdamGrpc.ScoredExecutionPathsMessage getScoredExecutionPath(org.vitrivr.adampro.grpc.AdamGrpc.QuerySimulationMessage request) {
      return blockingUnaryCall(
          getChannel(), METHOD_GET_SCORED_EXECUTION_PATH, getCallOptions(), request);
    }

    /**
     */
    public org.vitrivr.adampro.grpc.AdamGrpc.AckMessage stopQuery(org.vitrivr.adampro.grpc.AdamGrpc.StopQueryMessage request) {
      return blockingUnaryCall(
          getChannel(), METHOD_STOP_QUERY, getCallOptions(), request);
    }

    /**
     */
    public org.vitrivr.adampro.grpc.AdamGrpc.AckMessage stopAllQueries(org.vitrivr.adampro.grpc.AdamGrpc.EmptyMessage request) {
      return blockingUnaryCall(
          getChannel(), METHOD_STOP_ALL_QUERIES, getCallOptions(), request);
    }

    /**
     */
    public org.vitrivr.adampro.grpc.AdamGrpc.AckMessage ping(org.vitrivr.adampro.grpc.AdamGrpc.EmptyMessage request) {
      return blockingUnaryCall(
          getChannel(), METHOD_PING, getCallOptions(), request);
    }
  }

  /**
   */
  public static final class AdamSearchFutureStub extends io.grpc.stub.AbstractStub<AdamSearchFutureStub> {
    private AdamSearchFutureStub(io.grpc.Channel channel) {
      super(channel);
    }

    private AdamSearchFutureStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected AdamSearchFutureStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new AdamSearchFutureStub(channel, callOptions);
    }

    /**
     * <pre>
     *caches an index explicitly (before performing a query to speed up retrieval time)
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<org.vitrivr.adampro.grpc.AdamGrpc.AckMessage> cacheIndex(
        org.vitrivr.adampro.grpc.AdamGrpc.IndexNameMessage request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_CACHE_INDEX, getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<org.vitrivr.adampro.grpc.AdamGrpc.AckMessage> cacheEntity(
        org.vitrivr.adampro.grpc.AdamGrpc.EntityNameMessage request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_CACHE_ENTITY, getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<org.vitrivr.adampro.grpc.AdamGrpc.QueryResultsMessage> preview(
        org.vitrivr.adampro.grpc.AdamGrpc.PreviewMessage request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_PREVIEW, getCallOptions()), request);
    }

    /**
     * <pre>
     *performs a query on an entity with hints on which search method to use
     *(if no hint is specified a fallback is used)
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<org.vitrivr.adampro.grpc.AdamGrpc.QueryResultsMessage> doQuery(
        org.vitrivr.adampro.grpc.AdamGrpc.QueryMessage request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_DO_QUERY, getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<org.vitrivr.adampro.grpc.AdamGrpc.BatchedQueryResultsMessage> doBatchQuery(
        org.vitrivr.adampro.grpc.AdamGrpc.BatchedQueryMessage request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_DO_BATCH_QUERY, getCallOptions()), request);
    }

    /**
     * <pre>
     *return cached results
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<org.vitrivr.adampro.grpc.AdamGrpc.QueryResultsMessage> getCachedResults(
        org.vitrivr.adampro.grpc.AdamGrpc.CachedResultsMessage request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_GET_CACHED_RESULTS, getCallOptions()), request);
    }

    /**
     * <pre>
     *get scores for execution paths under empirical querying
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<org.vitrivr.adampro.grpc.AdamGrpc.ScoredExecutionPathsMessage> getScoredExecutionPath(
        org.vitrivr.adampro.grpc.AdamGrpc.QuerySimulationMessage request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_GET_SCORED_EXECUTION_PATH, getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<org.vitrivr.adampro.grpc.AdamGrpc.AckMessage> stopQuery(
        org.vitrivr.adampro.grpc.AdamGrpc.StopQueryMessage request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_STOP_QUERY, getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<org.vitrivr.adampro.grpc.AdamGrpc.AckMessage> stopAllQueries(
        org.vitrivr.adampro.grpc.AdamGrpc.EmptyMessage request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_STOP_ALL_QUERIES, getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<org.vitrivr.adampro.grpc.AdamGrpc.AckMessage> ping(
        org.vitrivr.adampro.grpc.AdamGrpc.EmptyMessage request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_PING, getCallOptions()), request);
    }
  }

  private static final int METHODID_CACHE_INDEX = 0;
  private static final int METHODID_CACHE_ENTITY = 1;
  private static final int METHODID_PREVIEW = 2;
  private static final int METHODID_DO_QUERY = 3;
  private static final int METHODID_DO_BATCH_QUERY = 4;
  private static final int METHODID_DO_PARALLEL_QUERY = 5;
  private static final int METHODID_DO_PROGRESSIVE_QUERY = 6;
  private static final int METHODID_GET_CACHED_RESULTS = 7;
  private static final int METHODID_GET_SCORED_EXECUTION_PATH = 8;
  private static final int METHODID_STOP_QUERY = 9;
  private static final int METHODID_STOP_ALL_QUERIES = 10;
  private static final int METHODID_PING = 11;
  private static final int METHODID_DO_STREAMING_QUERY = 12;

  private static class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final AdamSearchImplBase serviceImpl;
    private final int methodId;

    public MethodHandlers(AdamSearchImplBase serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_CACHE_INDEX:
          serviceImpl.cacheIndex((org.vitrivr.adampro.grpc.AdamGrpc.IndexNameMessage) request,
              (io.grpc.stub.StreamObserver<org.vitrivr.adampro.grpc.AdamGrpc.AckMessage>) responseObserver);
          break;
        case METHODID_CACHE_ENTITY:
          serviceImpl.cacheEntity((org.vitrivr.adampro.grpc.AdamGrpc.EntityNameMessage) request,
              (io.grpc.stub.StreamObserver<org.vitrivr.adampro.grpc.AdamGrpc.AckMessage>) responseObserver);
          break;
        case METHODID_PREVIEW:
          serviceImpl.preview((org.vitrivr.adampro.grpc.AdamGrpc.PreviewMessage) request,
              (io.grpc.stub.StreamObserver<org.vitrivr.adampro.grpc.AdamGrpc.QueryResultsMessage>) responseObserver);
          break;
        case METHODID_DO_QUERY:
          serviceImpl.doQuery((org.vitrivr.adampro.grpc.AdamGrpc.QueryMessage) request,
              (io.grpc.stub.StreamObserver<org.vitrivr.adampro.grpc.AdamGrpc.QueryResultsMessage>) responseObserver);
          break;
        case METHODID_DO_BATCH_QUERY:
          serviceImpl.doBatchQuery((org.vitrivr.adampro.grpc.AdamGrpc.BatchedQueryMessage) request,
              (io.grpc.stub.StreamObserver<org.vitrivr.adampro.grpc.AdamGrpc.BatchedQueryResultsMessage>) responseObserver);
          break;
        case METHODID_DO_PARALLEL_QUERY:
          serviceImpl.doParallelQuery((org.vitrivr.adampro.grpc.AdamGrpc.QueryMessage) request,
              (io.grpc.stub.StreamObserver<org.vitrivr.adampro.grpc.AdamGrpc.QueryResultsMessage>) responseObserver);
          break;
        case METHODID_DO_PROGRESSIVE_QUERY:
          serviceImpl.doProgressiveQuery((org.vitrivr.adampro.grpc.AdamGrpc.QueryMessage) request,
              (io.grpc.stub.StreamObserver<org.vitrivr.adampro.grpc.AdamGrpc.QueryResultsMessage>) responseObserver);
          break;
        case METHODID_GET_CACHED_RESULTS:
          serviceImpl.getCachedResults((org.vitrivr.adampro.grpc.AdamGrpc.CachedResultsMessage) request,
              (io.grpc.stub.StreamObserver<org.vitrivr.adampro.grpc.AdamGrpc.QueryResultsMessage>) responseObserver);
          break;
        case METHODID_GET_SCORED_EXECUTION_PATH:
          serviceImpl.getScoredExecutionPath((org.vitrivr.adampro.grpc.AdamGrpc.QuerySimulationMessage) request,
              (io.grpc.stub.StreamObserver<org.vitrivr.adampro.grpc.AdamGrpc.ScoredExecutionPathsMessage>) responseObserver);
          break;
        case METHODID_STOP_QUERY:
          serviceImpl.stopQuery((org.vitrivr.adampro.grpc.AdamGrpc.StopQueryMessage) request,
              (io.grpc.stub.StreamObserver<org.vitrivr.adampro.grpc.AdamGrpc.AckMessage>) responseObserver);
          break;
        case METHODID_STOP_ALL_QUERIES:
          serviceImpl.stopAllQueries((org.vitrivr.adampro.grpc.AdamGrpc.EmptyMessage) request,
              (io.grpc.stub.StreamObserver<org.vitrivr.adampro.grpc.AdamGrpc.AckMessage>) responseObserver);
          break;
        case METHODID_PING:
          serviceImpl.ping((org.vitrivr.adampro.grpc.AdamGrpc.EmptyMessage) request,
              (io.grpc.stub.StreamObserver<org.vitrivr.adampro.grpc.AdamGrpc.AckMessage>) responseObserver);
          break;
        default:
          throw new AssertionError();
      }
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<Req> invoke(
        io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_DO_STREAMING_QUERY:
          return (io.grpc.stub.StreamObserver<Req>) serviceImpl.doStreamingQuery(
              (io.grpc.stub.StreamObserver<org.vitrivr.adampro.grpc.AdamGrpc.QueryResultsMessage>) responseObserver);
        default:
          throw new AssertionError();
      }
    }
  }

  public static io.grpc.ServiceDescriptor getServiceDescriptor() {
    return new io.grpc.ServiceDescriptor(SERVICE_NAME,
        METHOD_CACHE_INDEX,
        METHOD_CACHE_ENTITY,
        METHOD_PREVIEW,
        METHOD_DO_QUERY,
        METHOD_DO_STREAMING_QUERY,
        METHOD_DO_BATCH_QUERY,
        METHOD_DO_PARALLEL_QUERY,
        METHOD_DO_PROGRESSIVE_QUERY,
        METHOD_GET_CACHED_RESULTS,
        METHOD_GET_SCORED_EXECUTION_PATH,
        METHOD_STOP_QUERY,
        METHOD_STOP_ALL_QUERIES,
        METHOD_PING);
  }

}
