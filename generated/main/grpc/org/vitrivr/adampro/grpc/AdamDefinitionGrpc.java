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

@javax.annotation.Generated("by gRPC proto compiler")
public class AdamDefinitionGrpc {

  private AdamDefinitionGrpc() {}

  public static final String SERVICE_NAME = "AdamDefinition";

  // Static method descriptors that strictly reflect the proto.
  @io.grpc.ExperimentalApi
  public static final io.grpc.MethodDescriptor<org.vitrivr.adampro.grpc.AdamGrpc.CreateEntityMessage,
      org.vitrivr.adampro.grpc.AdamGrpc.AckMessage> METHOD_CREATE_ENTITY =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "AdamDefinition", "CreateEntity"),
          io.grpc.protobuf.ProtoUtils.marshaller(org.vitrivr.adampro.grpc.AdamGrpc.CreateEntityMessage.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(org.vitrivr.adampro.grpc.AdamGrpc.AckMessage.getDefaultInstance()));
  @io.grpc.ExperimentalApi
  public static final io.grpc.MethodDescriptor<org.vitrivr.adampro.grpc.AdamGrpc.EmptyMessage,
      org.vitrivr.adampro.grpc.AdamGrpc.AvailableAttributeTypesMessage> METHOD_AVAILABLE_ATTRIBUTE_TYPES =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "AdamDefinition", "AvailableAttributeTypes"),
          io.grpc.protobuf.ProtoUtils.marshaller(org.vitrivr.adampro.grpc.AdamGrpc.EmptyMessage.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(org.vitrivr.adampro.grpc.AdamGrpc.AvailableAttributeTypesMessage.getDefaultInstance()));
  @io.grpc.ExperimentalApi
  public static final io.grpc.MethodDescriptor<org.vitrivr.adampro.grpc.AdamGrpc.EntityNameMessage,
      org.vitrivr.adampro.grpc.AdamGrpc.AckMessage> METHOD_COUNT =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "AdamDefinition", "Count"),
          io.grpc.protobuf.ProtoUtils.marshaller(org.vitrivr.adampro.grpc.AdamGrpc.EntityNameMessage.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(org.vitrivr.adampro.grpc.AdamGrpc.AckMessage.getDefaultInstance()));
  @io.grpc.ExperimentalApi
  public static final io.grpc.MethodDescriptor<org.vitrivr.adampro.grpc.AdamGrpc.EntityNameMessage,
      org.vitrivr.adampro.grpc.AdamGrpc.AckMessage> METHOD_DROP_ENTITY =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "AdamDefinition", "DropEntity"),
          io.grpc.protobuf.ProtoUtils.marshaller(org.vitrivr.adampro.grpc.AdamGrpc.EntityNameMessage.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(org.vitrivr.adampro.grpc.AdamGrpc.AckMessage.getDefaultInstance()));
  @io.grpc.ExperimentalApi
  public static final io.grpc.MethodDescriptor<org.vitrivr.adampro.grpc.AdamGrpc.EntityNameMessage,
      org.vitrivr.adampro.grpc.AdamGrpc.ExistsMessage> METHOD_EXISTS_ENTITY =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "AdamDefinition", "ExistsEntity"),
          io.grpc.protobuf.ProtoUtils.marshaller(org.vitrivr.adampro.grpc.AdamGrpc.EntityNameMessage.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(org.vitrivr.adampro.grpc.AdamGrpc.ExistsMessage.getDefaultInstance()));
  @io.grpc.ExperimentalApi
  public static final io.grpc.MethodDescriptor<org.vitrivr.adampro.grpc.AdamGrpc.InsertMessage,
      org.vitrivr.adampro.grpc.AdamGrpc.AckMessage> METHOD_INSERT =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "AdamDefinition", "Insert"),
          io.grpc.protobuf.ProtoUtils.marshaller(org.vitrivr.adampro.grpc.AdamGrpc.InsertMessage.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(org.vitrivr.adampro.grpc.AdamGrpc.AckMessage.getDefaultInstance()));
  @io.grpc.ExperimentalApi
  public static final io.grpc.MethodDescriptor<org.vitrivr.adampro.grpc.AdamGrpc.InsertMessage,
      org.vitrivr.adampro.grpc.AdamGrpc.AckMessage> METHOD_STREAM_INSERT =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.BIDI_STREAMING,
          generateFullMethodName(
              "AdamDefinition", "StreamInsert"),
          io.grpc.protobuf.ProtoUtils.marshaller(org.vitrivr.adampro.grpc.AdamGrpc.InsertMessage.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(org.vitrivr.adampro.grpc.AdamGrpc.AckMessage.getDefaultInstance()));
  @io.grpc.ExperimentalApi
  public static final io.grpc.MethodDescriptor<org.vitrivr.adampro.grpc.AdamGrpc.EntityNameMessage,
      org.vitrivr.adampro.grpc.AdamGrpc.AckMessage> METHOD_GET_NEXT_PK_VALUE =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "AdamDefinition", "GetNextPkValue"),
          io.grpc.protobuf.ProtoUtils.marshaller(org.vitrivr.adampro.grpc.AdamGrpc.EntityNameMessage.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(org.vitrivr.adampro.grpc.AdamGrpc.AckMessage.getDefaultInstance()));
  @io.grpc.ExperimentalApi
  public static final io.grpc.MethodDescriptor<org.vitrivr.adampro.grpc.AdamGrpc.DeleteMessage,
      org.vitrivr.adampro.grpc.AdamGrpc.AckMessage> METHOD_DELETE =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "AdamDefinition", "Delete"),
          io.grpc.protobuf.ProtoUtils.marshaller(org.vitrivr.adampro.grpc.AdamGrpc.DeleteMessage.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(org.vitrivr.adampro.grpc.AdamGrpc.AckMessage.getDefaultInstance()));
  @io.grpc.ExperimentalApi
  public static final io.grpc.MethodDescriptor<org.vitrivr.adampro.grpc.AdamGrpc.IndexMessage,
      org.vitrivr.adampro.grpc.AdamGrpc.AckMessage> METHOD_INDEX =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "AdamDefinition", "Index"),
          io.grpc.protobuf.ProtoUtils.marshaller(org.vitrivr.adampro.grpc.AdamGrpc.IndexMessage.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(org.vitrivr.adampro.grpc.AdamGrpc.AckMessage.getDefaultInstance()));
  @io.grpc.ExperimentalApi
  public static final io.grpc.MethodDescriptor<org.vitrivr.adampro.grpc.AdamGrpc.IndexMessage,
      org.vitrivr.adampro.grpc.AdamGrpc.AckMessage> METHOD_GENERATE_ALL_INDEXES =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "AdamDefinition", "GenerateAllIndexes"),
          io.grpc.protobuf.ProtoUtils.marshaller(org.vitrivr.adampro.grpc.AdamGrpc.IndexMessage.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(org.vitrivr.adampro.grpc.AdamGrpc.AckMessage.getDefaultInstance()));
  @io.grpc.ExperimentalApi
  public static final io.grpc.MethodDescriptor<org.vitrivr.adampro.grpc.AdamGrpc.IndexMessage,
      org.vitrivr.adampro.grpc.AdamGrpc.ExistsMessage> METHOD_EXISTS_INDEX =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "AdamDefinition", "ExistsIndex"),
          io.grpc.protobuf.ProtoUtils.marshaller(org.vitrivr.adampro.grpc.AdamGrpc.IndexMessage.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(org.vitrivr.adampro.grpc.AdamGrpc.ExistsMessage.getDefaultInstance()));
  @io.grpc.ExperimentalApi
  public static final io.grpc.MethodDescriptor<org.vitrivr.adampro.grpc.AdamGrpc.IndexNameMessage,
      org.vitrivr.adampro.grpc.AdamGrpc.AckMessage> METHOD_DROP_INDEX =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "AdamDefinition", "DropIndex"),
          io.grpc.protobuf.ProtoUtils.marshaller(org.vitrivr.adampro.grpc.AdamGrpc.IndexNameMessage.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(org.vitrivr.adampro.grpc.AdamGrpc.AckMessage.getDefaultInstance()));
  @io.grpc.ExperimentalApi
  public static final io.grpc.MethodDescriptor<org.vitrivr.adampro.grpc.AdamGrpc.EntityNameMessage,
      org.vitrivr.adampro.grpc.AdamGrpc.IndexesMessage> METHOD_LIST_INDEXES =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "AdamDefinition", "ListIndexes"),
          io.grpc.protobuf.ProtoUtils.marshaller(org.vitrivr.adampro.grpc.AdamGrpc.EntityNameMessage.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(org.vitrivr.adampro.grpc.AdamGrpc.IndexesMessage.getDefaultInstance()));
  @io.grpc.ExperimentalApi
  public static final io.grpc.MethodDescriptor<org.vitrivr.adampro.grpc.AdamGrpc.GenerateRandomDataMessage,
      org.vitrivr.adampro.grpc.AdamGrpc.AckMessage> METHOD_GENERATE_RANDOM_DATA =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "AdamDefinition", "GenerateRandomData"),
          io.grpc.protobuf.ProtoUtils.marshaller(org.vitrivr.adampro.grpc.AdamGrpc.GenerateRandomDataMessage.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(org.vitrivr.adampro.grpc.AdamGrpc.AckMessage.getDefaultInstance()));
  @io.grpc.ExperimentalApi
  public static final io.grpc.MethodDescriptor<org.vitrivr.adampro.grpc.AdamGrpc.EmptyMessage,
      org.vitrivr.adampro.grpc.AdamGrpc.EntitiesMessage> METHOD_LIST_ENTITIES =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "AdamDefinition", "ListEntities"),
          io.grpc.protobuf.ProtoUtils.marshaller(org.vitrivr.adampro.grpc.AdamGrpc.EmptyMessage.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(org.vitrivr.adampro.grpc.AdamGrpc.EntitiesMessage.getDefaultInstance()));
  @io.grpc.ExperimentalApi
  public static final io.grpc.MethodDescriptor<org.vitrivr.adampro.grpc.AdamGrpc.EntityPropertiesMessage,
      org.vitrivr.adampro.grpc.AdamGrpc.PropertiesMessage> METHOD_GET_ENTITY_PROPERTIES =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "AdamDefinition", "GetEntityProperties"),
          io.grpc.protobuf.ProtoUtils.marshaller(org.vitrivr.adampro.grpc.AdamGrpc.EntityPropertiesMessage.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(org.vitrivr.adampro.grpc.AdamGrpc.PropertiesMessage.getDefaultInstance()));
  @io.grpc.ExperimentalApi
  public static final io.grpc.MethodDescriptor<org.vitrivr.adampro.grpc.AdamGrpc.AttributePropertiesMessage,
      org.vitrivr.adampro.grpc.AdamGrpc.PropertiesMessage> METHOD_GET_ATTRIBUTE_PROPERTIES =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "AdamDefinition", "GetAttributeProperties"),
          io.grpc.protobuf.ProtoUtils.marshaller(org.vitrivr.adampro.grpc.AdamGrpc.AttributePropertiesMessage.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(org.vitrivr.adampro.grpc.AdamGrpc.PropertiesMessage.getDefaultInstance()));
  @io.grpc.ExperimentalApi
  public static final io.grpc.MethodDescriptor<org.vitrivr.adampro.grpc.AdamGrpc.IndexPropertiesMessage,
      org.vitrivr.adampro.grpc.AdamGrpc.PropertiesMessage> METHOD_GET_INDEX_PROPERTIES =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "AdamDefinition", "GetIndexProperties"),
          io.grpc.protobuf.ProtoUtils.marshaller(org.vitrivr.adampro.grpc.AdamGrpc.IndexPropertiesMessage.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(org.vitrivr.adampro.grpc.AdamGrpc.PropertiesMessage.getDefaultInstance()));
  @io.grpc.ExperimentalApi
  public static final io.grpc.MethodDescriptor<org.vitrivr.adampro.grpc.AdamGrpc.RepartitionMessage,
      org.vitrivr.adampro.grpc.AdamGrpc.AckMessage> METHOD_REPARTITION_ENTITY_DATA =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "AdamDefinition", "RepartitionEntityData"),
          io.grpc.protobuf.ProtoUtils.marshaller(org.vitrivr.adampro.grpc.AdamGrpc.RepartitionMessage.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(org.vitrivr.adampro.grpc.AdamGrpc.AckMessage.getDefaultInstance()));
  @io.grpc.ExperimentalApi
  public static final io.grpc.MethodDescriptor<org.vitrivr.adampro.grpc.AdamGrpc.RepartitionMessage,
      org.vitrivr.adampro.grpc.AdamGrpc.AckMessage> METHOD_REPARTITION_INDEX_DATA =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "AdamDefinition", "RepartitionIndexData"),
          io.grpc.protobuf.ProtoUtils.marshaller(org.vitrivr.adampro.grpc.AdamGrpc.RepartitionMessage.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(org.vitrivr.adampro.grpc.AdamGrpc.AckMessage.getDefaultInstance()));
  @io.grpc.ExperimentalApi
  public static final io.grpc.MethodDescriptor<org.vitrivr.adampro.grpc.AdamGrpc.EntityNameMessage,
      org.vitrivr.adampro.grpc.AdamGrpc.AckMessage> METHOD_RESET_SCAN_WEIGHTS =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "AdamDefinition", "ResetScanWeights"),
          io.grpc.protobuf.ProtoUtils.marshaller(org.vitrivr.adampro.grpc.AdamGrpc.EntityNameMessage.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(org.vitrivr.adampro.grpc.AdamGrpc.AckMessage.getDefaultInstance()));
  @io.grpc.ExperimentalApi
  public static final io.grpc.MethodDescriptor<org.vitrivr.adampro.grpc.AdamGrpc.WeightMessage,
      org.vitrivr.adampro.grpc.AdamGrpc.AckMessage> METHOD_SET_SCAN_WEIGHT =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "AdamDefinition", "SetScanWeight"),
          io.grpc.protobuf.ProtoUtils.marshaller(org.vitrivr.adampro.grpc.AdamGrpc.WeightMessage.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(org.vitrivr.adampro.grpc.AdamGrpc.AckMessage.getDefaultInstance()));
  @io.grpc.ExperimentalApi
  public static final io.grpc.MethodDescriptor<org.vitrivr.adampro.grpc.AdamGrpc.AdaptScanMethodsMessage,
      org.vitrivr.adampro.grpc.AdamGrpc.AckMessage> METHOD_ADAPT_SCAN_METHODS =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "AdamDefinition", "AdaptScanMethods"),
          io.grpc.protobuf.ProtoUtils.marshaller(org.vitrivr.adampro.grpc.AdamGrpc.AdaptScanMethodsMessage.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(org.vitrivr.adampro.grpc.AdamGrpc.AckMessage.getDefaultInstance()));
  @io.grpc.ExperimentalApi
  public static final io.grpc.MethodDescriptor<org.vitrivr.adampro.grpc.AdamGrpc.SparsifyEntityMessage,
      org.vitrivr.adampro.grpc.AdamGrpc.AckMessage> METHOD_SPARSIFY_ENTITY =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "AdamDefinition", "SparsifyEntity"),
          io.grpc.protobuf.ProtoUtils.marshaller(org.vitrivr.adampro.grpc.AdamGrpc.SparsifyEntityMessage.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(org.vitrivr.adampro.grpc.AdamGrpc.AckMessage.getDefaultInstance()));
  @io.grpc.ExperimentalApi
  public static final io.grpc.MethodDescriptor<org.vitrivr.adampro.grpc.AdamGrpc.ImportMessage,
      org.vitrivr.adampro.grpc.AdamGrpc.AckMessage> METHOD_IMPORT_DATA =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "AdamDefinition", "ImportData"),
          io.grpc.protobuf.ProtoUtils.marshaller(org.vitrivr.adampro.grpc.AdamGrpc.ImportMessage.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(org.vitrivr.adampro.grpc.AdamGrpc.AckMessage.getDefaultInstance()));
  @io.grpc.ExperimentalApi
  public static final io.grpc.MethodDescriptor<org.vitrivr.adampro.grpc.AdamGrpc.ImportDataFileMessage,
      org.vitrivr.adampro.grpc.AdamGrpc.AckMessage> METHOD_IMPORT_DATA_FILE =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "AdamDefinition", "ImportDataFile"),
          io.grpc.protobuf.ProtoUtils.marshaller(org.vitrivr.adampro.grpc.AdamGrpc.ImportDataFileMessage.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(org.vitrivr.adampro.grpc.AdamGrpc.AckMessage.getDefaultInstance()));
  @io.grpc.ExperimentalApi
  public static final io.grpc.MethodDescriptor<org.vitrivr.adampro.grpc.AdamGrpc.EntityNameMessage,
      org.vitrivr.adampro.grpc.AdamGrpc.ExportDataFileMessage> METHOD_EXPORT_DATA_FILE =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "AdamDefinition", "ExportDataFile"),
          io.grpc.protobuf.ProtoUtils.marshaller(org.vitrivr.adampro.grpc.AdamGrpc.EntityNameMessage.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(org.vitrivr.adampro.grpc.AdamGrpc.ExportDataFileMessage.getDefaultInstance()));
  @io.grpc.ExperimentalApi
  public static final io.grpc.MethodDescriptor<org.vitrivr.adampro.grpc.AdamGrpc.EmptyMessage,
      org.vitrivr.adampro.grpc.AdamGrpc.StorageHandlersMessage> METHOD_LIST_STORAGE_HANDLERS =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "AdamDefinition", "ListStorageHandlers"),
          io.grpc.protobuf.ProtoUtils.marshaller(org.vitrivr.adampro.grpc.AdamGrpc.EmptyMessage.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(org.vitrivr.adampro.grpc.AdamGrpc.StorageHandlersMessage.getDefaultInstance()));

  public static AdamDefinitionStub newStub(io.grpc.Channel channel) {
    return new AdamDefinitionStub(channel);
  }

  public static AdamDefinitionBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    return new AdamDefinitionBlockingStub(channel);
  }

  public static AdamDefinitionFutureStub newFutureStub(
      io.grpc.Channel channel) {
    return new AdamDefinitionFutureStub(channel);
  }

  public static interface AdamDefinition {

    public void createEntity(org.vitrivr.adampro.grpc.AdamGrpc.CreateEntityMessage request,
        io.grpc.stub.StreamObserver<org.vitrivr.adampro.grpc.AdamGrpc.AckMessage> responseObserver);

    public void availableAttributeTypes(org.vitrivr.adampro.grpc.AdamGrpc.EmptyMessage request,
        io.grpc.stub.StreamObserver<org.vitrivr.adampro.grpc.AdamGrpc.AvailableAttributeTypesMessage> responseObserver);

    public void count(org.vitrivr.adampro.grpc.AdamGrpc.EntityNameMessage request,
        io.grpc.stub.StreamObserver<org.vitrivr.adampro.grpc.AdamGrpc.AckMessage> responseObserver);

    public void dropEntity(org.vitrivr.adampro.grpc.AdamGrpc.EntityNameMessage request,
        io.grpc.stub.StreamObserver<org.vitrivr.adampro.grpc.AdamGrpc.AckMessage> responseObserver);

    public void existsEntity(org.vitrivr.adampro.grpc.AdamGrpc.EntityNameMessage request,
        io.grpc.stub.StreamObserver<org.vitrivr.adampro.grpc.AdamGrpc.ExistsMessage> responseObserver);

    public void insert(org.vitrivr.adampro.grpc.AdamGrpc.InsertMessage request,
        io.grpc.stub.StreamObserver<org.vitrivr.adampro.grpc.AdamGrpc.AckMessage> responseObserver);

    public io.grpc.stub.StreamObserver<org.vitrivr.adampro.grpc.AdamGrpc.InsertMessage> streamInsert(
        io.grpc.stub.StreamObserver<org.vitrivr.adampro.grpc.AdamGrpc.AckMessage> responseObserver);

    public void getNextPkValue(org.vitrivr.adampro.grpc.AdamGrpc.EntityNameMessage request,
        io.grpc.stub.StreamObserver<org.vitrivr.adampro.grpc.AdamGrpc.AckMessage> responseObserver);

    public void delete(org.vitrivr.adampro.grpc.AdamGrpc.DeleteMessage request,
        io.grpc.stub.StreamObserver<org.vitrivr.adampro.grpc.AdamGrpc.AckMessage> responseObserver);

    public void index(org.vitrivr.adampro.grpc.AdamGrpc.IndexMessage request,
        io.grpc.stub.StreamObserver<org.vitrivr.adampro.grpc.AdamGrpc.AckMessage> responseObserver);

    public void generateAllIndexes(org.vitrivr.adampro.grpc.AdamGrpc.IndexMessage request,
        io.grpc.stub.StreamObserver<org.vitrivr.adampro.grpc.AdamGrpc.AckMessage> responseObserver);

    public void existsIndex(org.vitrivr.adampro.grpc.AdamGrpc.IndexMessage request,
        io.grpc.stub.StreamObserver<org.vitrivr.adampro.grpc.AdamGrpc.ExistsMessage> responseObserver);

    public void dropIndex(org.vitrivr.adampro.grpc.AdamGrpc.IndexNameMessage request,
        io.grpc.stub.StreamObserver<org.vitrivr.adampro.grpc.AdamGrpc.AckMessage> responseObserver);

    public void listIndexes(org.vitrivr.adampro.grpc.AdamGrpc.EntityNameMessage request,
        io.grpc.stub.StreamObserver<org.vitrivr.adampro.grpc.AdamGrpc.IndexesMessage> responseObserver);

    public void generateRandomData(org.vitrivr.adampro.grpc.AdamGrpc.GenerateRandomDataMessage request,
        io.grpc.stub.StreamObserver<org.vitrivr.adampro.grpc.AdamGrpc.AckMessage> responseObserver);

    public void listEntities(org.vitrivr.adampro.grpc.AdamGrpc.EmptyMessage request,
        io.grpc.stub.StreamObserver<org.vitrivr.adampro.grpc.AdamGrpc.EntitiesMessage> responseObserver);

    public void getEntityProperties(org.vitrivr.adampro.grpc.AdamGrpc.EntityPropertiesMessage request,
        io.grpc.stub.StreamObserver<org.vitrivr.adampro.grpc.AdamGrpc.PropertiesMessage> responseObserver);

    public void getAttributeProperties(org.vitrivr.adampro.grpc.AdamGrpc.AttributePropertiesMessage request,
        io.grpc.stub.StreamObserver<org.vitrivr.adampro.grpc.AdamGrpc.PropertiesMessage> responseObserver);

    public void getIndexProperties(org.vitrivr.adampro.grpc.AdamGrpc.IndexPropertiesMessage request,
        io.grpc.stub.StreamObserver<org.vitrivr.adampro.grpc.AdamGrpc.PropertiesMessage> responseObserver);

    public void repartitionEntityData(org.vitrivr.adampro.grpc.AdamGrpc.RepartitionMessage request,
        io.grpc.stub.StreamObserver<org.vitrivr.adampro.grpc.AdamGrpc.AckMessage> responseObserver);

    public void repartitionIndexData(org.vitrivr.adampro.grpc.AdamGrpc.RepartitionMessage request,
        io.grpc.stub.StreamObserver<org.vitrivr.adampro.grpc.AdamGrpc.AckMessage> responseObserver);

    public void resetScanWeights(org.vitrivr.adampro.grpc.AdamGrpc.EntityNameMessage request,
        io.grpc.stub.StreamObserver<org.vitrivr.adampro.grpc.AdamGrpc.AckMessage> responseObserver);

    public void setScanWeight(org.vitrivr.adampro.grpc.AdamGrpc.WeightMessage request,
        io.grpc.stub.StreamObserver<org.vitrivr.adampro.grpc.AdamGrpc.AckMessage> responseObserver);

    public void adaptScanMethods(org.vitrivr.adampro.grpc.AdamGrpc.AdaptScanMethodsMessage request,
        io.grpc.stub.StreamObserver<org.vitrivr.adampro.grpc.AdamGrpc.AckMessage> responseObserver);

    public void sparsifyEntity(org.vitrivr.adampro.grpc.AdamGrpc.SparsifyEntityMessage request,
        io.grpc.stub.StreamObserver<org.vitrivr.adampro.grpc.AdamGrpc.AckMessage> responseObserver);

    public void importData(org.vitrivr.adampro.grpc.AdamGrpc.ImportMessage request,
        io.grpc.stub.StreamObserver<org.vitrivr.adampro.grpc.AdamGrpc.AckMessage> responseObserver);

    public void importDataFile(org.vitrivr.adampro.grpc.AdamGrpc.ImportDataFileMessage request,
        io.grpc.stub.StreamObserver<org.vitrivr.adampro.grpc.AdamGrpc.AckMessage> responseObserver);

    public void exportDataFile(org.vitrivr.adampro.grpc.AdamGrpc.EntityNameMessage request,
        io.grpc.stub.StreamObserver<org.vitrivr.adampro.grpc.AdamGrpc.ExportDataFileMessage> responseObserver);

    public void listStorageHandlers(org.vitrivr.adampro.grpc.AdamGrpc.EmptyMessage request,
        io.grpc.stub.StreamObserver<org.vitrivr.adampro.grpc.AdamGrpc.StorageHandlersMessage> responseObserver);
  }

  public static interface AdamDefinitionBlockingClient {

    public org.vitrivr.adampro.grpc.AdamGrpc.AckMessage createEntity(org.vitrivr.adampro.grpc.AdamGrpc.CreateEntityMessage request);

    public org.vitrivr.adampro.grpc.AdamGrpc.AvailableAttributeTypesMessage availableAttributeTypes(org.vitrivr.adampro.grpc.AdamGrpc.EmptyMessage request);

    public org.vitrivr.adampro.grpc.AdamGrpc.AckMessage count(org.vitrivr.adampro.grpc.AdamGrpc.EntityNameMessage request);

    public org.vitrivr.adampro.grpc.AdamGrpc.AckMessage dropEntity(org.vitrivr.adampro.grpc.AdamGrpc.EntityNameMessage request);

    public org.vitrivr.adampro.grpc.AdamGrpc.ExistsMessage existsEntity(org.vitrivr.adampro.grpc.AdamGrpc.EntityNameMessage request);

    public org.vitrivr.adampro.grpc.AdamGrpc.AckMessage insert(org.vitrivr.adampro.grpc.AdamGrpc.InsertMessage request);

    public org.vitrivr.adampro.grpc.AdamGrpc.AckMessage getNextPkValue(org.vitrivr.adampro.grpc.AdamGrpc.EntityNameMessage request);

    public org.vitrivr.adampro.grpc.AdamGrpc.AckMessage delete(org.vitrivr.adampro.grpc.AdamGrpc.DeleteMessage request);

    public org.vitrivr.adampro.grpc.AdamGrpc.AckMessage index(org.vitrivr.adampro.grpc.AdamGrpc.IndexMessage request);

    public org.vitrivr.adampro.grpc.AdamGrpc.AckMessage generateAllIndexes(org.vitrivr.adampro.grpc.AdamGrpc.IndexMessage request);

    public org.vitrivr.adampro.grpc.AdamGrpc.ExistsMessage existsIndex(org.vitrivr.adampro.grpc.AdamGrpc.IndexMessage request);

    public org.vitrivr.adampro.grpc.AdamGrpc.AckMessage dropIndex(org.vitrivr.adampro.grpc.AdamGrpc.IndexNameMessage request);

    public org.vitrivr.adampro.grpc.AdamGrpc.IndexesMessage listIndexes(org.vitrivr.adampro.grpc.AdamGrpc.EntityNameMessage request);

    public org.vitrivr.adampro.grpc.AdamGrpc.AckMessage generateRandomData(org.vitrivr.adampro.grpc.AdamGrpc.GenerateRandomDataMessage request);

    public org.vitrivr.adampro.grpc.AdamGrpc.EntitiesMessage listEntities(org.vitrivr.adampro.grpc.AdamGrpc.EmptyMessage request);

    public org.vitrivr.adampro.grpc.AdamGrpc.PropertiesMessage getEntityProperties(org.vitrivr.adampro.grpc.AdamGrpc.EntityPropertiesMessage request);

    public org.vitrivr.adampro.grpc.AdamGrpc.PropertiesMessage getAttributeProperties(org.vitrivr.adampro.grpc.AdamGrpc.AttributePropertiesMessage request);

    public org.vitrivr.adampro.grpc.AdamGrpc.PropertiesMessage getIndexProperties(org.vitrivr.adampro.grpc.AdamGrpc.IndexPropertiesMessage request);

    public org.vitrivr.adampro.grpc.AdamGrpc.AckMessage repartitionEntityData(org.vitrivr.adampro.grpc.AdamGrpc.RepartitionMessage request);

    public org.vitrivr.adampro.grpc.AdamGrpc.AckMessage repartitionIndexData(org.vitrivr.adampro.grpc.AdamGrpc.RepartitionMessage request);

    public org.vitrivr.adampro.grpc.AdamGrpc.AckMessage resetScanWeights(org.vitrivr.adampro.grpc.AdamGrpc.EntityNameMessage request);

    public org.vitrivr.adampro.grpc.AdamGrpc.AckMessage setScanWeight(org.vitrivr.adampro.grpc.AdamGrpc.WeightMessage request);

    public org.vitrivr.adampro.grpc.AdamGrpc.AckMessage adaptScanMethods(org.vitrivr.adampro.grpc.AdamGrpc.AdaptScanMethodsMessage request);

    public org.vitrivr.adampro.grpc.AdamGrpc.AckMessage sparsifyEntity(org.vitrivr.adampro.grpc.AdamGrpc.SparsifyEntityMessage request);

    public org.vitrivr.adampro.grpc.AdamGrpc.AckMessage importData(org.vitrivr.adampro.grpc.AdamGrpc.ImportMessage request);

    public org.vitrivr.adampro.grpc.AdamGrpc.AckMessage importDataFile(org.vitrivr.adampro.grpc.AdamGrpc.ImportDataFileMessage request);

    public org.vitrivr.adampro.grpc.AdamGrpc.ExportDataFileMessage exportDataFile(org.vitrivr.adampro.grpc.AdamGrpc.EntityNameMessage request);

    public org.vitrivr.adampro.grpc.AdamGrpc.StorageHandlersMessage listStorageHandlers(org.vitrivr.adampro.grpc.AdamGrpc.EmptyMessage request);
  }

  public static interface AdamDefinitionFutureClient {

    public com.google.common.util.concurrent.ListenableFuture<org.vitrivr.adampro.grpc.AdamGrpc.AckMessage> createEntity(
        org.vitrivr.adampro.grpc.AdamGrpc.CreateEntityMessage request);

    public com.google.common.util.concurrent.ListenableFuture<org.vitrivr.adampro.grpc.AdamGrpc.AvailableAttributeTypesMessage> availableAttributeTypes(
        org.vitrivr.adampro.grpc.AdamGrpc.EmptyMessage request);

    public com.google.common.util.concurrent.ListenableFuture<org.vitrivr.adampro.grpc.AdamGrpc.AckMessage> count(
        org.vitrivr.adampro.grpc.AdamGrpc.EntityNameMessage request);

    public com.google.common.util.concurrent.ListenableFuture<org.vitrivr.adampro.grpc.AdamGrpc.AckMessage> dropEntity(
        org.vitrivr.adampro.grpc.AdamGrpc.EntityNameMessage request);

    public com.google.common.util.concurrent.ListenableFuture<org.vitrivr.adampro.grpc.AdamGrpc.ExistsMessage> existsEntity(
        org.vitrivr.adampro.grpc.AdamGrpc.EntityNameMessage request);

    public com.google.common.util.concurrent.ListenableFuture<org.vitrivr.adampro.grpc.AdamGrpc.AckMessage> insert(
        org.vitrivr.adampro.grpc.AdamGrpc.InsertMessage request);

    public com.google.common.util.concurrent.ListenableFuture<org.vitrivr.adampro.grpc.AdamGrpc.AckMessage> getNextPkValue(
        org.vitrivr.adampro.grpc.AdamGrpc.EntityNameMessage request);

    public com.google.common.util.concurrent.ListenableFuture<org.vitrivr.adampro.grpc.AdamGrpc.AckMessage> delete(
        org.vitrivr.adampro.grpc.AdamGrpc.DeleteMessage request);

    public com.google.common.util.concurrent.ListenableFuture<org.vitrivr.adampro.grpc.AdamGrpc.AckMessage> index(
        org.vitrivr.adampro.grpc.AdamGrpc.IndexMessage request);

    public com.google.common.util.concurrent.ListenableFuture<org.vitrivr.adampro.grpc.AdamGrpc.AckMessage> generateAllIndexes(
        org.vitrivr.adampro.grpc.AdamGrpc.IndexMessage request);

    public com.google.common.util.concurrent.ListenableFuture<org.vitrivr.adampro.grpc.AdamGrpc.ExistsMessage> existsIndex(
        org.vitrivr.adampro.grpc.AdamGrpc.IndexMessage request);

    public com.google.common.util.concurrent.ListenableFuture<org.vitrivr.adampro.grpc.AdamGrpc.AckMessage> dropIndex(
        org.vitrivr.adampro.grpc.AdamGrpc.IndexNameMessage request);

    public com.google.common.util.concurrent.ListenableFuture<org.vitrivr.adampro.grpc.AdamGrpc.IndexesMessage> listIndexes(
        org.vitrivr.adampro.grpc.AdamGrpc.EntityNameMessage request);

    public com.google.common.util.concurrent.ListenableFuture<org.vitrivr.adampro.grpc.AdamGrpc.AckMessage> generateRandomData(
        org.vitrivr.adampro.grpc.AdamGrpc.GenerateRandomDataMessage request);

    public com.google.common.util.concurrent.ListenableFuture<org.vitrivr.adampro.grpc.AdamGrpc.EntitiesMessage> listEntities(
        org.vitrivr.adampro.grpc.AdamGrpc.EmptyMessage request);

    public com.google.common.util.concurrent.ListenableFuture<org.vitrivr.adampro.grpc.AdamGrpc.PropertiesMessage> getEntityProperties(
        org.vitrivr.adampro.grpc.AdamGrpc.EntityPropertiesMessage request);

    public com.google.common.util.concurrent.ListenableFuture<org.vitrivr.adampro.grpc.AdamGrpc.PropertiesMessage> getAttributeProperties(
        org.vitrivr.adampro.grpc.AdamGrpc.AttributePropertiesMessage request);

    public com.google.common.util.concurrent.ListenableFuture<org.vitrivr.adampro.grpc.AdamGrpc.PropertiesMessage> getIndexProperties(
        org.vitrivr.adampro.grpc.AdamGrpc.IndexPropertiesMessage request);

    public com.google.common.util.concurrent.ListenableFuture<org.vitrivr.adampro.grpc.AdamGrpc.AckMessage> repartitionEntityData(
        org.vitrivr.adampro.grpc.AdamGrpc.RepartitionMessage request);

    public com.google.common.util.concurrent.ListenableFuture<org.vitrivr.adampro.grpc.AdamGrpc.AckMessage> repartitionIndexData(
        org.vitrivr.adampro.grpc.AdamGrpc.RepartitionMessage request);

    public com.google.common.util.concurrent.ListenableFuture<org.vitrivr.adampro.grpc.AdamGrpc.AckMessage> resetScanWeights(
        org.vitrivr.adampro.grpc.AdamGrpc.EntityNameMessage request);

    public com.google.common.util.concurrent.ListenableFuture<org.vitrivr.adampro.grpc.AdamGrpc.AckMessage> setScanWeight(
        org.vitrivr.adampro.grpc.AdamGrpc.WeightMessage request);

    public com.google.common.util.concurrent.ListenableFuture<org.vitrivr.adampro.grpc.AdamGrpc.AckMessage> adaptScanMethods(
        org.vitrivr.adampro.grpc.AdamGrpc.AdaptScanMethodsMessage request);

    public com.google.common.util.concurrent.ListenableFuture<org.vitrivr.adampro.grpc.AdamGrpc.AckMessage> sparsifyEntity(
        org.vitrivr.adampro.grpc.AdamGrpc.SparsifyEntityMessage request);

    public com.google.common.util.concurrent.ListenableFuture<org.vitrivr.adampro.grpc.AdamGrpc.AckMessage> importData(
        org.vitrivr.adampro.grpc.AdamGrpc.ImportMessage request);

    public com.google.common.util.concurrent.ListenableFuture<org.vitrivr.adampro.grpc.AdamGrpc.AckMessage> importDataFile(
        org.vitrivr.adampro.grpc.AdamGrpc.ImportDataFileMessage request);

    public com.google.common.util.concurrent.ListenableFuture<org.vitrivr.adampro.grpc.AdamGrpc.ExportDataFileMessage> exportDataFile(
        org.vitrivr.adampro.grpc.AdamGrpc.EntityNameMessage request);

    public com.google.common.util.concurrent.ListenableFuture<org.vitrivr.adampro.grpc.AdamGrpc.StorageHandlersMessage> listStorageHandlers(
        org.vitrivr.adampro.grpc.AdamGrpc.EmptyMessage request);
  }

  public static class AdamDefinitionStub extends io.grpc.stub.AbstractStub<AdamDefinitionStub>
      implements AdamDefinition {
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

    @java.lang.Override
    public void createEntity(org.vitrivr.adampro.grpc.AdamGrpc.CreateEntityMessage request,
        io.grpc.stub.StreamObserver<org.vitrivr.adampro.grpc.AdamGrpc.AckMessage> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_CREATE_ENTITY, getCallOptions()), request, responseObserver);
    }

    @java.lang.Override
    public void availableAttributeTypes(org.vitrivr.adampro.grpc.AdamGrpc.EmptyMessage request,
        io.grpc.stub.StreamObserver<org.vitrivr.adampro.grpc.AdamGrpc.AvailableAttributeTypesMessage> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_AVAILABLE_ATTRIBUTE_TYPES, getCallOptions()), request, responseObserver);
    }

    @java.lang.Override
    public void count(org.vitrivr.adampro.grpc.AdamGrpc.EntityNameMessage request,
        io.grpc.stub.StreamObserver<org.vitrivr.adampro.grpc.AdamGrpc.AckMessage> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_COUNT, getCallOptions()), request, responseObserver);
    }

    @java.lang.Override
    public void dropEntity(org.vitrivr.adampro.grpc.AdamGrpc.EntityNameMessage request,
        io.grpc.stub.StreamObserver<org.vitrivr.adampro.grpc.AdamGrpc.AckMessage> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_DROP_ENTITY, getCallOptions()), request, responseObserver);
    }

    @java.lang.Override
    public void existsEntity(org.vitrivr.adampro.grpc.AdamGrpc.EntityNameMessage request,
        io.grpc.stub.StreamObserver<org.vitrivr.adampro.grpc.AdamGrpc.ExistsMessage> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_EXISTS_ENTITY, getCallOptions()), request, responseObserver);
    }

    @java.lang.Override
    public void insert(org.vitrivr.adampro.grpc.AdamGrpc.InsertMessage request,
        io.grpc.stub.StreamObserver<org.vitrivr.adampro.grpc.AdamGrpc.AckMessage> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_INSERT, getCallOptions()), request, responseObserver);
    }

    @java.lang.Override
    public io.grpc.stub.StreamObserver<org.vitrivr.adampro.grpc.AdamGrpc.InsertMessage> streamInsert(
        io.grpc.stub.StreamObserver<org.vitrivr.adampro.grpc.AdamGrpc.AckMessage> responseObserver) {
      return asyncBidiStreamingCall(
          getChannel().newCall(METHOD_STREAM_INSERT, getCallOptions()), responseObserver);
    }

    @java.lang.Override
    public void getNextPkValue(org.vitrivr.adampro.grpc.AdamGrpc.EntityNameMessage request,
        io.grpc.stub.StreamObserver<org.vitrivr.adampro.grpc.AdamGrpc.AckMessage> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_GET_NEXT_PK_VALUE, getCallOptions()), request, responseObserver);
    }

    @java.lang.Override
    public void delete(org.vitrivr.adampro.grpc.AdamGrpc.DeleteMessage request,
        io.grpc.stub.StreamObserver<org.vitrivr.adampro.grpc.AdamGrpc.AckMessage> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_DELETE, getCallOptions()), request, responseObserver);
    }

    @java.lang.Override
    public void index(org.vitrivr.adampro.grpc.AdamGrpc.IndexMessage request,
        io.grpc.stub.StreamObserver<org.vitrivr.adampro.grpc.AdamGrpc.AckMessage> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_INDEX, getCallOptions()), request, responseObserver);
    }

    @java.lang.Override
    public void generateAllIndexes(org.vitrivr.adampro.grpc.AdamGrpc.IndexMessage request,
        io.grpc.stub.StreamObserver<org.vitrivr.adampro.grpc.AdamGrpc.AckMessage> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_GENERATE_ALL_INDEXES, getCallOptions()), request, responseObserver);
    }

    @java.lang.Override
    public void existsIndex(org.vitrivr.adampro.grpc.AdamGrpc.IndexMessage request,
        io.grpc.stub.StreamObserver<org.vitrivr.adampro.grpc.AdamGrpc.ExistsMessage> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_EXISTS_INDEX, getCallOptions()), request, responseObserver);
    }

    @java.lang.Override
    public void dropIndex(org.vitrivr.adampro.grpc.AdamGrpc.IndexNameMessage request,
        io.grpc.stub.StreamObserver<org.vitrivr.adampro.grpc.AdamGrpc.AckMessage> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_DROP_INDEX, getCallOptions()), request, responseObserver);
    }

    @java.lang.Override
    public void listIndexes(org.vitrivr.adampro.grpc.AdamGrpc.EntityNameMessage request,
        io.grpc.stub.StreamObserver<org.vitrivr.adampro.grpc.AdamGrpc.IndexesMessage> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_LIST_INDEXES, getCallOptions()), request, responseObserver);
    }

    @java.lang.Override
    public void generateRandomData(org.vitrivr.adampro.grpc.AdamGrpc.GenerateRandomDataMessage request,
        io.grpc.stub.StreamObserver<org.vitrivr.adampro.grpc.AdamGrpc.AckMessage> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_GENERATE_RANDOM_DATA, getCallOptions()), request, responseObserver);
    }

    @java.lang.Override
    public void listEntities(org.vitrivr.adampro.grpc.AdamGrpc.EmptyMessage request,
        io.grpc.stub.StreamObserver<org.vitrivr.adampro.grpc.AdamGrpc.EntitiesMessage> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_LIST_ENTITIES, getCallOptions()), request, responseObserver);
    }

    @java.lang.Override
    public void getEntityProperties(org.vitrivr.adampro.grpc.AdamGrpc.EntityPropertiesMessage request,
        io.grpc.stub.StreamObserver<org.vitrivr.adampro.grpc.AdamGrpc.PropertiesMessage> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_GET_ENTITY_PROPERTIES, getCallOptions()), request, responseObserver);
    }

    @java.lang.Override
    public void getAttributeProperties(org.vitrivr.adampro.grpc.AdamGrpc.AttributePropertiesMessage request,
        io.grpc.stub.StreamObserver<org.vitrivr.adampro.grpc.AdamGrpc.PropertiesMessage> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_GET_ATTRIBUTE_PROPERTIES, getCallOptions()), request, responseObserver);
    }

    @java.lang.Override
    public void getIndexProperties(org.vitrivr.adampro.grpc.AdamGrpc.IndexPropertiesMessage request,
        io.grpc.stub.StreamObserver<org.vitrivr.adampro.grpc.AdamGrpc.PropertiesMessage> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_GET_INDEX_PROPERTIES, getCallOptions()), request, responseObserver);
    }

    @java.lang.Override
    public void repartitionEntityData(org.vitrivr.adampro.grpc.AdamGrpc.RepartitionMessage request,
        io.grpc.stub.StreamObserver<org.vitrivr.adampro.grpc.AdamGrpc.AckMessage> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_REPARTITION_ENTITY_DATA, getCallOptions()), request, responseObserver);
    }

    @java.lang.Override
    public void repartitionIndexData(org.vitrivr.adampro.grpc.AdamGrpc.RepartitionMessage request,
        io.grpc.stub.StreamObserver<org.vitrivr.adampro.grpc.AdamGrpc.AckMessage> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_REPARTITION_INDEX_DATA, getCallOptions()), request, responseObserver);
    }

    @java.lang.Override
    public void resetScanWeights(org.vitrivr.adampro.grpc.AdamGrpc.EntityNameMessage request,
        io.grpc.stub.StreamObserver<org.vitrivr.adampro.grpc.AdamGrpc.AckMessage> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_RESET_SCAN_WEIGHTS, getCallOptions()), request, responseObserver);
    }

    @java.lang.Override
    public void setScanWeight(org.vitrivr.adampro.grpc.AdamGrpc.WeightMessage request,
        io.grpc.stub.StreamObserver<org.vitrivr.adampro.grpc.AdamGrpc.AckMessage> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_SET_SCAN_WEIGHT, getCallOptions()), request, responseObserver);
    }

    @java.lang.Override
    public void adaptScanMethods(org.vitrivr.adampro.grpc.AdamGrpc.AdaptScanMethodsMessage request,
        io.grpc.stub.StreamObserver<org.vitrivr.adampro.grpc.AdamGrpc.AckMessage> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_ADAPT_SCAN_METHODS, getCallOptions()), request, responseObserver);
    }

    @java.lang.Override
    public void sparsifyEntity(org.vitrivr.adampro.grpc.AdamGrpc.SparsifyEntityMessage request,
        io.grpc.stub.StreamObserver<org.vitrivr.adampro.grpc.AdamGrpc.AckMessage> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_SPARSIFY_ENTITY, getCallOptions()), request, responseObserver);
    }

    @java.lang.Override
    public void importData(org.vitrivr.adampro.grpc.AdamGrpc.ImportMessage request,
        io.grpc.stub.StreamObserver<org.vitrivr.adampro.grpc.AdamGrpc.AckMessage> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_IMPORT_DATA, getCallOptions()), request, responseObserver);
    }

    @java.lang.Override
    public void importDataFile(org.vitrivr.adampro.grpc.AdamGrpc.ImportDataFileMessage request,
        io.grpc.stub.StreamObserver<org.vitrivr.adampro.grpc.AdamGrpc.AckMessage> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_IMPORT_DATA_FILE, getCallOptions()), request, responseObserver);
    }

    @java.lang.Override
    public void exportDataFile(org.vitrivr.adampro.grpc.AdamGrpc.EntityNameMessage request,
        io.grpc.stub.StreamObserver<org.vitrivr.adampro.grpc.AdamGrpc.ExportDataFileMessage> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_EXPORT_DATA_FILE, getCallOptions()), request, responseObserver);
    }

    @java.lang.Override
    public void listStorageHandlers(org.vitrivr.adampro.grpc.AdamGrpc.EmptyMessage request,
        io.grpc.stub.StreamObserver<org.vitrivr.adampro.grpc.AdamGrpc.StorageHandlersMessage> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_LIST_STORAGE_HANDLERS, getCallOptions()), request, responseObserver);
    }
  }

  public static class AdamDefinitionBlockingStub extends io.grpc.stub.AbstractStub<AdamDefinitionBlockingStub>
      implements AdamDefinitionBlockingClient {
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

    @java.lang.Override
    public org.vitrivr.adampro.grpc.AdamGrpc.AckMessage createEntity(org.vitrivr.adampro.grpc.AdamGrpc.CreateEntityMessage request) {
      return blockingUnaryCall(
          getChannel(), METHOD_CREATE_ENTITY, getCallOptions(), request);
    }

    @java.lang.Override
    public org.vitrivr.adampro.grpc.AdamGrpc.AvailableAttributeTypesMessage availableAttributeTypes(org.vitrivr.adampro.grpc.AdamGrpc.EmptyMessage request) {
      return blockingUnaryCall(
          getChannel(), METHOD_AVAILABLE_ATTRIBUTE_TYPES, getCallOptions(), request);
    }

    @java.lang.Override
    public org.vitrivr.adampro.grpc.AdamGrpc.AckMessage count(org.vitrivr.adampro.grpc.AdamGrpc.EntityNameMessage request) {
      return blockingUnaryCall(
          getChannel(), METHOD_COUNT, getCallOptions(), request);
    }

    @java.lang.Override
    public org.vitrivr.adampro.grpc.AdamGrpc.AckMessage dropEntity(org.vitrivr.adampro.grpc.AdamGrpc.EntityNameMessage request) {
      return blockingUnaryCall(
          getChannel(), METHOD_DROP_ENTITY, getCallOptions(), request);
    }

    @java.lang.Override
    public org.vitrivr.adampro.grpc.AdamGrpc.ExistsMessage existsEntity(org.vitrivr.adampro.grpc.AdamGrpc.EntityNameMessage request) {
      return blockingUnaryCall(
          getChannel(), METHOD_EXISTS_ENTITY, getCallOptions(), request);
    }

    @java.lang.Override
    public org.vitrivr.adampro.grpc.AdamGrpc.AckMessage insert(org.vitrivr.adampro.grpc.AdamGrpc.InsertMessage request) {
      return blockingUnaryCall(
          getChannel(), METHOD_INSERT, getCallOptions(), request);
    }

    @java.lang.Override
    public org.vitrivr.adampro.grpc.AdamGrpc.AckMessage getNextPkValue(org.vitrivr.adampro.grpc.AdamGrpc.EntityNameMessage request) {
      return blockingUnaryCall(
          getChannel(), METHOD_GET_NEXT_PK_VALUE, getCallOptions(), request);
    }

    @java.lang.Override
    public org.vitrivr.adampro.grpc.AdamGrpc.AckMessage delete(org.vitrivr.adampro.grpc.AdamGrpc.DeleteMessage request) {
      return blockingUnaryCall(
          getChannel(), METHOD_DELETE, getCallOptions(), request);
    }

    @java.lang.Override
    public org.vitrivr.adampro.grpc.AdamGrpc.AckMessage index(org.vitrivr.adampro.grpc.AdamGrpc.IndexMessage request) {
      return blockingUnaryCall(
          getChannel(), METHOD_INDEX, getCallOptions(), request);
    }

    @java.lang.Override
    public org.vitrivr.adampro.grpc.AdamGrpc.AckMessage generateAllIndexes(org.vitrivr.adampro.grpc.AdamGrpc.IndexMessage request) {
      return blockingUnaryCall(
          getChannel(), METHOD_GENERATE_ALL_INDEXES, getCallOptions(), request);
    }

    @java.lang.Override
    public org.vitrivr.adampro.grpc.AdamGrpc.ExistsMessage existsIndex(org.vitrivr.adampro.grpc.AdamGrpc.IndexMessage request) {
      return blockingUnaryCall(
          getChannel(), METHOD_EXISTS_INDEX, getCallOptions(), request);
    }

    @java.lang.Override
    public org.vitrivr.adampro.grpc.AdamGrpc.AckMessage dropIndex(org.vitrivr.adampro.grpc.AdamGrpc.IndexNameMessage request) {
      return blockingUnaryCall(
          getChannel(), METHOD_DROP_INDEX, getCallOptions(), request);
    }

    @java.lang.Override
    public org.vitrivr.adampro.grpc.AdamGrpc.IndexesMessage listIndexes(org.vitrivr.adampro.grpc.AdamGrpc.EntityNameMessage request) {
      return blockingUnaryCall(
          getChannel(), METHOD_LIST_INDEXES, getCallOptions(), request);
    }

    @java.lang.Override
    public org.vitrivr.adampro.grpc.AdamGrpc.AckMessage generateRandomData(org.vitrivr.adampro.grpc.AdamGrpc.GenerateRandomDataMessage request) {
      return blockingUnaryCall(
          getChannel(), METHOD_GENERATE_RANDOM_DATA, getCallOptions(), request);
    }

    @java.lang.Override
    public org.vitrivr.adampro.grpc.AdamGrpc.EntitiesMessage listEntities(org.vitrivr.adampro.grpc.AdamGrpc.EmptyMessage request) {
      return blockingUnaryCall(
          getChannel(), METHOD_LIST_ENTITIES, getCallOptions(), request);
    }

    @java.lang.Override
    public org.vitrivr.adampro.grpc.AdamGrpc.PropertiesMessage getEntityProperties(org.vitrivr.adampro.grpc.AdamGrpc.EntityPropertiesMessage request) {
      return blockingUnaryCall(
          getChannel(), METHOD_GET_ENTITY_PROPERTIES, getCallOptions(), request);
    }

    @java.lang.Override
    public org.vitrivr.adampro.grpc.AdamGrpc.PropertiesMessage getAttributeProperties(org.vitrivr.adampro.grpc.AdamGrpc.AttributePropertiesMessage request) {
      return blockingUnaryCall(
          getChannel(), METHOD_GET_ATTRIBUTE_PROPERTIES, getCallOptions(), request);
    }

    @java.lang.Override
    public org.vitrivr.adampro.grpc.AdamGrpc.PropertiesMessage getIndexProperties(org.vitrivr.adampro.grpc.AdamGrpc.IndexPropertiesMessage request) {
      return blockingUnaryCall(
          getChannel(), METHOD_GET_INDEX_PROPERTIES, getCallOptions(), request);
    }

    @java.lang.Override
    public org.vitrivr.adampro.grpc.AdamGrpc.AckMessage repartitionEntityData(org.vitrivr.adampro.grpc.AdamGrpc.RepartitionMessage request) {
      return blockingUnaryCall(
          getChannel(), METHOD_REPARTITION_ENTITY_DATA, getCallOptions(), request);
    }

    @java.lang.Override
    public org.vitrivr.adampro.grpc.AdamGrpc.AckMessage repartitionIndexData(org.vitrivr.adampro.grpc.AdamGrpc.RepartitionMessage request) {
      return blockingUnaryCall(
          getChannel(), METHOD_REPARTITION_INDEX_DATA, getCallOptions(), request);
    }

    @java.lang.Override
    public org.vitrivr.adampro.grpc.AdamGrpc.AckMessage resetScanWeights(org.vitrivr.adampro.grpc.AdamGrpc.EntityNameMessage request) {
      return blockingUnaryCall(
          getChannel(), METHOD_RESET_SCAN_WEIGHTS, getCallOptions(), request);
    }

    @java.lang.Override
    public org.vitrivr.adampro.grpc.AdamGrpc.AckMessage setScanWeight(org.vitrivr.adampro.grpc.AdamGrpc.WeightMessage request) {
      return blockingUnaryCall(
          getChannel(), METHOD_SET_SCAN_WEIGHT, getCallOptions(), request);
    }

    @java.lang.Override
    public org.vitrivr.adampro.grpc.AdamGrpc.AckMessage adaptScanMethods(org.vitrivr.adampro.grpc.AdamGrpc.AdaptScanMethodsMessage request) {
      return blockingUnaryCall(
          getChannel(), METHOD_ADAPT_SCAN_METHODS, getCallOptions(), request);
    }

    @java.lang.Override
    public org.vitrivr.adampro.grpc.AdamGrpc.AckMessage sparsifyEntity(org.vitrivr.adampro.grpc.AdamGrpc.SparsifyEntityMessage request) {
      return blockingUnaryCall(
          getChannel(), METHOD_SPARSIFY_ENTITY, getCallOptions(), request);
    }

    @java.lang.Override
    public org.vitrivr.adampro.grpc.AdamGrpc.AckMessage importData(org.vitrivr.adampro.grpc.AdamGrpc.ImportMessage request) {
      return blockingUnaryCall(
          getChannel(), METHOD_IMPORT_DATA, getCallOptions(), request);
    }

    @java.lang.Override
    public org.vitrivr.adampro.grpc.AdamGrpc.AckMessage importDataFile(org.vitrivr.adampro.grpc.AdamGrpc.ImportDataFileMessage request) {
      return blockingUnaryCall(
          getChannel(), METHOD_IMPORT_DATA_FILE, getCallOptions(), request);
    }

    @java.lang.Override
    public org.vitrivr.adampro.grpc.AdamGrpc.ExportDataFileMessage exportDataFile(org.vitrivr.adampro.grpc.AdamGrpc.EntityNameMessage request) {
      return blockingUnaryCall(
          getChannel(), METHOD_EXPORT_DATA_FILE, getCallOptions(), request);
    }

    @java.lang.Override
    public org.vitrivr.adampro.grpc.AdamGrpc.StorageHandlersMessage listStorageHandlers(org.vitrivr.adampro.grpc.AdamGrpc.EmptyMessage request) {
      return blockingUnaryCall(
          getChannel(), METHOD_LIST_STORAGE_HANDLERS, getCallOptions(), request);
    }
  }

  public static class AdamDefinitionFutureStub extends io.grpc.stub.AbstractStub<AdamDefinitionFutureStub>
      implements AdamDefinitionFutureClient {
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

    @java.lang.Override
    public com.google.common.util.concurrent.ListenableFuture<org.vitrivr.adampro.grpc.AdamGrpc.AckMessage> createEntity(
        org.vitrivr.adampro.grpc.AdamGrpc.CreateEntityMessage request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_CREATE_ENTITY, getCallOptions()), request);
    }

    @java.lang.Override
    public com.google.common.util.concurrent.ListenableFuture<org.vitrivr.adampro.grpc.AdamGrpc.AvailableAttributeTypesMessage> availableAttributeTypes(
        org.vitrivr.adampro.grpc.AdamGrpc.EmptyMessage request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_AVAILABLE_ATTRIBUTE_TYPES, getCallOptions()), request);
    }

    @java.lang.Override
    public com.google.common.util.concurrent.ListenableFuture<org.vitrivr.adampro.grpc.AdamGrpc.AckMessage> count(
        org.vitrivr.adampro.grpc.AdamGrpc.EntityNameMessage request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_COUNT, getCallOptions()), request);
    }

    @java.lang.Override
    public com.google.common.util.concurrent.ListenableFuture<org.vitrivr.adampro.grpc.AdamGrpc.AckMessage> dropEntity(
        org.vitrivr.adampro.grpc.AdamGrpc.EntityNameMessage request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_DROP_ENTITY, getCallOptions()), request);
    }

    @java.lang.Override
    public com.google.common.util.concurrent.ListenableFuture<org.vitrivr.adampro.grpc.AdamGrpc.ExistsMessage> existsEntity(
        org.vitrivr.adampro.grpc.AdamGrpc.EntityNameMessage request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_EXISTS_ENTITY, getCallOptions()), request);
    }

    @java.lang.Override
    public com.google.common.util.concurrent.ListenableFuture<org.vitrivr.adampro.grpc.AdamGrpc.AckMessage> insert(
        org.vitrivr.adampro.grpc.AdamGrpc.InsertMessage request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_INSERT, getCallOptions()), request);
    }

    @java.lang.Override
    public com.google.common.util.concurrent.ListenableFuture<org.vitrivr.adampro.grpc.AdamGrpc.AckMessage> getNextPkValue(
        org.vitrivr.adampro.grpc.AdamGrpc.EntityNameMessage request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_GET_NEXT_PK_VALUE, getCallOptions()), request);
    }

    @java.lang.Override
    public com.google.common.util.concurrent.ListenableFuture<org.vitrivr.adampro.grpc.AdamGrpc.AckMessage> delete(
        org.vitrivr.adampro.grpc.AdamGrpc.DeleteMessage request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_DELETE, getCallOptions()), request);
    }

    @java.lang.Override
    public com.google.common.util.concurrent.ListenableFuture<org.vitrivr.adampro.grpc.AdamGrpc.AckMessage> index(
        org.vitrivr.adampro.grpc.AdamGrpc.IndexMessage request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_INDEX, getCallOptions()), request);
    }

    @java.lang.Override
    public com.google.common.util.concurrent.ListenableFuture<org.vitrivr.adampro.grpc.AdamGrpc.AckMessage> generateAllIndexes(
        org.vitrivr.adampro.grpc.AdamGrpc.IndexMessage request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_GENERATE_ALL_INDEXES, getCallOptions()), request);
    }

    @java.lang.Override
    public com.google.common.util.concurrent.ListenableFuture<org.vitrivr.adampro.grpc.AdamGrpc.ExistsMessage> existsIndex(
        org.vitrivr.adampro.grpc.AdamGrpc.IndexMessage request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_EXISTS_INDEX, getCallOptions()), request);
    }

    @java.lang.Override
    public com.google.common.util.concurrent.ListenableFuture<org.vitrivr.adampro.grpc.AdamGrpc.AckMessage> dropIndex(
        org.vitrivr.adampro.grpc.AdamGrpc.IndexNameMessage request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_DROP_INDEX, getCallOptions()), request);
    }

    @java.lang.Override
    public com.google.common.util.concurrent.ListenableFuture<org.vitrivr.adampro.grpc.AdamGrpc.IndexesMessage> listIndexes(
        org.vitrivr.adampro.grpc.AdamGrpc.EntityNameMessage request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_LIST_INDEXES, getCallOptions()), request);
    }

    @java.lang.Override
    public com.google.common.util.concurrent.ListenableFuture<org.vitrivr.adampro.grpc.AdamGrpc.AckMessage> generateRandomData(
        org.vitrivr.adampro.grpc.AdamGrpc.GenerateRandomDataMessage request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_GENERATE_RANDOM_DATA, getCallOptions()), request);
    }

    @java.lang.Override
    public com.google.common.util.concurrent.ListenableFuture<org.vitrivr.adampro.grpc.AdamGrpc.EntitiesMessage> listEntities(
        org.vitrivr.adampro.grpc.AdamGrpc.EmptyMessage request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_LIST_ENTITIES, getCallOptions()), request);
    }

    @java.lang.Override
    public com.google.common.util.concurrent.ListenableFuture<org.vitrivr.adampro.grpc.AdamGrpc.PropertiesMessage> getEntityProperties(
        org.vitrivr.adampro.grpc.AdamGrpc.EntityPropertiesMessage request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_GET_ENTITY_PROPERTIES, getCallOptions()), request);
    }

    @java.lang.Override
    public com.google.common.util.concurrent.ListenableFuture<org.vitrivr.adampro.grpc.AdamGrpc.PropertiesMessage> getAttributeProperties(
        org.vitrivr.adampro.grpc.AdamGrpc.AttributePropertiesMessage request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_GET_ATTRIBUTE_PROPERTIES, getCallOptions()), request);
    }

    @java.lang.Override
    public com.google.common.util.concurrent.ListenableFuture<org.vitrivr.adampro.grpc.AdamGrpc.PropertiesMessage> getIndexProperties(
        org.vitrivr.adampro.grpc.AdamGrpc.IndexPropertiesMessage request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_GET_INDEX_PROPERTIES, getCallOptions()), request);
    }

    @java.lang.Override
    public com.google.common.util.concurrent.ListenableFuture<org.vitrivr.adampro.grpc.AdamGrpc.AckMessage> repartitionEntityData(
        org.vitrivr.adampro.grpc.AdamGrpc.RepartitionMessage request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_REPARTITION_ENTITY_DATA, getCallOptions()), request);
    }

    @java.lang.Override
    public com.google.common.util.concurrent.ListenableFuture<org.vitrivr.adampro.grpc.AdamGrpc.AckMessage> repartitionIndexData(
        org.vitrivr.adampro.grpc.AdamGrpc.RepartitionMessage request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_REPARTITION_INDEX_DATA, getCallOptions()), request);
    }

    @java.lang.Override
    public com.google.common.util.concurrent.ListenableFuture<org.vitrivr.adampro.grpc.AdamGrpc.AckMessage> resetScanWeights(
        org.vitrivr.adampro.grpc.AdamGrpc.EntityNameMessage request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_RESET_SCAN_WEIGHTS, getCallOptions()), request);
    }

    @java.lang.Override
    public com.google.common.util.concurrent.ListenableFuture<org.vitrivr.adampro.grpc.AdamGrpc.AckMessage> setScanWeight(
        org.vitrivr.adampro.grpc.AdamGrpc.WeightMessage request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_SET_SCAN_WEIGHT, getCallOptions()), request);
    }

    @java.lang.Override
    public com.google.common.util.concurrent.ListenableFuture<org.vitrivr.adampro.grpc.AdamGrpc.AckMessage> adaptScanMethods(
        org.vitrivr.adampro.grpc.AdamGrpc.AdaptScanMethodsMessage request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_ADAPT_SCAN_METHODS, getCallOptions()), request);
    }

    @java.lang.Override
    public com.google.common.util.concurrent.ListenableFuture<org.vitrivr.adampro.grpc.AdamGrpc.AckMessage> sparsifyEntity(
        org.vitrivr.adampro.grpc.AdamGrpc.SparsifyEntityMessage request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_SPARSIFY_ENTITY, getCallOptions()), request);
    }

    @java.lang.Override
    public com.google.common.util.concurrent.ListenableFuture<org.vitrivr.adampro.grpc.AdamGrpc.AckMessage> importData(
        org.vitrivr.adampro.grpc.AdamGrpc.ImportMessage request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_IMPORT_DATA, getCallOptions()), request);
    }

    @java.lang.Override
    public com.google.common.util.concurrent.ListenableFuture<org.vitrivr.adampro.grpc.AdamGrpc.AckMessage> importDataFile(
        org.vitrivr.adampro.grpc.AdamGrpc.ImportDataFileMessage request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_IMPORT_DATA_FILE, getCallOptions()), request);
    }

    @java.lang.Override
    public com.google.common.util.concurrent.ListenableFuture<org.vitrivr.adampro.grpc.AdamGrpc.ExportDataFileMessage> exportDataFile(
        org.vitrivr.adampro.grpc.AdamGrpc.EntityNameMessage request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_EXPORT_DATA_FILE, getCallOptions()), request);
    }

    @java.lang.Override
    public com.google.common.util.concurrent.ListenableFuture<org.vitrivr.adampro.grpc.AdamGrpc.StorageHandlersMessage> listStorageHandlers(
        org.vitrivr.adampro.grpc.AdamGrpc.EmptyMessage request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_LIST_STORAGE_HANDLERS, getCallOptions()), request);
    }
  }

  private static final int METHODID_CREATE_ENTITY = 0;
  private static final int METHODID_AVAILABLE_ATTRIBUTE_TYPES = 1;
  private static final int METHODID_COUNT = 2;
  private static final int METHODID_DROP_ENTITY = 3;
  private static final int METHODID_EXISTS_ENTITY = 4;
  private static final int METHODID_INSERT = 5;
  private static final int METHODID_GET_NEXT_PK_VALUE = 6;
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
  private static final int METHODID_RESET_SCAN_WEIGHTS = 20;
  private static final int METHODID_SET_SCAN_WEIGHT = 21;
  private static final int METHODID_ADAPT_SCAN_METHODS = 22;
  private static final int METHODID_SPARSIFY_ENTITY = 23;
  private static final int METHODID_IMPORT_DATA = 24;
  private static final int METHODID_IMPORT_DATA_FILE = 25;
  private static final int METHODID_EXPORT_DATA_FILE = 26;
  private static final int METHODID_LIST_STORAGE_HANDLERS = 27;
  private static final int METHODID_STREAM_INSERT = 28;

  private static class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final AdamDefinition serviceImpl;
    private final int methodId;

    public MethodHandlers(AdamDefinition serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

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
        case METHODID_GET_NEXT_PK_VALUE:
          serviceImpl.getNextPkValue((org.vitrivr.adampro.grpc.AdamGrpc.EntityNameMessage) request,
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
          serviceImpl.existsIndex((org.vitrivr.adampro.grpc.AdamGrpc.IndexMessage) request,
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
        case METHODID_RESET_SCAN_WEIGHTS:
          serviceImpl.resetScanWeights((org.vitrivr.adampro.grpc.AdamGrpc.EntityNameMessage) request,
              (io.grpc.stub.StreamObserver<org.vitrivr.adampro.grpc.AdamGrpc.AckMessage>) responseObserver);
          break;
        case METHODID_SET_SCAN_WEIGHT:
          serviceImpl.setScanWeight((org.vitrivr.adampro.grpc.AdamGrpc.WeightMessage) request,
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
        case METHODID_IMPORT_DATA_FILE:
          serviceImpl.importDataFile((org.vitrivr.adampro.grpc.AdamGrpc.ImportDataFileMessage) request,
              (io.grpc.stub.StreamObserver<org.vitrivr.adampro.grpc.AdamGrpc.AckMessage>) responseObserver);
          break;
        case METHODID_EXPORT_DATA_FILE:
          serviceImpl.exportDataFile((org.vitrivr.adampro.grpc.AdamGrpc.EntityNameMessage) request,
              (io.grpc.stub.StreamObserver<org.vitrivr.adampro.grpc.AdamGrpc.ExportDataFileMessage>) responseObserver);
          break;
        case METHODID_LIST_STORAGE_HANDLERS:
          serviceImpl.listStorageHandlers((org.vitrivr.adampro.grpc.AdamGrpc.EmptyMessage) request,
              (io.grpc.stub.StreamObserver<org.vitrivr.adampro.grpc.AdamGrpc.StorageHandlersMessage>) responseObserver);
          break;
        default:
          throw new AssertionError();
      }
    }

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

  public static io.grpc.ServerServiceDefinition bindService(
      final AdamDefinition serviceImpl) {
    return io.grpc.ServerServiceDefinition.builder(SERVICE_NAME)
        .addMethod(
          METHOD_CREATE_ENTITY,
          asyncUnaryCall(
            new MethodHandlers<
              org.vitrivr.adampro.grpc.AdamGrpc.CreateEntityMessage,
              org.vitrivr.adampro.grpc.AdamGrpc.AckMessage>(
                serviceImpl, METHODID_CREATE_ENTITY)))
        .addMethod(
          METHOD_AVAILABLE_ATTRIBUTE_TYPES,
          asyncUnaryCall(
            new MethodHandlers<
              org.vitrivr.adampro.grpc.AdamGrpc.EmptyMessage,
              org.vitrivr.adampro.grpc.AdamGrpc.AvailableAttributeTypesMessage>(
                serviceImpl, METHODID_AVAILABLE_ATTRIBUTE_TYPES)))
        .addMethod(
          METHOD_COUNT,
          asyncUnaryCall(
            new MethodHandlers<
              org.vitrivr.adampro.grpc.AdamGrpc.EntityNameMessage,
              org.vitrivr.adampro.grpc.AdamGrpc.AckMessage>(
                serviceImpl, METHODID_COUNT)))
        .addMethod(
          METHOD_DROP_ENTITY,
          asyncUnaryCall(
            new MethodHandlers<
              org.vitrivr.adampro.grpc.AdamGrpc.EntityNameMessage,
              org.vitrivr.adampro.grpc.AdamGrpc.AckMessage>(
                serviceImpl, METHODID_DROP_ENTITY)))
        .addMethod(
          METHOD_EXISTS_ENTITY,
          asyncUnaryCall(
            new MethodHandlers<
              org.vitrivr.adampro.grpc.AdamGrpc.EntityNameMessage,
              org.vitrivr.adampro.grpc.AdamGrpc.ExistsMessage>(
                serviceImpl, METHODID_EXISTS_ENTITY)))
        .addMethod(
          METHOD_INSERT,
          asyncUnaryCall(
            new MethodHandlers<
              org.vitrivr.adampro.grpc.AdamGrpc.InsertMessage,
              org.vitrivr.adampro.grpc.AdamGrpc.AckMessage>(
                serviceImpl, METHODID_INSERT)))
        .addMethod(
          METHOD_STREAM_INSERT,
          asyncBidiStreamingCall(
            new MethodHandlers<
              org.vitrivr.adampro.grpc.AdamGrpc.InsertMessage,
              org.vitrivr.adampro.grpc.AdamGrpc.AckMessage>(
                serviceImpl, METHODID_STREAM_INSERT)))
        .addMethod(
          METHOD_GET_NEXT_PK_VALUE,
          asyncUnaryCall(
            new MethodHandlers<
              org.vitrivr.adampro.grpc.AdamGrpc.EntityNameMessage,
              org.vitrivr.adampro.grpc.AdamGrpc.AckMessage>(
                serviceImpl, METHODID_GET_NEXT_PK_VALUE)))
        .addMethod(
          METHOD_DELETE,
          asyncUnaryCall(
            new MethodHandlers<
              org.vitrivr.adampro.grpc.AdamGrpc.DeleteMessage,
              org.vitrivr.adampro.grpc.AdamGrpc.AckMessage>(
                serviceImpl, METHODID_DELETE)))
        .addMethod(
          METHOD_INDEX,
          asyncUnaryCall(
            new MethodHandlers<
              org.vitrivr.adampro.grpc.AdamGrpc.IndexMessage,
              org.vitrivr.adampro.grpc.AdamGrpc.AckMessage>(
                serviceImpl, METHODID_INDEX)))
        .addMethod(
          METHOD_GENERATE_ALL_INDEXES,
          asyncUnaryCall(
            new MethodHandlers<
              org.vitrivr.adampro.grpc.AdamGrpc.IndexMessage,
              org.vitrivr.adampro.grpc.AdamGrpc.AckMessage>(
                serviceImpl, METHODID_GENERATE_ALL_INDEXES)))
        .addMethod(
          METHOD_EXISTS_INDEX,
          asyncUnaryCall(
            new MethodHandlers<
              org.vitrivr.adampro.grpc.AdamGrpc.IndexMessage,
              org.vitrivr.adampro.grpc.AdamGrpc.ExistsMessage>(
                serviceImpl, METHODID_EXISTS_INDEX)))
        .addMethod(
          METHOD_DROP_INDEX,
          asyncUnaryCall(
            new MethodHandlers<
              org.vitrivr.adampro.grpc.AdamGrpc.IndexNameMessage,
              org.vitrivr.adampro.grpc.AdamGrpc.AckMessage>(
                serviceImpl, METHODID_DROP_INDEX)))
        .addMethod(
          METHOD_LIST_INDEXES,
          asyncUnaryCall(
            new MethodHandlers<
              org.vitrivr.adampro.grpc.AdamGrpc.EntityNameMessage,
              org.vitrivr.adampro.grpc.AdamGrpc.IndexesMessage>(
                serviceImpl, METHODID_LIST_INDEXES)))
        .addMethod(
          METHOD_GENERATE_RANDOM_DATA,
          asyncUnaryCall(
            new MethodHandlers<
              org.vitrivr.adampro.grpc.AdamGrpc.GenerateRandomDataMessage,
              org.vitrivr.adampro.grpc.AdamGrpc.AckMessage>(
                serviceImpl, METHODID_GENERATE_RANDOM_DATA)))
        .addMethod(
          METHOD_LIST_ENTITIES,
          asyncUnaryCall(
            new MethodHandlers<
              org.vitrivr.adampro.grpc.AdamGrpc.EmptyMessage,
              org.vitrivr.adampro.grpc.AdamGrpc.EntitiesMessage>(
                serviceImpl, METHODID_LIST_ENTITIES)))
        .addMethod(
          METHOD_GET_ENTITY_PROPERTIES,
          asyncUnaryCall(
            new MethodHandlers<
              org.vitrivr.adampro.grpc.AdamGrpc.EntityPropertiesMessage,
              org.vitrivr.adampro.grpc.AdamGrpc.PropertiesMessage>(
                serviceImpl, METHODID_GET_ENTITY_PROPERTIES)))
        .addMethod(
          METHOD_GET_ATTRIBUTE_PROPERTIES,
          asyncUnaryCall(
            new MethodHandlers<
              org.vitrivr.adampro.grpc.AdamGrpc.AttributePropertiesMessage,
              org.vitrivr.adampro.grpc.AdamGrpc.PropertiesMessage>(
                serviceImpl, METHODID_GET_ATTRIBUTE_PROPERTIES)))
        .addMethod(
          METHOD_GET_INDEX_PROPERTIES,
          asyncUnaryCall(
            new MethodHandlers<
              org.vitrivr.adampro.grpc.AdamGrpc.IndexPropertiesMessage,
              org.vitrivr.adampro.grpc.AdamGrpc.PropertiesMessage>(
                serviceImpl, METHODID_GET_INDEX_PROPERTIES)))
        .addMethod(
          METHOD_REPARTITION_ENTITY_DATA,
          asyncUnaryCall(
            new MethodHandlers<
              org.vitrivr.adampro.grpc.AdamGrpc.RepartitionMessage,
              org.vitrivr.adampro.grpc.AdamGrpc.AckMessage>(
                serviceImpl, METHODID_REPARTITION_ENTITY_DATA)))
        .addMethod(
          METHOD_REPARTITION_INDEX_DATA,
          asyncUnaryCall(
            new MethodHandlers<
              org.vitrivr.adampro.grpc.AdamGrpc.RepartitionMessage,
              org.vitrivr.adampro.grpc.AdamGrpc.AckMessage>(
                serviceImpl, METHODID_REPARTITION_INDEX_DATA)))
        .addMethod(
          METHOD_RESET_SCAN_WEIGHTS,
          asyncUnaryCall(
            new MethodHandlers<
              org.vitrivr.adampro.grpc.AdamGrpc.EntityNameMessage,
              org.vitrivr.adampro.grpc.AdamGrpc.AckMessage>(
                serviceImpl, METHODID_RESET_SCAN_WEIGHTS)))
        .addMethod(
          METHOD_SET_SCAN_WEIGHT,
          asyncUnaryCall(
            new MethodHandlers<
              org.vitrivr.adampro.grpc.AdamGrpc.WeightMessage,
              org.vitrivr.adampro.grpc.AdamGrpc.AckMessage>(
                serviceImpl, METHODID_SET_SCAN_WEIGHT)))
        .addMethod(
          METHOD_ADAPT_SCAN_METHODS,
          asyncUnaryCall(
            new MethodHandlers<
              org.vitrivr.adampro.grpc.AdamGrpc.AdaptScanMethodsMessage,
              org.vitrivr.adampro.grpc.AdamGrpc.AckMessage>(
                serviceImpl, METHODID_ADAPT_SCAN_METHODS)))
        .addMethod(
          METHOD_SPARSIFY_ENTITY,
          asyncUnaryCall(
            new MethodHandlers<
              org.vitrivr.adampro.grpc.AdamGrpc.SparsifyEntityMessage,
              org.vitrivr.adampro.grpc.AdamGrpc.AckMessage>(
                serviceImpl, METHODID_SPARSIFY_ENTITY)))
        .addMethod(
          METHOD_IMPORT_DATA,
          asyncUnaryCall(
            new MethodHandlers<
              org.vitrivr.adampro.grpc.AdamGrpc.ImportMessage,
              org.vitrivr.adampro.grpc.AdamGrpc.AckMessage>(
                serviceImpl, METHODID_IMPORT_DATA)))
        .addMethod(
          METHOD_IMPORT_DATA_FILE,
          asyncUnaryCall(
            new MethodHandlers<
              org.vitrivr.adampro.grpc.AdamGrpc.ImportDataFileMessage,
              org.vitrivr.adampro.grpc.AdamGrpc.AckMessage>(
                serviceImpl, METHODID_IMPORT_DATA_FILE)))
        .addMethod(
          METHOD_EXPORT_DATA_FILE,
          asyncUnaryCall(
            new MethodHandlers<
              org.vitrivr.adampro.grpc.AdamGrpc.EntityNameMessage,
              org.vitrivr.adampro.grpc.AdamGrpc.ExportDataFileMessage>(
                serviceImpl, METHODID_EXPORT_DATA_FILE)))
        .addMethod(
          METHOD_LIST_STORAGE_HANDLERS,
          asyncUnaryCall(
            new MethodHandlers<
              org.vitrivr.adampro.grpc.AdamGrpc.EmptyMessage,
              org.vitrivr.adampro.grpc.AdamGrpc.StorageHandlersMessage>(
                serviceImpl, METHODID_LIST_STORAGE_HANDLERS)))
        .build();
  }
}
