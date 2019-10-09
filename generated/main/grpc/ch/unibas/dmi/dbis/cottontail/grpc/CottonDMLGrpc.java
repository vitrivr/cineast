package ch.unibas.dmi.dbis.cottontail.grpc;

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
    value = "by gRPC proto compiler (version 1.7.0)",
    comments = "Source: cottontail.proto")
public final class CottonDMLGrpc {

  private CottonDMLGrpc() {}

  public static final String SERVICE_NAME = "ch.unibas.dmi.dbis.cottontail.grpc.CottonDML";

  // Static method descriptors that strictly reflect the proto.
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.InsertMessage,
      ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.InsertStatus> METHOD_INSERT =
      io.grpc.MethodDescriptor.<ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.InsertMessage, ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.InsertStatus>newBuilder()
          .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
          .setFullMethodName(generateFullMethodName(
              "ch.unibas.dmi.dbis.cottontail.grpc.CottonDML", "Insert"))
          .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.InsertMessage.getDefaultInstance()))
          .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.InsertStatus.getDefaultInstance()))
          .setSchemaDescriptor(new CottonDMLMethodDescriptorSupplier("Insert"))
          .build();
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.InsertMessage,
      ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.InsertStatus> METHOD_INSERT_STREAM =
      io.grpc.MethodDescriptor.<ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.InsertMessage, ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.InsertStatus>newBuilder()
          .setType(io.grpc.MethodDescriptor.MethodType.BIDI_STREAMING)
          .setFullMethodName(generateFullMethodName(
              "ch.unibas.dmi.dbis.cottontail.grpc.CottonDML", "InsertStream"))
          .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.InsertMessage.getDefaultInstance()))
          .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.InsertStatus.getDefaultInstance()))
          .setSchemaDescriptor(new CottonDMLMethodDescriptorSupplier("InsertStream"))
          .build();

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static CottonDMLStub newStub(io.grpc.Channel channel) {
    return new CottonDMLStub(channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static CottonDMLBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    return new CottonDMLBlockingStub(channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static CottonDMLFutureStub newFutureStub(
      io.grpc.Channel channel) {
    return new CottonDMLFutureStub(channel);
  }

  /**
   */
  public static abstract class CottonDMLImplBase implements io.grpc.BindableService {

    /**
     * <pre>
     * Insert data into entities (either in a single message or a streaming manner). 
     * </pre>
     */
    public void insert(ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.InsertMessage request,
        io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.InsertStatus> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_INSERT, responseObserver);
    }

    /**
     */
    public io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.InsertMessage> insertStream(
        io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.InsertStatus> responseObserver) {
      return asyncUnimplementedStreamingCall(METHOD_INSERT_STREAM, responseObserver);
    }

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
          .addMethod(
            METHOD_INSERT,
            asyncUnaryCall(
              new MethodHandlers<
                ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.InsertMessage,
                ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.InsertStatus>(
                  this, METHODID_INSERT)))
          .addMethod(
            METHOD_INSERT_STREAM,
            asyncBidiStreamingCall(
              new MethodHandlers<
                ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.InsertMessage,
                ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.InsertStatus>(
                  this, METHODID_INSERT_STREAM)))
          .build();
    }
  }

  /**
   */
  public static final class CottonDMLStub extends io.grpc.stub.AbstractStub<CottonDMLStub> {
    private CottonDMLStub(io.grpc.Channel channel) {
      super(channel);
    }

    private CottonDMLStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected CottonDMLStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new CottonDMLStub(channel, callOptions);
    }

    /**
     * <pre>
     * Insert data into entities (either in a single message or a streaming manner). 
     * </pre>
     */
    public void insert(ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.InsertMessage request,
        io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.InsertStatus> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_INSERT, getCallOptions()), request, responseObserver);
    }

    /**
     */
    public io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.InsertMessage> insertStream(
        io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.InsertStatus> responseObserver) {
      return asyncBidiStreamingCall(
          getChannel().newCall(METHOD_INSERT_STREAM, getCallOptions()), responseObserver);
    }
  }

  /**
   */
  public static final class CottonDMLBlockingStub extends io.grpc.stub.AbstractStub<CottonDMLBlockingStub> {
    private CottonDMLBlockingStub(io.grpc.Channel channel) {
      super(channel);
    }

    private CottonDMLBlockingStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected CottonDMLBlockingStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new CottonDMLBlockingStub(channel, callOptions);
    }

    /**
     * <pre>
     * Insert data into entities (either in a single message or a streaming manner). 
     * </pre>
     */
    public ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.InsertStatus insert(ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.InsertMessage request) {
      return blockingUnaryCall(
          getChannel(), METHOD_INSERT, getCallOptions(), request);
    }
  }

  /**
   */
  public static final class CottonDMLFutureStub extends io.grpc.stub.AbstractStub<CottonDMLFutureStub> {
    private CottonDMLFutureStub(io.grpc.Channel channel) {
      super(channel);
    }

    private CottonDMLFutureStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected CottonDMLFutureStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new CottonDMLFutureStub(channel, callOptions);
    }

    /**
     * <pre>
     * Insert data into entities (either in a single message or a streaming manner). 
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.InsertStatus> insert(
        ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.InsertMessage request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_INSERT, getCallOptions()), request);
    }
  }

  private static final int METHODID_INSERT = 0;
  private static final int METHODID_INSERT_STREAM = 1;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final CottonDMLImplBase serviceImpl;
    private final int methodId;

    MethodHandlers(CottonDMLImplBase serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_INSERT:
          serviceImpl.insert((ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.InsertMessage) request,
              (io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.InsertStatus>) responseObserver);
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
        case METHODID_INSERT_STREAM:
          return (io.grpc.stub.StreamObserver<Req>) serviceImpl.insertStream(
              (io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.InsertStatus>) responseObserver);
        default:
          throw new AssertionError();
      }
    }
  }

  private static abstract class CottonDMLBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    CottonDMLBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("CottonDML");
    }
  }

  private static final class CottonDMLFileDescriptorSupplier
      extends CottonDMLBaseDescriptorSupplier {
    CottonDMLFileDescriptorSupplier() {}
  }

  private static final class CottonDMLMethodDescriptorSupplier
      extends CottonDMLBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final String methodName;

    CottonDMLMethodDescriptorSupplier(String methodName) {
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
      synchronized (CottonDMLGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new CottonDMLFileDescriptorSupplier())
              .addMethod(METHOD_INSERT)
              .addMethod(METHOD_INSERT_STREAM)
              .build();
        }
      }
    }
    return result;
  }
}
