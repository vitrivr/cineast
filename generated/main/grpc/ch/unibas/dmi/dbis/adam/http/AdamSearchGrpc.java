package ch.unibas.dmi.dbis.adam.http;

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
  public static final io.grpc.MethodDescriptor<ch.unibas.dmi.dbis.adam.http.Grpc.IndexNameMessage,
      ch.unibas.dmi.dbis.adam.http.Grpc.AckMessage> METHOD_CACHE_INDEX =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "AdamSearch", "CacheIndex"),
          io.grpc.protobuf.ProtoUtils.marshaller(ch.unibas.dmi.dbis.adam.http.Grpc.IndexNameMessage.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(ch.unibas.dmi.dbis.adam.http.Grpc.AckMessage.getDefaultInstance()));
  @io.grpc.ExperimentalApi
  public static final io.grpc.MethodDescriptor<ch.unibas.dmi.dbis.adam.http.Grpc.EntityNameMessage,
      ch.unibas.dmi.dbis.adam.http.Grpc.AckMessage> METHOD_CACHE_ENTITY =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "AdamSearch", "CacheEntity"),
          io.grpc.protobuf.ProtoUtils.marshaller(ch.unibas.dmi.dbis.adam.http.Grpc.EntityNameMessage.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(ch.unibas.dmi.dbis.adam.http.Grpc.AckMessage.getDefaultInstance()));
  @io.grpc.ExperimentalApi
  public static final io.grpc.MethodDescriptor<ch.unibas.dmi.dbis.adam.http.Grpc.SimpleQueryMessage,
      ch.unibas.dmi.dbis.adam.http.Grpc.QueryResponseInfoMessage> METHOD_DO_STANDARD_QUERY =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "AdamSearch", "DoStandardQuery"),
          io.grpc.protobuf.ProtoUtils.marshaller(ch.unibas.dmi.dbis.adam.http.Grpc.SimpleQueryMessage.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(ch.unibas.dmi.dbis.adam.http.Grpc.QueryResponseInfoMessage.getDefaultInstance()));
  @io.grpc.ExperimentalApi
  public static final io.grpc.MethodDescriptor<ch.unibas.dmi.dbis.adam.http.Grpc.SimpleSequentialQueryMessage,
      ch.unibas.dmi.dbis.adam.http.Grpc.QueryResponseInfoMessage> METHOD_DO_SEQUENTIAL_QUERY =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "AdamSearch", "DoSequentialQuery"),
          io.grpc.protobuf.ProtoUtils.marshaller(ch.unibas.dmi.dbis.adam.http.Grpc.SimpleSequentialQueryMessage.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(ch.unibas.dmi.dbis.adam.http.Grpc.QueryResponseInfoMessage.getDefaultInstance()));
  @io.grpc.ExperimentalApi
  public static final io.grpc.MethodDescriptor<ch.unibas.dmi.dbis.adam.http.Grpc.SimpleSpecifiedIndexQueryMessage,
      ch.unibas.dmi.dbis.adam.http.Grpc.QueryResponseInfoMessage> METHOD_DO_SPECIFIED_INDEX_QUERY =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "AdamSearch", "DoSpecifiedIndexQuery"),
          io.grpc.protobuf.ProtoUtils.marshaller(ch.unibas.dmi.dbis.adam.http.Grpc.SimpleSpecifiedIndexQueryMessage.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(ch.unibas.dmi.dbis.adam.http.Grpc.QueryResponseInfoMessage.getDefaultInstance()));
  @io.grpc.ExperimentalApi
  public static final io.grpc.MethodDescriptor<ch.unibas.dmi.dbis.adam.http.Grpc.SimpleIndexQueryMessage,
      ch.unibas.dmi.dbis.adam.http.Grpc.QueryResponseInfoMessage> METHOD_DO_INDEX_QUERY =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "AdamSearch", "DoIndexQuery"),
          io.grpc.protobuf.ProtoUtils.marshaller(ch.unibas.dmi.dbis.adam.http.Grpc.SimpleIndexQueryMessage.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(ch.unibas.dmi.dbis.adam.http.Grpc.QueryResponseInfoMessage.getDefaultInstance()));
  @io.grpc.ExperimentalApi
  public static final io.grpc.MethodDescriptor<ch.unibas.dmi.dbis.adam.http.Grpc.SimpleQueryMessage,
      ch.unibas.dmi.dbis.adam.http.Grpc.QueryResponseInfoMessage> METHOD_DO_PROGRESSIVE_QUERY =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING,
          generateFullMethodName(
              "AdamSearch", "DoProgressiveQuery"),
          io.grpc.protobuf.ProtoUtils.marshaller(ch.unibas.dmi.dbis.adam.http.Grpc.SimpleQueryMessage.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(ch.unibas.dmi.dbis.adam.http.Grpc.QueryResponseInfoMessage.getDefaultInstance()));
  @io.grpc.ExperimentalApi
  public static final io.grpc.MethodDescriptor<ch.unibas.dmi.dbis.adam.http.Grpc.TimedQueryMessage,
      ch.unibas.dmi.dbis.adam.http.Grpc.QueryResponseInfoMessage> METHOD_DO_TIMED_PROGRESSIVE_QUERY =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "AdamSearch", "DoTimedProgressiveQuery"),
          io.grpc.protobuf.ProtoUtils.marshaller(ch.unibas.dmi.dbis.adam.http.Grpc.TimedQueryMessage.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(ch.unibas.dmi.dbis.adam.http.Grpc.QueryResponseInfoMessage.getDefaultInstance()));
  @io.grpc.ExperimentalApi
  public static final io.grpc.MethodDescriptor<ch.unibas.dmi.dbis.adam.http.Grpc.CompoundQueryMessage,
      ch.unibas.dmi.dbis.adam.http.Grpc.CompoundQueryResponseInfoMessage> METHOD_DO_COMPOUND_QUERY =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "AdamSearch", "DoCompoundQuery"),
          io.grpc.protobuf.ProtoUtils.marshaller(ch.unibas.dmi.dbis.adam.http.Grpc.CompoundQueryMessage.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(ch.unibas.dmi.dbis.adam.http.Grpc.CompoundQueryResponseInfoMessage.getDefaultInstance()));
  @io.grpc.ExperimentalApi
  public static final io.grpc.MethodDescriptor<ch.unibas.dmi.dbis.adam.http.Grpc.SimpleBooleanQueryMessage,
      ch.unibas.dmi.dbis.adam.http.Grpc.QueryResponseInfoMessage> METHOD_DO_BOOLEAN_QUERY =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "AdamSearch", "DoBooleanQuery"),
          io.grpc.protobuf.ProtoUtils.marshaller(ch.unibas.dmi.dbis.adam.http.Grpc.SimpleBooleanQueryMessage.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(ch.unibas.dmi.dbis.adam.http.Grpc.QueryResponseInfoMessage.getDefaultInstance()));
  @io.grpc.ExperimentalApi
  public static final io.grpc.MethodDescriptor<ch.unibas.dmi.dbis.adam.http.Grpc.CachedResultsMessage,
      ch.unibas.dmi.dbis.adam.http.Grpc.QueryResponseInfoMessage> METHOD_GET_CACHED_RESULTS =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "AdamSearch", "GetCachedResults"),
          io.grpc.protobuf.ProtoUtils.marshaller(ch.unibas.dmi.dbis.adam.http.Grpc.CachedResultsMessage.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(ch.unibas.dmi.dbis.adam.http.Grpc.QueryResponseInfoMessage.getDefaultInstance()));

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

    public void cacheIndex(ch.unibas.dmi.dbis.adam.http.Grpc.IndexNameMessage request,
        io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.adam.http.Grpc.AckMessage> responseObserver);

    public void cacheEntity(ch.unibas.dmi.dbis.adam.http.Grpc.EntityNameMessage request,
        io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.adam.http.Grpc.AckMessage> responseObserver);

    public void doStandardQuery(ch.unibas.dmi.dbis.adam.http.Grpc.SimpleQueryMessage request,
        io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.adam.http.Grpc.QueryResponseInfoMessage> responseObserver);

    public void doSequentialQuery(ch.unibas.dmi.dbis.adam.http.Grpc.SimpleSequentialQueryMessage request,
        io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.adam.http.Grpc.QueryResponseInfoMessage> responseObserver);

    public void doSpecifiedIndexQuery(ch.unibas.dmi.dbis.adam.http.Grpc.SimpleSpecifiedIndexQueryMessage request,
        io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.adam.http.Grpc.QueryResponseInfoMessage> responseObserver);

    public void doIndexQuery(ch.unibas.dmi.dbis.adam.http.Grpc.SimpleIndexQueryMessage request,
        io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.adam.http.Grpc.QueryResponseInfoMessage> responseObserver);

    public void doProgressiveQuery(ch.unibas.dmi.dbis.adam.http.Grpc.SimpleQueryMessage request,
        io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.adam.http.Grpc.QueryResponseInfoMessage> responseObserver);

    public void doTimedProgressiveQuery(ch.unibas.dmi.dbis.adam.http.Grpc.TimedQueryMessage request,
        io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.adam.http.Grpc.QueryResponseInfoMessage> responseObserver);

    public void doCompoundQuery(ch.unibas.dmi.dbis.adam.http.Grpc.CompoundQueryMessage request,
        io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.adam.http.Grpc.CompoundQueryResponseInfoMessage> responseObserver);

    public void doBooleanQuery(ch.unibas.dmi.dbis.adam.http.Grpc.SimpleBooleanQueryMessage request,
        io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.adam.http.Grpc.QueryResponseInfoMessage> responseObserver);

    public void getCachedResults(ch.unibas.dmi.dbis.adam.http.Grpc.CachedResultsMessage request,
        io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.adam.http.Grpc.QueryResponseInfoMessage> responseObserver);
  }

  public static interface AdamSearchBlockingClient {

    public ch.unibas.dmi.dbis.adam.http.Grpc.AckMessage cacheIndex(ch.unibas.dmi.dbis.adam.http.Grpc.IndexNameMessage request);

    public ch.unibas.dmi.dbis.adam.http.Grpc.AckMessage cacheEntity(ch.unibas.dmi.dbis.adam.http.Grpc.EntityNameMessage request);

    public ch.unibas.dmi.dbis.adam.http.Grpc.QueryResponseInfoMessage doStandardQuery(ch.unibas.dmi.dbis.adam.http.Grpc.SimpleQueryMessage request);

    public ch.unibas.dmi.dbis.adam.http.Grpc.QueryResponseInfoMessage doSequentialQuery(ch.unibas.dmi.dbis.adam.http.Grpc.SimpleSequentialQueryMessage request);

    public ch.unibas.dmi.dbis.adam.http.Grpc.QueryResponseInfoMessage doSpecifiedIndexQuery(ch.unibas.dmi.dbis.adam.http.Grpc.SimpleSpecifiedIndexQueryMessage request);

    public ch.unibas.dmi.dbis.adam.http.Grpc.QueryResponseInfoMessage doIndexQuery(ch.unibas.dmi.dbis.adam.http.Grpc.SimpleIndexQueryMessage request);

    public java.util.Iterator<ch.unibas.dmi.dbis.adam.http.Grpc.QueryResponseInfoMessage> doProgressiveQuery(
        ch.unibas.dmi.dbis.adam.http.Grpc.SimpleQueryMessage request);

    public ch.unibas.dmi.dbis.adam.http.Grpc.QueryResponseInfoMessage doTimedProgressiveQuery(ch.unibas.dmi.dbis.adam.http.Grpc.TimedQueryMessage request);

    public ch.unibas.dmi.dbis.adam.http.Grpc.CompoundQueryResponseInfoMessage doCompoundQuery(ch.unibas.dmi.dbis.adam.http.Grpc.CompoundQueryMessage request);

    public ch.unibas.dmi.dbis.adam.http.Grpc.QueryResponseInfoMessage doBooleanQuery(ch.unibas.dmi.dbis.adam.http.Grpc.SimpleBooleanQueryMessage request);

    public ch.unibas.dmi.dbis.adam.http.Grpc.QueryResponseInfoMessage getCachedResults(ch.unibas.dmi.dbis.adam.http.Grpc.CachedResultsMessage request);
  }

  public static interface AdamSearchFutureClient {

    public com.google.common.util.concurrent.ListenableFuture<ch.unibas.dmi.dbis.adam.http.Grpc.AckMessage> cacheIndex(
        ch.unibas.dmi.dbis.adam.http.Grpc.IndexNameMessage request);

    public com.google.common.util.concurrent.ListenableFuture<ch.unibas.dmi.dbis.adam.http.Grpc.AckMessage> cacheEntity(
        ch.unibas.dmi.dbis.adam.http.Grpc.EntityNameMessage request);

    public com.google.common.util.concurrent.ListenableFuture<ch.unibas.dmi.dbis.adam.http.Grpc.QueryResponseInfoMessage> doStandardQuery(
        ch.unibas.dmi.dbis.adam.http.Grpc.SimpleQueryMessage request);

    public com.google.common.util.concurrent.ListenableFuture<ch.unibas.dmi.dbis.adam.http.Grpc.QueryResponseInfoMessage> doSequentialQuery(
        ch.unibas.dmi.dbis.adam.http.Grpc.SimpleSequentialQueryMessage request);

    public com.google.common.util.concurrent.ListenableFuture<ch.unibas.dmi.dbis.adam.http.Grpc.QueryResponseInfoMessage> doSpecifiedIndexQuery(
        ch.unibas.dmi.dbis.adam.http.Grpc.SimpleSpecifiedIndexQueryMessage request);

    public com.google.common.util.concurrent.ListenableFuture<ch.unibas.dmi.dbis.adam.http.Grpc.QueryResponseInfoMessage> doIndexQuery(
        ch.unibas.dmi.dbis.adam.http.Grpc.SimpleIndexQueryMessage request);

    public com.google.common.util.concurrent.ListenableFuture<ch.unibas.dmi.dbis.adam.http.Grpc.QueryResponseInfoMessage> doTimedProgressiveQuery(
        ch.unibas.dmi.dbis.adam.http.Grpc.TimedQueryMessage request);

    public com.google.common.util.concurrent.ListenableFuture<ch.unibas.dmi.dbis.adam.http.Grpc.CompoundQueryResponseInfoMessage> doCompoundQuery(
        ch.unibas.dmi.dbis.adam.http.Grpc.CompoundQueryMessage request);

    public com.google.common.util.concurrent.ListenableFuture<ch.unibas.dmi.dbis.adam.http.Grpc.QueryResponseInfoMessage> doBooleanQuery(
        ch.unibas.dmi.dbis.adam.http.Grpc.SimpleBooleanQueryMessage request);

    public com.google.common.util.concurrent.ListenableFuture<ch.unibas.dmi.dbis.adam.http.Grpc.QueryResponseInfoMessage> getCachedResults(
        ch.unibas.dmi.dbis.adam.http.Grpc.CachedResultsMessage request);
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
    public void cacheIndex(ch.unibas.dmi.dbis.adam.http.Grpc.IndexNameMessage request,
        io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.adam.http.Grpc.AckMessage> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_CACHE_INDEX, getCallOptions()), request, responseObserver);
    }

    @java.lang.Override
    public void cacheEntity(ch.unibas.dmi.dbis.adam.http.Grpc.EntityNameMessage request,
        io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.adam.http.Grpc.AckMessage> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_CACHE_ENTITY, getCallOptions()), request, responseObserver);
    }

    @java.lang.Override
    public void doStandardQuery(ch.unibas.dmi.dbis.adam.http.Grpc.SimpleQueryMessage request,
        io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.adam.http.Grpc.QueryResponseInfoMessage> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_DO_STANDARD_QUERY, getCallOptions()), request, responseObserver);
    }

    @java.lang.Override
    public void doSequentialQuery(ch.unibas.dmi.dbis.adam.http.Grpc.SimpleSequentialQueryMessage request,
        io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.adam.http.Grpc.QueryResponseInfoMessage> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_DO_SEQUENTIAL_QUERY, getCallOptions()), request, responseObserver);
    }

    @java.lang.Override
    public void doSpecifiedIndexQuery(ch.unibas.dmi.dbis.adam.http.Grpc.SimpleSpecifiedIndexQueryMessage request,
        io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.adam.http.Grpc.QueryResponseInfoMessage> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_DO_SPECIFIED_INDEX_QUERY, getCallOptions()), request, responseObserver);
    }

    @java.lang.Override
    public void doIndexQuery(ch.unibas.dmi.dbis.adam.http.Grpc.SimpleIndexQueryMessage request,
        io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.adam.http.Grpc.QueryResponseInfoMessage> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_DO_INDEX_QUERY, getCallOptions()), request, responseObserver);
    }

    @java.lang.Override
    public void doProgressiveQuery(ch.unibas.dmi.dbis.adam.http.Grpc.SimpleQueryMessage request,
        io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.adam.http.Grpc.QueryResponseInfoMessage> responseObserver) {
      asyncServerStreamingCall(
          getChannel().newCall(METHOD_DO_PROGRESSIVE_QUERY, getCallOptions()), request, responseObserver);
    }

    @java.lang.Override
    public void doTimedProgressiveQuery(ch.unibas.dmi.dbis.adam.http.Grpc.TimedQueryMessage request,
        io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.adam.http.Grpc.QueryResponseInfoMessage> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_DO_TIMED_PROGRESSIVE_QUERY, getCallOptions()), request, responseObserver);
    }

    @java.lang.Override
    public void doCompoundQuery(ch.unibas.dmi.dbis.adam.http.Grpc.CompoundQueryMessage request,
        io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.adam.http.Grpc.CompoundQueryResponseInfoMessage> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_DO_COMPOUND_QUERY, getCallOptions()), request, responseObserver);
    }

    @java.lang.Override
    public void doBooleanQuery(ch.unibas.dmi.dbis.adam.http.Grpc.SimpleBooleanQueryMessage request,
        io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.adam.http.Grpc.QueryResponseInfoMessage> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_DO_BOOLEAN_QUERY, getCallOptions()), request, responseObserver);
    }

    @java.lang.Override
    public void getCachedResults(ch.unibas.dmi.dbis.adam.http.Grpc.CachedResultsMessage request,
        io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.adam.http.Grpc.QueryResponseInfoMessage> responseObserver) {
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
    public ch.unibas.dmi.dbis.adam.http.Grpc.AckMessage cacheIndex(ch.unibas.dmi.dbis.adam.http.Grpc.IndexNameMessage request) {
      return blockingUnaryCall(
          getChannel(), METHOD_CACHE_INDEX, getCallOptions(), request);
    }

    @java.lang.Override
    public ch.unibas.dmi.dbis.adam.http.Grpc.AckMessage cacheEntity(ch.unibas.dmi.dbis.adam.http.Grpc.EntityNameMessage request) {
      return blockingUnaryCall(
          getChannel(), METHOD_CACHE_ENTITY, getCallOptions(), request);
    }

    @java.lang.Override
    public ch.unibas.dmi.dbis.adam.http.Grpc.QueryResponseInfoMessage doStandardQuery(ch.unibas.dmi.dbis.adam.http.Grpc.SimpleQueryMessage request) {
      return blockingUnaryCall(
          getChannel(), METHOD_DO_STANDARD_QUERY, getCallOptions(), request);
    }

    @java.lang.Override
    public ch.unibas.dmi.dbis.adam.http.Grpc.QueryResponseInfoMessage doSequentialQuery(ch.unibas.dmi.dbis.adam.http.Grpc.SimpleSequentialQueryMessage request) {
      return blockingUnaryCall(
          getChannel(), METHOD_DO_SEQUENTIAL_QUERY, getCallOptions(), request);
    }

    @java.lang.Override
    public ch.unibas.dmi.dbis.adam.http.Grpc.QueryResponseInfoMessage doSpecifiedIndexQuery(ch.unibas.dmi.dbis.adam.http.Grpc.SimpleSpecifiedIndexQueryMessage request) {
      return blockingUnaryCall(
          getChannel(), METHOD_DO_SPECIFIED_INDEX_QUERY, getCallOptions(), request);
    }

    @java.lang.Override
    public ch.unibas.dmi.dbis.adam.http.Grpc.QueryResponseInfoMessage doIndexQuery(ch.unibas.dmi.dbis.adam.http.Grpc.SimpleIndexQueryMessage request) {
      return blockingUnaryCall(
          getChannel(), METHOD_DO_INDEX_QUERY, getCallOptions(), request);
    }

    @java.lang.Override
    public java.util.Iterator<ch.unibas.dmi.dbis.adam.http.Grpc.QueryResponseInfoMessage> doProgressiveQuery(
        ch.unibas.dmi.dbis.adam.http.Grpc.SimpleQueryMessage request) {
      return blockingServerStreamingCall(
          getChannel(), METHOD_DO_PROGRESSIVE_QUERY, getCallOptions(), request);
    }

    @java.lang.Override
    public ch.unibas.dmi.dbis.adam.http.Grpc.QueryResponseInfoMessage doTimedProgressiveQuery(ch.unibas.dmi.dbis.adam.http.Grpc.TimedQueryMessage request) {
      return blockingUnaryCall(
          getChannel(), METHOD_DO_TIMED_PROGRESSIVE_QUERY, getCallOptions(), request);
    }

    @java.lang.Override
    public ch.unibas.dmi.dbis.adam.http.Grpc.CompoundQueryResponseInfoMessage doCompoundQuery(ch.unibas.dmi.dbis.adam.http.Grpc.CompoundQueryMessage request) {
      return blockingUnaryCall(
          getChannel(), METHOD_DO_COMPOUND_QUERY, getCallOptions(), request);
    }

    @java.lang.Override
    public ch.unibas.dmi.dbis.adam.http.Grpc.QueryResponseInfoMessage doBooleanQuery(ch.unibas.dmi.dbis.adam.http.Grpc.SimpleBooleanQueryMessage request) {
      return blockingUnaryCall(
          getChannel(), METHOD_DO_BOOLEAN_QUERY, getCallOptions(), request);
    }

    @java.lang.Override
    public ch.unibas.dmi.dbis.adam.http.Grpc.QueryResponseInfoMessage getCachedResults(ch.unibas.dmi.dbis.adam.http.Grpc.CachedResultsMessage request) {
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
    public com.google.common.util.concurrent.ListenableFuture<ch.unibas.dmi.dbis.adam.http.Grpc.AckMessage> cacheIndex(
        ch.unibas.dmi.dbis.adam.http.Grpc.IndexNameMessage request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_CACHE_INDEX, getCallOptions()), request);
    }

    @java.lang.Override
    public com.google.common.util.concurrent.ListenableFuture<ch.unibas.dmi.dbis.adam.http.Grpc.AckMessage> cacheEntity(
        ch.unibas.dmi.dbis.adam.http.Grpc.EntityNameMessage request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_CACHE_ENTITY, getCallOptions()), request);
    }

    @java.lang.Override
    public com.google.common.util.concurrent.ListenableFuture<ch.unibas.dmi.dbis.adam.http.Grpc.QueryResponseInfoMessage> doStandardQuery(
        ch.unibas.dmi.dbis.adam.http.Grpc.SimpleQueryMessage request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_DO_STANDARD_QUERY, getCallOptions()), request);
    }

    @java.lang.Override
    public com.google.common.util.concurrent.ListenableFuture<ch.unibas.dmi.dbis.adam.http.Grpc.QueryResponseInfoMessage> doSequentialQuery(
        ch.unibas.dmi.dbis.adam.http.Grpc.SimpleSequentialQueryMessage request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_DO_SEQUENTIAL_QUERY, getCallOptions()), request);
    }

    @java.lang.Override
    public com.google.common.util.concurrent.ListenableFuture<ch.unibas.dmi.dbis.adam.http.Grpc.QueryResponseInfoMessage> doSpecifiedIndexQuery(
        ch.unibas.dmi.dbis.adam.http.Grpc.SimpleSpecifiedIndexQueryMessage request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_DO_SPECIFIED_INDEX_QUERY, getCallOptions()), request);
    }

    @java.lang.Override
    public com.google.common.util.concurrent.ListenableFuture<ch.unibas.dmi.dbis.adam.http.Grpc.QueryResponseInfoMessage> doIndexQuery(
        ch.unibas.dmi.dbis.adam.http.Grpc.SimpleIndexQueryMessage request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_DO_INDEX_QUERY, getCallOptions()), request);
    }

    @java.lang.Override
    public com.google.common.util.concurrent.ListenableFuture<ch.unibas.dmi.dbis.adam.http.Grpc.QueryResponseInfoMessage> doTimedProgressiveQuery(
        ch.unibas.dmi.dbis.adam.http.Grpc.TimedQueryMessage request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_DO_TIMED_PROGRESSIVE_QUERY, getCallOptions()), request);
    }

    @java.lang.Override
    public com.google.common.util.concurrent.ListenableFuture<ch.unibas.dmi.dbis.adam.http.Grpc.CompoundQueryResponseInfoMessage> doCompoundQuery(
        ch.unibas.dmi.dbis.adam.http.Grpc.CompoundQueryMessage request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_DO_COMPOUND_QUERY, getCallOptions()), request);
    }

    @java.lang.Override
    public com.google.common.util.concurrent.ListenableFuture<ch.unibas.dmi.dbis.adam.http.Grpc.QueryResponseInfoMessage> doBooleanQuery(
        ch.unibas.dmi.dbis.adam.http.Grpc.SimpleBooleanQueryMessage request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_DO_BOOLEAN_QUERY, getCallOptions()), request);
    }

    @java.lang.Override
    public com.google.common.util.concurrent.ListenableFuture<ch.unibas.dmi.dbis.adam.http.Grpc.QueryResponseInfoMessage> getCachedResults(
        ch.unibas.dmi.dbis.adam.http.Grpc.CachedResultsMessage request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_GET_CACHED_RESULTS, getCallOptions()), request);
    }
  }

  private static final int METHODID_CACHE_INDEX = 0;
  private static final int METHODID_CACHE_ENTITY = 1;
  private static final int METHODID_DO_STANDARD_QUERY = 2;
  private static final int METHODID_DO_SEQUENTIAL_QUERY = 3;
  private static final int METHODID_DO_SPECIFIED_INDEX_QUERY = 4;
  private static final int METHODID_DO_INDEX_QUERY = 5;
  private static final int METHODID_DO_PROGRESSIVE_QUERY = 6;
  private static final int METHODID_DO_TIMED_PROGRESSIVE_QUERY = 7;
  private static final int METHODID_DO_COMPOUND_QUERY = 8;
  private static final int METHODID_DO_BOOLEAN_QUERY = 9;
  private static final int METHODID_GET_CACHED_RESULTS = 10;

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
          serviceImpl.cacheIndex((ch.unibas.dmi.dbis.adam.http.Grpc.IndexNameMessage) request,
              (io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.adam.http.Grpc.AckMessage>) responseObserver);
          break;
        case METHODID_CACHE_ENTITY:
          serviceImpl.cacheEntity((ch.unibas.dmi.dbis.adam.http.Grpc.EntityNameMessage) request,
              (io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.adam.http.Grpc.AckMessage>) responseObserver);
          break;
        case METHODID_DO_STANDARD_QUERY:
          serviceImpl.doStandardQuery((ch.unibas.dmi.dbis.adam.http.Grpc.SimpleQueryMessage) request,
              (io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.adam.http.Grpc.QueryResponseInfoMessage>) responseObserver);
          break;
        case METHODID_DO_SEQUENTIAL_QUERY:
          serviceImpl.doSequentialQuery((ch.unibas.dmi.dbis.adam.http.Grpc.SimpleSequentialQueryMessage) request,
              (io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.adam.http.Grpc.QueryResponseInfoMessage>) responseObserver);
          break;
        case METHODID_DO_SPECIFIED_INDEX_QUERY:
          serviceImpl.doSpecifiedIndexQuery((ch.unibas.dmi.dbis.adam.http.Grpc.SimpleSpecifiedIndexQueryMessage) request,
              (io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.adam.http.Grpc.QueryResponseInfoMessage>) responseObserver);
          break;
        case METHODID_DO_INDEX_QUERY:
          serviceImpl.doIndexQuery((ch.unibas.dmi.dbis.adam.http.Grpc.SimpleIndexQueryMessage) request,
              (io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.adam.http.Grpc.QueryResponseInfoMessage>) responseObserver);
          break;
        case METHODID_DO_PROGRESSIVE_QUERY:
          serviceImpl.doProgressiveQuery((ch.unibas.dmi.dbis.adam.http.Grpc.SimpleQueryMessage) request,
              (io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.adam.http.Grpc.QueryResponseInfoMessage>) responseObserver);
          break;
        case METHODID_DO_TIMED_PROGRESSIVE_QUERY:
          serviceImpl.doTimedProgressiveQuery((ch.unibas.dmi.dbis.adam.http.Grpc.TimedQueryMessage) request,
              (io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.adam.http.Grpc.QueryResponseInfoMessage>) responseObserver);
          break;
        case METHODID_DO_COMPOUND_QUERY:
          serviceImpl.doCompoundQuery((ch.unibas.dmi.dbis.adam.http.Grpc.CompoundQueryMessage) request,
              (io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.adam.http.Grpc.CompoundQueryResponseInfoMessage>) responseObserver);
          break;
        case METHODID_DO_BOOLEAN_QUERY:
          serviceImpl.doBooleanQuery((ch.unibas.dmi.dbis.adam.http.Grpc.SimpleBooleanQueryMessage) request,
              (io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.adam.http.Grpc.QueryResponseInfoMessage>) responseObserver);
          break;
        case METHODID_GET_CACHED_RESULTS:
          serviceImpl.getCachedResults((ch.unibas.dmi.dbis.adam.http.Grpc.CachedResultsMessage) request,
              (io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.adam.http.Grpc.QueryResponseInfoMessage>) responseObserver);
          break;
        default:
          throw new AssertionError();
      }
    }

    @java.lang.SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<Req> invoke(
        io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
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
              ch.unibas.dmi.dbis.adam.http.Grpc.IndexNameMessage,
              ch.unibas.dmi.dbis.adam.http.Grpc.AckMessage>(
                serviceImpl, METHODID_CACHE_INDEX)))
        .addMethod(
          METHOD_CACHE_ENTITY,
          asyncUnaryCall(
            new MethodHandlers<
              ch.unibas.dmi.dbis.adam.http.Grpc.EntityNameMessage,
              ch.unibas.dmi.dbis.adam.http.Grpc.AckMessage>(
                serviceImpl, METHODID_CACHE_ENTITY)))
        .addMethod(
          METHOD_DO_STANDARD_QUERY,
          asyncUnaryCall(
            new MethodHandlers<
              ch.unibas.dmi.dbis.adam.http.Grpc.SimpleQueryMessage,
              ch.unibas.dmi.dbis.adam.http.Grpc.QueryResponseInfoMessage>(
                serviceImpl, METHODID_DO_STANDARD_QUERY)))
        .addMethod(
          METHOD_DO_SEQUENTIAL_QUERY,
          asyncUnaryCall(
            new MethodHandlers<
              ch.unibas.dmi.dbis.adam.http.Grpc.SimpleSequentialQueryMessage,
              ch.unibas.dmi.dbis.adam.http.Grpc.QueryResponseInfoMessage>(
                serviceImpl, METHODID_DO_SEQUENTIAL_QUERY)))
        .addMethod(
          METHOD_DO_SPECIFIED_INDEX_QUERY,
          asyncUnaryCall(
            new MethodHandlers<
              ch.unibas.dmi.dbis.adam.http.Grpc.SimpleSpecifiedIndexQueryMessage,
              ch.unibas.dmi.dbis.adam.http.Grpc.QueryResponseInfoMessage>(
                serviceImpl, METHODID_DO_SPECIFIED_INDEX_QUERY)))
        .addMethod(
          METHOD_DO_INDEX_QUERY,
          asyncUnaryCall(
            new MethodHandlers<
              ch.unibas.dmi.dbis.adam.http.Grpc.SimpleIndexQueryMessage,
              ch.unibas.dmi.dbis.adam.http.Grpc.QueryResponseInfoMessage>(
                serviceImpl, METHODID_DO_INDEX_QUERY)))
        .addMethod(
          METHOD_DO_PROGRESSIVE_QUERY,
          asyncServerStreamingCall(
            new MethodHandlers<
              ch.unibas.dmi.dbis.adam.http.Grpc.SimpleQueryMessage,
              ch.unibas.dmi.dbis.adam.http.Grpc.QueryResponseInfoMessage>(
                serviceImpl, METHODID_DO_PROGRESSIVE_QUERY)))
        .addMethod(
          METHOD_DO_TIMED_PROGRESSIVE_QUERY,
          asyncUnaryCall(
            new MethodHandlers<
              ch.unibas.dmi.dbis.adam.http.Grpc.TimedQueryMessage,
              ch.unibas.dmi.dbis.adam.http.Grpc.QueryResponseInfoMessage>(
                serviceImpl, METHODID_DO_TIMED_PROGRESSIVE_QUERY)))
        .addMethod(
          METHOD_DO_COMPOUND_QUERY,
          asyncUnaryCall(
            new MethodHandlers<
              ch.unibas.dmi.dbis.adam.http.Grpc.CompoundQueryMessage,
              ch.unibas.dmi.dbis.adam.http.Grpc.CompoundQueryResponseInfoMessage>(
                serviceImpl, METHODID_DO_COMPOUND_QUERY)))
        .addMethod(
          METHOD_DO_BOOLEAN_QUERY,
          asyncUnaryCall(
            new MethodHandlers<
              ch.unibas.dmi.dbis.adam.http.Grpc.SimpleBooleanQueryMessage,
              ch.unibas.dmi.dbis.adam.http.Grpc.QueryResponseInfoMessage>(
                serviceImpl, METHODID_DO_BOOLEAN_QUERY)))
        .addMethod(
          METHOD_GET_CACHED_RESULTS,
          asyncUnaryCall(
            new MethodHandlers<
              ch.unibas.dmi.dbis.adam.http.Grpc.CachedResultsMessage,
              ch.unibas.dmi.dbis.adam.http.Grpc.QueryResponseInfoMessage>(
                serviceImpl, METHODID_GET_CACHED_RESULTS)))
        .build();
  }
}
