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
public final class CottonDDLGrpc {

  private CottonDDLGrpc() {}

  public static final String SERVICE_NAME = "CottonDDL";

  // Static method descriptors that strictly reflect the proto.
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<com.google.protobuf.Empty,
      ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Schema> METHOD_LIST_SCHEMAS =
      io.grpc.MethodDescriptor.<com.google.protobuf.Empty, ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Schema>newBuilder()
          .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
          .setFullMethodName(generateFullMethodName(
              "CottonDDL", "ListSchemas"))
          .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              com.google.protobuf.Empty.getDefaultInstance()))
          .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Schema.getDefaultInstance()))
          .setSchemaDescriptor(new CottonDDLMethodDescriptorSupplier("ListSchemas"))
          .build();
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Schema,
      com.google.protobuf.Empty> METHOD_CREATE_SCHEMA =
      io.grpc.MethodDescriptor.<ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Schema, com.google.protobuf.Empty>newBuilder()
          .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
          .setFullMethodName(generateFullMethodName(
              "CottonDDL", "CreateSchema"))
          .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Schema.getDefaultInstance()))
          .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              com.google.protobuf.Empty.getDefaultInstance()))
          .setSchemaDescriptor(new CottonDDLMethodDescriptorSupplier("CreateSchema"))
          .build();
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Schema,
      com.google.protobuf.Empty> METHOD_DROP_SCHEMA =
      io.grpc.MethodDescriptor.<ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Schema, com.google.protobuf.Empty>newBuilder()
          .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
          .setFullMethodName(generateFullMethodName(
              "CottonDDL", "DropSchema"))
          .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Schema.getDefaultInstance()))
          .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              com.google.protobuf.Empty.getDefaultInstance()))
          .setSchemaDescriptor(new CottonDDLMethodDescriptorSupplier("DropSchema"))
          .build();
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Schema,
      ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Entity> METHOD_LIST_ENTITIES =
      io.grpc.MethodDescriptor.<ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Schema, ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Entity>newBuilder()
          .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
          .setFullMethodName(generateFullMethodName(
              "CottonDDL", "ListEntities"))
          .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Schema.getDefaultInstance()))
          .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Entity.getDefaultInstance()))
          .setSchemaDescriptor(new CottonDDLMethodDescriptorSupplier("ListEntities"))
          .build();
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.CreateEntityMessage,
      com.google.protobuf.Empty> METHOD_CREATE_ENTITY =
      io.grpc.MethodDescriptor.<ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.CreateEntityMessage, com.google.protobuf.Empty>newBuilder()
          .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
          .setFullMethodName(generateFullMethodName(
              "CottonDDL", "CreateEntity"))
          .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.CreateEntityMessage.getDefaultInstance()))
          .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              com.google.protobuf.Empty.getDefaultInstance()))
          .setSchemaDescriptor(new CottonDDLMethodDescriptorSupplier("CreateEntity"))
          .build();
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Entity,
      com.google.protobuf.Empty> METHOD_DROP_ENTITY =
      io.grpc.MethodDescriptor.<ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Entity, com.google.protobuf.Empty>newBuilder()
          .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
          .setFullMethodName(generateFullMethodName(
              "CottonDDL", "DropEntity"))
          .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Entity.getDefaultInstance()))
          .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              com.google.protobuf.Empty.getDefaultInstance()))
          .setSchemaDescriptor(new CottonDDLMethodDescriptorSupplier("DropEntity"))
          .build();
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Entity,
      com.google.protobuf.Empty> METHOD_OPTIMIZE_ENTITY =
      io.grpc.MethodDescriptor.<ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Entity, com.google.protobuf.Empty>newBuilder()
          .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
          .setFullMethodName(generateFullMethodName(
              "CottonDDL", "OptimizeEntity"))
          .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Entity.getDefaultInstance()))
          .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              com.google.protobuf.Empty.getDefaultInstance()))
          .setSchemaDescriptor(new CottonDDLMethodDescriptorSupplier("OptimizeEntity"))
          .build();
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Entity,
      com.google.protobuf.Empty> METHOD_TRUNCATE_ENTITY =
      io.grpc.MethodDescriptor.<ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Entity, com.google.protobuf.Empty>newBuilder()
          .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
          .setFullMethodName(generateFullMethodName(
              "CottonDDL", "TruncateEntity"))
          .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Entity.getDefaultInstance()))
          .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              com.google.protobuf.Empty.getDefaultInstance()))
          .setSchemaDescriptor(new CottonDDLMethodDescriptorSupplier("TruncateEntity"))
          .build();

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static CottonDDLStub newStub(io.grpc.Channel channel) {
    return new CottonDDLStub(channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static CottonDDLBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    return new CottonDDLBlockingStub(channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static CottonDDLFutureStub newFutureStub(
      io.grpc.Channel channel) {
    return new CottonDDLFutureStub(channel);
  }

  /**
   */
  public static abstract class CottonDDLImplBase implements io.grpc.BindableService {

    /**
     * <pre>
     * Handling DB schemas. 
     * </pre>
     */
    public void listSchemas(com.google.protobuf.Empty request,
        io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Schema> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_LIST_SCHEMAS, responseObserver);
    }

    /**
     */
    public void createSchema(ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Schema request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_CREATE_SCHEMA, responseObserver);
    }

    /**
     */
    public void dropSchema(ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Schema request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_DROP_SCHEMA, responseObserver);
    }

    /**
     * <pre>
     * Handling entities. 
     * </pre>
     */
    public void listEntities(ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Schema request,
        io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Entity> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_LIST_ENTITIES, responseObserver);
    }

    /**
     */
    public void createEntity(ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.CreateEntityMessage request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_CREATE_ENTITY, responseObserver);
    }

    /**
     */
    public void dropEntity(ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Entity request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_DROP_ENTITY, responseObserver);
    }

    /**
     */
    public void optimizeEntity(ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Entity request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_OPTIMIZE_ENTITY, responseObserver);
    }

    /**
     */
    public void truncateEntity(ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Entity request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_TRUNCATE_ENTITY, responseObserver);
    }

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
          .addMethod(
            METHOD_LIST_SCHEMAS,
            asyncUnaryCall(
              new MethodHandlers<
                com.google.protobuf.Empty,
                ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Schema>(
                  this, METHODID_LIST_SCHEMAS)))
          .addMethod(
            METHOD_CREATE_SCHEMA,
            asyncUnaryCall(
              new MethodHandlers<
                ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Schema,
                com.google.protobuf.Empty>(
                  this, METHODID_CREATE_SCHEMA)))
          .addMethod(
            METHOD_DROP_SCHEMA,
            asyncUnaryCall(
              new MethodHandlers<
                ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Schema,
                com.google.protobuf.Empty>(
                  this, METHODID_DROP_SCHEMA)))
          .addMethod(
            METHOD_LIST_ENTITIES,
            asyncUnaryCall(
              new MethodHandlers<
                ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Schema,
                ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Entity>(
                  this, METHODID_LIST_ENTITIES)))
          .addMethod(
            METHOD_CREATE_ENTITY,
            asyncUnaryCall(
              new MethodHandlers<
                ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.CreateEntityMessage,
                com.google.protobuf.Empty>(
                  this, METHODID_CREATE_ENTITY)))
          .addMethod(
            METHOD_DROP_ENTITY,
            asyncUnaryCall(
              new MethodHandlers<
                ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Entity,
                com.google.protobuf.Empty>(
                  this, METHODID_DROP_ENTITY)))
          .addMethod(
            METHOD_OPTIMIZE_ENTITY,
            asyncUnaryCall(
              new MethodHandlers<
                ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Entity,
                com.google.protobuf.Empty>(
                  this, METHODID_OPTIMIZE_ENTITY)))
          .addMethod(
            METHOD_TRUNCATE_ENTITY,
            asyncUnaryCall(
              new MethodHandlers<
                ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Entity,
                com.google.protobuf.Empty>(
                  this, METHODID_TRUNCATE_ENTITY)))
          .build();
    }
  }

  /**
   */
  public static final class CottonDDLStub extends io.grpc.stub.AbstractStub<CottonDDLStub> {
    private CottonDDLStub(io.grpc.Channel channel) {
      super(channel);
    }

    private CottonDDLStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected CottonDDLStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new CottonDDLStub(channel, callOptions);
    }

    /**
     * <pre>
     * Handling DB schemas. 
     * </pre>
     */
    public void listSchemas(com.google.protobuf.Empty request,
        io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Schema> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_LIST_SCHEMAS, getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void createSchema(ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Schema request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_CREATE_SCHEMA, getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void dropSchema(ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Schema request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_DROP_SCHEMA, getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Handling entities. 
     * </pre>
     */
    public void listEntities(ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Schema request,
        io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Entity> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_LIST_ENTITIES, getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void createEntity(ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.CreateEntityMessage request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_CREATE_ENTITY, getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void dropEntity(ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Entity request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_DROP_ENTITY, getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void optimizeEntity(ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Entity request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_OPTIMIZE_ENTITY, getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void truncateEntity(ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Entity request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_TRUNCATE_ENTITY, getCallOptions()), request, responseObserver);
    }
  }

  /**
   */
  public static final class CottonDDLBlockingStub extends io.grpc.stub.AbstractStub<CottonDDLBlockingStub> {
    private CottonDDLBlockingStub(io.grpc.Channel channel) {
      super(channel);
    }

    private CottonDDLBlockingStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected CottonDDLBlockingStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new CottonDDLBlockingStub(channel, callOptions);
    }

    /**
     * <pre>
     * Handling DB schemas. 
     * </pre>
     */
    public ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Schema listSchemas(com.google.protobuf.Empty request) {
      return blockingUnaryCall(
          getChannel(), METHOD_LIST_SCHEMAS, getCallOptions(), request);
    }

    /**
     */
    public com.google.protobuf.Empty createSchema(ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Schema request) {
      return blockingUnaryCall(
          getChannel(), METHOD_CREATE_SCHEMA, getCallOptions(), request);
    }

    /**
     */
    public com.google.protobuf.Empty dropSchema(ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Schema request) {
      return blockingUnaryCall(
          getChannel(), METHOD_DROP_SCHEMA, getCallOptions(), request);
    }

    /**
     * <pre>
     * Handling entities. 
     * </pre>
     */
    public ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Entity listEntities(ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Schema request) {
      return blockingUnaryCall(
          getChannel(), METHOD_LIST_ENTITIES, getCallOptions(), request);
    }

    /**
     */
    public com.google.protobuf.Empty createEntity(ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.CreateEntityMessage request) {
      return blockingUnaryCall(
          getChannel(), METHOD_CREATE_ENTITY, getCallOptions(), request);
    }

    /**
     */
    public com.google.protobuf.Empty dropEntity(ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Entity request) {
      return blockingUnaryCall(
          getChannel(), METHOD_DROP_ENTITY, getCallOptions(), request);
    }

    /**
     */
    public com.google.protobuf.Empty optimizeEntity(ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Entity request) {
      return blockingUnaryCall(
          getChannel(), METHOD_OPTIMIZE_ENTITY, getCallOptions(), request);
    }

    /**
     */
    public com.google.protobuf.Empty truncateEntity(ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Entity request) {
      return blockingUnaryCall(
          getChannel(), METHOD_TRUNCATE_ENTITY, getCallOptions(), request);
    }
  }

  /**
   */
  public static final class CottonDDLFutureStub extends io.grpc.stub.AbstractStub<CottonDDLFutureStub> {
    private CottonDDLFutureStub(io.grpc.Channel channel) {
      super(channel);
    }

    private CottonDDLFutureStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected CottonDDLFutureStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new CottonDDLFutureStub(channel, callOptions);
    }

    /**
     * <pre>
     * Handling DB schemas. 
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Schema> listSchemas(
        com.google.protobuf.Empty request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_LIST_SCHEMAS, getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.google.protobuf.Empty> createSchema(
        ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Schema request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_CREATE_SCHEMA, getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.google.protobuf.Empty> dropSchema(
        ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Schema request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_DROP_SCHEMA, getCallOptions()), request);
    }

    /**
     * <pre>
     * Handling entities. 
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Entity> listEntities(
        ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Schema request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_LIST_ENTITIES, getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.google.protobuf.Empty> createEntity(
        ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.CreateEntityMessage request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_CREATE_ENTITY, getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.google.protobuf.Empty> dropEntity(
        ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Entity request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_DROP_ENTITY, getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.google.protobuf.Empty> optimizeEntity(
        ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Entity request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_OPTIMIZE_ENTITY, getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.google.protobuf.Empty> truncateEntity(
        ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Entity request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_TRUNCATE_ENTITY, getCallOptions()), request);
    }
  }

  private static final int METHODID_LIST_SCHEMAS = 0;
  private static final int METHODID_CREATE_SCHEMA = 1;
  private static final int METHODID_DROP_SCHEMA = 2;
  private static final int METHODID_LIST_ENTITIES = 3;
  private static final int METHODID_CREATE_ENTITY = 4;
  private static final int METHODID_DROP_ENTITY = 5;
  private static final int METHODID_OPTIMIZE_ENTITY = 6;
  private static final int METHODID_TRUNCATE_ENTITY = 7;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final CottonDDLImplBase serviceImpl;
    private final int methodId;

    MethodHandlers(CottonDDLImplBase serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_LIST_SCHEMAS:
          serviceImpl.listSchemas((com.google.protobuf.Empty) request,
              (io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Schema>) responseObserver);
          break;
        case METHODID_CREATE_SCHEMA:
          serviceImpl.createSchema((ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Schema) request,
              (io.grpc.stub.StreamObserver<com.google.protobuf.Empty>) responseObserver);
          break;
        case METHODID_DROP_SCHEMA:
          serviceImpl.dropSchema((ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Schema) request,
              (io.grpc.stub.StreamObserver<com.google.protobuf.Empty>) responseObserver);
          break;
        case METHODID_LIST_ENTITIES:
          serviceImpl.listEntities((ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Schema) request,
              (io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Entity>) responseObserver);
          break;
        case METHODID_CREATE_ENTITY:
          serviceImpl.createEntity((ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.CreateEntityMessage) request,
              (io.grpc.stub.StreamObserver<com.google.protobuf.Empty>) responseObserver);
          break;
        case METHODID_DROP_ENTITY:
          serviceImpl.dropEntity((ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Entity) request,
              (io.grpc.stub.StreamObserver<com.google.protobuf.Empty>) responseObserver);
          break;
        case METHODID_OPTIMIZE_ENTITY:
          serviceImpl.optimizeEntity((ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Entity) request,
              (io.grpc.stub.StreamObserver<com.google.protobuf.Empty>) responseObserver);
          break;
        case METHODID_TRUNCATE_ENTITY:
          serviceImpl.truncateEntity((ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Entity) request,
              (io.grpc.stub.StreamObserver<com.google.protobuf.Empty>) responseObserver);
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

  private static abstract class CottonDDLBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    CottonDDLBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("CottonDDL");
    }
  }

  private static final class CottonDDLFileDescriptorSupplier
      extends CottonDDLBaseDescriptorSupplier {
    CottonDDLFileDescriptorSupplier() {}
  }

  private static final class CottonDDLMethodDescriptorSupplier
      extends CottonDDLBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final String methodName;

    CottonDDLMethodDescriptorSupplier(String methodName) {
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
      synchronized (CottonDDLGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new CottonDDLFileDescriptorSupplier())
              .addMethod(METHOD_LIST_SCHEMAS)
              .addMethod(METHOD_CREATE_SCHEMA)
              .addMethod(METHOD_DROP_SCHEMA)
              .addMethod(METHOD_LIST_ENTITIES)
              .addMethod(METHOD_CREATE_ENTITY)
              .addMethod(METHOD_DROP_ENTITY)
              .addMethod(METHOD_OPTIMIZE_ENTITY)
              .addMethod(METHOD_TRUNCATE_ENTITY)
              .build();
        }
      }
    }
    return result;
  }
}
