package ch.unibas.dmi.dbis.cottontail.grpc;

import static io.grpc.MethodDescriptor.generateFullMethodName;
import static io.grpc.stub.ClientCalls.asyncServerStreamingCall;
import static io.grpc.stub.ClientCalls.asyncUnaryCall;
import static io.grpc.stub.ClientCalls.*;
import static io.grpc.stub.ServerCalls.asyncServerStreamingCall;
import static io.grpc.stub.ServerCalls.asyncUnaryCall;
import static io.grpc.stub.ServerCalls.*;

/**
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.22.1)",
    comments = "Source: cottontail.proto")
public final class CottonDQLGrpc {

  private CottonDQLGrpc() {}

  public static final String SERVICE_NAME = "CottonDQL";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.QueryMessage,
      ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.QueryResponseMessage> getQueryMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "Query",
      requestType = ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.QueryMessage.class,
      responseType = ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.QueryResponseMessage.class,
      methodType = io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
  public static io.grpc.MethodDescriptor<ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.QueryMessage,
      ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.QueryResponseMessage> getQueryMethod() {
    io.grpc.MethodDescriptor<ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.QueryMessage, ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.QueryResponseMessage> getQueryMethod;
    if ((getQueryMethod = CottonDQLGrpc.getQueryMethod) == null) {
      synchronized (CottonDQLGrpc.class) {
        if ((getQueryMethod = CottonDQLGrpc.getQueryMethod) == null) {
          CottonDQLGrpc.getQueryMethod = getQueryMethod = 
              io.grpc.MethodDescriptor.<ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.QueryMessage, ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.QueryResponseMessage>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
              .setFullMethodName(generateFullMethodName(
                  "CottonDQL", "Query"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.QueryMessage.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.QueryResponseMessage.getDefaultInstance()))
                  .setSchemaDescriptor(new CottonDQLMethodDescriptorSupplier("Query"))
                  .build();
          }
        }
     }
     return getQueryMethod;
  }

  private static volatile io.grpc.MethodDescriptor<ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.BatchedQueryMessage,
      ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.QueryResponseMessage> getBatchedQueryMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "BatchedQuery",
      requestType = ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.BatchedQueryMessage.class,
      responseType = ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.QueryResponseMessage.class,
      methodType = io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
  public static io.grpc.MethodDescriptor<ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.BatchedQueryMessage,
      ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.QueryResponseMessage> getBatchedQueryMethod() {
    io.grpc.MethodDescriptor<ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.BatchedQueryMessage, ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.QueryResponseMessage> getBatchedQueryMethod;
    if ((getBatchedQueryMethod = CottonDQLGrpc.getBatchedQueryMethod) == null) {
      synchronized (CottonDQLGrpc.class) {
        if ((getBatchedQueryMethod = CottonDQLGrpc.getBatchedQueryMethod) == null) {
          CottonDQLGrpc.getBatchedQueryMethod = getBatchedQueryMethod = 
              io.grpc.MethodDescriptor.<ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.BatchedQueryMessage, ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.QueryResponseMessage>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
              .setFullMethodName(generateFullMethodName(
                  "CottonDQL", "BatchedQuery"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.BatchedQueryMessage.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.QueryResponseMessage.getDefaultInstance()))
                  .setSchemaDescriptor(new CottonDQLMethodDescriptorSupplier("BatchedQuery"))
                  .build();
          }
        }
     }
     return getBatchedQueryMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.google.protobuf.Empty,
      ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.SuccessStatus> getPingMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "Ping",
      requestType = com.google.protobuf.Empty.class,
      responseType = ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.SuccessStatus.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.google.protobuf.Empty,
      ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.SuccessStatus> getPingMethod() {
    io.grpc.MethodDescriptor<com.google.protobuf.Empty, ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.SuccessStatus> getPingMethod;
    if ((getPingMethod = CottonDQLGrpc.getPingMethod) == null) {
      synchronized (CottonDQLGrpc.class) {
        if ((getPingMethod = CottonDQLGrpc.getPingMethod) == null) {
          CottonDQLGrpc.getPingMethod = getPingMethod = 
              io.grpc.MethodDescriptor.<com.google.protobuf.Empty, ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.SuccessStatus>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "CottonDQL", "Ping"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.google.protobuf.Empty.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.SuccessStatus.getDefaultInstance()))
                  .setSchemaDescriptor(new CottonDQLMethodDescriptorSupplier("Ping"))
                  .build();
          }
        }
     }
     return getPingMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static CottonDQLStub newStub(io.grpc.Channel channel) {
    return new CottonDQLStub(channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static CottonDQLBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    return new CottonDQLBlockingStub(channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static CottonDQLFutureStub newFutureStub(
      io.grpc.Channel channel) {
    return new CottonDQLFutureStub(channel);
  }

  /**
   */
  public static abstract class CottonDQLImplBase implements io.grpc.BindableService {

    /**
     */
    public void query(ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.QueryMessage request,
        io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.QueryResponseMessage> responseObserver) {
      asyncUnimplementedUnaryCall(getQueryMethod(), responseObserver);
    }

    /**
     */
    public void batchedQuery(ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.BatchedQueryMessage request,
        io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.QueryResponseMessage> responseObserver) {
      asyncUnimplementedUnaryCall(getBatchedQueryMethod(), responseObserver);
    }

    /**
     */
    public void ping(com.google.protobuf.Empty request,
        io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.SuccessStatus> responseObserver) {
      asyncUnimplementedUnaryCall(getPingMethod(), responseObserver);
    }

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
          .addMethod(
            getQueryMethod(),
            asyncServerStreamingCall(
              new MethodHandlers<
                ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.QueryMessage,
                ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.QueryResponseMessage>(
                  this, METHODID_QUERY)))
          .addMethod(
            getBatchedQueryMethod(),
            asyncServerStreamingCall(
              new MethodHandlers<
                ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.BatchedQueryMessage,
                ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.QueryResponseMessage>(
                  this, METHODID_BATCHED_QUERY)))
          .addMethod(
            getPingMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                com.google.protobuf.Empty,
                ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.SuccessStatus>(
                  this, METHODID_PING)))
          .build();
    }
  }

  /**
   */
  public static final class CottonDQLStub extends io.grpc.stub.AbstractStub<CottonDQLStub> {
    private CottonDQLStub(io.grpc.Channel channel) {
      super(channel);
    }

    private CottonDQLStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected CottonDQLStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new CottonDQLStub(channel, callOptions);
    }

    /**
     */
    public void query(ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.QueryMessage request,
        io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.QueryResponseMessage> responseObserver) {
      asyncServerStreamingCall(
          getChannel().newCall(getQueryMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void batchedQuery(ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.BatchedQueryMessage request,
        io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.QueryResponseMessage> responseObserver) {
      asyncServerStreamingCall(
          getChannel().newCall(getBatchedQueryMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void ping(com.google.protobuf.Empty request,
        io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.SuccessStatus> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getPingMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   */
  public static final class CottonDQLBlockingStub extends io.grpc.stub.AbstractStub<CottonDQLBlockingStub> {
    private CottonDQLBlockingStub(io.grpc.Channel channel) {
      super(channel);
    }

    private CottonDQLBlockingStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected CottonDQLBlockingStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new CottonDQLBlockingStub(channel, callOptions);
    }

    /**
     */
    public java.util.Iterator<ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.QueryResponseMessage> query(
        ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.QueryMessage request) {
      return blockingServerStreamingCall(
          getChannel(), getQueryMethod(), getCallOptions(), request);
    }

    /**
     */
    public java.util.Iterator<ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.QueryResponseMessage> batchedQuery(
        ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.BatchedQueryMessage request) {
      return blockingServerStreamingCall(
          getChannel(), getBatchedQueryMethod(), getCallOptions(), request);
    }

    /**
     */
    public ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.SuccessStatus ping(com.google.protobuf.Empty request) {
      return blockingUnaryCall(
          getChannel(), getPingMethod(), getCallOptions(), request);
    }
  }

  /**
   */
  public static final class CottonDQLFutureStub extends io.grpc.stub.AbstractStub<CottonDQLFutureStub> {
    private CottonDQLFutureStub(io.grpc.Channel channel) {
      super(channel);
    }

    private CottonDQLFutureStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected CottonDQLFutureStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new CottonDQLFutureStub(channel, callOptions);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.SuccessStatus> ping(
        com.google.protobuf.Empty request) {
      return futureUnaryCall(
          getChannel().newCall(getPingMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_QUERY = 0;
  private static final int METHODID_BATCHED_QUERY = 1;
  private static final int METHODID_PING = 2;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final CottonDQLImplBase serviceImpl;
    private final int methodId;

    MethodHandlers(CottonDQLImplBase serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_QUERY:
          serviceImpl.query((ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.QueryMessage) request,
              (io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.QueryResponseMessage>) responseObserver);
          break;
        case METHODID_BATCHED_QUERY:
          serviceImpl.batchedQuery((ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.BatchedQueryMessage) request,
              (io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.QueryResponseMessage>) responseObserver);
          break;
        case METHODID_PING:
          serviceImpl.ping((com.google.protobuf.Empty) request,
              (io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.SuccessStatus>) responseObserver);
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
        default:
          throw new AssertionError();
      }
    }
  }

  private static abstract class CottonDQLBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    CottonDQLBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("CottonDQL");
    }
  }

  private static final class CottonDQLFileDescriptorSupplier
      extends CottonDQLBaseDescriptorSupplier {
    CottonDQLFileDescriptorSupplier() {}
  }

  private static final class CottonDQLMethodDescriptorSupplier
      extends CottonDQLBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final String methodName;

    CottonDQLMethodDescriptorSupplier(String methodName) {
      this.methodName = methodName;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.MethodDescriptor getMethodDescriptor() {
      return getServiceDescriptor().findMethodByName(methodName);
    }
  }

  private static volatile io.grpc.ServiceDescriptor serviceDescriptor;

  public static io.grpc.ServiceDescriptor getServiceDescriptor() {
    io.grpc.ServiceDescriptor result = serviceDescriptor;
    if (result == null) {
      synchronized (CottonDQLGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new CottonDQLFileDescriptorSupplier())
              .addMethod(getQueryMethod())
              .addMethod(getBatchedQueryMethod())
              .addMethod(getPingMethod())
              .build();
        }
      }
    }
    return result;
  }
}
