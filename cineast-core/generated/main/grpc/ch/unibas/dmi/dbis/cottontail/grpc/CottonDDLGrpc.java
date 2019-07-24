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
public final class CottonDDLGrpc {

  private CottonDDLGrpc() {}

  public static final String SERVICE_NAME = "CottonDDL";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<com.google.protobuf.Empty,
      ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Schema> getListSchemasMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "ListSchemas",
      requestType = com.google.protobuf.Empty.class,
      responseType = ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Schema.class,
      methodType = io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
  public static io.grpc.MethodDescriptor<com.google.protobuf.Empty,
      ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Schema> getListSchemasMethod() {
    io.grpc.MethodDescriptor<com.google.protobuf.Empty, ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Schema> getListSchemasMethod;
    if ((getListSchemasMethod = CottonDDLGrpc.getListSchemasMethod) == null) {
      synchronized (CottonDDLGrpc.class) {
        if ((getListSchemasMethod = CottonDDLGrpc.getListSchemasMethod) == null) {
          CottonDDLGrpc.getListSchemasMethod = getListSchemasMethod = 
              io.grpc.MethodDescriptor.<com.google.protobuf.Empty, ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Schema>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
              .setFullMethodName(generateFullMethodName(
                  "CottonDDL", "ListSchemas"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.google.protobuf.Empty.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Schema.getDefaultInstance()))
                  .setSchemaDescriptor(new CottonDDLMethodDescriptorSupplier("ListSchemas"))
                  .build();
          }
        }
     }
     return getListSchemasMethod;
  }

