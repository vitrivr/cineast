package org.vitrivr.adam.grpc;

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

@javax.annotation.Generated("by gRPC proto compiler")
public class AdamSearchGrpc {

  private AdamSearchGrpc() {}

  public static final String SERVICE_NAME = "AdamSearch";

  // Static method descriptors that strictly reflect the proto.
  @io.grpc.ExperimentalApi
  public static final io.grpc.MethodDescriptor<org.vitrivr.adam.grpc.AdamGrpc.IndexNameMessage,
      org.vitrivr.adam.grpc.AdamGrpc.AckMessage> METHOD_CACHE_INDEX =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "AdamSearch", "CacheIndex"),
          io.grpc.protobuf.ProtoUtils.marshaller(org.vitrivr.adam.grpc.AdamGrpc.IndexNameMessage.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(org.vitrivr.adam.grpc.AdamGrpc.AckMessage.getDefaultInstance()));
  @io.grpc.ExperimentalApi
  public static final io.grpc.MethodDescriptor<org.vitrivr.adam.grpc.AdamGrpc.EntityNameMessage,
      org.vitrivr.adam.grpc.AdamGrpc.AckMessage> METHOD_CACHE_ENTITY =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "AdamSearch", "CacheEntity"),
          io.grpc.protobuf.ProtoUtils.marshaller(org.vitrivr.adam.grpc.AdamGrpc.EntityNameMessage.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(org.vitrivr.adam.grpc.AdamGrpc.AckMessage.getDefaultInstance()));
  @io.grpc.ExperimentalApi
  public static final io.grpc.MethodDescriptor<org.vitrivr.adam.grpc.AdamGrpc.PreviewMessage,
      org.vitrivr.adam.grpc.AdamGrpc.QueryResultsMessage> METHOD_PREVIEW =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "AdamSearch", "Preview"),
          io.grpc.protobuf.ProtoUtils.marshaller(org.vitrivr.adam.grpc.AdamGrpc.PreviewMessage.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(org.vitrivr.adam.grpc.AdamGrpc.QueryResultsMessage.getDefaultInstance()));
  @io.grpc.ExperimentalApi
  public static final io.grpc.MethodDescriptor<org.vitrivr.adam.grpc.AdamGrpc.QueryMessage,
      org.vitrivr.adam.grpc.AdamGrpc.QueryResultsMessage> METHOD_DO_QUERY =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "AdamSearch", "DoQuery"),
          io.grpc.protobuf.ProtoUtils.marshaller(org.vitrivr.adam.grpc.AdamGrpc.QueryMessage.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(org.vitrivr.adam.grpc.AdamGrpc.QueryResultsMessage.getDefaultInstance()));
  @io.grpc.ExperimentalApi
  public static final io.grpc.MethodDescriptor<org.vitrivr.adam.grpc.AdamGrpc.QueryMessage,
      org.vitrivr.adam.grpc.AdamGrpc.QueryResultsMessage> METHOD_DO_STREAMING_QUERY =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.BIDI_STREAMING,
          generateFullMethodName(
              "AdamSearch", "DoStreamingQuery"),
          io.grpc.protobuf.ProtoUtils.marshaller(org.vitrivr.adam.grpc.AdamGrpc.QueryMessage.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(org.vitrivr.adam.grpc.AdamGrpc.QueryResultsMessage.getDefaultInstance()));
  @io.grpc.ExperimentalApi
  public static final io.grpc.MethodDescriptor<org.vitrivr.adam.grpc.AdamGrpc.BatchedQueryMessage,
      org.vitrivr.adam.grpc.AdamGrpc.BatchedQueryResultsMessage> METHOD_DO_BATCH_QUERY =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "AdamSearch", "DoBatchQuery"),
          io.grpc.protobuf.ProtoUtils.marshaller(org.vitrivr.adam.grpc.AdamGrpc.BatchedQueryMessage.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(org.vitrivr.adam.grpc.AdamGrpc.BatchedQueryResultsMessage.getDefaultInstance()));
  @io.grpc.ExperimentalApi
  public static final io.grpc.MethodDescriptor<org.vitrivr.adam.grpc.AdamGrpc.QueryMessage,
      org.vitrivr.adam.grpc.AdamGrpc.QueryResultsMessage> METHOD_DO_PROGRESSIVE_QUERY =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING,
          generateFullMethodName(
              "AdamSearch", "DoProgressiveQuery"),
          io.grpc.protobuf.ProtoUtils.marshaller(org.vitrivr.adam.grpc.AdamGrpc.QueryMessage.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(org.vitrivr.adam.grpc.AdamGrpc.QueryResultsMessage.getDefaultInstance()));
  @io.grpc.ExperimentalApi
  public static final io.grpc.MethodDescriptor<org.vitrivr.adam.grpc.AdamGrpc.CachedResultsMessage,
      org.vitrivr.adam.grpc.AdamGrpc.QueryResultsMessage> METHOD_GET_CACHED_RESULTS =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "AdamSearch", "GetCachedResults"),
          io.grpc.protobuf.ProtoUtils.marshaller(org.vitrivr.adam.grpc.AdamGrpc.CachedResultsMessage.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(org.vitrivr.adam.grpc.AdamGrpc.QueryResultsMessage.getDefaultInstance()));

  public static AdamSearchStub newStub(io.grpc.Channel channel) {
    return new AdamSearchStub(channel);
  }

  public static AdamSearchBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    return new AdamSearchBlockingStub(channel);
  }

  public static AdamSearchFutureStub newFutureStub(
      io.grpc.Channel channel) {
    return new AdamSearchFutureStub(channel);
  }

  public static interface AdamSearch {

    public void cacheIndex(org.vitrivr.adam.grpc.AdamGrpc.IndexNameMessage request,
        io.grpc.stub.StreamObserver<org.vitrivr.adam.grpc.AdamGrpc.AckMessage> responseObserver);

    public void cacheEntity(org.vitrivr.adam.grpc.AdamGrpc.EntityNameMessage request,
        io.grpc.stub.StreamObserver<org.vitrivr.adam.grpc.AdamGrpc.AckMessage> responseObserver);

    public void preview(org.vitrivr.adam.grpc.AdamGrpc.PreviewMessage request,
        io.grpc.stub.StreamObserver<org.vitrivr.adam.grpc.AdamGrpc.QueryResultsMessage> responseObserver);

    public void doQuery(org.vitrivr.adam.grpc.AdamGrpc.QueryMessage request,
        io.grpc.stub.StreamObserver<org.vitrivr.adam.grpc.AdamGrpc.QueryResultsMessage> responseObserver);

    public io.grpc.stub.StreamObserver<org.vitrivr.adam.grpc.AdamGrpc.QueryMessage> doStreamingQuery(
        io.grpc.stub.StreamObserver<org.vitrivr.adam.grpc.AdamGrpc.QueryResultsMessage> responseObserver);

    public void doBatchQuery(org.vitrivr.adam.grpc.AdamGrpc.BatchedQueryMessage request,
        io.grpc.stub.StreamObserver<org.vitrivr.adam.grpc.AdamGrpc.BatchedQueryResultsMessage> responseObserver);

    public void doProgressiveQuery(org.vitrivr.adam.grpc.AdamGrpc.QueryMessage request,
        io.grpc.stub.StreamObserver<org.vitrivr.adam.grpc.AdamGrpc.QueryResultsMessage> responseObserver);

    public void getCachedResults(org.vitrivr.adam.grpc.AdamGrpc.CachedResultsMessage request,
        io.grpc.stub.StreamObserver<org.vitrivr.adam.grpc.AdamGrpc.QueryResultsMessage> responseObserver);
  }

  public static interface AdamSearchBlockingClient {

    public org.vitrivr.adam.grpc.AdamGrpc.AckMessage cacheIndex(org.vitrivr.adam.grpc.AdamGrpc.IndexNameMessage request);

    public org.vitrivr.adam.grpc.AdamGrpc.AckMessage cacheEntity(org.vitrivr.adam.grpc.AdamGrpc.EntityNameMessage request);

    public org.vitrivr.adam.grpc.AdamGrpc.QueryResultsMessage preview(org.vitrivr.adam.grpc.AdamGrpc.PreviewMessage request);

    public org.vitrivr.adam.grpc.AdamGrpc.QueryResultsMessage doQuery(org.vitrivr.adam.grpc.AdamGrpc.QueryMessage request);

    public org.vitrivr.adam.grpc.AdamGrpc.BatchedQueryResultsMessage doBatchQuery(org.vitrivr.adam.grpc.AdamGrpc.BatchedQueryMessage request);

    public java.util.Iterator<org.vitrivr.adam.grpc.AdamGrpc.QueryResultsMessage> doProgressiveQuery(
        org.vitrivr.adam.grpc.AdamGrpc.QueryMessage request);

    public org.vitrivr.adam.grpc.AdamGrpc.QueryResultsMessage getCachedResults(org.vitrivr.adam.grpc.AdamGrpc.CachedResultsMessage request);
  }

  public static interface AdamSearchFutureClient {

    public com.google.common.util.concurrent.ListenableFuture<org.vitrivr.adam.grpc.AdamGrpc.AckMessage> cacheIndex(
        org.vitrivr.adam.grpc.AdamGrpc.IndexNameMessage request);

    public com.google.common.util.concurrent.ListenableFuture<org.vitrivr.adam.grpc.AdamGrpc.AckMessage> cacheEntity(
        org.vitrivr.adam.grpc.AdamGrpc.EntityNameMessage request);

    public com.google.common.util.concurrent.ListenableFuture<org.vitrivr.adam.grpc.AdamGrpc.QueryResultsMessage> preview(
        org.vitrivr.adam.grpc.AdamGrpc.PreviewMessage request);

    public com.google.common.util.concurrent.ListenableFuture<org.vitrivr.adam.grpc.AdamGrpc.QueryResultsMessage> doQuery(
        org.vitrivr.adam.grpc.AdamGrpc.QueryMessage request);

    public com.google.common.util.concurrent.ListenableFuture<org.vitrivr.adam.grpc.AdamGrpc.BatchedQueryResultsMessage> doBatchQuery(
        org.vitrivr.adam.grpc.AdamGrpc.BatchedQueryMessage request);

    public com.google.common.util.concurrent.ListenableFuture<org.vitrivr.adam.grpc.AdamGrpc.QueryResultsMessage> getCachedResults(
        org.vitrivr.adam.grpc.AdamGrpc.CachedResultsMessage request);
  }

  public static class AdamSearchStub extends io.grpc.stub.AbstractStub<AdamSearchStub>
      implements AdamSearch {
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

    @java.lang.Override
    public void cacheIndex(org.vitrivr.adam.grpc.AdamGrpc.IndexNameMessage request,
        io.grpc.stub.StreamObserver<org.vitrivr.adam.grpc.AdamGrpc.AckMessage> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_CACHE_INDEX, getCallOptions()), request, responseObserver);
    }

    @java.lang.Override
    public void cacheEntity(org.vitrivr.adam.grpc.AdamGrpc.EntityNameMessage request,
        io.grpc.stub.StreamObserver<org.vitrivr.adam.grpc.AdamGrpc.AckMessage> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_CACHE_ENTITY, getCallOptions()), request, responseObserver);
    }

    @java.lang.Override
    public void preview(org.vitrivr.adam.grpc.AdamGrpc.PreviewMessage request,
        io.grpc.stub.StreamObserver<org.vitrivr.adam.grpc.AdamGrpc.QueryResultsMessage> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_PREVIEW, getCallOptions()), request, responseObserver);
    }

    @java.lang.Override
    public void doQuery(org.vitrivr.adam.grpc.AdamGrpc.QueryMessage request,
        io.grpc.stub.StreamObserver<org.vitrivr.adam.grpc.AdamGrpc.QueryResultsMessage> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_DO_QUERY, getCallOptions()), request, responseObserver);
    }

    @java.lang.Override
    public io.grpc.stub.StreamObserver<org.vitrivr.adam.grpc.AdamGrpc.QueryMessage> doStreamingQuery(
        io.grpc.stub.StreamObserver<org.vitrivr.adam.grpc.AdamGrpc.QueryResultsMessage> responseObserver) {
      return asyncBidiStreamingCall(
          getChannel().newCall(METHOD_DO_STREAMING_QUERY, getCallOptions()), responseObserver);
    }

    @java.lang.Override
    public void doBatchQuery(org.vitrivr.adam.grpc.AdamGrpc.BatchedQueryMessage request,
        io.grpc.stub.StreamObserver<org.vitrivr.adam.grpc.AdamGrpc.BatchedQueryResultsMessage> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_DO_BATCH_QUERY, getCallOptions()), request, responseObserver);
    }

    @java.lang.Override
    public void doProgressiveQuery(org.vitrivr.adam.grpc.AdamGrpc.QueryMessage request,
        io.grpc.stub.StreamObserver<org.vitrivr.adam.grpc.AdamGrpc.QueryResultsMessage> responseObserver) {
      asyncServerStreamingCall(
          getChannel().newCall(METHOD_DO_PROGRESSIVE_QUERY, getCallOptions()), request, responseObserver);
    }

    @java.lang.Override
    public void getCachedResults(org.vitrivr.adam.grpc.AdamGrpc.CachedResultsMessage request,
        io.grpc.stub.StreamObserver<org.vitrivr.adam.grpc.AdamGrpc.QueryResultsMessage> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_GET_CACHED_RESULTS, getCallOptions()), request, responseObserver);
    }
  }

  public static class AdamSearchBlockingStub extends io.grpc.stub.AbstractStub<AdamSearchBlockingStub>
      implements AdamSearchBlockingClient {
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

    @java.lang.Override
    public org.vitrivr.adam.grpc.AdamGrpc.AckMessage cacheIndex(org.vitrivr.adam.grpc.AdamGrpc.IndexNameMessage request) {
      return blockingUnaryCall(
          getChannel(), METHOD_CACHE_INDEX, getCallOptions(), request);
    }

    @java.lang.Override
    public org.vitrivr.adam.grpc.AdamGrpc.AckMessage cacheEntity(org.vitrivr.adam.grpc.AdamGrpc.EntityNameMessage request) {
      return blockingUnaryCall(
          getChannel(), METHOD_CACHE_ENTITY, getCallOptions(), request);
    }

    @java.lang.Override
    public org.vitrivr.adam.grpc.AdamGrpc.QueryResultsMessage preview(org.vitrivr.adam.grpc.AdamGrpc.PreviewMessage request) {
      return blockingUnaryCall(
          getChannel(), METHOD_PREVIEW, getCallOptions(), request);
    }

    @java.lang.Override
    public org.vitrivr.adam.grpc.AdamGrpc.QueryResultsMessage doQuery(org.vitrivr.adam.grpc.AdamGrpc.QueryMessage request) {
      return blockingUnaryCall(
          getChannel(), METHOD_DO_QUERY, getCallOptions(), request);
    }

    @java.lang.Override
    public org.vitrivr.adam.grpc.AdamGrpc.BatchedQueryResultsMessage doBatchQuery(org.vitrivr.adam.grpc.AdamGrpc.BatchedQueryMessage request) {
      return blockingUnaryCall(
          getChannel(), METHOD_DO_BATCH_QUERY, getCallOptions(), request);
    }

    @java.lang.Override
    public java.util.Iterator<org.vitrivr.adam.grpc.AdamGrpc.QueryResultsMessage> doProgressiveQuery(
        org.vitrivr.adam.grpc.AdamGrpc.QueryMessage request) {
      return blockingServerStreamingCall(
          getChannel(), METHOD_DO_PROGRESSIVE_QUERY, getCallOptions(), request);
    }

    @java.lang.Override
    public org.vitrivr.adam.grpc.AdamGrpc.QueryResultsMessage getCachedResults(org.vitrivr.adam.grpc.AdamGrpc.CachedResultsMessage request) {
      return blockingUnaryCall(
          getChannel(), METHOD_GET_CACHED_RESULTS, getCallOptions(), request);
    }
  }

  public static class AdamSearchFutureStub extends io.grpc.stub.AbstractStub<AdamSearchFutureStub>
      implements AdamSearchFutureClient {
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

    @java.lang.Override
    public com.google.common.util.concurrent.ListenableFuture<org.vitrivr.adam.grpc.AdamGrpc.AckMessage> cacheIndex(
        org.vitrivr.adam.grpc.AdamGrpc.IndexNameMessage request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_CACHE_INDEX, getCallOptions()), request);
    }

    @java.lang.Override
    public com.google.common.util.concurrent.ListenableFuture<org.vitrivr.adam.grpc.AdamGrpc.AckMessage> cacheEntity(
        org.vitrivr.adam.grpc.AdamGrpc.EntityNameMessage request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_CACHE_ENTITY, getCallOptions()), request);
    }

    @java.lang.Override
    public com.google.common.util.concurrent.ListenableFuture<org.vitrivr.adam.grpc.AdamGrpc.QueryResultsMessage> preview(
        org.vitrivr.adam.grpc.AdamGrpc.PreviewMessage request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_PREVIEW, getCallOptions()), request);
    }

    @java.lang.Override
    public com.google.common.util.concurrent.ListenableFuture<org.vitrivr.adam.grpc.AdamGrpc.QueryResultsMessage> doQuery(
        org.vitrivr.adam.grpc.AdamGrpc.QueryMessage request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_DO_QUERY, getCallOptions()), request);
    }

    @java.lang.Override
    public com.google.common.util.concurrent.ListenableFuture<org.vitrivr.adam.grpc.AdamGrpc.BatchedQueryResultsMessage> doBatchQuery(
        org.vitrivr.adam.grpc.AdamGrpc.BatchedQueryMessage request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_DO_BATCH_QUERY, getCallOptions()), request);
    }

    @java.lang.Override
    public com.google.common.util.concurrent.ListenableFuture<org.vitrivr.adam.grpc.AdamGrpc.QueryResultsMessage> getCachedResults(
        org.vitrivr.adam.grpc.AdamGrpc.CachedResultsMessage request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_GET_CACHED_RESULTS, getCallOptions()), request);
    }
  }

  private static final int METHODID_CACHE_INDEX = 0;
  private static final int METHODID_CACHE_ENTITY = 1;
  private static final int METHODID_PREVIEW = 2;
  private static final int METHODID_DO_QUERY = 3;
  private static final int METHODID_DO_BATCH_QUERY = 4;
  private static final int METHODID_DO_PROGRESSIVE_QUERY = 5;
  private static final int METHODID_GET_CACHED_RESULTS = 6;
  private static final int METHODID_DO_STREAMING_QUERY = 7;

  private static class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final AdamSearch serviceImpl;
    private final int methodId;

    public MethodHandlers(AdamSearch serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_CACHE_INDEX:
          serviceImpl.cacheIndex((org.vitrivr.adam.grpc.AdamGrpc.IndexNameMessage) request,
              (io.grpc.stub.StreamObserver<org.vitrivr.adam.grpc.AdamGrpc.AckMessage>) responseObserver);
          break;
        case METHODID_CACHE_ENTITY:
          serviceImpl.cacheEntity((org.vitrivr.adam.grpc.AdamGrpc.EntityNameMessage) request,
              (io.grpc.stub.StreamObserver<org.vitrivr.adam.grpc.AdamGrpc.AckMessage>) responseObserver);
          break;
        case METHODID_PREVIEW:
          serviceImpl.preview((org.vitrivr.adam.grpc.AdamGrpc.PreviewMessage) request,
              (io.grpc.stub.StreamObserver<org.vitrivr.adam.grpc.AdamGrpc.QueryResultsMessage>) responseObserver);
          break;
        case METHODID_DO_QUERY:
          serviceImpl.doQuery((org.vitrivr.adam.grpc.AdamGrpc.QueryMessage) request,
              (io.grpc.stub.StreamObserver<org.vitrivr.adam.grpc.AdamGrpc.QueryResultsMessage>) responseObserver);
          break;
        case METHODID_DO_BATCH_QUERY:
          serviceImpl.doBatchQuery((org.vitrivr.adam.grpc.AdamGrpc.BatchedQueryMessage) request,
              (io.grpc.stub.StreamObserver<org.vitrivr.adam.grpc.AdamGrpc.BatchedQueryResultsMessage>) responseObserver);
          break;
        case METHODID_DO_PROGRESSIVE_QUERY:
          serviceImpl.doProgressiveQuery((org.vitrivr.adam.grpc.AdamGrpc.QueryMessage) request,
              (io.grpc.stub.StreamObserver<org.vitrivr.adam.grpc.AdamGrpc.QueryResultsMessage>) responseObserver);
          break;
        case METHODID_GET_CACHED_RESULTS:
          serviceImpl.getCachedResults((org.vitrivr.adam.grpc.AdamGrpc.CachedResultsMessage) request,
              (io.grpc.stub.StreamObserver<org.vitrivr.adam.grpc.AdamGrpc.QueryResultsMessage>) responseObserver);
          break;
        default:
          throw new AssertionError();
      }
    }

    @java.lang.SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<Req> invoke(
        io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_DO_STREAMING_QUERY:
          return (io.grpc.stub.StreamObserver<Req>) serviceImpl.doStreamingQuery(
              (io.grpc.stub.StreamObserver<org.vitrivr.adam.grpc.AdamGrpc.QueryResultsMessage>) responseObserver);
        default:
          throw new AssertionError();
      }
    }
  }

  public static io.grpc.ServerServiceDefinition bindService(
      final AdamSearch serviceImpl) {
    return io.grpc.ServerServiceDefinition.builder(SERVICE_NAME)
        .addMethod(
          METHOD_CACHE_INDEX,
          asyncUnaryCall(
            new MethodHandlers<
              org.vitrivr.adam.grpc.AdamGrpc.IndexNameMessage,
              org.vitrivr.adam.grpc.AdamGrpc.AckMessage>(
                serviceImpl, METHODID_CACHE_INDEX)))
        .addMethod(
          METHOD_CACHE_ENTITY,
          asyncUnaryCall(
            new MethodHandlers<
              org.vitrivr.adam.grpc.AdamGrpc.EntityNameMessage,
              org.vitrivr.adam.grpc.AdamGrpc.AckMessage>(
                serviceImpl, METHODID_CACHE_ENTITY)))
        .addMethod(
          METHOD_PREVIEW,
          asyncUnaryCall(
            new MethodHandlers<
              org.vitrivr.adam.grpc.AdamGrpc.PreviewMessage,
              org.vitrivr.adam.grpc.AdamGrpc.QueryResultsMessage>(
                serviceImpl, METHODID_PREVIEW)))
        .addMethod(
          METHOD_DO_QUERY,
          asyncUnaryCall(
            new MethodHandlers<
              org.vitrivr.adam.grpc.AdamGrpc.QueryMessage,
              org.vitrivr.adam.grpc.AdamGrpc.QueryResultsMessage>(
                serviceImpl, METHODID_DO_QUERY)))
        .addMethod(
          METHOD_DO_STREAMING_QUERY,
          asyncBidiStreamingCall(
            new MethodHandlers<
              org.vitrivr.adam.grpc.AdamGrpc.QueryMessage,
              org.vitrivr.adam.grpc.AdamGrpc.QueryResultsMessage>(
                serviceImpl, METHODID_DO_STREAMING_QUERY)))
        .addMethod(
          METHOD_DO_BATCH_QUERY,
          asyncUnaryCall(
            new MethodHandlers<
              org.vitrivr.adam.grpc.AdamGrpc.BatchedQueryMessage,
              org.vitrivr.adam.grpc.AdamGrpc.BatchedQueryResultsMessage>(
                serviceImpl, METHODID_DO_BATCH_QUERY)))
        .addMethod(
          METHOD_DO_PROGRESSIVE_QUERY,
          asyncServerStreamingCall(
            new MethodHandlers<
              org.vitrivr.adam.grpc.AdamGrpc.QueryMessage,
              org.vitrivr.adam.grpc.AdamGrpc.QueryResultsMessage>(
                serviceImpl, METHODID_DO_PROGRESSIVE_QUERY)))
        .addMethod(
          METHOD_GET_CACHED_RESULTS,
          asyncUnaryCall(
            new MethodHandlers<
              org.vitrivr.adam.grpc.AdamGrpc.CachedResultsMessage,
              org.vitrivr.adam.grpc.AdamGrpc.QueryResultsMessage>(
                serviceImpl, METHODID_GET_CACHED_RESULTS)))
        .build();
  }
}
