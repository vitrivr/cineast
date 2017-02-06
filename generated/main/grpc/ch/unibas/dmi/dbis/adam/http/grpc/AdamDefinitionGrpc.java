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
import static io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedStreamingCall;

/**
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.0.1)",
    comments = "Source: adam.proto")
public class AdamDefinitionGrpc {

  private AdamDefinitionGrpc() {}

  public static final String SERVICE_NAME = "ch.unibas.dmi.dbis.adam.http.grpc.AdamDefinition";

  // Static method descriptors that strictly reflect the proto.
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<ch.unibas.dmi.dbis.adam.http.grpc.Adam.CreateEntityMessage,
      ch.unibas.dmi.dbis.adam.http.grpc.Adam.AckMessage> METHOD_CREATE_ENTITY =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "ch.unibas.dmi.dbis.adam.http.grpc.AdamDefinition", "CreateEntity"),
          io.grpc.protobuf.ProtoUtils.marshaller(ch.unibas.dmi.dbis.adam.http.grpc.Adam.CreateEntityMessage.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(ch.unibas.dmi.dbis.adam.http.grpc.Adam.AckMessage.getDefaultInstance()));
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<ch.unibas.dmi.dbis.adam.http.grpc.Adam.EntityNameMessage,
      ch.unibas.dmi.dbis.adam.http.grpc.Adam.AckMessage> METHOD_DROP_ENTITY =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "ch.unibas.dmi.dbis.adam.http.grpc.AdamDefinition", "DropEntity"),
          io.grpc.protobuf.ProtoUtils.marshaller(ch.unibas.dmi.dbis.adam.http.grpc.Adam.EntityNameMessage.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(ch.unibas.dmi.dbis.adam.http.grpc.Adam.AckMessage.getDefaultInstance()));
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<ch.unibas.dmi.dbis.adam.http.grpc.Adam.InsertMessage,
      ch.unibas.dmi.dbis.adam.http.grpc.Adam.AckMessage> METHOD_INSERT =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "ch.unibas.dmi.dbis.adam.http.grpc.AdamDefinition", "Insert"),
          io.grpc.protobuf.ProtoUtils.marshaller(ch.unibas.dmi.dbis.adam.http.grpc.Adam.InsertMessage.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(ch.unibas.dmi.dbis.adam.http.grpc.Adam.AckMessage.getDefaultInstance()));
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<ch.unibas.dmi.dbis.adam.http.grpc.Adam.IndexMessage,
      ch.unibas.dmi.dbis.adam.http.grpc.Adam.AckMessage> METHOD_INDEX =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "ch.unibas.dmi.dbis.adam.http.grpc.AdamDefinition", "Index"),
          io.grpc.protobuf.ProtoUtils.marshaller(ch.unibas.dmi.dbis.adam.http.grpc.Adam.IndexMessage.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(ch.unibas.dmi.dbis.adam.http.grpc.Adam.AckMessage.getDefaultInstance()));
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<ch.unibas.dmi.dbis.adam.http.grpc.Adam.IndexNameMessage,
      ch.unibas.dmi.dbis.adam.http.grpc.Adam.AckMessage> METHOD_DROP_INDEX =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "ch.unibas.dmi.dbis.adam.http.grpc.AdamDefinition", "DropIndex"),
          io.grpc.protobuf.ProtoUtils.marshaller(ch.unibas.dmi.dbis.adam.http.grpc.Adam.IndexNameMessage.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(ch.unibas.dmi.dbis.adam.http.grpc.Adam.AckMessage.getDefaultInstance()));
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<ch.unibas.dmi.dbis.adam.http.grpc.Adam.EntityNameMessage,
      ch.unibas.dmi.dbis.adam.http.grpc.Adam.AckMessage> METHOD_COUNT =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "ch.unibas.dmi.dbis.adam.http.grpc.AdamDefinition", "Count"),
          io.grpc.protobuf.ProtoUtils.marshaller(ch.unibas.dmi.dbis.adam.http.grpc.Adam.EntityNameMessage.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(ch.unibas.dmi.dbis.adam.http.grpc.Adam.AckMessage.getDefaultInstance()));

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static AdamDefinitionStub newStub(io.grpc.Channel channel) {
    return new AdamDefinitionStub(channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static AdamDefinitionBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    return new AdamDefinitionBlockingStub(channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary and streaming output calls on the service
   */
  public static AdamDefinitionFutureStub newFutureStub(
      io.grpc.Channel channel) {
    return new AdamDefinitionFutureStub(channel);
  }

  /**
   */
  public static abstract class AdamDefinitionImplBase implements io.grpc.BindableService {

    /**
     */
    public void createEntity(ch.unibas.dmi.dbis.adam.http.grpc.Adam.CreateEntityMessage request,
        io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.adam.http.grpc.Adam.AckMessage> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_CREATE_ENTITY, responseObserver);
    }

    /**
     */
    public void dropEntity(ch.unibas.dmi.dbis.adam.http.grpc.Adam.EntityNameMessage request,
        io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.adam.http.grpc.Adam.AckMessage> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_DROP_ENTITY, responseObserver);
    }

    /**
     */
    public void insert(ch.unibas.dmi.dbis.adam.http.grpc.Adam.InsertMessage request,
        io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.adam.http.grpc.Adam.AckMessage> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_INSERT, responseObserver);
    }

    /**
     */
    public void index(ch.unibas.dmi.dbis.adam.http.grpc.Adam.IndexMessage request,
        io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.adam.http.grpc.Adam.AckMessage> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_INDEX, responseObserver);
    }

    /**
     */
    public void dropIndex(ch.unibas.dmi.dbis.adam.http.grpc.Adam.IndexNameMessage request,
        io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.adam.http.grpc.Adam.AckMessage> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_DROP_INDEX, responseObserver);
    }

    /**
     */
    public void count(ch.unibas.dmi.dbis.adam.http.grpc.Adam.EntityNameMessage request,
        io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.adam.http.grpc.Adam.AckMessage> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_COUNT, responseObserver);
    }

    @java.lang.Override public io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
          .addMethod(
            METHOD_CREATE_ENTITY,
            asyncUnaryCall(
              new MethodHandlers<
                ch.unibas.dmi.dbis.adam.http.grpc.Adam.CreateEntityMessage,
                ch.unibas.dmi.dbis.adam.http.grpc.Adam.AckMessage>(
                  this, METHODID_CREATE_ENTITY)))
          .addMethod(
            METHOD_DROP_ENTITY,
            asyncUnaryCall(
              new MethodHandlers<
                ch.unibas.dmi.dbis.adam.http.grpc.Adam.EntityNameMessage,
                ch.unibas.dmi.dbis.adam.http.grpc.Adam.AckMessage>(
                  this, METHODID_DROP_ENTITY)))
          .addMethod(
            METHOD_INSERT,
            asyncUnaryCall(
              new MethodHandlers<
                ch.unibas.dmi.dbis.adam.http.grpc.Adam.InsertMessage,
                ch.unibas.dmi.dbis.adam.http.grpc.Adam.AckMessage>(
                  this, METHODID_INSERT)))
          .addMethod(
            METHOD_INDEX,
            asyncUnaryCall(
              new MethodHandlers<
                ch.unibas.dmi.dbis.adam.http.grpc.Adam.IndexMessage,
                ch.unibas.dmi.dbis.adam.http.grpc.Adam.AckMessage>(
                  this, METHODID_INDEX)))
          .addMethod(
            METHOD_DROP_INDEX,
            asyncUnaryCall(
              new MethodHandlers<
                ch.unibas.dmi.dbis.adam.http.grpc.Adam.IndexNameMessage,
                ch.unibas.dmi.dbis.adam.http.grpc.Adam.AckMessage>(
                  this, METHODID_DROP_INDEX)))
          .addMethod(
            METHOD_COUNT,
            asyncUnaryCall(
              new MethodHandlers<
                ch.unibas.dmi.dbis.adam.http.grpc.Adam.EntityNameMessage,
                ch.unibas.dmi.dbis.adam.http.grpc.Adam.AckMessage>(
                  this, METHODID_COUNT)))
          .build();
    }
  }

  /**
   */
  public static final class AdamDefinitionStub extends io.grpc.stub.AbstractStub<AdamDefinitionStub> {
    private AdamDefinitionStub(io.grpc.Channel channel) {
      super(channel);
    }

    private AdamDefinitionStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected AdamDefinitionStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new AdamDefinitionStub(channel, callOptions);
    }

    /**
     */
    public void createEntity(ch.unibas.dmi.dbis.adam.http.grpc.Adam.CreateEntityMessage request,
        io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.adam.http.grpc.Adam.AckMessage> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_CREATE_ENTITY, getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void dropEntity(ch.unibas.dmi.dbis.adam.http.grpc.Adam.EntityNameMessage request,
        io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.adam.http.grpc.Adam.AckMessage> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_DROP_ENTITY, getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void insert(ch.unibas.dmi.dbis.adam.http.grpc.Adam.InsertMessage request,
        io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.adam.http.grpc.Adam.AckMessage> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_INSERT, getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void index(ch.unibas.dmi.dbis.adam.http.grpc.Adam.IndexMessage request,
        io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.adam.http.grpc.Adam.AckMessage> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_INDEX, getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void dropIndex(ch.unibas.dmi.dbis.adam.http.grpc.Adam.IndexNameMessage request,
        io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.adam.http.grpc.Adam.AckMessage> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_DROP_INDEX, getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void count(ch.unibas.dmi.dbis.adam.http.grpc.Adam.EntityNameMessage request,
        io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.adam.http.grpc.Adam.AckMessage> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_COUNT, getCallOptions()), request, responseObserver);
    }
  }

  /**
   */
  public static final class AdamDefinitionBlockingStub extends io.grpc.stub.AbstractStub<AdamDefinitionBlockingStub> {
    private AdamDefinitionBlockingStub(io.grpc.Channel channel) {
      super(channel);
    }

    private AdamDefinitionBlockingStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected AdamDefinitionBlockingStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new AdamDefinitionBlockingStub(channel, callOptions);
    }

    /**
     */
    public ch.unibas.dmi.dbis.adam.http.grpc.Adam.AckMessage createEntity(ch.unibas.dmi.dbis.adam.http.grpc.Adam.CreateEntityMessage request) {
      return blockingUnaryCall(
          getChannel(), METHOD_CREATE_ENTITY, getCallOptions(), request);
    }

    /**
     */
    public ch.unibas.dmi.dbis.adam.http.grpc.Adam.AckMessage dropEntity(ch.unibas.dmi.dbis.adam.http.grpc.Adam.EntityNameMessage request) {
      return blockingUnaryCall(
          getChannel(), METHOD_DROP_ENTITY, getCallOptions(), request);
    }

    /**
     */
    public ch.unibas.dmi.dbis.adam.http.grpc.Adam.AckMessage insert(ch.unibas.dmi.dbis.adam.http.grpc.Adam.InsertMessage request) {
      return blockingUnaryCall(
          getChannel(), METHOD_INSERT, getCallOptions(), request);
    }

    /**
     */
    public ch.unibas.dmi.dbis.adam.http.grpc.Adam.AckMessage index(ch.unibas.dmi.dbis.adam.http.grpc.Adam.IndexMessage request) {
      return blockingUnaryCall(
          getChannel(), METHOD_INDEX, getCallOptions(), request);
    }

    /**
     */
    public ch.unibas.dmi.dbis.adam.http.grpc.Adam.AckMessage dropIndex(ch.unibas.dmi.dbis.adam.http.grpc.Adam.IndexNameMessage request) {
      return blockingUnaryCall(
          getChannel(), METHOD_DROP_INDEX, getCallOptions(), request);
    }

    /**
     */
    public ch.unibas.dmi.dbis.adam.http.grpc.Adam.AckMessage count(ch.unibas.dmi.dbis.adam.http.grpc.Adam.EntityNameMessage request) {
      return blockingUnaryCall(
          getChannel(), METHOD_COUNT, getCallOptions(), request);
    }
  }

  /**
   */
  public static final class AdamDefinitionFutureStub extends io.grpc.stub.AbstractStub<AdamDefinitionFutureStub> {
    private AdamDefinitionFutureStub(io.grpc.Channel channel) {
      super(channel);
    }

    private AdamDefinitionFutureStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected AdamDefinitionFutureStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new AdamDefinitionFutureStub(channel, callOptions);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<ch.unibas.dmi.dbis.adam.http.grpc.Adam.AckMessage> createEntity(
        ch.unibas.dmi.dbis.adam.http.grpc.Adam.CreateEntityMessage request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_CREATE_ENTITY, getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<ch.unibas.dmi.dbis.adam.http.grpc.Adam.AckMessage> dropEntity(
        ch.unibas.dmi.dbis.adam.http.grpc.Adam.EntityNameMessage request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_DROP_ENTITY, getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<ch.unibas.dmi.dbis.adam.http.grpc.Adam.AckMessage> insert(
        ch.unibas.dmi.dbis.adam.http.grpc.Adam.InsertMessage request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_INSERT, getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<ch.unibas.dmi.dbis.adam.http.grpc.Adam.AckMessage> index(
        ch.unibas.dmi.dbis.adam.http.grpc.Adam.IndexMessage request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_INDEX, getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<ch.unibas.dmi.dbis.adam.http.grpc.Adam.AckMessage> dropIndex(
        ch.unibas.dmi.dbis.adam.http.grpc.Adam.IndexNameMessage request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_DROP_INDEX, getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<ch.unibas.dmi.dbis.adam.http.grpc.Adam.AckMessage> count(
        ch.unibas.dmi.dbis.adam.http.grpc.Adam.EntityNameMessage request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_COUNT, getCallOptions()), request);
    }
  }

  private static final int METHODID_CREATE_ENTITY = 0;
  private static final int METHODID_DROP_ENTITY = 1;
  private static final int METHODID_INSERT = 2;
  private static final int METHODID_INDEX = 3;
  private static final int METHODID_DROP_INDEX = 4;
  private static final int METHODID_COUNT = 5;

  private static class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final AdamDefinitionImplBase serviceImpl;
    private final int methodId;

    public MethodHandlers(AdamDefinitionImplBase serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_CREATE_ENTITY:
          serviceImpl.createEntity((ch.unibas.dmi.dbis.adam.http.grpc.Adam.CreateEntityMessage) request,
              (io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.adam.http.grpc.Adam.AckMessage>) responseObserver);
          break;
        case METHODID_DROP_ENTITY:
          serviceImpl.dropEntity((ch.unibas.dmi.dbis.adam.http.grpc.Adam.EntityNameMessage) request,
              (io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.adam.http.grpc.Adam.AckMessage>) responseObserver);
          break;
        case METHODID_INSERT:
          serviceImpl.insert((ch.unibas.dmi.dbis.adam.http.grpc.Adam.InsertMessage) request,
              (io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.adam.http.grpc.Adam.AckMessage>) responseObserver);
          break;
        case METHODID_INDEX:
          serviceImpl.index((ch.unibas.dmi.dbis.adam.http.grpc.Adam.IndexMessage) request,
              (io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.adam.http.grpc.Adam.AckMessage>) responseObserver);
          break;
        case METHODID_DROP_INDEX:
          serviceImpl.dropIndex((ch.unibas.dmi.dbis.adam.http.grpc.Adam.IndexNameMessage) request,
              (io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.adam.http.grpc.Adam.AckMessage>) responseObserver);
          break;
        case METHODID_COUNT:
          serviceImpl.count((ch.unibas.dmi.dbis.adam.http.grpc.Adam.EntityNameMessage) request,
              (io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.adam.http.grpc.Adam.AckMessage>) responseObserver);
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

  public static io.grpc.ServiceDescriptor getServiceDescriptor() {
    return new io.grpc.ServiceDescriptor(SERVICE_NAME,
        METHOD_CREATE_ENTITY,
        METHOD_DROP_ENTITY,
        METHOD_INSERT,
        METHOD_INDEX,
        METHOD_DROP_INDEX,
        METHOD_COUNT);
  }

}
