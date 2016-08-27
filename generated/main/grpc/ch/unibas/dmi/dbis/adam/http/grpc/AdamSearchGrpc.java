package ch.unibas.dmi.dbis.adam.http.grpc;

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

  public static final String SERVICE_NAME = "ch.unibas.dmi.dbis.adam.http.grpc.AdamSearch";

  // Static method descriptors that strictly reflect the proto.
  @io.grpc.ExperimentalApi
  public static final io.grpc.MethodDescriptor<ch.unibas.dmi.dbis.adam.http.grpc.Adam.SimpleQueryMessage,
      ch.unibas.dmi.dbis.adam.http.grpc.Adam.QueryResponseListMessage> METHOD_DO_STANDARD_QUERY =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "ch.unibas.dmi.dbis.adam.http.grpc.AdamSearch", "DoStandardQuery"),
          io.grpc.protobuf.ProtoUtils.marshaller(ch.unibas.dmi.dbis.adam.http.grpc.Adam.SimpleQueryMessage.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(ch.unibas.dmi.dbis.adam.http.grpc.Adam.QueryResponseListMessage.getDefaultInstance()));
  @io.grpc.ExperimentalApi
  public static final io.grpc.MethodDescriptor<ch.unibas.dmi.dbis.adam.http.grpc.Adam.SimpleSequentialQueryMessage,
      ch.unibas.dmi.dbis.adam.http.grpc.Adam.QueryResponseListMessage> METHOD_DO_SEQUENTIAL_QUERY =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "ch.unibas.dmi.dbis.adam.http.grpc.AdamSearch", "DoSequentialQuery"),
          io.grpc.protobuf.ProtoUtils.marshaller(ch.unibas.dmi.dbis.adam.http.grpc.Adam.SimpleSequentialQueryMessage.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(ch.unibas.dmi.dbis.adam.http.grpc.Adam.QueryResponseListMessage.getDefaultInstance()));
  @io.grpc.ExperimentalApi
  public static final io.grpc.MethodDescriptor<ch.unibas.dmi.dbis.adam.http.grpc.Adam.SimpleIndexQueryMessage,
      ch.unibas.dmi.dbis.adam.http.grpc.Adam.QueryResponseListMessage> METHOD_DO_INDEX_QUERY =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "ch.unibas.dmi.dbis.adam.http.grpc.AdamSearch", "DoIndexQuery"),
          io.grpc.protobuf.ProtoUtils.marshaller(ch.unibas.dmi.dbis.adam.http.grpc.Adam.SimpleIndexQueryMessage.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(ch.unibas.dmi.dbis.adam.http.grpc.Adam.QueryResponseListMessage.getDefaultInstance()));
  @io.grpc.ExperimentalApi
  public static final io.grpc.MethodDescriptor<ch.unibas.dmi.dbis.adam.http.grpc.Adam.SimpleQueryMessage,
      ch.unibas.dmi.dbis.adam.http.grpc.Adam.QueryResponseInfoMessage> METHOD_DO_PROGRESSIVE_QUERY =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING,
          generateFullMethodName(
              "ch.unibas.dmi.dbis.adam.http.grpc.AdamSearch", "DoProgressiveQuery"),
          io.grpc.protobuf.ProtoUtils.marshaller(ch.unibas.dmi.dbis.adam.http.grpc.Adam.SimpleQueryMessage.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(ch.unibas.dmi.dbis.adam.http.grpc.Adam.QueryResponseInfoMessage.getDefaultInstance()));
  @io.grpc.ExperimentalApi
  public static final io.grpc.MethodDescriptor<ch.unibas.dmi.dbis.adam.http.grpc.Adam.TimedQueryMessage,
      ch.unibas.dmi.dbis.adam.http.grpc.Adam.QueryResponseInfoMessage> METHOD_DO_TIMED_PROGRESSIVE_QUERY =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "ch.unibas.dmi.dbis.adam.http.grpc.AdamSearch", "DoTimedProgressiveQuery"),
          io.grpc.protobuf.ProtoUtils.marshaller(ch.unibas.dmi.dbis.adam.http.grpc.Adam.TimedQueryMessage.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(ch.unibas.dmi.dbis.adam.http.grpc.Adam.QueryResponseInfoMessage.getDefaultInstance()));

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

    public void doStandardQuery(ch.unibas.dmi.dbis.adam.http.grpc.Adam.SimpleQueryMessage request,
        io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.adam.http.grpc.Adam.QueryResponseListMessage> responseObserver);

    public void doSequentialQuery(ch.unibas.dmi.dbis.adam.http.grpc.Adam.SimpleSequentialQueryMessage request,
        io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.adam.http.grpc.Adam.QueryResponseListMessage> responseObserver);

    public void doIndexQuery(ch.unibas.dmi.dbis.adam.http.grpc.Adam.SimpleIndexQueryMessage request,
        io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.adam.http.grpc.Adam.QueryResponseListMessage> responseObserver);

    public void doProgressiveQuery(ch.unibas.dmi.dbis.adam.http.grpc.Adam.SimpleQueryMessage request,
        io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.adam.http.grpc.Adam.QueryResponseInfoMessage> responseObserver);

    public void doTimedProgressiveQuery(ch.unibas.dmi.dbis.adam.http.grpc.Adam.TimedQueryMessage request,
        io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.adam.http.grpc.Adam.QueryResponseInfoMessage> responseObserver);
  }

  public static interface AdamSearchBlockingClient {

    public ch.unibas.dmi.dbis.adam.http.grpc.Adam.QueryResponseListMessage doStandardQuery(ch.unibas.dmi.dbis.adam.http.grpc.Adam.SimpleQueryMessage request);

    public ch.unibas.dmi.dbis.adam.http.grpc.Adam.QueryResponseListMessage doSequentialQuery(ch.unibas.dmi.dbis.adam.http.grpc.Adam.SimpleSequentialQueryMessage request);

    public ch.unibas.dmi.dbis.adam.http.grpc.Adam.QueryResponseListMessage doIndexQuery(ch.unibas.dmi.dbis.adam.http.grpc.Adam.SimpleIndexQueryMessage request);

    public java.util.Iterator<ch.unibas.dmi.dbis.adam.http.grpc.Adam.QueryResponseInfoMessage> doProgressiveQuery(
        ch.unibas.dmi.dbis.adam.http.grpc.Adam.SimpleQueryMessage request);

    public ch.unibas.dmi.dbis.adam.http.grpc.Adam.QueryResponseInfoMessage doTimedProgressiveQuery(ch.unibas.dmi.dbis.adam.http.grpc.Adam.TimedQueryMessage request);
  }

  public static interface AdamSearchFutureClient {

    public com.google.common.util.concurrent.ListenableFuture<ch.unibas.dmi.dbis.adam.http.grpc.Adam.QueryResponseListMessage> doStandardQuery(
        ch.unibas.dmi.dbis.adam.http.grpc.Adam.SimpleQueryMessage request);

    public com.google.common.util.concurrent.ListenableFuture<ch.unibas.dmi.dbis.adam.http.grpc.Adam.QueryResponseListMessage> doSequentialQuery(
        ch.unibas.dmi.dbis.adam.http.grpc.Adam.SimpleSequentialQueryMessage request);

    public com.google.common.util.concurrent.ListenableFuture<ch.unibas.dmi.dbis.adam.http.grpc.Adam.QueryResponseListMessage> doIndexQuery(
        ch.unibas.dmi.dbis.adam.http.grpc.Adam.SimpleIndexQueryMessage request);

    public com.google.common.util.concurrent.ListenableFuture<ch.unibas.dmi.dbis.adam.http.grpc.Adam.QueryResponseInfoMessage> doTimedProgressiveQuery(
        ch.unibas.dmi.dbis.adam.http.grpc.Adam.TimedQueryMessage request);
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
    public void doStandardQuery(ch.unibas.dmi.dbis.adam.http.grpc.Adam.SimpleQueryMessage request,
        io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.adam.http.grpc.Adam.QueryResponseListMessage> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_DO_STANDARD_QUERY, getCallOptions()), request, responseObserver);
    }

    @java.lang.Override
    public void doSequentialQuery(ch.unibas.dmi.dbis.adam.http.grpc.Adam.SimpleSequentialQueryMessage request,
        io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.adam.http.grpc.Adam.QueryResponseListMessage> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_DO_SEQUENTIAL_QUERY, getCallOptions()), request, responseObserver);
    }

    @java.lang.Override
    public void doIndexQuery(ch.unibas.dmi.dbis.adam.http.grpc.Adam.SimpleIndexQueryMessage request,
        io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.adam.http.grpc.Adam.QueryResponseListMessage> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_DO_INDEX_QUERY, getCallOptions()), request, responseObserver);
    }

    @java.lang.Override
    public void doProgressiveQuery(ch.unibas.dmi.dbis.adam.http.grpc.Adam.SimpleQueryMessage request,
        io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.adam.http.grpc.Adam.QueryResponseInfoMessage> responseObserver) {
      asyncServerStreamingCall(
          getChannel().newCall(METHOD_DO_PROGRESSIVE_QUERY, getCallOptions()), request, responseObserver);
    }

    @java.lang.Override
    public void doTimedProgressiveQuery(ch.unibas.dmi.dbis.adam.http.grpc.Adam.TimedQueryMessage request,
        io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.adam.http.grpc.Adam.QueryResponseInfoMessage> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_DO_TIMED_PROGRESSIVE_QUERY, getCallOptions()), request, responseObserver);
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
    public ch.unibas.dmi.dbis.adam.http.grpc.Adam.QueryResponseListMessage doStandardQuery(ch.unibas.dmi.dbis.adam.http.grpc.Adam.SimpleQueryMessage request) {
      return blockingUnaryCall(
          getChannel(), METHOD_DO_STANDARD_QUERY, getCallOptions(), request);
    }

    @java.lang.Override
    public ch.unibas.dmi.dbis.adam.http.grpc.Adam.QueryResponseListMessage doSequentialQuery(ch.unibas.dmi.dbis.adam.http.grpc.Adam.SimpleSequentialQueryMessage request) {
      return blockingUnaryCall(
          getChannel(), METHOD_DO_SEQUENTIAL_QUERY, getCallOptions(), request);
    }

    @java.lang.Override
    public ch.unibas.dmi.dbis.adam.http.grpc.Adam.QueryResponseListMessage doIndexQuery(ch.unibas.dmi.dbis.adam.http.grpc.Adam.SimpleIndexQueryMessage request) {
      return blockingUnaryCall(
          getChannel(), METHOD_DO_INDEX_QUERY, getCallOptions(), request);
    }

    @java.lang.Override
    public java.util.Iterator<ch.unibas.dmi.dbis.adam.http.grpc.Adam.QueryResponseInfoMessage> doProgressiveQuery(
        ch.unibas.dmi.dbis.adam.http.grpc.Adam.SimpleQueryMessage request) {
      return blockingServerStreamingCall(
          getChannel(), METHOD_DO_PROGRESSIVE_QUERY, getCallOptions(), request);
    }

    @java.lang.Override
    public ch.unibas.dmi.dbis.adam.http.grpc.Adam.QueryResponseInfoMessage doTimedProgressiveQuery(ch.unibas.dmi.dbis.adam.http.grpc.Adam.TimedQueryMessage request) {
      return blockingUnaryCall(
          getChannel(), METHOD_DO_TIMED_PROGRESSIVE_QUERY, getCallOptions(), request);
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
    public com.google.common.util.concurrent.ListenableFuture<ch.unibas.dmi.dbis.adam.http.grpc.Adam.QueryResponseListMessage> doStandardQuery(
        ch.unibas.dmi.dbis.adam.http.grpc.Adam.SimpleQueryMessage request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_DO_STANDARD_QUERY, getCallOptions()), request);
    }

    @java.lang.Override
    public com.google.common.util.concurrent.ListenableFuture<ch.unibas.dmi.dbis.adam.http.grpc.Adam.QueryResponseListMessage> doSequentialQuery(
        ch.unibas.dmi.dbis.adam.http.grpc.Adam.SimpleSequentialQueryMessage request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_DO_SEQUENTIAL_QUERY, getCallOptions()), request);
    }

    @java.lang.Override
    public com.google.common.util.concurrent.ListenableFuture<ch.unibas.dmi.dbis.adam.http.grpc.Adam.QueryResponseListMessage> doIndexQuery(
        ch.unibas.dmi.dbis.adam.http.grpc.Adam.SimpleIndexQueryMessage request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_DO_INDEX_QUERY, getCallOptions()), request);
    }

    @java.lang.Override
    public com.google.common.util.concurrent.ListenableFuture<ch.unibas.dmi.dbis.adam.http.grpc.Adam.QueryResponseInfoMessage> doTimedProgressiveQuery(
        ch.unibas.dmi.dbis.adam.http.grpc.Adam.TimedQueryMessage request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_DO_TIMED_PROGRESSIVE_QUERY, getCallOptions()), request);
    }
  }

  private static final int METHODID_DO_STANDARD_QUERY = 0;
  private static final int METHODID_DO_SEQUENTIAL_QUERY = 1;
  private static final int METHODID_DO_INDEX_QUERY = 2;
  private static final int METHODID_DO_PROGRESSIVE_QUERY = 3;
  private static final int METHODID_DO_TIMED_PROGRESSIVE_QUERY = 4;

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
        case METHODID_DO_STANDARD_QUERY:
          serviceImpl.doStandardQuery((ch.unibas.dmi.dbis.adam.http.grpc.Adam.SimpleQueryMessage) request,
              (io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.adam.http.grpc.Adam.QueryResponseListMessage>) responseObserver);
          break;
        case METHODID_DO_SEQUENTIAL_QUERY:
          serviceImpl.doSequentialQuery((ch.unibas.dmi.dbis.adam.http.grpc.Adam.SimpleSequentialQueryMessage) request,
              (io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.adam.http.grpc.Adam.QueryResponseListMessage>) responseObserver);
          break;
        case METHODID_DO_INDEX_QUERY:
          serviceImpl.doIndexQuery((ch.unibas.dmi.dbis.adam.http.grpc.Adam.SimpleIndexQueryMessage) request,
              (io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.adam.http.grpc.Adam.QueryResponseListMessage>) responseObserver);
          break;
        case METHODID_DO_PROGRESSIVE_QUERY:
          serviceImpl.doProgressiveQuery((ch.unibas.dmi.dbis.adam.http.grpc.Adam.SimpleQueryMessage) request,
              (io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.adam.http.grpc.Adam.QueryResponseInfoMessage>) responseObserver);
          break;
        case METHODID_DO_TIMED_PROGRESSIVE_QUERY:
          serviceImpl.doTimedProgressiveQuery((ch.unibas.dmi.dbis.adam.http.grpc.Adam.TimedQueryMessage) request,
              (io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.adam.http.grpc.Adam.QueryResponseInfoMessage>) responseObserver);
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
          METHOD_DO_STANDARD_QUERY,
          asyncUnaryCall(
            new MethodHandlers<
              ch.unibas.dmi.dbis.adam.http.grpc.Adam.SimpleQueryMessage,
              ch.unibas.dmi.dbis.adam.http.grpc.Adam.QueryResponseListMessage>(
                serviceImpl, METHODID_DO_STANDARD_QUERY)))
        .addMethod(
          METHOD_DO_SEQUENTIAL_QUERY,
          asyncUnaryCall(
            new MethodHandlers<
              ch.unibas.dmi.dbis.adam.http.grpc.Adam.SimpleSequentialQueryMessage,
              ch.unibas.dmi.dbis.adam.http.grpc.Adam.QueryResponseListMessage>(
                serviceImpl, METHODID_DO_SEQUENTIAL_QUERY)))
        .addMethod(
          METHOD_DO_INDEX_QUERY,
          asyncUnaryCall(
            new MethodHandlers<
              ch.unibas.dmi.dbis.adam.http.grpc.Adam.SimpleIndexQueryMessage,
              ch.unibas.dmi.dbis.adam.http.grpc.Adam.QueryResponseListMessage>(
                serviceImpl, METHODID_DO_INDEX_QUERY)))
        .addMethod(
          METHOD_DO_PROGRESSIVE_QUERY,
          asyncServerStreamingCall(
            new MethodHandlers<
              ch.unibas.dmi.dbis.adam.http.grpc.Adam.SimpleQueryMessage,
              ch.unibas.dmi.dbis.adam.http.grpc.Adam.QueryResponseInfoMessage>(
                serviceImpl, METHODID_DO_PROGRESSIVE_QUERY)))
        .addMethod(
          METHOD_DO_TIMED_PROGRESSIVE_QUERY,
          asyncUnaryCall(
            new MethodHandlers<
              ch.unibas.dmi.dbis.adam.http.grpc.Adam.TimedQueryMessage,
              ch.unibas.dmi.dbis.adam.http.grpc.Adam.QueryResponseInfoMessage>(
                serviceImpl, METHODID_DO_TIMED_PROGRESSIVE_QUERY)))
        .build();
  }
}