  private static volatile io.grpc.MethodDescriptor<ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Schema,
      ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.SuccessStatus> getCreateSchemaMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "CreateSchema",
      requestType = ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Schema.class,
      responseType = ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.SuccessStatus.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Schema,
      ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.SuccessStatus> getCreateSchemaMethod() {
    io.grpc.MethodDescriptor<ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Schema, ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.SuccessStatus> getCreateSchemaMethod;
    if ((getCreateSchemaMethod = CottonDDLGrpc.getCreateSchemaMethod) == null) {
      synchronized (CottonDDLGrpc.class) {
        if ((getCreateSchemaMethod = CottonDDLGrpc.getCreateSchemaMethod) == null) {
          CottonDDLGrpc.getCreateSchemaMethod = getCreateSchemaMethod = 
              io.grpc.MethodDescriptor.<ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Schema, ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.SuccessStatus>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "CottonDDL", "CreateSchema"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Schema.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.SuccessStatus.getDefaultInstance()))
                  .setSchemaDescriptor(new CottonDDLMethodDescriptorSupplier("CreateSchema"))
                  .build();
          }
        }
     }
     return getCreateSchemaMethod;
  }

  private static volatile io.grpc.MethodDescriptor<ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Schema,
      ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.SuccessStatus> getDropSchemaMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "DropSchema",
      requestType = ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Schema.class,
      responseType = ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.SuccessStatus.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Schema,
      ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.SuccessStatus> getDropSchemaMethod() {
    io.grpc.MethodDescriptor<ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Schema, ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.SuccessStatus> getDropSchemaMethod;
    if ((getDropSchemaMethod = CottonDDLGrpc.getDropSchemaMethod) == null) {
      synchronized (CottonDDLGrpc.class) {
        if ((getDropSchemaMethod = CottonDDLGrpc.getDropSchemaMethod) == null) {
          CottonDDLGrpc.getDropSchemaMethod = getDropSchemaMethod = 
              io.grpc.MethodDescriptor.<ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Schema, ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.SuccessStatus>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "CottonDDL", "DropSchema"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Schema.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.SuccessStatus.getDefaultInstance()))
                  .setSchemaDescriptor(new CottonDDLMethodDescriptorSupplier("DropSchema"))
                  .build();
          }
        }
     }
     return getDropSchemaMethod;
  }

  private static volatile io.grpc.MethodDescriptor<ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Schema,
      ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Entity> getListEntitiesMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "ListEntities",
      requestType = ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Schema.class,
      responseType = ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Entity.class,
      methodType = io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
  public static io.grpc.MethodDescriptor<ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Schema,
      ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Entity> getListEntitiesMethod() {
    io.grpc.MethodDescriptor<ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Schema, ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Entity> getListEntitiesMethod;
    if ((getListEntitiesMethod = CottonDDLGrpc.getListEntitiesMethod) == null) {
      synchronized (CottonDDLGrpc.class) {
        if ((getListEntitiesMethod = CottonDDLGrpc.getListEntitiesMethod) == null) {
          CottonDDLGrpc.getListEntitiesMethod = getListEntitiesMethod = 
              io.grpc.MethodDescriptor.<ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Schema, ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Entity>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
              .setFullMethodName(generateFullMethodName(
                  "CottonDDL", "ListEntities"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Schema.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Entity.getDefaultInstance()))
                  .setSchemaDescriptor(new CottonDDLMethodDescriptorSupplier("ListEntities"))
                  .build();
          }
        }
     }
     return getListEntitiesMethod;
  }

  private static volatile io.grpc.MethodDescriptor<ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.CreateEntityMessage,
      ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.SuccessStatus> getCreateEntityMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "CreateEntity",
      requestType = ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.CreateEntityMessage.class,
      responseType = ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.SuccessStatus.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.CreateEntityMessage,
      ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.SuccessStatus> getCreateEntityMethod() {
    io.grpc.MethodDescriptor<ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.CreateEntityMessage, ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.SuccessStatus> getCreateEntityMethod;
    if ((getCreateEntityMethod = CottonDDLGrpc.getCreateEntityMethod) == null) {
      synchronized (CottonDDLGrpc.class) {
        if ((getCreateEntityMethod = CottonDDLGrpc.getCreateEntityMethod) == null) {
          CottonDDLGrpc.getCreateEntityMethod = getCreateEntityMethod = 
              io.grpc.MethodDescriptor.<ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.CreateEntityMessage, ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.SuccessStatus>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "CottonDDL", "CreateEntity"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.CreateEntityMessage.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.SuccessStatus.getDefaultInstance()))
                  .setSchemaDescriptor(new CottonDDLMethodDescriptorSupplier("CreateEntity"))
                  .build();
          }
        }
     }
     return getCreateEntityMethod;
  }

  private static volatile io.grpc.MethodDescriptor<ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Entity,
      ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.SuccessStatus> getDropEntityMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "DropEntity",
      requestType = ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Entity.class,
      responseType = ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.SuccessStatus.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Entity,
      ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.SuccessStatus> getDropEntityMethod() {
    io.grpc.MethodDescriptor<ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Entity, ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.SuccessStatus> getDropEntityMethod;
    if ((getDropEntityMethod = CottonDDLGrpc.getDropEntityMethod) == null) {
      synchronized (CottonDDLGrpc.class) {
        if ((getDropEntityMethod = CottonDDLGrpc.getDropEntityMethod) == null) {
          CottonDDLGrpc.getDropEntityMethod = getDropEntityMethod = 
              io.grpc.MethodDescriptor.<ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Entity, ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.SuccessStatus>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "CottonDDL", "DropEntity"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Entity.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.SuccessStatus.getDefaultInstance()))
                  .setSchemaDescriptor(new CottonDDLMethodDescriptorSupplier("DropEntity"))
                  .build();
          }
        }
     }
     return getDropEntityMethod;
  }

  private static volatile io.grpc.MethodDescriptor<ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.CreateIndexMessage,
      ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.SuccessStatus> getCreateIndexMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "CreateIndex",
      requestType = ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.CreateIndexMessage.class,
      responseType = ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.SuccessStatus.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.CreateIndexMessage,
      ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.SuccessStatus> getCreateIndexMethod() {
    io.grpc.MethodDescriptor<ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.CreateIndexMessage, ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.SuccessStatus> getCreateIndexMethod;
    if ((getCreateIndexMethod = CottonDDLGrpc.getCreateIndexMethod) == null) {
      synchronized (CottonDDLGrpc.class) {
        if ((getCreateIndexMethod = CottonDDLGrpc.getCreateIndexMethod) == null) {
          CottonDDLGrpc.getCreateIndexMethod = getCreateIndexMethod = 
              io.grpc.MethodDescriptor.<ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.CreateIndexMessage, ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.SuccessStatus>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "CottonDDL", "CreateIndex"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.CreateIndexMessage.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.SuccessStatus.getDefaultInstance()))
                  .setSchemaDescriptor(new CottonDDLMethodDescriptorSupplier("CreateIndex"))
                  .build();
          }
        }
     }
     return getCreateIndexMethod;
  }

  private static volatile io.grpc.MethodDescriptor<ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.DropIndexMessage,
      ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.SuccessStatus> getDropIndexMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "DropIndex",
      requestType = ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.DropIndexMessage.class,
      responseType = ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.SuccessStatus.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.DropIndexMessage,
      ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.SuccessStatus> getDropIndexMethod() {
    io.grpc.MethodDescriptor<ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.DropIndexMessage, ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.SuccessStatus> getDropIndexMethod;
    if ((getDropIndexMethod = CottonDDLGrpc.getDropIndexMethod) == null) {
      synchronized (CottonDDLGrpc.class) {
        if ((getDropIndexMethod = CottonDDLGrpc.getDropIndexMethod) == null) {
          CottonDDLGrpc.getDropIndexMethod = getDropIndexMethod = 
              io.grpc.MethodDescriptor.<ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.DropIndexMessage, ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.SuccessStatus>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "CottonDDL", "DropIndex"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.DropIndexMessage.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.SuccessStatus.getDefaultInstance()))
                  .setSchemaDescriptor(new CottonDDLMethodDescriptorSupplier("DropIndex"))
                  .build();
          }
        }
     }
     return getDropIndexMethod;
  }

  private static volatile io.grpc.MethodDescriptor<ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.RebuildIndexMessage,
      ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.SuccessStatus> getRebuildIndexMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "RebuildIndex",
      requestType = ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.RebuildIndexMessage.class,
      responseType = ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.SuccessStatus.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.RebuildIndexMessage,
      ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.SuccessStatus> getRebuildIndexMethod() {
    io.grpc.MethodDescriptor<ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.RebuildIndexMessage, ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.SuccessStatus> getRebuildIndexMethod;
    if ((getRebuildIndexMethod = CottonDDLGrpc.getRebuildIndexMethod) == null) {
      synchronized (CottonDDLGrpc.class) {
        if ((getRebuildIndexMethod = CottonDDLGrpc.getRebuildIndexMethod) == null) {
          CottonDDLGrpc.getRebuildIndexMethod = getRebuildIndexMethod = 
              io.grpc.MethodDescriptor.<ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.RebuildIndexMessage, ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.SuccessStatus>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "CottonDDL", "RebuildIndex"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.RebuildIndexMessage.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.SuccessStatus.getDefaultInstance()))
                  .setSchemaDescriptor(new CottonDDLMethodDescriptorSupplier("RebuildIndex"))
                  .build();
          }
        }
     }
     return getRebuildIndexMethod;
  }

  private static volatile io.grpc.MethodDescriptor<ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Entity,
      ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.SuccessStatus> getOptimizeEntityMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "OptimizeEntity",
      requestType = ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Entity.class,
      responseType = ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.SuccessStatus.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Entity,
      ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.SuccessStatus> getOptimizeEntityMethod() {
    io.grpc.MethodDescriptor<ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Entity, ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.SuccessStatus> getOptimizeEntityMethod;
    if ((getOptimizeEntityMethod = CottonDDLGrpc.getOptimizeEntityMethod) == null) {
      synchronized (CottonDDLGrpc.class) {
        if ((getOptimizeEntityMethod = CottonDDLGrpc.getOptimizeEntityMethod) == null) {
          CottonDDLGrpc.getOptimizeEntityMethod = getOptimizeEntityMethod = 
              io.grpc.MethodDescriptor.<ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Entity, ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.SuccessStatus>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "CottonDDL", "OptimizeEntity"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Entity.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.SuccessStatus.getDefaultInstance()))
                  .setSchemaDescriptor(new CottonDDLMethodDescriptorSupplier("OptimizeEntity"))
                  .build();
          }
        }
     }
     return getOptimizeEntityMethod;
  }

  private static volatile io.grpc.MethodDescriptor<ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Entity,
      ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.SuccessStatus> getTruncateEntityMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "TruncateEntity",
      requestType = ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Entity.class,
      responseType = ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.SuccessStatus.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Entity,
      ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.SuccessStatus> getTruncateEntityMethod() {
    io.grpc.MethodDescriptor<ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Entity, ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.SuccessStatus> getTruncateEntityMethod;
    if ((getTruncateEntityMethod = CottonDDLGrpc.getTruncateEntityMethod) == null) {
      synchronized (CottonDDLGrpc.class) {
        if ((getTruncateEntityMethod = CottonDDLGrpc.getTruncateEntityMethod) == null) {
          CottonDDLGrpc.getTruncateEntityMethod = getTruncateEntityMethod = 
              io.grpc.MethodDescriptor.<ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Entity, ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.SuccessStatus>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "CottonDDL", "TruncateEntity"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Entity.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.SuccessStatus.getDefaultInstance()))
                  .setSchemaDescriptor(new CottonDDLMethodDescriptorSupplier("TruncateEntity"))
                  .build();
          }
        }
     }
     return getTruncateEntityMethod;
  }

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
      asyncUnimplementedUnaryCall(getListSchemasMethod(), responseObserver);
    }

    /**
     */
    public void createSchema(ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Schema request,
        io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.SuccessStatus> responseObserver) {
      asyncUnimplementedUnaryCall(getCreateSchemaMethod(), responseObserver);
    }

    /**
     */
    public void dropSchema(ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Schema request,
        io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.SuccessStatus> responseObserver) {
      asyncUnimplementedUnaryCall(getDropSchemaMethod(), responseObserver);
    }

    /**
     * <pre>
     * Handling entities. 
     * </pre>
     */
    public void listEntities(ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Schema request,
        io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Entity> responseObserver) {
      asyncUnimplementedUnaryCall(getListEntitiesMethod(), responseObserver);
    }

    /**
     */
    public void createEntity(ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.CreateEntityMessage request,
        io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.SuccessStatus> responseObserver) {
      asyncUnimplementedUnaryCall(getCreateEntityMethod(), responseObserver);
    }

    /**
     */
    public void dropEntity(ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Entity request,
        io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.SuccessStatus> responseObserver) {
      asyncUnimplementedUnaryCall(getDropEntityMethod(), responseObserver);
    }

    /**
     * <pre>
     * Handling indexes. 
     * </pre>
     */
    public void createIndex(ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.CreateIndexMessage request,
        io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.SuccessStatus> responseObserver) {
      asyncUnimplementedUnaryCall(getCreateIndexMethod(), responseObserver);
    }

    /**
     */
    public void dropIndex(ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.DropIndexMessage request,
        io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.SuccessStatus> responseObserver) {
      asyncUnimplementedUnaryCall(getDropIndexMethod(), responseObserver);
    }

    /**
     */
    public void rebuildIndex(ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.RebuildIndexMessage request,
        io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.SuccessStatus> responseObserver) {
      asyncUnimplementedUnaryCall(getRebuildIndexMethod(), responseObserver);
    }

    /**
     */
    public void optimizeEntity(ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Entity request,
        io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.SuccessStatus> responseObserver) {
      asyncUnimplementedUnaryCall(getOptimizeEntityMethod(), responseObserver);
    }

    /**
     */
    public void truncateEntity(ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Entity request,
        io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.SuccessStatus> responseObserver) {
      asyncUnimplementedUnaryCall(getTruncateEntityMethod(), responseObserver);
    }

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
          .addMethod(
            getListSchemasMethod(),
            asyncServerStreamingCall(
              new MethodHandlers<
                com.google.protobuf.Empty,
                ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Schema>(
                  this, METHODID_LIST_SCHEMAS)))
          .addMethod(
            getCreateSchemaMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Schema,
                ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.SuccessStatus>(
                  this, METHODID_CREATE_SCHEMA)))
          .addMethod(
            getDropSchemaMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Schema,
                ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.SuccessStatus>(
                  this, METHODID_DROP_SCHEMA)))
          .addMethod(
            getListEntitiesMethod(),
            asyncServerStreamingCall(
              new MethodHandlers<
                ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Schema,
                ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Entity>(
                  this, METHODID_LIST_ENTITIES)))
          .addMethod(
            getCreateEntityMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.CreateEntityMessage,
                ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.SuccessStatus>(
                  this, METHODID_CREATE_ENTITY)))
          .addMethod(
            getDropEntityMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Entity,
                ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.SuccessStatus>(
                  this, METHODID_DROP_ENTITY)))
          .addMethod(
            getCreateIndexMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.CreateIndexMessage,
                ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.SuccessStatus>(
                  this, METHODID_CREATE_INDEX)))
          .addMethod(
            getDropIndexMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.DropIndexMessage,
                ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.SuccessStatus>(
                  this, METHODID_DROP_INDEX)))
          .addMethod(
            getRebuildIndexMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.RebuildIndexMessage,
                ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.SuccessStatus>(
                  this, METHODID_REBUILD_INDEX)))
          .addMethod(
            getOptimizeEntityMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Entity,
                ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.SuccessStatus>(
                  this, METHODID_OPTIMIZE_ENTITY)))
          .addMethod(
            getTruncateEntityMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Entity,
                ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.SuccessStatus>(
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
      asyncServerStreamingCall(
          getChannel().newCall(getListSchemasMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void createSchema(ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Schema request,
        io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.SuccessStatus> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getCreateSchemaMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void dropSchema(ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Schema request,
        io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.SuccessStatus> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getDropSchemaMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Handling entities. 
     * </pre>
     */
    public void listEntities(ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Schema request,
        io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Entity> responseObserver) {
      asyncServerStreamingCall(
          getChannel().newCall(getListEntitiesMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void createEntity(ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.CreateEntityMessage request,
        io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.SuccessStatus> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getCreateEntityMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void dropEntity(ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Entity request,
        io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.SuccessStatus> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getDropEntityMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Handling indexes. 
     * </pre>
     */
    public void createIndex(ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.CreateIndexMessage request,
        io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.SuccessStatus> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getCreateIndexMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void dropIndex(ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.DropIndexMessage request,
        io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.SuccessStatus> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getDropIndexMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void rebuildIndex(ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.RebuildIndexMessage request,
        io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.SuccessStatus> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getRebuildIndexMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void optimizeEntity(ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Entity request,
        io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.SuccessStatus> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getOptimizeEntityMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void truncateEntity(ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Entity request,
        io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.SuccessStatus> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getTruncateEntityMethod(), getCallOptions()), request, responseObserver);
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
    public java.util.Iterator<ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Schema> listSchemas(
        com.google.protobuf.Empty request) {
      return blockingServerStreamingCall(
          getChannel(), getListSchemasMethod(), getCallOptions(), request);
    }

    /**
     */
    public ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.SuccessStatus createSchema(ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Schema request) {
      return blockingUnaryCall(
          getChannel(), getCreateSchemaMethod(), getCallOptions(), request);
    }

    /**
     */
    public ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.SuccessStatus dropSchema(ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Schema request) {
      return blockingUnaryCall(
          getChannel(), getDropSchemaMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Handling entities. 
     * </pre>
     */
    public java.util.Iterator<ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Entity> listEntities(
        ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Schema request) {
      return blockingServerStreamingCall(
          getChannel(), getListEntitiesMethod(), getCallOptions(), request);
    }

    /**
     */
    public ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.SuccessStatus createEntity(ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.CreateEntityMessage request) {
      return blockingUnaryCall(
          getChannel(), getCreateEntityMethod(), getCallOptions(), request);
    }

    /**
     */
    public ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.SuccessStatus dropEntity(ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Entity request) {
      return blockingUnaryCall(
          getChannel(), getDropEntityMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Handling indexes. 
     * </pre>
     */
    public ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.SuccessStatus createIndex(ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.CreateIndexMessage request) {
      return blockingUnaryCall(
          getChannel(), getCreateIndexMethod(), getCallOptions(), request);
    }

    /**
     */
    public ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.SuccessStatus dropIndex(ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.DropIndexMessage request) {
      return blockingUnaryCall(
          getChannel(), getDropIndexMethod(), getCallOptions(), request);
    }

    /**
     */
    public ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.SuccessStatus rebuildIndex(ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.RebuildIndexMessage request) {
      return blockingUnaryCall(
          getChannel(), getRebuildIndexMethod(), getCallOptions(), request);
    }

    /**
     */
    public ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.SuccessStatus optimizeEntity(ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Entity request) {
      return blockingUnaryCall(
          getChannel(), getOptimizeEntityMethod(), getCallOptions(), request);
    }

    /**
     */
    public ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.SuccessStatus truncateEntity(ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Entity request) {
      return blockingUnaryCall(
          getChannel(), getTruncateEntityMethod(), getCallOptions(), request);
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
     */
    public com.google.common.util.concurrent.ListenableFuture<ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.SuccessStatus> createSchema(
        ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Schema request) {
      return futureUnaryCall(
          getChannel().newCall(getCreateSchemaMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.SuccessStatus> dropSchema(
        ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Schema request) {
      return futureUnaryCall(
          getChannel().newCall(getDropSchemaMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.SuccessStatus> createEntity(
        ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.CreateEntityMessage request) {
      return futureUnaryCall(
          getChannel().newCall(getCreateEntityMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.SuccessStatus> dropEntity(
        ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Entity request) {
      return futureUnaryCall(
          getChannel().newCall(getDropEntityMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Handling indexes. 
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.SuccessStatus> createIndex(
        ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.CreateIndexMessage request) {
      return futureUnaryCall(
          getChannel().newCall(getCreateIndexMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.SuccessStatus> dropIndex(
        ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.DropIndexMessage request) {
      return futureUnaryCall(
          getChannel().newCall(getDropIndexMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.SuccessStatus> rebuildIndex(
        ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.RebuildIndexMessage request) {
      return futureUnaryCall(
          getChannel().newCall(getRebuildIndexMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.SuccessStatus> optimizeEntity(
        ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Entity request) {
      return futureUnaryCall(
          getChannel().newCall(getOptimizeEntityMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.SuccessStatus> truncateEntity(
        ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Entity request) {
      return futureUnaryCall(
          getChannel().newCall(getTruncateEntityMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_LIST_SCHEMAS = 0;
  private static final int METHODID_CREATE_SCHEMA = 1;
  private static final int METHODID_DROP_SCHEMA = 2;
  private static final int METHODID_LIST_ENTITIES = 3;
  private static final int METHODID_CREATE_ENTITY = 4;
  private static final int METHODID_DROP_ENTITY = 5;
  private static final int METHODID_CREATE_INDEX = 6;
  private static final int METHODID_DROP_INDEX = 7;
  private static final int METHODID_REBUILD_INDEX = 8;
  private static final int METHODID_OPTIMIZE_ENTITY = 9;
  private static final int METHODID_TRUNCATE_ENTITY = 10;

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
              (io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.SuccessStatus>) responseObserver);
          break;
        case METHODID_DROP_SCHEMA:
          serviceImpl.dropSchema((ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Schema) request,
              (io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.SuccessStatus>) responseObserver);
          break;
        case METHODID_LIST_ENTITIES:
          serviceImpl.listEntities((ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Schema) request,
              (io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Entity>) responseObserver);
          break;
        case METHODID_CREATE_ENTITY:
          serviceImpl.createEntity((ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.CreateEntityMessage) request,
              (io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.SuccessStatus>) responseObserver);
          break;
        case METHODID_DROP_ENTITY:
          serviceImpl.dropEntity((ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Entity) request,
              (io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.SuccessStatus>) responseObserver);
          break;
        case METHODID_CREATE_INDEX:
          serviceImpl.createIndex((ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.CreateIndexMessage) request,
              (io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.SuccessStatus>) responseObserver);
          break;
        case METHODID_DROP_INDEX:
          serviceImpl.dropIndex((ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.DropIndexMessage) request,
              (io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.SuccessStatus>) responseObserver);
          break;
        case METHODID_REBUILD_INDEX:
          serviceImpl.rebuildIndex((ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.RebuildIndexMessage) request,
              (io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.SuccessStatus>) responseObserver);
          break;
        case METHODID_OPTIMIZE_ENTITY:
          serviceImpl.optimizeEntity((ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Entity) request,
              (io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.SuccessStatus>) responseObserver);
          break;
        case METHODID_TRUNCATE_ENTITY:
          serviceImpl.truncateEntity((ch.unibas.dmi.dbis.cottontail.grpc.CottontailGrpc.Entity) request,
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
              .addMethod(getListSchemasMethod())
              .addMethod(getCreateSchemaMethod())
              .addMethod(getDropSchemaMethod())
              .addMethod(getListEntitiesMethod())
              .addMethod(getCreateEntityMethod())
              .addMethod(getDropEntityMethod())
              .addMethod(getCreateIndexMethod())
              .addMethod(getDropIndexMethod())
              .addMethod(getRebuildIndexMethod())
              .addMethod(getOptimizeEntityMethod())
              .addMethod(getTruncateEntityMethod())
              .build();
        }
      }
    }
    return result;
  }
}
