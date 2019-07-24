package org.vitrivr.adampro.grpc;

import static io.grpc.MethodDescriptor.generateFullMethodName;
import static io.grpc.stub.ClientCalls.asyncBidiStreamingCall;
import static io.grpc.stub.ClientCalls.asyncServerStreamingCall;
import static io.grpc.stub.ClientCalls.asyncUnaryCall;
import static io.grpc.stub.ClientCalls.*;
import static io.grpc.stub.ServerCalls.asyncBidiStreamingCall;
import static io.grpc.stub.ServerCalls.asyncServerStreamingCall;
import static io.grpc.stub.ServerCalls.asyncUnaryCall;
import static io.grpc.stub.ServerCalls.*;

/**
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.7.0)",
    comments = "Source: grpc.proto")
public final class AdamDefinitionGrpc {

  private AdamDefinitionGrpc() {}

  public static final String SERVICE_NAME = "AdamDefinition";

  // Static method descriptors that strictly reflect the proto.
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<org.vitrivr.adampro.grpc.AdamGrpc.CreateEntityMessage,
      org.vitrivr.adampro.grpc.AdamGrpc.AckMessage> METHOD_CREATE_ENTITY =
      io.grpc.MethodDescriptor.<org.vitrivr.adampro.grpc.AdamGrpc.CreateEntityMessage, org.vitrivr.adampro.grpc.AdamGrpc.AckMessage>newBuilder()
          .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
          .setFullMethodName(generateFullMethodName(
              "AdamDefinition", "CreateEntity"))
          .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              org.vitrivr.adampro.grpc.AdamGrpc.CreateEntityMessage.getDefaultInstance()))
          .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              org.vitrivr.adampro.grpc.AdamGrpc.AckMessage.getDefaultInstance()))
          .setSchemaDescriptor(new AdamDefinitionMethodDescriptorSupplier("CreateEntity"))
          .build();
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<org.vitrivr.adampro.grpc.AdamGrpc.EmptyMessage,
      org.vitrivr.adampro.grpc.AdamGrpc.AvailableAttributeTypesMessage> METHOD_AVAILABLE_ATTRIBUTE_TYPES =
      io.grpc.MethodDescriptor.<org.vitrivr.adampro.grpc.AdamGrpc.EmptyMessage, org.vitrivr.adampro.grpc.AdamGrpc.AvailableAttributeTypesMessage>newBuilder()
          .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
          .setFullMethodName(generateFullMethodName(
              "AdamDefinition", "AvailableAttributeTypes"))
          .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              org.vitrivr.adampro.grpc.AdamGrpc.EmptyMessage.getDefaultInstance()))
          .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              org.vitrivr.adampro.grpc.AdamGrpc.AvailableAttributeTypesMessage.getDefaultInstance()))
          .setSchemaDescriptor(new AdamDefinitionMethodDescriptorSupplier("AvailableAttributeTypes"))
          .build();
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<org.vitrivr.adampro.grpc.AdamGrpc.EntityNameMessage,
      org.vitrivr.adampro.grpc.AdamGrpc.AckMessage> METHOD_COUNT =
      io.grpc.MethodDescriptor.<org.vitrivr.adampro.grpc.AdamGrpc.EntityNameMessage, org.vitrivr.adampro.grpc.AdamGrpc.AckMessage>newBuilder()
          .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
          .setFullMethodName(generateFullMethodName(
              "AdamDefinition", "Count"))
          .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              org.vitrivr.adampro.grpc.AdamGrpc.EntityNameMessage.getDefaultInstance()))
          .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              org.vitrivr.adampro.grpc.AdamGrpc.AckMessage.getDefaultInstance()))
          .setSchemaDescriptor(new AdamDefinitionMethodDescriptorSupplier("Count"))
          .build();
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<org.vitrivr.adampro.grpc.AdamGrpc.EntityNameMessage,
      org.vitrivr.adampro.grpc.AdamGrpc.AckMessage> METHOD_DROP_ENTITY =
      io.grpc.MethodDescriptor.<org.vitrivr.adampro.grpc.AdamGrpc.EntityNameMessage, org.vitrivr.adampro.grpc.AdamGrpc.AckMessage>newBuilder()
          .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
          .setFullMethodName(generateFullMethodName(
              "AdamDefinition", "DropEntity"))
          .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              org.vitrivr.adampro.grpc.AdamGrpc.EntityNameMessage.getDefaultInstance()))
          .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              org.vitrivr.adampro.grpc.AdamGrpc.AckMessage.getDefaultInstance()))
          .setSchemaDescriptor(new AdamDefinitionMethodDescriptorSupplier("DropEntity"))
          .build();
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<org.vitrivr.adampro.grpc.AdamGrpc.EntityNameMessage,
      org.vitrivr.adampro.grpc.AdamGrpc.ExistsMessage> METHOD_EXISTS_ENTITY =
      io.grpc.MethodDescriptor.<org.vitrivr.adampro.grpc.AdamGrpc.EntityNameMessage, org.vitrivr.adampro.grpc.AdamGrpc.ExistsMessage>newBuilder()
          .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
          .setFullMethodName(generateFullMethodName(
              "AdamDefinition", "ExistsEntity"))
          .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              org.vitrivr.adampro.grpc.AdamGrpc.EntityNameMessage.getDefaultInstance()))
          .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              org.vitrivr.adampro.grpc.AdamGrpc.ExistsMessage.getDefaultInstance()))
          .setSchemaDescriptor(new AdamDefinitionMethodDescriptorSupplier("ExistsEntity"))
          .build();
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<org.vitrivr.adampro.grpc.AdamGrpc.InsertMessage,
      org.vitrivr.adampro.grpc.AdamGrpc.AckMessage> METHOD_INSERT =
      io.grpc.MethodDescriptor.<org.vitrivr.adampro.grpc.AdamGrpc.InsertMessage, org.vitrivr.adampro.grpc.AdamGrpc.AckMessage>newBuilder()
          .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
          .setFullMethodName(generateFullMethodName(
              "AdamDefinition", "Insert"))
          .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              org.vitrivr.adampro.grpc.AdamGrpc.InsertMessage.getDefaultInstance()))
          .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              org.vitrivr.adampro.grpc.AdamGrpc.AckMessage.getDefaultInstance()))
          .setSchemaDescriptor(new AdamDefinitionMethodDescriptorSupplier("Insert"))
          .build();
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<org.vitrivr.adampro.grpc.AdamGrpc.InsertMessage,
      org.vitrivr.adampro.grpc.AdamGrpc.AckMessage> METHOD_STREAM_INSERT =
      io.grpc.MethodDescriptor.<org.vitrivr.adampro.grpc.AdamGrpc.InsertMessage, org.vitrivr.adampro.grpc.AdamGrpc.AckMessage>newBuilder()
          .setType(io.grpc.MethodDescriptor.MethodType.BIDI_STREAMING)
          .setFullMethodName(generateFullMethodName(
              "AdamDefinition", "StreamInsert"))
          .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              org.vitrivr.adampro.grpc.AdamGrpc.InsertMessage.getDefaultInstance()))
          .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              org.vitrivr.adampro.grpc.AdamGrpc.AckMessage.getDefaultInstance()))
          .setSchemaDescriptor(new AdamDefinitionMethodDescriptorSupplier("StreamInsert"))
          .build();
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<org.vitrivr.adampro.grpc.AdamGrpc.EntityNameMessage,
      org.vitrivr.adampro.grpc.AdamGrpc.AckMessage> METHOD_VACUUM_ENTITY =
      io.grpc.MethodDescriptor.<org.vitrivr.adampro.grpc.AdamGrpc.EntityNameMessage, org.vitrivr.adampro.grpc.AdamGrpc.AckMessage>newBuilder()
          .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
          .setFullMethodName(generateFullMethodName(
              "AdamDefinition", "VacuumEntity"))
          .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              org.vitrivr.adampro.grpc.AdamGrpc.EntityNameMessage.getDefaultInstance()))
          .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              org.vitrivr.adampro.grpc.AdamGrpc.AckMessage.getDefaultInstance()))
          .setSchemaDescriptor(new AdamDefinitionMethodDescriptorSupplier("VacuumEntity"))
          .build();
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<org.vitrivr.adampro.grpc.AdamGrpc.DeleteMessage,
      org.vitrivr.adampro.grpc.AdamGrpc.AckMessage> METHOD_DELETE =
      io.grpc.MethodDescriptor.<org.vitrivr.adampro.grpc.AdamGrpc.DeleteMessage, org.vitrivr.adampro.grpc.AdamGrpc.AckMessage>newBuilder()
          .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
          .setFullMethodName(generateFullMethodName(
              "AdamDefinition", "Delete"))
          .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              org.vitrivr.adampro.grpc.AdamGrpc.DeleteMessage.getDefaultInstance()))
          .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              org.vitrivr.adampro.grpc.AdamGrpc.AckMessage.getDefaultInstance()))
          .setSchemaDescriptor(new AdamDefinitionMethodDescriptorSupplier("Delete"))
          .build();
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<org.vitrivr.adampro.grpc.AdamGrpc.IndexMessage,
      org.vitrivr.adampro.grpc.AdamGrpc.AckMessage> METHOD_INDEX =
      io.grpc.MethodDescriptor.<org.vitrivr.adampro.grpc.AdamGrpc.IndexMessage, org.vitrivr.adampro.grpc.AdamGrpc.AckMessage>newBuilder()
          .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
          .setFullMethodName(generateFullMethodName(
              "AdamDefinition", "Index"))
          .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              org.vitrivr.adampro.grpc.AdamGrpc.IndexMessage.getDefaultInstance()))
          .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              org.vitrivr.adampro.grpc.AdamGrpc.AckMessage.getDefaultInstance()))
          .setSchemaDescriptor(new AdamDefinitionMethodDescriptorSupplier("Index"))
          .build();
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<org.vitrivr.adampro.grpc.AdamGrpc.IndexMessage,
      org.vitrivr.adampro.grpc.AdamGrpc.AckMessage> METHOD_GENERATE_ALL_INDEXES =
      io.grpc.MethodDescriptor.<org.vitrivr.adampro.grpc.AdamGrpc.IndexMessage, org.vitrivr.adampro.grpc.AdamGrpc.AckMessage>newBuilder()
          .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
          .setFullMethodName(generateFullMethodName(
              "AdamDefinition", "GenerateAllIndexes"))
          .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              org.vitrivr.adampro.grpc.AdamGrpc.IndexMessage.getDefaultInstance()))
          .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              org.vitrivr.adampro.grpc.AdamGrpc.AckMessage.getDefaultInstance()))
          .setSchemaDescriptor(new AdamDefinitionMethodDescriptorSupplier("GenerateAllIndexes"))
          .build();
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<org.vitrivr.adampro.grpc.AdamGrpc.IndexExistsMessage,
      org.vitrivr.adampro.grpc.AdamGrpc.ExistsMessage> METHOD_EXISTS_INDEX =
      io.grpc.MethodDescriptor.<org.vitrivr.adampro.grpc.AdamGrpc.IndexExistsMessage, org.vitrivr.adampro.grpc.AdamGrpc.ExistsMessage>newBuilder()
          .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
          .setFullMethodName(generateFullMethodName(
              "AdamDefinition", "ExistsIndex"))
          .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              org.vitrivr.adampro.grpc.AdamGrpc.IndexExistsMessage.getDefaultInstance()))
          .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              org.vitrivr.adampro.grpc.AdamGrpc.ExistsMessage.getDefaultInstance()))
          .setSchemaDescriptor(new AdamDefinitionMethodDescriptorSupplier("ExistsIndex"))
          .build();
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<org.vitrivr.adampro.grpc.AdamGrpc.IndexNameMessage,
      org.vitrivr.adampro.grpc.AdamGrpc.AckMessage> METHOD_DROP_INDEX =
      io.grpc.MethodDescriptor.<org.vitrivr.adampro.grpc.AdamGrpc.IndexNameMessage, org.vitrivr.adampro.grpc.AdamGrpc.AckMessage>newBuilder()
          .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
          .setFullMethodName(generateFullMethodName(
              "AdamDefinition", "DropIndex"))
          .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              org.vitrivr.adampro.grpc.AdamGrpc.IndexNameMessage.getDefaultInstance()))
          .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              org.vitrivr.adampro.grpc.AdamGrpc.AckMessage.getDefaultInstance()))
          .setSchemaDescriptor(new AdamDefinitionMethodDescriptorSupplier("DropIndex"))
          .build();
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<org.vitrivr.adampro.grpc.AdamGrpc.EntityNameMessage,
      org.vitrivr.adampro.grpc.AdamGrpc.IndexesMessage> METHOD_LIST_INDEXES =
      io.grpc.MethodDescriptor.<org.vitrivr.adampro.grpc.AdamGrpc.EntityNameMessage, org.vitrivr.adampro.grpc.AdamGrpc.IndexesMessage>newBuilder()
          .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
          .setFullMethodName(generateFullMethodName(
              "AdamDefinition", "ListIndexes"))
          .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              org.vitrivr.adampro.grpc.AdamGrpc.EntityNameMessage.getDefaultInstance()))
          .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              org.vitrivr.adampro.grpc.AdamGrpc.IndexesMessage.getDefaultInstance()))
          .setSchemaDescriptor(new AdamDefinitionMethodDescriptorSupplier("ListIndexes"))
          .build();
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<org.vitrivr.adampro.grpc.AdamGrpc.GenerateRandomDataMessage,
      org.vitrivr.adampro.grpc.AdamGrpc.AckMessage> METHOD_GENERATE_RANDOM_DATA =
      io.grpc.MethodDescriptor.<org.vitrivr.adampro.grpc.AdamGrpc.GenerateRandomDataMessage, org.vitrivr.adampro.grpc.AdamGrpc.AckMessage>newBuilder()
          .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
          .setFullMethodName(generateFullMethodName(
              "AdamDefinition", "GenerateRandomData"))
          .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              org.vitrivr.adampro.grpc.AdamGrpc.GenerateRandomDataMessage.getDefaultInstance()))
          .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              org.vitrivr.adampro.grpc.AdamGrpc.AckMessage.getDefaultInstance()))
          .setSchemaDescriptor(new AdamDefinitionMethodDescriptorSupplier("GenerateRandomData"))
          .build();
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<org.vitrivr.adampro.grpc.AdamGrpc.EmptyMessage,
      org.vitrivr.adampro.grpc.AdamGrpc.EntitiesMessage> METHOD_LIST_ENTITIES =
      io.grpc.MethodDescriptor.<org.vitrivr.adampro.grpc.AdamGrpc.EmptyMessage, org.vitrivr.adampro.grpc.AdamGrpc.EntitiesMessage>newBuilder()
          .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
          .setFullMethodName(generateFullMethodName(
              "AdamDefinition", "ListEntities"))
          .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              org.vitrivr.adampro.grpc.AdamGrpc.EmptyMessage.getDefaultInstance()))
          .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              org.vitrivr.adampro.grpc.AdamGrpc.EntitiesMessage.getDefaultInstance()))
          .setSchemaDescriptor(new AdamDefinitionMethodDescriptorSupplier("ListEntities"))
          .build();
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<org.vitrivr.adampro.grpc.AdamGrpc.EntityPropertiesMessage,
      org.vitrivr.adampro.grpc.AdamGrpc.PropertiesMessage> METHOD_GET_ENTITY_PROPERTIES =
      io.grpc.MethodDescriptor.<org.vitrivr.adampro.grpc.AdamGrpc.EntityPropertiesMessage, org.vitrivr.adampro.grpc.AdamGrpc.PropertiesMessage>newBuilder()
          .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
          .setFullMethodName(generateFullMethodName(
              "AdamDefinition", "GetEntityProperties"))
          .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              org.vitrivr.adampro.grpc.AdamGrpc.EntityPropertiesMessage.getDefaultInstance()))
          .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              org.vitrivr.adampro.grpc.AdamGrpc.PropertiesMessage.getDefaultInstance()))
          .setSchemaDescriptor(new AdamDefinitionMethodDescriptorSupplier("GetEntityProperties"))
          .build();
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<org.vitrivr.adampro.grpc.AdamGrpc.AttributePropertiesMessage,
      org.vitrivr.adampro.grpc.AdamGrpc.PropertiesMessage> METHOD_GET_ATTRIBUTE_PROPERTIES =
      io.grpc.MethodDescriptor.<org.vitrivr.adampro.grpc.AdamGrpc.AttributePropertiesMessage, org.vitrivr.adampro.grpc.AdamGrpc.PropertiesMessage>newBuilder()
          .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
          .setFullMethodName(generateFullMethodName(
              "AdamDefinition", "GetAttributeProperties"))
          .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              org.vitrivr.adampro.grpc.AdamGrpc.AttributePropertiesMessage.getDefaultInstance()))
          .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              org.vitrivr.adampro.grpc.AdamGrpc.PropertiesMessage.getDefaultInstance()))
          .setSchemaDescriptor(new AdamDefinitionMethodDescriptorSupplier("GetAttributeProperties"))
          .build();
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<org.vitrivr.adampro.grpc.AdamGrpc.IndexPropertiesMessage,
      org.vitrivr.adampro.grpc.AdamGrpc.PropertiesMessage> METHOD_GET_INDEX_PROPERTIES =
      io.grpc.MethodDescriptor.<org.vitrivr.adampro.grpc.AdamGrpc.IndexPropertiesMessage, org.vitrivr.adampro.grpc.AdamGrpc.PropertiesMessage>newBuilder()
          .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
          .setFullMethodName(generateFullMethodName(
              "AdamDefinition", "GetIndexProperties"))
          .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              org.vitrivr.adampro.grpc.AdamGrpc.IndexPropertiesMessage.getDefaultInstance()))
          .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              org.vitrivr.adampro.grpc.AdamGrpc.PropertiesMessage.getDefaultInstance()))
          .setSchemaDescriptor(new AdamDefinitionMethodDescriptorSupplier("GetIndexProperties"))
          .build();
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<org.vitrivr.adampro.grpc.AdamGrpc.RepartitionMessage,
      org.vitrivr.adampro.grpc.AdamGrpc.AckMessage> METHOD_REPARTITION_ENTITY_DATA =
      io.grpc.MethodDescriptor.<org.vitrivr.adampro.grpc.AdamGrpc.RepartitionMessage, org.vitrivr.adampro.grpc.AdamGrpc.AckMessage>newBuilder()
          .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
          .setFullMethodName(generateFullMethodName(
              "AdamDefinition", "RepartitionEntityData"))
          .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              org.vitrivr.adampro.grpc.AdamGrpc.RepartitionMessage.getDefaultInstance()))
          .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              org.vitrivr.adampro.grpc.AdamGrpc.AckMessage.getDefaultInstance()))
          .setSchemaDescriptor(new AdamDefinitionMethodDescriptorSupplier("RepartitionEntityData"))
          .build();
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<org.vitrivr.adampro.grpc.AdamGrpc.RepartitionMessage,
      org.vitrivr.adampro.grpc.AdamGrpc.AckMessage> METHOD_REPARTITION_INDEX_DATA =
      io.grpc.MethodDescriptor.<org.vitrivr.adampro.grpc.AdamGrpc.RepartitionMessage, org.vitrivr.adampro.grpc.AdamGrpc.AckMessage>newBuilder()
          .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
          .setFullMethodName(generateFullMethodName(
              "AdamDefinition", "RepartitionIndexData"))
          .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              org.vitrivr.adampro.grpc.AdamGrpc.RepartitionMessage.getDefaultInstance()))
          .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              org.vitrivr.adampro.grpc.AdamGrpc.AckMessage.getDefaultInstance()))
          .setSchemaDescriptor(new AdamDefinitionMethodDescriptorSupplier("RepartitionIndexData"))
          .build();
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<org.vitrivr.adampro.grpc.AdamGrpc.AdaptScanMethodsMessage,
      org.vitrivr.adampro.grpc.AdamGrpc.AckMessage> METHOD_ADAPT_SCAN_METHODS =
      io.grpc.MethodDescriptor.<org.vitrivr.adampro.grpc.AdamGrpc.AdaptScanMethodsMessage, org.vitrivr.adampro.grpc.AdamGrpc.AckMessage>newBuilder()
          .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
          .setFullMethodName(generateFullMethodName(
              "AdamDefinition", "AdaptScanMethods"))
          .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              org.vitrivr.adampro.grpc.AdamGrpc.AdaptScanMethodsMessage.getDefaultInstance()))
          .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              org.vitrivr.adampro.grpc.AdamGrpc.AckMessage.getDefaultInstance()))
          .setSchemaDescriptor(new AdamDefinitionMethodDescriptorSupplier("AdaptScanMethods"))
          .build();
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<org.vitrivr.adampro.grpc.AdamGrpc.SparsifyEntityMessage,
      org.vitrivr.adampro.grpc.AdamGrpc.AckMessage> METHOD_SPARSIFY_ENTITY =
      io.grpc.MethodDescriptor.<org.vitrivr.adampro.grpc.AdamGrpc.SparsifyEntityMessage, org.vitrivr.adampro.grpc.AdamGrpc.AckMessage>newBuilder()
          .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
          .setFullMethodName(generateFullMethodName(
              "AdamDefinition", "SparsifyEntity"))
          .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              org.vitrivr.adampro.grpc.AdamGrpc.SparsifyEntityMessage.getDefaultInstance()))
          .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              org.vitrivr.adampro.grpc.AdamGrpc.AckMessage.getDefaultInstance()))
          .setSchemaDescriptor(new AdamDefinitionMethodDescriptorSupplier("SparsifyEntity"))
          .build();
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<org.vitrivr.adampro.grpc.AdamGrpc.ImportMessage,
      org.vitrivr.adampro.grpc.AdamGrpc.AckMessage> METHOD_IMPORT_DATA =
      io.grpc.MethodDescriptor.<org.vitrivr.adampro.grpc.AdamGrpc.ImportMessage, org.vitrivr.adampro.grpc.AdamGrpc.AckMessage>newBuilder()
          .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
          .setFullMethodName(generateFullMethodName(
              "AdamDefinition", "ImportData"))
          .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              org.vitrivr.adampro.grpc.AdamGrpc.ImportMessage.getDefaultInstance()))
          .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              org.vitrivr.adampro.grpc.AdamGrpc.AckMessage.getDefaultInstance()))
          .setSchemaDescriptor(new AdamDefinitionMethodDescriptorSupplier("ImportData"))
          .build();
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<org.vitrivr.adampro.grpc.AdamGrpc.ProtoImportMessage,
      org.vitrivr.adampro.grpc.AdamGrpc.AckMessage> METHOD_PROTO_IMPORT_DATA =
      io.grpc.MethodDescriptor.<org.vitrivr.adampro.grpc.AdamGrpc.ProtoImportMessage, org.vitrivr.adampro.grpc.AdamGrpc.AckMessage>newBuilder()
          .setType(io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
          .setFullMethodName(generateFullMethodName(
              "AdamDefinition", "ProtoImportData"))
          .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              org.vitrivr.adampro.grpc.AdamGrpc.ProtoImportMessage.getDefaultInstance()))
          .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              org.vitrivr.adampro.grpc.AdamGrpc.AckMessage.getDefaultInstance()))
          .setSchemaDescriptor(new AdamDefinitionMethodDescriptorSupplier("ProtoImportData"))
          .build();
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<org.vitrivr.adampro.grpc.AdamGrpc.ProtoExportMessage,
      org.vitrivr.adampro.grpc.AdamGrpc.AckMessage> METHOD_PROTO_EXPORT_DATA =
      io.grpc.MethodDescriptor.<org.vitrivr.adampro.grpc.AdamGrpc.ProtoExportMessage, org.vitrivr.adampro.grpc.AdamGrpc.AckMessage>newBuilder()
          .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
          .setFullMethodName(generateFullMethodName(
              "AdamDefinition", "ProtoExportData"))
          .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              org.vitrivr.adampro.grpc.AdamGrpc.ProtoExportMessage.getDefaultInstance()))
          .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              org.vitrivr.adampro.grpc.AdamGrpc.AckMessage.getDefaultInstance()))
          .setSchemaDescriptor(new AdamDefinitionMethodDescriptorSupplier("ProtoExportData"))
          .build();
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<org.vitrivr.adampro.grpc.AdamGrpc.EmptyMessage,
      org.vitrivr.adampro.grpc.AdamGrpc.StorageHandlersMessage> METHOD_LIST_STORAGE_HANDLERS =
      io.grpc.MethodDescriptor.<org.vitrivr.adampro.grpc.AdamGrpc.EmptyMessage, org.vitrivr.adampro.grpc.AdamGrpc.StorageHandlersMessage>newBuilder()
          .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
          .setFullMethodName(generateFullMethodName(
              "AdamDefinition", "ListStorageHandlers"))
          .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              org.vitrivr.adampro.grpc.AdamGrpc.EmptyMessage.getDefaultInstance()))
          .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              org.vitrivr.adampro.grpc.AdamGrpc.StorageHandlersMessage.getDefaultInstance()))
          .setSchemaDescriptor(new AdamDefinitionMethodDescriptorSupplier("ListStorageHandlers"))
          .build();
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static final io.grpc.MethodDescriptor<org.vitrivr.adampro.grpc.AdamGrpc.TransferStorageHandlerMessage,
      org.vitrivr.adampro.grpc.AdamGrpc.AckMessage> METHOD_TRANSFER_STORAGE_HANDLER =
      io.grpc.MethodDescriptor.<org.vitrivr.adampro.grpc.AdamGrpc.TransferStorageHandlerMessage, org.vitrivr.adampro.grpc.AdamGrpc.AckMessage>newBuilder()
          .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
          .setFullMethodName(generateFullMethodName(
              "AdamDefinition", "TransferStorageHandler"))
          .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              org.vitrivr.adampro.grpc.AdamGrpc.TransferStorageHandlerMessage.getDefaultInstance()))
          .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
              org.vitrivr.adampro.grpc.AdamGrpc.AckMessage.getDefaultInstance()))
          .setSchemaDescriptor(new AdamDefinitionMethodDescriptorSupplier("TransferStorageHandler"))
          .build();

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
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
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
    public void createEntity(org.vitrivr.adampro.grpc.AdamGrpc.CreateEntityMessage request,
        io.grpc.stub.StreamObserver<org.vitrivr.adampro.grpc.AdamGrpc.AckMessage> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_CREATE_ENTITY, responseObserver);
    }

    /**
     */
    public void availableAttributeTypes(org.vitrivr.adampro.grpc.AdamGrpc.EmptyMessage request,
        io.grpc.stub.StreamObserver<org.vitrivr.adampro.grpc.AdamGrpc.AvailableAttributeTypesMessage> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_AVAILABLE_ATTRIBUTE_TYPES, responseObserver);
    }

    /**
     */
    public void count(org.vitrivr.adampro.grpc.AdamGrpc.EntityNameMessage request,
        io.grpc.stub.StreamObserver<org.vitrivr.adampro.grpc.AdamGrpc.AckMessage> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_COUNT, responseObserver);
    }

    /**
     */
    public void dropEntity(org.vitrivr.adampro.grpc.AdamGrpc.EntityNameMessage request,
        io.grpc.stub.StreamObserver<org.vitrivr.adampro.grpc.AdamGrpc.AckMessage> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_DROP_ENTITY, responseObserver);
    }

    /**
     */
    public void existsEntity(org.vitrivr.adampro.grpc.AdamGrpc.EntityNameMessage request,
        io.grpc.stub.StreamObserver<org.vitrivr.adampro.grpc.AdamGrpc.ExistsMessage> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_EXISTS_ENTITY, responseObserver);
    }

    /**
     */
    public void insert(org.vitrivr.adampro.grpc.AdamGrpc.InsertMessage request,
        io.grpc.stub.StreamObserver<org.vitrivr.adampro.grpc.AdamGrpc.AckMessage> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_INSERT, responseObserver);
    }

    /**
     */
    public io.grpc.stub.StreamObserver<org.vitrivr.adampro.grpc.AdamGrpc.InsertMessage> streamInsert(
        io.grpc.stub.StreamObserver<org.vitrivr.adampro.grpc.AdamGrpc.AckMessage> responseObserver) {
      return asyncUnimplementedStreamingCall(METHOD_STREAM_INSERT, responseObserver);
    }

    /**
     */
    public void vacuumEntity(org.vitrivr.adampro.grpc.AdamGrpc.EntityNameMessage request,
        io.grpc.stub.StreamObserver<org.vitrivr.adampro.grpc.AdamGrpc.AckMessage> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_VACUUM_ENTITY, responseObserver);
    }

    /**
     */
    public void delete(org.vitrivr.adampro.grpc.AdamGrpc.DeleteMessage request,
        io.grpc.stub.StreamObserver<org.vitrivr.adampro.grpc.AdamGrpc.AckMessage> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_DELETE, responseObserver);
    }

    /**
     * <pre>
     *creates an index on the data
     * </pre>
     */
    public void index(org.vitrivr.adampro.grpc.AdamGrpc.IndexMessage request,
        io.grpc.stub.StreamObserver<org.vitrivr.adampro.grpc.AdamGrpc.AckMessage> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_INDEX, responseObserver);
    }

    /**
     */
    public void generateAllIndexes(org.vitrivr.adampro.grpc.AdamGrpc.IndexMessage request,
        io.grpc.stub.StreamObserver<org.vitrivr.adampro.grpc.AdamGrpc.AckMessage> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_GENERATE_ALL_INDEXES, responseObserver);
    }

    /**
     */
    public void existsIndex(org.vitrivr.adampro.grpc.AdamGrpc.IndexExistsMessage request,
        io.grpc.stub.StreamObserver<org.vitrivr.adampro.grpc.AdamGrpc.ExistsMessage> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_EXISTS_INDEX, responseObserver);
    }

    /**
     */
    public void dropIndex(org.vitrivr.adampro.grpc.AdamGrpc.IndexNameMessage request,
        io.grpc.stub.StreamObserver<org.vitrivr.adampro.grpc.AdamGrpc.AckMessage> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_DROP_INDEX, responseObserver);
    }

    /**
     */
    public void listIndexes(org.vitrivr.adampro.grpc.AdamGrpc.EntityNameMessage request,
        io.grpc.stub.StreamObserver<org.vitrivr.adampro.grpc.AdamGrpc.IndexesMessage> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_LIST_INDEXES, responseObserver);
    }

    /**
     * <pre>
     *generates an entity with random data and with all available indexes (for demo purposes)
     * </pre>
     */
    public void generateRandomData(org.vitrivr.adampro.grpc.AdamGrpc.GenerateRandomDataMessage request,
        io.grpc.stub.StreamObserver<org.vitrivr.adampro.grpc.AdamGrpc.AckMessage> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_GENERATE_RANDOM_DATA, responseObserver);
    }

    /**
     */
    public void listEntities(org.vitrivr.adampro.grpc.AdamGrpc.EmptyMessage request,
        io.grpc.stub.StreamObserver<org.vitrivr.adampro.grpc.AdamGrpc.EntitiesMessage> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_LIST_ENTITIES, responseObserver);
    }

    /**
     */
    public void getEntityProperties(org.vitrivr.adampro.grpc.AdamGrpc.EntityPropertiesMessage request,
        io.grpc.stub.StreamObserver<org.vitrivr.adampro.grpc.AdamGrpc.PropertiesMessage> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_GET_ENTITY_PROPERTIES, responseObserver);
    }

    /**
     */
    public void getAttributeProperties(org.vitrivr.adampro.grpc.AdamGrpc.AttributePropertiesMessage request,
        io.grpc.stub.StreamObserver<org.vitrivr.adampro.grpc.AdamGrpc.PropertiesMessage> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_GET_ATTRIBUTE_PROPERTIES, responseObserver);
    }

    /**
     */
    public void getIndexProperties(org.vitrivr.adampro.grpc.AdamGrpc.IndexPropertiesMessage request,
        io.grpc.stub.StreamObserver<org.vitrivr.adampro.grpc.AdamGrpc.PropertiesMessage> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_GET_INDEX_PROPERTIES, responseObserver);
    }

    /**
     */
    public void repartitionEntityData(org.vitrivr.adampro.grpc.AdamGrpc.RepartitionMessage request,
        io.grpc.stub.StreamObserver<org.vitrivr.adampro.grpc.AdamGrpc.AckMessage> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_REPARTITION_ENTITY_DATA, responseObserver);
    }

    /**
     */
    public void repartitionIndexData(org.vitrivr.adampro.grpc.AdamGrpc.RepartitionMessage request,
        io.grpc.stub.StreamObserver<org.vitrivr.adampro.grpc.AdamGrpc.AckMessage> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_REPARTITION_INDEX_DATA, responseObserver);
    }

    /**
     */
    public void adaptScanMethods(org.vitrivr.adampro.grpc.AdamGrpc.AdaptScanMethodsMessage request,
        io.grpc.stub.StreamObserver<org.vitrivr.adampro.grpc.AdamGrpc.AckMessage> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_ADAPT_SCAN_METHODS, responseObserver);
    }

    /**
     */
    public void sparsifyEntity(org.vitrivr.adampro.grpc.AdamGrpc.SparsifyEntityMessage request,
        io.grpc.stub.StreamObserver<org.vitrivr.adampro.grpc.AdamGrpc.AckMessage> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_SPARSIFY_ENTITY, responseObserver);
    }

    /**
     */
    public void importData(org.vitrivr.adampro.grpc.AdamGrpc.ImportMessage request,
        io.grpc.stub.StreamObserver<org.vitrivr.adampro.grpc.AdamGrpc.AckMessage> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_IMPORT_DATA, responseObserver);
    }

    /**
     */
    public void protoImportData(org.vitrivr.adampro.grpc.AdamGrpc.ProtoImportMessage request,
        io.grpc.stub.StreamObserver<org.vitrivr.adampro.grpc.AdamGrpc.AckMessage> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_PROTO_IMPORT_DATA, responseObserver);
    }

    /**
     */
    public void protoExportData(org.vitrivr.adampro.grpc.AdamGrpc.ProtoExportMessage request,
        io.grpc.stub.StreamObserver<org.vitrivr.adampro.grpc.AdamGrpc.AckMessage> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_PROTO_EXPORT_DATA, responseObserver);
    }

    /**
     */
    public void listStorageHandlers(org.vitrivr.adampro.grpc.AdamGrpc.EmptyMessage request,
        io.grpc.stub.StreamObserver<org.vitrivr.adampro.grpc.AdamGrpc.StorageHandlersMessage> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_LIST_STORAGE_HANDLERS, responseObserver);
    }

    /**
     * <pre>
     *TODO: register storage handlers
     * </pre>
     */
    public void transferStorageHandler(org.vitrivr.adampro.grpc.AdamGrpc.TransferStorageHandlerMessage request,
        io.grpc.stub.StreamObserver<org.vitrivr.adampro.grpc.AdamGrpc.AckMessage> responseObserver) {
      asyncUnimplementedUnaryCall(METHOD_TRANSFER_STORAGE_HANDLER, responseObserver);
    }

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
          .addMethod(
            METHOD_CREATE_ENTITY,
            asyncUnaryCall(
              new MethodHandlers<
                org.vitrivr.adampro.grpc.AdamGrpc.CreateEntityMessage,
                org.vitrivr.adampro.grpc.AdamGrpc.AckMessage>(
                  this, METHODID_CREATE_ENTITY)))
          .addMethod(
            METHOD_AVAILABLE_ATTRIBUTE_TYPES,
            asyncUnaryCall(
              new MethodHandlers<
                org.vitrivr.adampro.grpc.AdamGrpc.EmptyMessage,
                org.vitrivr.adampro.grpc.AdamGrpc.AvailableAttributeTypesMessage>(
                  this, METHODID_AVAILABLE_ATTRIBUTE_TYPES)))
          .addMethod(
            METHOD_COUNT,
            asyncUnaryCall(
              new MethodHandlers<
                org.vitrivr.adampro.grpc.AdamGrpc.EntityNameMessage,
                org.vitrivr.adampro.grpc.AdamGrpc.AckMessage>(
                  this, METHODID_COUNT)))
          .addMethod(
            METHOD_DROP_ENTITY,
            asyncUnaryCall(
              new MethodHandlers<
                org.vitrivr.adampro.grpc.AdamGrpc.EntityNameMessage,
                org.vitrivr.adampro.grpc.AdamGrpc.AckMessage>(
                  this, METHODID_DROP_ENTITY)))
          .addMethod(
            METHOD_EXISTS_ENTITY,
            asyncUnaryCall(
              new MethodHandlers<
                org.vitrivr.adampro.grpc.AdamGrpc.EntityNameMessage,
                org.vitrivr.adampro.grpc.AdamGrpc.ExistsMessage>(
                  this, METHODID_EXISTS_ENTITY)))
          .addMethod(
            METHOD_INSERT,
            asyncUnaryCall(
              new MethodHandlers<
                org.vitrivr.adampro.grpc.AdamGrpc.InsertMessage,
                org.vitrivr.adampro.grpc.AdamGrpc.AckMessage>(
                  this, METHODID_INSERT)))
          .addMethod(
            METHOD_STREAM_INSERT,
            asyncBidiStreamingCall(
              new MethodHandlers<
                org.vitrivr.adampro.grpc.AdamGrpc.InsertMessage,
                org.vitrivr.adampro.grpc.AdamGrpc.AckMessage>(
                  this, METHODID_STREAM_INSERT)))
          .addMethod(
            METHOD_VACUUM_ENTITY,
            asyncUnaryCall(
              new MethodHandlers<
                org.vitrivr.adampro.grpc.AdamGrpc.EntityNameMessage,
                org.vitrivr.adampro.grpc.AdamGrpc.AckMessage>(
                  this, METHODID_VACUUM_ENTITY)))
          .addMethod(
            METHOD_DELETE,
            asyncUnaryCall(
              new MethodHandlers<
                org.vitrivr.adampro.grpc.AdamGrpc.DeleteMessage,
                org.vitrivr.adampro.grpc.AdamGrpc.AckMessage>(
                  this, METHODID_DELETE)))
          .addMethod(
            METHOD_INDEX,
            asyncUnaryCall(
              new MethodHandlers<
                org.vitrivr.adampro.grpc.AdamGrpc.IndexMessage,
                org.vitrivr.adampro.grpc.AdamGrpc.AckMessage>(
                  this, METHODID_INDEX)))
          .addMethod(
            METHOD_GENERATE_ALL_INDEXES,
            asyncUnaryCall(
              new MethodHandlers<
                org.vitrivr.adampro.grpc.AdamGrpc.IndexMessage,
                org.vitrivr.adampro.grpc.AdamGrpc.AckMessage>(
                  this, METHODID_GENERATE_ALL_INDEXES)))
          .addMethod(
            METHOD_EXISTS_INDEX,
            asyncUnaryCall(
              new MethodHandlers<
                org.vitrivr.adampro.grpc.AdamGrpc.IndexExistsMessage,
                org.vitrivr.adampro.grpc.AdamGrpc.ExistsMessage>(
                  this, METHODID_EXISTS_INDEX)))
          .addMethod(
            METHOD_DROP_INDEX,
            asyncUnaryCall(
              new MethodHandlers<
                org.vitrivr.adampro.grpc.AdamGrpc.IndexNameMessage,
                org.vitrivr.adampro.grpc.AdamGrpc.AckMessage>(
                  this, METHODID_DROP_INDEX)))
          .addMethod(
            METHOD_LIST_INDEXES,
            asyncUnaryCall(
              new MethodHandlers<
                org.vitrivr.adampro.grpc.AdamGrpc.EntityNameMessage,
                org.vitrivr.adampro.grpc.AdamGrpc.IndexesMessage>(
                  this, METHODID_LIST_INDEXES)))
          .addMethod(
            METHOD_GENERATE_RANDOM_DATA,
            asyncUnaryCall(
              new MethodHandlers<
                org.vitrivr.adampro.grpc.AdamGrpc.GenerateRandomDataMessage,
                org.vitrivr.adampro.grpc.AdamGrpc.AckMessage>(
                  this, METHODID_GENERATE_RANDOM_DATA)))
          .addMethod(
            METHOD_LIST_ENTITIES,
            asyncUnaryCall(
              new MethodHandlers<
                org.vitrivr.adampro.grpc.AdamGrpc.EmptyMessage,
                org.vitrivr.adampro.grpc.AdamGrpc.EntitiesMessage>(
                  this, METHODID_LIST_ENTITIES)))
          .addMethod(
            METHOD_GET_ENTITY_PROPERTIES,
            asyncUnaryCall(
              new MethodHandlers<
                org.vitrivr.adampro.grpc.AdamGrpc.EntityPropertiesMessage,
                org.vitrivr.adampro.grpc.AdamGrpc.PropertiesMessage>(
                  this, METHODID_GET_ENTITY_PROPERTIES)))
          .addMethod(
            METHOD_GET_ATTRIBUTE_PROPERTIES,
            asyncUnaryCall(
              new MethodHandlers<
                org.vitrivr.adampro.grpc.AdamGrpc.AttributePropertiesMessage,
                org.vitrivr.adampro.grpc.AdamGrpc.PropertiesMessage>(
                  this, METHODID_GET_ATTRIBUTE_PROPERTIES)))
          .addMethod(
            METHOD_GET_INDEX_PROPERTIES,
            asyncUnaryCall(
              new MethodHandlers<
                org.vitrivr.adampro.grpc.AdamGrpc.IndexPropertiesMessage,
                org.vitrivr.adampro.grpc.AdamGrpc.PropertiesMessage>(
                  this, METHODID_GET_INDEX_PROPERTIES)))
          .addMethod(
            METHOD_REPARTITION_ENTITY_DATA,
            asyncUnaryCall(
              new MethodHandlers<
                org.vitrivr.adampro.grpc.AdamGrpc.RepartitionMessage,
                org.vitrivr.adampro.grpc.AdamGrpc.AckMessage>(
                  this, METHODID_REPARTITION_ENTITY_DATA)))
          .addMethod(
            METHOD_REPARTITION_INDEX_DATA,
            asyncUnaryCall(
              new MethodHandlers<
                org.vitrivr.adampro.grpc.AdamGrpc.RepartitionMessage,
                org.vitrivr.adampro.grpc.AdamGrpc.AckMessage>(
                  this, METHODID_REPARTITION_INDEX_DATA)))
          .addMethod(
            METHOD_ADAPT_SCAN_METHODS,
            asyncUnaryCall(
              new MethodHandlers<
                org.vitrivr.adampro.grpc.AdamGrpc.AdaptScanMethodsMessage,
                org.vitrivr.adampro.grpc.AdamGrpc.AckMessage>(
                  this, METHODID_ADAPT_SCAN_METHODS)))
          .addMethod(
            METHOD_SPARSIFY_ENTITY,
            asyncUnaryCall(
              new MethodHandlers<
                org.vitrivr.adampro.grpc.AdamGrpc.SparsifyEntityMessage,
                org.vitrivr.adampro.grpc.AdamGrpc.AckMessage>(
                  this, METHODID_SPARSIFY_ENTITY)))
          .addMethod(
            METHOD_IMPORT_DATA,
            asyncUnaryCall(
              new MethodHandlers<
                org.vitrivr.adampro.grpc.AdamGrpc.ImportMessage,
                org.vitrivr.adampro.grpc.AdamGrpc.AckMessage>(
                  this, METHODID_IMPORT_DATA)))
          .addMethod(
            METHOD_PROTO_IMPORT_DATA,
            asyncServerStreamingCall(
              new MethodHandlers<
                org.vitrivr.adampro.grpc.AdamGrpc.ProtoImportMessage,
                org.vitrivr.adampro.grpc.AdamGrpc.AckMessage>(
                  this, METHODID_PROTO_IMPORT_DATA)))
          .addMethod(
            METHOD_PROTO_EXPORT_DATA,
            asyncUnaryCall(
              new MethodHandlers<
                org.vitrivr.adampro.grpc.AdamGrpc.ProtoExportMessage,
                org.vitrivr.adampro.grpc.AdamGrpc.AckMessage>(
                  this, METHODID_PROTO_EXPORT_DATA)))
          .addMethod(
            METHOD_LIST_STORAGE_HANDLERS,
            asyncUnaryCall(
              new MethodHandlers<
                org.vitrivr.adampro.grpc.AdamGrpc.EmptyMessage,
                org.vitrivr.adampro.grpc.AdamGrpc.StorageHandlersMessage>(
                  this, METHODID_LIST_STORAGE_HANDLERS)))
          .addMethod(
            METHOD_TRANSFER_STORAGE_HANDLER,
            asyncUnaryCall(
              new MethodHandlers<
                org.vitrivr.adampro.grpc.AdamGrpc.TransferStorageHandlerMessage,
                org.vitrivr.adampro.grpc.AdamGrpc.AckMessage>(
                  this, METHODID_TRANSFER_STORAGE_HANDLER)))
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
    public void createEntity(org.vitrivr.adampro.grpc.AdamGrpc.CreateEntityMessage request,
        io.grpc.stub.StreamObserver<org.vitrivr.adampro.grpc.AdamGrpc.AckMessage> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_CREATE_ENTITY, getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void availableAttributeTypes(org.vitrivr.adampro.grpc.AdamGrpc.EmptyMessage request,
        io.grpc.stub.StreamObserver<org.vitrivr.adampro.grpc.AdamGrpc.AvailableAttributeTypesMessage> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_AVAILABLE_ATTRIBUTE_TYPES, getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void count(org.vitrivr.adampro.grpc.AdamGrpc.EntityNameMessage request,
        io.grpc.stub.StreamObserver<org.vitrivr.adampro.grpc.AdamGrpc.AckMessage> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_COUNT, getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void dropEntity(org.vitrivr.adampro.grpc.AdamGrpc.EntityNameMessage request,
        io.grpc.stub.StreamObserver<org.vitrivr.adampro.grpc.AdamGrpc.AckMessage> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_DROP_ENTITY, getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void existsEntity(org.vitrivr.adampro.grpc.AdamGrpc.EntityNameMessage request,
        io.grpc.stub.StreamObserver<org.vitrivr.adampro.grpc.AdamGrpc.ExistsMessage> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_EXISTS_ENTITY, getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void insert(org.vitrivr.adampro.grpc.AdamGrpc.InsertMessage request,
        io.grpc.stub.StreamObserver<org.vitrivr.adampro.grpc.AdamGrpc.AckMessage> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_INSERT, getCallOptions()), request, responseObserver);
    }

    /**
     */
    public io.grpc.stub.StreamObserver<org.vitrivr.adampro.grpc.AdamGrpc.InsertMessage> streamInsert(
        io.grpc.stub.StreamObserver<org.vitrivr.adampro.grpc.AdamGrpc.AckMessage> responseObserver) {
      return asyncBidiStreamingCall(
          getChannel().newCall(METHOD_STREAM_INSERT, getCallOptions()), responseObserver);
    }

    /**
     */
    public void vacuumEntity(org.vitrivr.adampro.grpc.AdamGrpc.EntityNameMessage request,
        io.grpc.stub.StreamObserver<org.vitrivr.adampro.grpc.AdamGrpc.AckMessage> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_VACUUM_ENTITY, getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void delete(org.vitrivr.adampro.grpc.AdamGrpc.DeleteMessage request,
        io.grpc.stub.StreamObserver<org.vitrivr.adampro.grpc.AdamGrpc.AckMessage> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_DELETE, getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     *creates an index on the data
     * </pre>
     */
    public void index(org.vitrivr.adampro.grpc.AdamGrpc.IndexMessage request,
        io.grpc.stub.StreamObserver<org.vitrivr.adampro.grpc.AdamGrpc.AckMessage> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_INDEX, getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void generateAllIndexes(org.vitrivr.adampro.grpc.AdamGrpc.IndexMessage request,
        io.grpc.stub.StreamObserver<org.vitrivr.adampro.grpc.AdamGrpc.AckMessage> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_GENERATE_ALL_INDEXES, getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void existsIndex(org.vitrivr.adampro.grpc.AdamGrpc.IndexExistsMessage request,
        io.grpc.stub.StreamObserver<org.vitrivr.adampro.grpc.AdamGrpc.ExistsMessage> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_EXISTS_INDEX, getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void dropIndex(org.vitrivr.adampro.grpc.AdamGrpc.IndexNameMessage request,
        io.grpc.stub.StreamObserver<org.vitrivr.adampro.grpc.AdamGrpc.AckMessage> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_DROP_INDEX, getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void listIndexes(org.vitrivr.adampro.grpc.AdamGrpc.EntityNameMessage request,
        io.grpc.stub.StreamObserver<org.vitrivr.adampro.grpc.AdamGrpc.IndexesMessage> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_LIST_INDEXES, getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     *generates an entity with random data and with all available indexes (for demo purposes)
     * </pre>
     */
    public void generateRandomData(org.vitrivr.adampro.grpc.AdamGrpc.GenerateRandomDataMessage request,
        io.grpc.stub.StreamObserver<org.vitrivr.adampro.grpc.AdamGrpc.AckMessage> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_GENERATE_RANDOM_DATA, getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void listEntities(org.vitrivr.adampro.grpc.AdamGrpc.EmptyMessage request,
        io.grpc.stub.StreamObserver<org.vitrivr.adampro.grpc.AdamGrpc.EntitiesMessage> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_LIST_ENTITIES, getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void getEntityProperties(org.vitrivr.adampro.grpc.AdamGrpc.EntityPropertiesMessage request,
        io.grpc.stub.StreamObserver<org.vitrivr.adampro.grpc.AdamGrpc.PropertiesMessage> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_GET_ENTITY_PROPERTIES, getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void getAttributeProperties(org.vitrivr.adampro.grpc.AdamGrpc.AttributePropertiesMessage request,
        io.grpc.stub.StreamObserver<org.vitrivr.adampro.grpc.AdamGrpc.PropertiesMessage> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_GET_ATTRIBUTE_PROPERTIES, getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void getIndexProperties(org.vitrivr.adampro.grpc.AdamGrpc.IndexPropertiesMessage request,
        io.grpc.stub.StreamObserver<org.vitrivr.adampro.grpc.AdamGrpc.PropertiesMessage> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_GET_INDEX_PROPERTIES, getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void repartitionEntityData(org.vitrivr.adampro.grpc.AdamGrpc.RepartitionMessage request,
        io.grpc.stub.StreamObserver<org.vitrivr.adampro.grpc.AdamGrpc.AckMessage> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_REPARTITION_ENTITY_DATA, getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void repartitionIndexData(org.vitrivr.adampro.grpc.AdamGrpc.RepartitionMessage request,
        io.grpc.stub.StreamObserver<org.vitrivr.adampro.grpc.AdamGrpc.AckMessage> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_REPARTITION_INDEX_DATA, getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void adaptScanMethods(org.vitrivr.adampro.grpc.AdamGrpc.AdaptScanMethodsMessage request,
        io.grpc.stub.StreamObserver<org.vitrivr.adampro.grpc.AdamGrpc.AckMessage> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_ADAPT_SCAN_METHODS, getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void sparsifyEntity(org.vitrivr.adampro.grpc.AdamGrpc.SparsifyEntityMessage request,
        io.grpc.stub.StreamObserver<org.vitrivr.adampro.grpc.AdamGrpc.AckMessage> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_SPARSIFY_ENTITY, getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void importData(org.vitrivr.adampro.grpc.AdamGrpc.ImportMessage request,
        io.grpc.stub.StreamObserver<org.vitrivr.adampro.grpc.AdamGrpc.AckMessage> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_IMPORT_DATA, getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void protoImportData(org.vitrivr.adampro.grpc.AdamGrpc.ProtoImportMessage request,
        io.grpc.stub.StreamObserver<org.vitrivr.adampro.grpc.AdamGrpc.AckMessage> responseObserver) {
      asyncServerStreamingCall(
          getChannel().newCall(METHOD_PROTO_IMPORT_DATA, getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void protoExportData(org.vitrivr.adampro.grpc.AdamGrpc.ProtoExportMessage request,
        io.grpc.stub.StreamObserver<org.vitrivr.adampro.grpc.AdamGrpc.AckMessage> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_PROTO_EXPORT_DATA, getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void listStorageHandlers(org.vitrivr.adampro.grpc.AdamGrpc.EmptyMessage request,
        io.grpc.stub.StreamObserver<org.vitrivr.adampro.grpc.AdamGrpc.StorageHandlersMessage> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_LIST_STORAGE_HANDLERS, getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     *TODO: register storage handlers
     * </pre>
     */
    public void transferStorageHandler(org.vitrivr.adampro.grpc.AdamGrpc.TransferStorageHandlerMessage request,
        io.grpc.stub.StreamObserver<org.vitrivr.adampro.grpc.AdamGrpc.AckMessage> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_TRANSFER_STORAGE_HANDLER, getCallOptions()), request, responseObserver);
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
    public org.vitrivr.adampro.grpc.AdamGrpc.AckMessage createEntity(org.vitrivr.adampro.grpc.AdamGrpc.CreateEntityMessage request) {
      return blockingUnaryCall(
          getChannel(), METHOD_CREATE_ENTITY, getCallOptions(), request);
    }

    /**
     */
    public org.vitrivr.adampro.grpc.AdamGrpc.AvailableAttributeTypesMessage availableAttributeTypes(org.vitrivr.adampro.grpc.AdamGrpc.EmptyMessage request) {
      return blockingUnaryCall(
          getChannel(), METHOD_AVAILABLE_ATTRIBUTE_TYPES, getCallOptions(), request);
    }

    /**
     */
    public org.vitrivr.adampro.grpc.AdamGrpc.AckMessage count(org.vitrivr.adampro.grpc.AdamGrpc.EntityNameMessage request) {
      return blockingUnaryCall(
          getChannel(), METHOD_COUNT, getCallOptions(), request);
    }

    /**
     */
    public org.vitrivr.adampro.grpc.AdamGrpc.AckMessage dropEntity(org.vitrivr.adampro.grpc.AdamGrpc.EntityNameMessage request) {
      return blockingUnaryCall(
          getChannel(), METHOD_DROP_ENTITY, getCallOptions(), request);
    }

    /**
     */
    public org.vitrivr.adampro.grpc.AdamGrpc.ExistsMessage existsEntity(org.vitrivr.adampro.grpc.AdamGrpc.EntityNameMessage request) {
      return blockingUnaryCall(
          getChannel(), METHOD_EXISTS_ENTITY, getCallOptions(), request);
    }

    /**
     */
    public org.vitrivr.adampro.grpc.AdamGrpc.AckMessage insert(org.vitrivr.adampro.grpc.AdamGrpc.InsertMessage request) {
      return blockingUnaryCall(
          getChannel(), METHOD_INSERT, getCallOptions(), request);
    }

    /**
     */
    public org.vitrivr.adampro.grpc.AdamGrpc.AckMessage vacuumEntity(org.vitrivr.adampro.grpc.AdamGrpc.EntityNameMessage request) {
      return blockingUnaryCall(
          getChannel(), METHOD_VACUUM_ENTITY, getCallOptions(), request);
    }

    /**
     */
    public org.vitrivr.adampro.grpc.AdamGrpc.AckMessage delete(org.vitrivr.adampro.grpc.AdamGrpc.DeleteMessage request) {
      return blockingUnaryCall(
          getChannel(), METHOD_DELETE, getCallOptions(), request);
    }

    /**
     * <pre>
     *creates an index on the data
     * </pre>
     */
    public org.vitrivr.adampro.grpc.AdamGrpc.AckMessage index(org.vitrivr.adampro.grpc.AdamGrpc.IndexMessage request) {
      return blockingUnaryCall(
          getChannel(), METHOD_INDEX, getCallOptions(), request);
    }

    /**
     */
    public org.vitrivr.adampro.grpc.AdamGrpc.AckMessage generateAllIndexes(org.vitrivr.adampro.grpc.AdamGrpc.IndexMessage request) {
      return blockingUnaryCall(
          getChannel(), METHOD_GENERATE_ALL_INDEXES, getCallOptions(), request);
    }

    /**
     */
    public org.vitrivr.adampro.grpc.AdamGrpc.ExistsMessage existsIndex(org.vitrivr.adampro.grpc.AdamGrpc.IndexExistsMessage request) {
      return blockingUnaryCall(
          getChannel(), METHOD_EXISTS_INDEX, getCallOptions(), request);
    }

    /**
     */
    public org.vitrivr.adampro.grpc.AdamGrpc.AckMessage dropIndex(org.vitrivr.adampro.grpc.AdamGrpc.IndexNameMessage request) {
      return blockingUnaryCall(
          getChannel(), METHOD_DROP_INDEX, getCallOptions(), request);
    }

    /**
     */
    public org.vitrivr.adampro.grpc.AdamGrpc.IndexesMessage listIndexes(org.vitrivr.adampro.grpc.AdamGrpc.EntityNameMessage request) {
      return blockingUnaryCall(
          getChannel(), METHOD_LIST_INDEXES, getCallOptions(), request);
    }

    /**
     * <pre>
     *generates an entity with random data and with all available indexes (for demo purposes)
     * </pre>
     */
    public org.vitrivr.adampro.grpc.AdamGrpc.AckMessage generateRandomData(org.vitrivr.adampro.grpc.AdamGrpc.GenerateRandomDataMessage request) {
      return blockingUnaryCall(
          getChannel(), METHOD_GENERATE_RANDOM_DATA, getCallOptions(), request);
    }

    /**
     */
    public org.vitrivr.adampro.grpc.AdamGrpc.EntitiesMessage listEntities(org.vitrivr.adampro.grpc.AdamGrpc.EmptyMessage request) {
      return blockingUnaryCall(
          getChannel(), METHOD_LIST_ENTITIES, getCallOptions(), request);
    }

    /**
     */
    public org.vitrivr.adampro.grpc.AdamGrpc.PropertiesMessage getEntityProperties(org.vitrivr.adampro.grpc.AdamGrpc.EntityPropertiesMessage request) {
      return blockingUnaryCall(
          getChannel(), METHOD_GET_ENTITY_PROPERTIES, getCallOptions(), request);
    }

    /**
     */
    public org.vitrivr.adampro.grpc.AdamGrpc.PropertiesMessage getAttributeProperties(org.vitrivr.adampro.grpc.AdamGrpc.AttributePropertiesMessage request) {
      return blockingUnaryCall(
          getChannel(), METHOD_GET_ATTRIBUTE_PROPERTIES, getCallOptions(), request);
    }

    /**
     */
    public org.vitrivr.adampro.grpc.AdamGrpc.PropertiesMessage getIndexProperties(org.vitrivr.adampro.grpc.AdamGrpc.IndexPropertiesMessage request) {
      return blockingUnaryCall(
          getChannel(), METHOD_GET_INDEX_PROPERTIES, getCallOptions(), request);
    }

    /**
     */
    public org.vitrivr.adampro.grpc.AdamGrpc.AckMessage repartitionEntityData(org.vitrivr.adampro.grpc.AdamGrpc.RepartitionMessage request) {
      return blockingUnaryCall(
          getChannel(), METHOD_REPARTITION_ENTITY_DATA, getCallOptions(), request);
    }

    /**
     */
    public org.vitrivr.adampro.grpc.AdamGrpc.AckMessage repartitionIndexData(org.vitrivr.adampro.grpc.AdamGrpc.RepartitionMessage request) {
      return blockingUnaryCall(
          getChannel(), METHOD_REPARTITION_INDEX_DATA, getCallOptions(), request);
    }

    /**
     */
    public org.vitrivr.adampro.grpc.AdamGrpc.AckMessage adaptScanMethods(org.vitrivr.adampro.grpc.AdamGrpc.AdaptScanMethodsMessage request) {
      return blockingUnaryCall(
          getChannel(), METHOD_ADAPT_SCAN_METHODS, getCallOptions(), request);
    }

    /**
     */
    public org.vitrivr.adampro.grpc.AdamGrpc.AckMessage sparsifyEntity(org.vitrivr.adampro.grpc.AdamGrpc.SparsifyEntityMessage request) {
      return blockingUnaryCall(
          getChannel(), METHOD_SPARSIFY_ENTITY, getCallOptions(), request);
    }

    /**
     */
    public org.vitrivr.adampro.grpc.AdamGrpc.AckMessage importData(org.vitrivr.adampro.grpc.AdamGrpc.ImportMessage request) {
      return blockingUnaryCall(
          getChannel(), METHOD_IMPORT_DATA, getCallOptions(), request);
    }

    /**
     */
    public java.util.Iterator<org.vitrivr.adampro.grpc.AdamGrpc.AckMessage> protoImportData(
        org.vitrivr.adampro.grpc.AdamGrpc.ProtoImportMessage request) {
      return blockingServerStreamingCall(
          getChannel(), METHOD_PROTO_IMPORT_DATA, getCallOptions(), request);
    }

    /**
     */
    public org.vitrivr.adampro.grpc.AdamGrpc.AckMessage protoExportData(org.vitrivr.adampro.grpc.AdamGrpc.ProtoExportMessage request) {
      return blockingUnaryCall(
          getChannel(), METHOD_PROTO_EXPORT_DATA, getCallOptions(), request);
    }

    /**
     */
    public org.vitrivr.adampro.grpc.AdamGrpc.StorageHandlersMessage listStorageHandlers(org.vitrivr.adampro.grpc.AdamGrpc.EmptyMessage request) {
      return blockingUnaryCall(
          getChannel(), METHOD_LIST_STORAGE_HANDLERS, getCallOptions(), request);
    }

    /**
     * <pre>
     *TODO: register storage handlers
     * </pre>
     */
    public org.vitrivr.adampro.grpc.AdamGrpc.AckMessage transferStorageHandler(org.vitrivr.adampro.grpc.AdamGrpc.TransferStorageHandlerMessage request) {
      return blockingUnaryCall(
          getChannel(), METHOD_TRANSFER_STORAGE_HANDLER, getCallOptions(), request);
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
    public com.google.common.util.concurrent.ListenableFuture<org.vitrivr.adampro.grpc.AdamGrpc.AckMessage> createEntity(
        org.vitrivr.adampro.grpc.AdamGrpc.CreateEntityMessage request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_CREATE_ENTITY, getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<org.vitrivr.adampro.grpc.AdamGrpc.AvailableAttributeTypesMessage> availableAttributeTypes(
        org.vitrivr.adampro.grpc.AdamGrpc.EmptyMessage request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_AVAILABLE_ATTRIBUTE_TYPES, getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<org.vitrivr.adampro.grpc.AdamGrpc.AckMessage> count(
        org.vitrivr.adampro.grpc.AdamGrpc.EntityNameMessage request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_COUNT, getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<org.vitrivr.adampro.grpc.AdamGrpc.AckMessage> dropEntity(
        org.vitrivr.adampro.grpc.AdamGrpc.EntityNameMessage request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_DROP_ENTITY, getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<org.vitrivr.adampro.grpc.AdamGrpc.ExistsMessage> existsEntity(
        org.vitrivr.adampro.grpc.AdamGrpc.EntityNameMessage request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_EXISTS_ENTITY, getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<org.vitrivr.adampro.grpc.AdamGrpc.AckMessage> insert(
        org.vitrivr.adampro.grpc.AdamGrpc.InsertMessage request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_INSERT, getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<org.vitrivr.adampro.grpc.AdamGrpc.AckMessage> vacuumEntity(
        org.vitrivr.adampro.grpc.AdamGrpc.EntityNameMessage request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_VACUUM_ENTITY, getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<org.vitrivr.adampro.grpc.AdamGrpc.AckMessage> delete(
        org.vitrivr.adampro.grpc.AdamGrpc.DeleteMessage request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_DELETE, getCallOptions()), request);
    }

    /**
     * <pre>
     *creates an index on the data
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<org.vitrivr.adampro.grpc.AdamGrpc.AckMessage> index(
        org.vitrivr.adampro.grpc.AdamGrpc.IndexMessage request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_INDEX, getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<org.vitrivr.adampro.grpc.AdamGrpc.AckMessage> generateAllIndexes(
        org.vitrivr.adampro.grpc.AdamGrpc.IndexMessage request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_GENERATE_ALL_INDEXES, getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<org.vitrivr.adampro.grpc.AdamGrpc.ExistsMessage> existsIndex(
        org.vitrivr.adampro.grpc.AdamGrpc.IndexExistsMessage request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_EXISTS_INDEX, getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<org.vitrivr.adampro.grpc.AdamGrpc.AckMessage> dropIndex(
        org.vitrivr.adampro.grpc.AdamGrpc.IndexNameMessage request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_DROP_INDEX, getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<org.vitrivr.adampro.grpc.AdamGrpc.IndexesMessage> listIndexes(
        org.vitrivr.adampro.grpc.AdamGrpc.EntityNameMessage request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_LIST_INDEXES, getCallOptions()), request);
    }

    /**
     * <pre>
     *generates an entity with random data and with all available indexes (for demo purposes)
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<org.vitrivr.adampro.grpc.AdamGrpc.AckMessage> generateRandomData(
        org.vitrivr.adampro.grpc.AdamGrpc.GenerateRandomDataMessage request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_GENERATE_RANDOM_DATA, getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<org.vitrivr.adampro.grpc.AdamGrpc.EntitiesMessage> listEntities(
        org.vitrivr.adampro.grpc.AdamGrpc.EmptyMessage request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_LIST_ENTITIES, getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<org.vitrivr.adampro.grpc.AdamGrpc.PropertiesMessage> getEntityProperties(
        org.vitrivr.adampro.grpc.AdamGrpc.EntityPropertiesMessage request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_GET_ENTITY_PROPERTIES, getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<org.vitrivr.adampro.grpc.AdamGrpc.PropertiesMessage> getAttributeProperties(
        org.vitrivr.adampro.grpc.AdamGrpc.AttributePropertiesMessage request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_GET_ATTRIBUTE_PROPERTIES, getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<org.vitrivr.adampro.grpc.AdamGrpc.PropertiesMessage> getIndexProperties(
        org.vitrivr.adampro.grpc.AdamGrpc.IndexPropertiesMessage request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_GET_INDEX_PROPERTIES, getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<org.vitrivr.adampro.grpc.AdamGrpc.AckMessage> repartitionEntityData(
        org.vitrivr.adampro.grpc.AdamGrpc.RepartitionMessage request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_REPARTITION_ENTITY_DATA, getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<org.vitrivr.adampro.grpc.AdamGrpc.AckMessage> repartitionIndexData(
        org.vitrivr.adampro.grpc.AdamGrpc.RepartitionMessage request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_REPARTITION_INDEX_DATA, getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<org.vitrivr.adampro.grpc.AdamGrpc.AckMessage> adaptScanMethods(
        org.vitrivr.adampro.grpc.AdamGrpc.AdaptScanMethodsMessage request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_ADAPT_SCAN_METHODS, getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<org.vitrivr.adampro.grpc.AdamGrpc.AckMessage> sparsifyEntity(
        org.vitrivr.adampro.grpc.AdamGrpc.SparsifyEntityMessage request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_SPARSIFY_ENTITY, getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<org.vitrivr.adampro.grpc.AdamGrpc.AckMessage> importData(
        org.vitrivr.adampro.grpc.AdamGrpc.ImportMessage request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_IMPORT_DATA, getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<org.vitrivr.adampro.grpc.AdamGrpc.AckMessage> protoExportData(
        org.vitrivr.adampro.grpc.AdamGrpc.ProtoExportMessage request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_PROTO_EXPORT_DATA, getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<org.vitrivr.adampro.grpc.AdamGrpc.StorageHandlersMessage> listStorageHandlers(
        org.vitrivr.adampro.grpc.AdamGrpc.EmptyMessage request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_LIST_STORAGE_HANDLERS, getCallOptions()), request);
    }

    /**
     * <pre>
     *TODO: register storage handlers
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<org.vitrivr.adampro.grpc.AdamGrpc.AckMessage> transferStorageHandler(
        org.vitrivr.adampro.grpc.AdamGrpc.TransferStorageHandlerMessage request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_TRANSFER_STORAGE_HANDLER, getCallOptions()), request);
    }
  }

  private static final int METHODID_CREATE_ENTITY = 0;
  private static final int METHODID_AVAILABLE_ATTRIBUTE_TYPES = 1;
  private static final int METHODID_COUNT = 2;
  private static final int METHODID_DROP_ENTITY = 3;
  private static final int METHODID_EXISTS_ENTITY = 4;
  private static final int METHODID_INSERT = 5;
  private static final int METHODID_VACUUM_ENTITY = 6;
  private static final int METHODID_DELETE = 7;
  private static final int METHODID_INDEX = 8;
  private static final int METHODID_GENERATE_ALL_INDEXES = 9;
  private static final int METHODID_EXISTS_INDEX = 10;
  private static final int METHODID_DROP_INDEX = 11;
  private static final int METHODID_LIST_INDEXES = 12;
  private static final int METHODID_GENERATE_RANDOM_DATA = 13;
  private static final int METHODID_LIST_ENTITIES = 14;
  private static final int METHODID_GET_ENTITY_PROPERTIES = 15;
  private static final int METHODID_GET_ATTRIBUTE_PROPERTIES = 16;
  private static final int METHODID_GET_INDEX_PROPERTIES = 17;
  private static final int METHODID_REPARTITION_ENTITY_DATA = 18;
  private static final int METHODID_REPARTITION_INDEX_DATA = 19;
  private static final int METHODID_ADAPT_SCAN_METHODS = 20;
  private static final int METHODID_SPARSIFY_ENTITY = 21;
  private static final int METHODID_IMPORT_DATA = 22;
  private static final int METHODID_PROTO_IMPORT_DATA = 23;
  private static final int METHODID_PROTO_EXPORT_DATA = 24;
  private static final int METHODID_LIST_STORAGE_HANDLERS = 25;
  private static final int METHODID_TRANSFER_STORAGE_HANDLER = 26;
  private static final int METHODID_STREAM_INSERT = 27;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final AdamDefinitionImplBase serviceImpl;
    private final int methodId;

    MethodHandlers(AdamDefinitionImplBase serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_CREATE_ENTITY:
          serviceImpl.createEntity((org.vitrivr.adampro.grpc.AdamGrpc.CreateEntityMessage) request,
              (io.grpc.stub.StreamObserver<org.vitrivr.adampro.grpc.AdamGrpc.AckMessage>) responseObserver);
          break;
        case METHODID_AVAILABLE_ATTRIBUTE_TYPES:
          serviceImpl.availableAttributeTypes((org.vitrivr.adampro.grpc.AdamGrpc.EmptyMessage) request,
              (io.grpc.stub.StreamObserver<org.vitrivr.adampro.grpc.AdamGrpc.AvailableAttributeTypesMessage>) responseObserver);
          break;
        case METHODID_COUNT:
          serviceImpl.count((org.vitrivr.adampro.grpc.AdamGrpc.EntityNameMessage) request,
              (io.grpc.stub.StreamObserver<org.vitrivr.adampro.grpc.AdamGrpc.AckMessage>) responseObserver);
          break;
        case METHODID_DROP_ENTITY:
          serviceImpl.dropEntity((org.vitrivr.adampro.grpc.AdamGrpc.EntityNameMessage) request,
              (io.grpc.stub.StreamObserver<org.vitrivr.adampro.grpc.AdamGrpc.AckMessage>) responseObserver);
          break;
        case METHODID_EXISTS_ENTITY:
          serviceImpl.existsEntity((org.vitrivr.adampro.grpc.AdamGrpc.EntityNameMessage) request,
              (io.grpc.stub.StreamObserver<org.vitrivr.adampro.grpc.AdamGrpc.ExistsMessage>) responseObserver);
          break;
        case METHODID_INSERT:
          serviceImpl.insert((org.vitrivr.adampro.grpc.AdamGrpc.InsertMessage) request,
              (io.grpc.stub.StreamObserver<org.vitrivr.adampro.grpc.AdamGrpc.AckMessage>) responseObserver);
          break;
        case METHODID_VACUUM_ENTITY:
          serviceImpl.vacuumEntity((org.vitrivr.adampro.grpc.AdamGrpc.EntityNameMessage) request,
              (io.grpc.stub.StreamObserver<org.vitrivr.adampro.grpc.AdamGrpc.AckMessage>) responseObserver);
          break;
        case METHODID_DELETE:
          serviceImpl.delete((org.vitrivr.adampro.grpc.AdamGrpc.DeleteMessage) request,
              (io.grpc.stub.StreamObserver<org.vitrivr.adampro.grpc.AdamGrpc.AckMessage>) responseObserver);
          break;
        case METHODID_INDEX:
          serviceImpl.index((org.vitrivr.adampro.grpc.AdamGrpc.IndexMessage) request,
              (io.grpc.stub.StreamObserver<org.vitrivr.adampro.grpc.AdamGrpc.AckMessage>) responseObserver);
          break;
        case METHODID_GENERATE_ALL_INDEXES:
          serviceImpl.generateAllIndexes((org.vitrivr.adampro.grpc.AdamGrpc.IndexMessage) request,
              (io.grpc.stub.StreamObserver<org.vitrivr.adampro.grpc.AdamGrpc.AckMessage>) responseObserver);
          break;
        case METHODID_EXISTS_INDEX:
          serviceImpl.existsIndex((org.vitrivr.adampro.grpc.AdamGrpc.IndexExistsMessage) request,
              (io.grpc.stub.StreamObserver<org.vitrivr.adampro.grpc.AdamGrpc.ExistsMessage>) responseObserver);
          break;
        case METHODID_DROP_INDEX:
          serviceImpl.dropIndex((org.vitrivr.adampro.grpc.AdamGrpc.IndexNameMessage) request,
              (io.grpc.stub.StreamObserver<org.vitrivr.adampro.grpc.AdamGrpc.AckMessage>) responseObserver);
          break;
        case METHODID_LIST_INDEXES:
          serviceImpl.listIndexes((org.vitrivr.adampro.grpc.AdamGrpc.EntityNameMessage) request,
              (io.grpc.stub.StreamObserver<org.vitrivr.adampro.grpc.AdamGrpc.IndexesMessage>) responseObserver);
          break;
        case METHODID_GENERATE_RANDOM_DATA:
          serviceImpl.generateRandomData((org.vitrivr.adampro.grpc.AdamGrpc.GenerateRandomDataMessage) request,
              (io.grpc.stub.StreamObserver<org.vitrivr.adampro.grpc.AdamGrpc.AckMessage>) responseObserver);
          break;
        case METHODID_LIST_ENTITIES:
          serviceImpl.listEntities((org.vitrivr.adampro.grpc.AdamGrpc.EmptyMessage) request,
              (io.grpc.stub.StreamObserver<org.vitrivr.adampro.grpc.AdamGrpc.EntitiesMessage>) responseObserver);
          break;
        case METHODID_GET_ENTITY_PROPERTIES:
          serviceImpl.getEntityProperties((org.vitrivr.adampro.grpc.AdamGrpc.EntityPropertiesMessage) request,
              (io.grpc.stub.StreamObserver<org.vitrivr.adampro.grpc.AdamGrpc.PropertiesMessage>) responseObserver);
          break;
        case METHODID_GET_ATTRIBUTE_PROPERTIES:
          serviceImpl.getAttributeProperties((org.vitrivr.adampro.grpc.AdamGrpc.AttributePropertiesMessage) request,
              (io.grpc.stub.StreamObserver<org.vitrivr.adampro.grpc.AdamGrpc.PropertiesMessage>) responseObserver);
          break;
        case METHODID_GET_INDEX_PROPERTIES:
          serviceImpl.getIndexProperties((org.vitrivr.adampro.grpc.AdamGrpc.IndexPropertiesMessage) request,
              (io.grpc.stub.StreamObserver<org.vitrivr.adampro.grpc.AdamGrpc.PropertiesMessage>) responseObserver);
          break;
        case METHODID_REPARTITION_ENTITY_DATA:
          serviceImpl.repartitionEntityData((org.vitrivr.adampro.grpc.AdamGrpc.RepartitionMessage) request,
              (io.grpc.stub.StreamObserver<org.vitrivr.adampro.grpc.AdamGrpc.AckMessage>) responseObserver);
          break;
        case METHODID_REPARTITION_INDEX_DATA:
          serviceImpl.repartitionIndexData((org.vitrivr.adampro.grpc.AdamGrpc.RepartitionMessage) request,
              (io.grpc.stub.StreamObserver<org.vitrivr.adampro.grpc.AdamGrpc.AckMessage>) responseObserver);
          break;
        case METHODID_ADAPT_SCAN_METHODS:
          serviceImpl.adaptScanMethods((org.vitrivr.adampro.grpc.AdamGrpc.AdaptScanMethodsMessage) request,
              (io.grpc.stub.StreamObserver<org.vitrivr.adampro.grpc.AdamGrpc.AckMessage>) responseObserver);
          break;
        case METHODID_SPARSIFY_ENTITY:
          serviceImpl.sparsifyEntity((org.vitrivr.adampro.grpc.AdamGrpc.SparsifyEntityMessage) request,
              (io.grpc.stub.StreamObserver<org.vitrivr.adampro.grpc.AdamGrpc.AckMessage>) responseObserver);
          break;
        case METHODID_IMPORT_DATA:
          serviceImpl.importData((org.vitrivr.adampro.grpc.AdamGrpc.ImportMessage) request,
              (io.grpc.stub.StreamObserver<org.vitrivr.adampro.grpc.AdamGrpc.AckMessage>) responseObserver);
          break;
        case METHODID_PROTO_IMPORT_DATA:
          serviceImpl.protoImportData((org.vitrivr.adampro.grpc.AdamGrpc.ProtoImportMessage) request,
              (io.grpc.stub.StreamObserver<org.vitrivr.adampro.grpc.AdamGrpc.AckMessage>) responseObserver);
          break;
        case METHODID_PROTO_EXPORT_DATA:
          serviceImpl.protoExportData((org.vitrivr.adampro.grpc.AdamGrpc.ProtoExportMessage) request,
              (io.grpc.stub.StreamObserver<org.vitrivr.adampro.grpc.AdamGrpc.AckMessage>) responseObserver);
          break;
        case METHODID_LIST_STORAGE_HANDLERS:
          serviceImpl.listStorageHandlers((org.vitrivr.adampro.grpc.AdamGrpc.EmptyMessage) request,
              (io.grpc.stub.StreamObserver<org.vitrivr.adampro.grpc.AdamGrpc.StorageHandlersMessage>) responseObserver);
          break;
        case METHODID_TRANSFER_STORAGE_HANDLER:
          serviceImpl.transferStorageHandler((org.vitrivr.adampro.grpc.AdamGrpc.TransferStorageHandlerMessage) request,
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
        case METHODID_STREAM_INSERT:
          return (io.grpc.stub.StreamObserver<Req>) serviceImpl.streamInsert(
              (io.grpc.stub.StreamObserver<org.vitrivr.adampro.grpc.AdamGrpc.AckMessage>) responseObserver);
        default:
          throw new AssertionError();
      }
    }
  }

  private static abstract class AdamDefinitionBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    AdamDefinitionBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return org.vitrivr.adampro.grpc.AdamGrpc.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("AdamDefinition");
    }
  }

  private static final class AdamDefinitionFileDescriptorSupplier
      extends AdamDefinitionBaseDescriptorSupplier {
    AdamDefinitionFileDescriptorSupplier() {}
  }

  private static final class AdamDefinitionMethodDescriptorSupplier
      extends AdamDefinitionBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final String methodName;

    AdamDefinitionMethodDescriptorSupplier(String methodName) {
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
      synchronized (AdamDefinitionGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new AdamDefinitionFileDescriptorSupplier())
              .addMethod(METHOD_CREATE_ENTITY)
              .addMethod(METHOD_AVAILABLE_ATTRIBUTE_TYPES)
              .addMethod(METHOD_COUNT)
              .addMethod(METHOD_DROP_ENTITY)
              .addMethod(METHOD_EXISTS_ENTITY)
              .addMethod(METHOD_INSERT)
              .addMethod(METHOD_STREAM_INSERT)
              .addMethod(METHOD_VACUUM_ENTITY)
              .addMethod(METHOD_DELETE)
              .addMethod(METHOD_INDEX)
              .addMethod(METHOD_GENERATE_ALL_INDEXES)
              .addMethod(METHOD_EXISTS_INDEX)
              .addMethod(METHOD_DROP_INDEX)
              .addMethod(METHOD_LIST_INDEXES)
              .addMethod(METHOD_GENERATE_RANDOM_DATA)
              .addMethod(METHOD_LIST_ENTITIES)
              .addMethod(METHOD_GET_ENTITY_PROPERTIES)
              .addMethod(METHOD_GET_ATTRIBUTE_PROPERTIES)
              .addMethod(METHOD_GET_INDEX_PROPERTIES)
              .addMethod(METHOD_REPARTITION_ENTITY_DATA)
              .addMethod(METHOD_REPARTITION_INDEX_DATA)
              .addMethod(METHOD_ADAPT_SCAN_METHODS)
              .addMethod(METHOD_SPARSIFY_ENTITY)
              .addMethod(METHOD_IMPORT_DATA)
              .addMethod(METHOD_PROTO_IMPORT_DATA)
              .addMethod(METHOD_PROTO_EXPORT_DATA)
              .addMethod(METHOD_LIST_STORAGE_HANDLERS)
              .addMethod(METHOD_TRANSFER_STORAGE_HANDLER)
              .build();
        }
      }
    }
    return result;
  }
}
