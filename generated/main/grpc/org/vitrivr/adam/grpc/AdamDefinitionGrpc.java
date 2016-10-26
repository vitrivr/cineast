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
public class AdamDefinitionGrpc {

  private AdamDefinitionGrpc() {}

  public static final String SERVICE_NAME = "AdamDefinition";

  // Static method descriptors that strictly reflect the proto.
  @io.grpc.ExperimentalApi
  public static final io.grpc.MethodDescriptor<org.vitrivr.adam.grpc.AdamGrpc.CreateEntityMessage,
      org.vitrivr.adam.grpc.AdamGrpc.AckMessage> METHOD_CREATE_ENTITY =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "AdamDefinition", "CreateEntity"),
          io.grpc.protobuf.ProtoUtils.marshaller(org.vitrivr.adam.grpc.AdamGrpc.CreateEntityMessage.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(org.vitrivr.adam.grpc.AdamGrpc.AckMessage.getDefaultInstance()));
  @io.grpc.ExperimentalApi
  public static final io.grpc.MethodDescriptor<org.vitrivr.adam.grpc.AdamGrpc.EmptyMessage,
      org.vitrivr.adam.grpc.AdamGrpc.AvailableAttributeTypesMessage> METHOD_AVAILABLE_ATTRIBUTE_TYPES =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "AdamDefinition", "AvailableAttributeTypes"),
          io.grpc.protobuf.ProtoUtils.marshaller(org.vitrivr.adam.grpc.AdamGrpc.EmptyMessage.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(org.vitrivr.adam.grpc.AdamGrpc.AvailableAttributeTypesMessage.getDefaultInstance()));
  @io.grpc.ExperimentalApi
  public static final io.grpc.MethodDescriptor<org.vitrivr.adam.grpc.AdamGrpc.EntityNameMessage,
      org.vitrivr.adam.grpc.AdamGrpc.AckMessage> METHOD_COUNT =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "AdamDefinition", "Count"),
          io.grpc.protobuf.ProtoUtils.marshaller(org.vitrivr.adam.grpc.AdamGrpc.EntityNameMessage.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(org.vitrivr.adam.grpc.AdamGrpc.AckMessage.getDefaultInstance()));
  @io.grpc.ExperimentalApi
  public static final io.grpc.MethodDescriptor<org.vitrivr.adam.grpc.AdamGrpc.EntityNameMessage,
      org.vitrivr.adam.grpc.AdamGrpc.AckMessage> METHOD_DROP_ENTITY =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "AdamDefinition", "DropEntity"),
          io.grpc.protobuf.ProtoUtils.marshaller(org.vitrivr.adam.grpc.AdamGrpc.EntityNameMessage.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(org.vitrivr.adam.grpc.AdamGrpc.AckMessage.getDefaultInstance()));
  @io.grpc.ExperimentalApi
  public static final io.grpc.MethodDescriptor<org.vitrivr.adam.grpc.AdamGrpc.EntityNameMessage,
      org.vitrivr.adam.grpc.AdamGrpc.ExistsMessage> METHOD_EXISTS_ENTITY =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "AdamDefinition", "ExistsEntity"),
          io.grpc.protobuf.ProtoUtils.marshaller(org.vitrivr.adam.grpc.AdamGrpc.EntityNameMessage.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(org.vitrivr.adam.grpc.AdamGrpc.ExistsMessage.getDefaultInstance()));
  @io.grpc.ExperimentalApi
  public static final io.grpc.MethodDescriptor<org.vitrivr.adam.grpc.AdamGrpc.InsertMessage,
      org.vitrivr.adam.grpc.AdamGrpc.AckMessage> METHOD_INSERT =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.CLIENT_STREAMING,
          generateFullMethodName(
              "AdamDefinition", "Insert"),
          io.grpc.protobuf.ProtoUtils.marshaller(org.vitrivr.adam.grpc.AdamGrpc.InsertMessage.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(org.vitrivr.adam.grpc.AdamGrpc.AckMessage.getDefaultInstance()));
  @io.grpc.ExperimentalApi
  public static final io.grpc.MethodDescriptor<org.vitrivr.adam.grpc.AdamGrpc.DeleteMessage,
      org.vitrivr.adam.grpc.AdamGrpc.AckMessage> METHOD_DELETE =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "AdamDefinition", "Delete"),
          io.grpc.protobuf.ProtoUtils.marshaller(org.vitrivr.adam.grpc.AdamGrpc.DeleteMessage.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(org.vitrivr.adam.grpc.AdamGrpc.AckMessage.getDefaultInstance()));
  @io.grpc.ExperimentalApi
  public static final io.grpc.MethodDescriptor<org.vitrivr.adam.grpc.AdamGrpc.IndexMessage,
      org.vitrivr.adam.grpc.AdamGrpc.AckMessage> METHOD_INDEX =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "AdamDefinition", "Index"),
          io.grpc.protobuf.ProtoUtils.marshaller(org.vitrivr.adam.grpc.AdamGrpc.IndexMessage.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(org.vitrivr.adam.grpc.AdamGrpc.AckMessage.getDefaultInstance()));
  @io.grpc.ExperimentalApi
  public static final io.grpc.MethodDescriptor<org.vitrivr.adam.grpc.AdamGrpc.IndexMessage,
      org.vitrivr.adam.grpc.AdamGrpc.AckMessage> METHOD_GENERATE_ALL_INDEXES =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "AdamDefinition", "GenerateAllIndexes"),
          io.grpc.protobuf.ProtoUtils.marshaller(org.vitrivr.adam.grpc.AdamGrpc.IndexMessage.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(org.vitrivr.adam.grpc.AdamGrpc.AckMessage.getDefaultInstance()));
  @io.grpc.ExperimentalApi
  public static final io.grpc.MethodDescriptor<org.vitrivr.adam.grpc.AdamGrpc.IndexMessage,
      org.vitrivr.adam.grpc.AdamGrpc.ExistsMessage> METHOD_EXISTS_INDEX =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "AdamDefinition", "ExistsIndex"),
          io.grpc.protobuf.ProtoUtils.marshaller(org.vitrivr.adam.grpc.AdamGrpc.IndexMessage.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(org.vitrivr.adam.grpc.AdamGrpc.ExistsMessage.getDefaultInstance()));
  @io.grpc.ExperimentalApi
  public static final io.grpc.MethodDescriptor<org.vitrivr.adam.grpc.AdamGrpc.IndexNameMessage,
      org.vitrivr.adam.grpc.AdamGrpc.AckMessage> METHOD_DROP_INDEX =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "AdamDefinition", "DropIndex"),
          io.grpc.protobuf.ProtoUtils.marshaller(org.vitrivr.adam.grpc.AdamGrpc.IndexNameMessage.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(org.vitrivr.adam.grpc.AdamGrpc.AckMessage.getDefaultInstance()));
  @io.grpc.ExperimentalApi
  public static final io.grpc.MethodDescriptor<org.vitrivr.adam.grpc.AdamGrpc.EntityNameMessage,
      org.vitrivr.adam.grpc.AdamGrpc.IndexesMessage> METHOD_LIST_INDEXES =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "AdamDefinition", "ListIndexes"),
          io.grpc.protobuf.ProtoUtils.marshaller(org.vitrivr.adam.grpc.AdamGrpc.EntityNameMessage.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(org.vitrivr.adam.grpc.AdamGrpc.IndexesMessage.getDefaultInstance()));
  @io.grpc.ExperimentalApi
  public static final io.grpc.MethodDescriptor<org.vitrivr.adam.grpc.AdamGrpc.GenerateRandomDataMessage,
      org.vitrivr.adam.grpc.AdamGrpc.AckMessage> METHOD_GENERATE_RANDOM_DATA =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "AdamDefinition", "GenerateRandomData"),
          io.grpc.protobuf.ProtoUtils.marshaller(org.vitrivr.adam.grpc.AdamGrpc.GenerateRandomDataMessage.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(org.vitrivr.adam.grpc.AdamGrpc.AckMessage.getDefaultInstance()));
  @io.grpc.ExperimentalApi
  public static final io.grpc.MethodDescriptor<org.vitrivr.adam.grpc.AdamGrpc.EmptyMessage,
      org.vitrivr.adam.grpc.AdamGrpc.EntitiesMessage> METHOD_LIST_ENTITIES =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "AdamDefinition", "ListEntities"),
          io.grpc.protobuf.ProtoUtils.marshaller(org.vitrivr.adam.grpc.AdamGrpc.EmptyMessage.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(org.vitrivr.adam.grpc.AdamGrpc.EntitiesMessage.getDefaultInstance()));
  @io.grpc.ExperimentalApi
  public static final io.grpc.MethodDescriptor<org.vitrivr.adam.grpc.AdamGrpc.EntityNameMessage,
      org.vitrivr.adam.grpc.AdamGrpc.EntityPropertiesMessage> METHOD_GET_ENTITY_PROPERTIES =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "AdamDefinition", "GetEntityProperties"),
          io.grpc.protobuf.ProtoUtils.marshaller(org.vitrivr.adam.grpc.AdamGrpc.EntityNameMessage.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(org.vitrivr.adam.grpc.AdamGrpc.EntityPropertiesMessage.getDefaultInstance()));
  @io.grpc.ExperimentalApi
  public static final io.grpc.MethodDescriptor<org.vitrivr.adam.grpc.AdamGrpc.RepartitionMessage,
      org.vitrivr.adam.grpc.AdamGrpc.AckMessage> METHOD_REPARTITION_ENTITY_DATA =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "AdamDefinition", "RepartitionEntityData"),
          io.grpc.protobuf.ProtoUtils.marshaller(org.vitrivr.adam.grpc.AdamGrpc.RepartitionMessage.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(org.vitrivr.adam.grpc.AdamGrpc.AckMessage.getDefaultInstance()));
  @io.grpc.ExperimentalApi
  public static final io.grpc.MethodDescriptor<org.vitrivr.adam.grpc.AdamGrpc.RepartitionMessage,
      org.vitrivr.adam.grpc.AdamGrpc.AckMessage> METHOD_REPARTITION_INDEX_DATA =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "AdamDefinition", "RepartitionIndexData"),
          io.grpc.protobuf.ProtoUtils.marshaller(org.vitrivr.adam.grpc.AdamGrpc.RepartitionMessage.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(org.vitrivr.adam.grpc.AdamGrpc.AckMessage.getDefaultInstance()));
  @io.grpc.ExperimentalApi
  public static final io.grpc.MethodDescriptor<org.vitrivr.adam.grpc.AdamGrpc.EntityNameMessage,
      org.vitrivr.adam.grpc.AdamGrpc.AckMessage> METHOD_RESET_SCAN_WEIGHTS =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "AdamDefinition", "ResetScanWeights"),
          io.grpc.protobuf.ProtoUtils.marshaller(org.vitrivr.adam.grpc.AdamGrpc.EntityNameMessage.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(org.vitrivr.adam.grpc.AdamGrpc.AckMessage.getDefaultInstance()));
  @io.grpc.ExperimentalApi
  public static final io.grpc.MethodDescriptor<org.vitrivr.adam.grpc.AdamGrpc.WeightMessage,
      org.vitrivr.adam.grpc.AdamGrpc.AckMessage> METHOD_SET_SCAN_WEIGHT =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "AdamDefinition", "SetScanWeight"),
          io.grpc.protobuf.ProtoUtils.marshaller(org.vitrivr.adam.grpc.AdamGrpc.WeightMessage.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(org.vitrivr.adam.grpc.AdamGrpc.AckMessage.getDefaultInstance()));
  @io.grpc.ExperimentalApi
  public static final io.grpc.MethodDescriptor<org.vitrivr.adam.grpc.AdamGrpc.AdaptScanMethodsMessage,
      org.vitrivr.adam.grpc.AdamGrpc.AckMessage> METHOD_ADAPT_SCAN_METHODS =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "AdamDefinition", "AdaptScanMethods"),
          io.grpc.protobuf.ProtoUtils.marshaller(org.vitrivr.adam.grpc.AdamGrpc.AdaptScanMethodsMessage.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(org.vitrivr.adam.grpc.AdamGrpc.AckMessage.getDefaultInstance()));
  @io.grpc.ExperimentalApi
  public static final io.grpc.MethodDescriptor<org.vitrivr.adam.grpc.AdamGrpc.SparsifyEntityMessage,
      org.vitrivr.adam.grpc.AdamGrpc.AckMessage> METHOD_SPARSIFY_ENTITY =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "AdamDefinition", "SparsifyEntity"),
          io.grpc.protobuf.ProtoUtils.marshaller(org.vitrivr.adam.grpc.AdamGrpc.SparsifyEntityMessage.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(org.vitrivr.adam.grpc.AdamGrpc.AckMessage.getDefaultInstance()));
  @io.grpc.ExperimentalApi
  public static final io.grpc.MethodDescriptor<org.vitrivr.adam.grpc.AdamGrpc.ImportMessage,
      org.vitrivr.adam.grpc.AdamGrpc.AckMessage> METHOD_IMPORT_DATA =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "AdamDefinition", "ImportData"),
          io.grpc.protobuf.ProtoUtils.marshaller(org.vitrivr.adam.grpc.AdamGrpc.ImportMessage.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(org.vitrivr.adam.grpc.AdamGrpc.AckMessage.getDefaultInstance()));
  @io.grpc.ExperimentalApi
  public static final io.grpc.MethodDescriptor<org.vitrivr.adam.grpc.AdamGrpc.ImportDataFileMessage,
      org.vitrivr.adam.grpc.AdamGrpc.AckMessage> METHOD_IMPORT_DATA_FILE =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "AdamDefinition", "ImportDataFile"),
          io.grpc.protobuf.ProtoUtils.marshaller(org.vitrivr.adam.grpc.AdamGrpc.ImportDataFileMessage.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(org.vitrivr.adam.grpc.AdamGrpc.AckMessage.getDefaultInstance()));
  @io.grpc.ExperimentalApi
  public static final io.grpc.MethodDescriptor<org.vitrivr.adam.grpc.AdamGrpc.EntityNameMessage,
      org.vitrivr.adam.grpc.AdamGrpc.ExportDataFileMessage> METHOD_EXPORT_DATA_FILE =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "AdamDefinition", "ExportDataFile"),
          io.grpc.protobuf.ProtoUtils.marshaller(org.vitrivr.adam.grpc.AdamGrpc.EntityNameMessage.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(org.vitrivr.adam.grpc.AdamGrpc.ExportDataFileMessage.getDefaultInstance()));
  @io.grpc.ExperimentalApi
  public static final io.grpc.MethodDescriptor<org.vitrivr.adam.grpc.AdamGrpc.EmptyMessage,
      org.vitrivr.adam.grpc.AdamGrpc.StorageHandlersMessage> METHOD_LIST_STORAGE_HANDLERS =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "AdamDefinition", "ListStorageHandlers"),
          io.grpc.protobuf.ProtoUtils.marshaller(org.vitrivr.adam.grpc.AdamGrpc.EmptyMessage.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(org.vitrivr.adam.grpc.AdamGrpc.StorageHandlersMessage.getDefaultInstance()));

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

    public void createEntity(org.vitrivr.adam.grpc.AdamGrpc.CreateEntityMessage request,
        io.grpc.stub.StreamObserver<org.vitrivr.adam.grpc.AdamGrpc.AckMessage> responseObserver);

    public void availableAttributeTypes(org.vitrivr.adam.grpc.AdamGrpc.EmptyMessage request,
        io.grpc.stub.StreamObserver<org.vitrivr.adam.grpc.AdamGrpc.AvailableAttributeTypesMessage> responseObserver);

    public void count(org.vitrivr.adam.grpc.AdamGrpc.EntityNameMessage request,
        io.grpc.stub.StreamObserver<org.vitrivr.adam.grpc.AdamGrpc.AckMessage> responseObserver);

    public void dropEntity(org.vitrivr.adam.grpc.AdamGrpc.EntityNameMessage request,
        io.grpc.stub.StreamObserver<org.vitrivr.adam.grpc.AdamGrpc.AckMessage> responseObserver);

    public void existsEntity(org.vitrivr.adam.grpc.AdamGrpc.EntityNameMessage request,
        io.grpc.stub.StreamObserver<org.vitrivr.adam.grpc.AdamGrpc.ExistsMessage> responseObserver);

    public io.grpc.stub.StreamObserver<org.vitrivr.adam.grpc.AdamGrpc.InsertMessage> insert(
        io.grpc.stub.StreamObserver<org.vitrivr.adam.grpc.AdamGrpc.AckMessage> responseObserver);

    public void delete(org.vitrivr.adam.grpc.AdamGrpc.DeleteMessage request,
        io.grpc.stub.StreamObserver<org.vitrivr.adam.grpc.AdamGrpc.AckMessage> responseObserver);

    public void index(org.vitrivr.adam.grpc.AdamGrpc.IndexMessage request,
        io.grpc.stub.StreamObserver<org.vitrivr.adam.grpc.AdamGrpc.AckMessage> responseObserver);

    public void generateAllIndexes(org.vitrivr.adam.grpc.AdamGrpc.IndexMessage request,
        io.grpc.stub.StreamObserver<org.vitrivr.adam.grpc.AdamGrpc.AckMessage> responseObserver);

    public void existsIndex(org.vitrivr.adam.grpc.AdamGrpc.IndexMessage request,
        io.grpc.stub.StreamObserver<org.vitrivr.adam.grpc.AdamGrpc.ExistsMessage> responseObserver);

    public void dropIndex(org.vitrivr.adam.grpc.AdamGrpc.IndexNameMessage request,
        io.grpc.stub.StreamObserver<org.vitrivr.adam.grpc.AdamGrpc.AckMessage> responseObserver);

    public void listIndexes(org.vitrivr.adam.grpc.AdamGrpc.EntityNameMessage request,
        io.grpc.stub.StreamObserver<org.vitrivr.adam.grpc.AdamGrpc.IndexesMessage> responseObserver);

    public void generateRandomData(org.vitrivr.adam.grpc.AdamGrpc.GenerateRandomDataMessage request,
        io.grpc.stub.StreamObserver<org.vitrivr.adam.grpc.AdamGrpc.AckMessage> responseObserver);

    public void listEntities(org.vitrivr.adam.grpc.AdamGrpc.EmptyMessage request,
        io.grpc.stub.StreamObserver<org.vitrivr.adam.grpc.AdamGrpc.EntitiesMessage> responseObserver);

    public void getEntityProperties(org.vitrivr.adam.grpc.AdamGrpc.EntityNameMessage request,
        io.grpc.stub.StreamObserver<org.vitrivr.adam.grpc.AdamGrpc.EntityPropertiesMessage> responseObserver);

    public void repartitionEntityData(org.vitrivr.adam.grpc.AdamGrpc.RepartitionMessage request,
        io.grpc.stub.StreamObserver<org.vitrivr.adam.grpc.AdamGrpc.AckMessage> responseObserver);

    public void repartitionIndexData(org.vitrivr.adam.grpc.AdamGrpc.RepartitionMessage request,
        io.grpc.stub.StreamObserver<org.vitrivr.adam.grpc.AdamGrpc.AckMessage> responseObserver);

    public void resetScanWeights(org.vitrivr.adam.grpc.AdamGrpc.EntityNameMessage request,
        io.grpc.stub.StreamObserver<org.vitrivr.adam.grpc.AdamGrpc.AckMessage> responseObserver);

    public void setScanWeight(org.vitrivr.adam.grpc.AdamGrpc.WeightMessage request,
        io.grpc.stub.StreamObserver<org.vitrivr.adam.grpc.AdamGrpc.AckMessage> responseObserver);

    public void adaptScanMethods(org.vitrivr.adam.grpc.AdamGrpc.AdaptScanMethodsMessage request,
        io.grpc.stub.StreamObserver<org.vitrivr.adam.grpc.AdamGrpc.AckMessage> responseObserver);

    public void sparsifyEntity(org.vitrivr.adam.grpc.AdamGrpc.SparsifyEntityMessage request,
        io.grpc.stub.StreamObserver<org.vitrivr.adam.grpc.AdamGrpc.AckMessage> responseObserver);

    public void importData(org.vitrivr.adam.grpc.AdamGrpc.ImportMessage request,
        io.grpc.stub.StreamObserver<org.vitrivr.adam.grpc.AdamGrpc.AckMessage> responseObserver);

    public void importDataFile(org.vitrivr.adam.grpc.AdamGrpc.ImportDataFileMessage request,
        io.grpc.stub.StreamObserver<org.vitrivr.adam.grpc.AdamGrpc.AckMessage> responseObserver);

    public void exportDataFile(org.vitrivr.adam.grpc.AdamGrpc.EntityNameMessage request,
        io.grpc.stub.StreamObserver<org.vitrivr.adam.grpc.AdamGrpc.ExportDataFileMessage> responseObserver);

    public void listStorageHandlers(org.vitrivr.adam.grpc.AdamGrpc.EmptyMessage request,
        io.grpc.stub.StreamObserver<org.vitrivr.adam.grpc.AdamGrpc.StorageHandlersMessage> responseObserver);
  }

  public static interface AdamDefinitionBlockingClient {

    public org.vitrivr.adam.grpc.AdamGrpc.AckMessage createEntity(org.vitrivr.adam.grpc.AdamGrpc.CreateEntityMessage request);

    public org.vitrivr.adam.grpc.AdamGrpc.AvailableAttributeTypesMessage availableAttributeTypes(org.vitrivr.adam.grpc.AdamGrpc.EmptyMessage request);

    public org.vitrivr.adam.grpc.AdamGrpc.AckMessage count(org.vitrivr.adam.grpc.AdamGrpc.EntityNameMessage request);

    public org.vitrivr.adam.grpc.AdamGrpc.AckMessage dropEntity(org.vitrivr.adam.grpc.AdamGrpc.EntityNameMessage request);

    public org.vitrivr.adam.grpc.AdamGrpc.ExistsMessage existsEntity(org.vitrivr.adam.grpc.AdamGrpc.EntityNameMessage request);

    public org.vitrivr.adam.grpc.AdamGrpc.AckMessage delete(org.vitrivr.adam.grpc.AdamGrpc.DeleteMessage request);

    public org.vitrivr.adam.grpc.AdamGrpc.AckMessage index(org.vitrivr.adam.grpc.AdamGrpc.IndexMessage request);

    public org.vitrivr.adam.grpc.AdamGrpc.AckMessage generateAllIndexes(org.vitrivr.adam.grpc.AdamGrpc.IndexMessage request);

    public org.vitrivr.adam.grpc.AdamGrpc.ExistsMessage existsIndex(org.vitrivr.adam.grpc.AdamGrpc.IndexMessage request);

    public org.vitrivr.adam.grpc.AdamGrpc.AckMessage dropIndex(org.vitrivr.adam.grpc.AdamGrpc.IndexNameMessage request);

    public org.vitrivr.adam.grpc.AdamGrpc.IndexesMessage listIndexes(org.vitrivr.adam.grpc.AdamGrpc.EntityNameMessage request);

    public org.vitrivr.adam.grpc.AdamGrpc.AckMessage generateRandomData(org.vitrivr.adam.grpc.AdamGrpc.GenerateRandomDataMessage request);

    public org.vitrivr.adam.grpc.AdamGrpc.EntitiesMessage listEntities(org.vitrivr.adam.grpc.AdamGrpc.EmptyMessage request);

    public org.vitrivr.adam.grpc.AdamGrpc.EntityPropertiesMessage getEntityProperties(org.vitrivr.adam.grpc.AdamGrpc.EntityNameMessage request);

    public org.vitrivr.adam.grpc.AdamGrpc.AckMessage repartitionEntityData(org.vitrivr.adam.grpc.AdamGrpc.RepartitionMessage request);

    public org.vitrivr.adam.grpc.AdamGrpc.AckMessage repartitionIndexData(org.vitrivr.adam.grpc.AdamGrpc.RepartitionMessage request);

    public org.vitrivr.adam.grpc.AdamGrpc.AckMessage resetScanWeights(org.vitrivr.adam.grpc.AdamGrpc.EntityNameMessage request);

    public org.vitrivr.adam.grpc.AdamGrpc.AckMessage setScanWeight(org.vitrivr.adam.grpc.AdamGrpc.WeightMessage request);

    public org.vitrivr.adam.grpc.AdamGrpc.AckMessage adaptScanMethods(org.vitrivr.adam.grpc.AdamGrpc.AdaptScanMethodsMessage request);

    public org.vitrivr.adam.grpc.AdamGrpc.AckMessage sparsifyEntity(org.vitrivr.adam.grpc.AdamGrpc.SparsifyEntityMessage request);

    public org.vitrivr.adam.grpc.AdamGrpc.AckMessage importData(org.vitrivr.adam.grpc.AdamGrpc.ImportMessage request);

    public org.vitrivr.adam.grpc.AdamGrpc.AckMessage importDataFile(org.vitrivr.adam.grpc.AdamGrpc.ImportDataFileMessage request);

    public org.vitrivr.adam.grpc.AdamGrpc.ExportDataFileMessage exportDataFile(org.vitrivr.adam.grpc.AdamGrpc.EntityNameMessage request);

    public org.vitrivr.adam.grpc.AdamGrpc.StorageHandlersMessage listStorageHandlers(org.vitrivr.adam.grpc.AdamGrpc.EmptyMessage request);
  }

  public static interface AdamDefinitionFutureClient {

    public com.google.common.util.concurrent.ListenableFuture<org.vitrivr.adam.grpc.AdamGrpc.AckMessage> createEntity(
        org.vitrivr.adam.grpc.AdamGrpc.CreateEntityMessage request);

    public com.google.common.util.concurrent.ListenableFuture<org.vitrivr.adam.grpc.AdamGrpc.AvailableAttributeTypesMessage> availableAttributeTypes(
        org.vitrivr.adam.grpc.AdamGrpc.EmptyMessage request);

    public com.google.common.util.concurrent.ListenableFuture<org.vitrivr.adam.grpc.AdamGrpc.AckMessage> count(
        org.vitrivr.adam.grpc.AdamGrpc.EntityNameMessage request);

    public com.google.common.util.concurrent.ListenableFuture<org.vitrivr.adam.grpc.AdamGrpc.AckMessage> dropEntity(
        org.vitrivr.adam.grpc.AdamGrpc.EntityNameMessage request);

    public com.google.common.util.concurrent.ListenableFuture<org.vitrivr.adam.grpc.AdamGrpc.ExistsMessage> existsEntity(
        org.vitrivr.adam.grpc.AdamGrpc.EntityNameMessage request);

    public com.google.common.util.concurrent.ListenableFuture<org.vitrivr.adam.grpc.AdamGrpc.AckMessage> delete(
        org.vitrivr.adam.grpc.AdamGrpc.DeleteMessage request);

    public com.google.common.util.concurrent.ListenableFuture<org.vitrivr.adam.grpc.AdamGrpc.AckMessage> index(
        org.vitrivr.adam.grpc.AdamGrpc.IndexMessage request);

    public com.google.common.util.concurrent.ListenableFuture<org.vitrivr.adam.grpc.AdamGrpc.AckMessage> generateAllIndexes(
        org.vitrivr.adam.grpc.AdamGrpc.IndexMessage request);

    public com.google.common.util.concurrent.ListenableFuture<org.vitrivr.adam.grpc.AdamGrpc.ExistsMessage> existsIndex(
        org.vitrivr.adam.grpc.AdamGrpc.IndexMessage request);

    public com.google.common.util.concurrent.ListenableFuture<org.vitrivr.adam.grpc.AdamGrpc.AckMessage> dropIndex(
        org.vitrivr.adam.grpc.AdamGrpc.IndexNameMessage request);

    public com.google.common.util.concurrent.ListenableFuture<org.vitrivr.adam.grpc.AdamGrpc.IndexesMessage> listIndexes(
        org.vitrivr.adam.grpc.AdamGrpc.EntityNameMessage request);

    public com.google.common.util.concurrent.ListenableFuture<org.vitrivr.adam.grpc.AdamGrpc.AckMessage> generateRandomData(
        org.vitrivr.adam.grpc.AdamGrpc.GenerateRandomDataMessage request);

    public com.google.common.util.concurrent.ListenableFuture<org.vitrivr.adam.grpc.AdamGrpc.EntitiesMessage> listEntities(
        org.vitrivr.adam.grpc.AdamGrpc.EmptyMessage request);

    public com.google.common.util.concurrent.ListenableFuture<org.vitrivr.adam.grpc.AdamGrpc.EntityPropertiesMessage> getEntityProperties(
        org.vitrivr.adam.grpc.AdamGrpc.EntityNameMessage request);

    public com.google.common.util.concurrent.ListenableFuture<org.vitrivr.adam.grpc.AdamGrpc.AckMessage> repartitionEntityData(
        org.vitrivr.adam.grpc.AdamGrpc.RepartitionMessage request);

    public com.google.common.util.concurrent.ListenableFuture<org.vitrivr.adam.grpc.AdamGrpc.AckMessage> repartitionIndexData(
        org.vitrivr.adam.grpc.AdamGrpc.RepartitionMessage request);

    public com.google.common.util.concurrent.ListenableFuture<org.vitrivr.adam.grpc.AdamGrpc.AckMessage> resetScanWeights(
        org.vitrivr.adam.grpc.AdamGrpc.EntityNameMessage request);

    public com.google.common.util.concurrent.ListenableFuture<org.vitrivr.adam.grpc.AdamGrpc.AckMessage> setScanWeight(
        org.vitrivr.adam.grpc.AdamGrpc.WeightMessage request);

    public com.google.common.util.concurrent.ListenableFuture<org.vitrivr.adam.grpc.AdamGrpc.AckMessage> adaptScanMethods(
        org.vitrivr.adam.grpc.AdamGrpc.AdaptScanMethodsMessage request);

    public com.google.common.util.concurrent.ListenableFuture<org.vitrivr.adam.grpc.AdamGrpc.AckMessage> sparsifyEntity(
        org.vitrivr.adam.grpc.AdamGrpc.SparsifyEntityMessage request);

    public com.google.common.util.concurrent.ListenableFuture<org.vitrivr.adam.grpc.AdamGrpc.AckMessage> importData(
        org.vitrivr.adam.grpc.AdamGrpc.ImportMessage request);

    public com.google.common.util.concurrent.ListenableFuture<org.vitrivr.adam.grpc.AdamGrpc.AckMessage> importDataFile(
        org.vitrivr.adam.grpc.AdamGrpc.ImportDataFileMessage request);

    public com.google.common.util.concurrent.ListenableFuture<org.vitrivr.adam.grpc.AdamGrpc.ExportDataFileMessage> exportDataFile(
        org.vitrivr.adam.grpc.AdamGrpc.EntityNameMessage request);

    public com.google.common.util.concurrent.ListenableFuture<org.vitrivr.adam.grpc.AdamGrpc.StorageHandlersMessage> listStorageHandlers(
        org.vitrivr.adam.grpc.AdamGrpc.EmptyMessage request);
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
    public void createEntity(org.vitrivr.adam.grpc.AdamGrpc.CreateEntityMessage request,
        io.grpc.stub.StreamObserver<org.vitrivr.adam.grpc.AdamGrpc.AckMessage> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_CREATE_ENTITY, getCallOptions()), request, responseObserver);
    }

    @java.lang.Override
    public void availableAttributeTypes(org.vitrivr.adam.grpc.AdamGrpc.EmptyMessage request,
        io.grpc.stub.StreamObserver<org.vitrivr.adam.grpc.AdamGrpc.AvailableAttributeTypesMessage> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_AVAILABLE_ATTRIBUTE_TYPES, getCallOptions()), request, responseObserver);
    }

    @java.lang.Override
    public void count(org.vitrivr.adam.grpc.AdamGrpc.EntityNameMessage request,
        io.grpc.stub.StreamObserver<org.vitrivr.adam.grpc.AdamGrpc.AckMessage> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_COUNT, getCallOptions()), request, responseObserver);
    }

    @java.lang.Override
    public void dropEntity(org.vitrivr.adam.grpc.AdamGrpc.EntityNameMessage request,
        io.grpc.stub.StreamObserver<org.vitrivr.adam.grpc.AdamGrpc.AckMessage> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_DROP_ENTITY, getCallOptions()), request, responseObserver);
    }

    @java.lang.Override
    public void existsEntity(org.vitrivr.adam.grpc.AdamGrpc.EntityNameMessage request,
        io.grpc.stub.StreamObserver<org.vitrivr.adam.grpc.AdamGrpc.ExistsMessage> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_EXISTS_ENTITY, getCallOptions()), request, responseObserver);
    }

    @java.lang.Override
    public io.grpc.stub.StreamObserver<org.vitrivr.adam.grpc.AdamGrpc.InsertMessage> insert(
        io.grpc.stub.StreamObserver<org.vitrivr.adam.grpc.AdamGrpc.AckMessage> responseObserver) {
      return asyncClientStreamingCall(
          getChannel().newCall(METHOD_INSERT, getCallOptions()), responseObserver);
    }

    @java.lang.Override
    public void delete(org.vitrivr.adam.grpc.AdamGrpc.DeleteMessage request,
        io.grpc.stub.StreamObserver<org.vitrivr.adam.grpc.AdamGrpc.AckMessage> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_DELETE, getCallOptions()), request, responseObserver);
    }

    @java.lang.Override
    public void index(org.vitrivr.adam.grpc.AdamGrpc.IndexMessage request,
        io.grpc.stub.StreamObserver<org.vitrivr.adam.grpc.AdamGrpc.AckMessage> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_INDEX, getCallOptions()), request, responseObserver);
    }

    @java.lang.Override
    public void generateAllIndexes(org.vitrivr.adam.grpc.AdamGrpc.IndexMessage request,
        io.grpc.stub.StreamObserver<org.vitrivr.adam.grpc.AdamGrpc.AckMessage> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_GENERATE_ALL_INDEXES, getCallOptions()), request, responseObserver);
    }

    @java.lang.Override
    public void existsIndex(org.vitrivr.adam.grpc.AdamGrpc.IndexMessage request,
        io.grpc.stub.StreamObserver<org.vitrivr.adam.grpc.AdamGrpc.ExistsMessage> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_EXISTS_INDEX, getCallOptions()), request, responseObserver);
    }

    @java.lang.Override
    public void dropIndex(org.vitrivr.adam.grpc.AdamGrpc.IndexNameMessage request,
        io.grpc.stub.StreamObserver<org.vitrivr.adam.grpc.AdamGrpc.AckMessage> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_DROP_INDEX, getCallOptions()), request, responseObserver);
    }

    @java.lang.Override
    public void listIndexes(org.vitrivr.adam.grpc.AdamGrpc.EntityNameMessage request,
        io.grpc.stub.StreamObserver<org.vitrivr.adam.grpc.AdamGrpc.IndexesMessage> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_LIST_INDEXES, getCallOptions()), request, responseObserver);
    }

    @java.lang.Override
    public void generateRandomData(org.vitrivr.adam.grpc.AdamGrpc.GenerateRandomDataMessage request,
        io.grpc.stub.StreamObserver<org.vitrivr.adam.grpc.AdamGrpc.AckMessage> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_GENERATE_RANDOM_DATA, getCallOptions()), request, responseObserver);
    }

    @java.lang.Override
    public void listEntities(org.vitrivr.adam.grpc.AdamGrpc.EmptyMessage request,
        io.grpc.stub.StreamObserver<org.vitrivr.adam.grpc.AdamGrpc.EntitiesMessage> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_LIST_ENTITIES, getCallOptions()), request, responseObserver);
    }

    @java.lang.Override
    public void getEntityProperties(org.vitrivr.adam.grpc.AdamGrpc.EntityNameMessage request,
        io.grpc.stub.StreamObserver<org.vitrivr.adam.grpc.AdamGrpc.EntityPropertiesMessage> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_GET_ENTITY_PROPERTIES, getCallOptions()), request, responseObserver);
    }

    @java.lang.Override
    public void repartitionEntityData(org.vitrivr.adam.grpc.AdamGrpc.RepartitionMessage request,
        io.grpc.stub.StreamObserver<org.vitrivr.adam.grpc.AdamGrpc.AckMessage> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_REPARTITION_ENTITY_DATA, getCallOptions()), request, responseObserver);
    }

    @java.lang.Override
    public void repartitionIndexData(org.vitrivr.adam.grpc.AdamGrpc.RepartitionMessage request,
        io.grpc.stub.StreamObserver<org.vitrivr.adam.grpc.AdamGrpc.AckMessage> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_REPARTITION_INDEX_DATA, getCallOptions()), request, responseObserver);
    }

    @java.lang.Override
    public void resetScanWeights(org.vitrivr.adam.grpc.AdamGrpc.EntityNameMessage request,
        io.grpc.stub.StreamObserver<org.vitrivr.adam.grpc.AdamGrpc.AckMessage> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_RESET_SCAN_WEIGHTS, getCallOptions()), request, responseObserver);
    }

    @java.lang.Override
    public void setScanWeight(org.vitrivr.adam.grpc.AdamGrpc.WeightMessage request,
        io.grpc.stub.StreamObserver<org.vitrivr.adam.grpc.AdamGrpc.AckMessage> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_SET_SCAN_WEIGHT, getCallOptions()), request, responseObserver);
    }

    @java.lang.Override
    public void adaptScanMethods(org.vitrivr.adam.grpc.AdamGrpc.AdaptScanMethodsMessage request,
        io.grpc.stub.StreamObserver<org.vitrivr.adam.grpc.AdamGrpc.AckMessage> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_ADAPT_SCAN_METHODS, getCallOptions()), request, responseObserver);
    }

    @java.lang.Override
    public void sparsifyEntity(org.vitrivr.adam.grpc.AdamGrpc.SparsifyEntityMessage request,
        io.grpc.stub.StreamObserver<org.vitrivr.adam.grpc.AdamGrpc.AckMessage> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_SPARSIFY_ENTITY, getCallOptions()), request, responseObserver);
    }

    @java.lang.Override
    public void importData(org.vitrivr.adam.grpc.AdamGrpc.ImportMessage request,
        io.grpc.stub.StreamObserver<org.vitrivr.adam.grpc.AdamGrpc.AckMessage> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_IMPORT_DATA, getCallOptions()), request, responseObserver);
    }

    @java.lang.Override
    public void importDataFile(org.vitrivr.adam.grpc.AdamGrpc.ImportDataFileMessage request,
        io.grpc.stub.StreamObserver<org.vitrivr.adam.grpc.AdamGrpc.AckMessage> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_IMPORT_DATA_FILE, getCallOptions()), request, responseObserver);
    }

    @java.lang.Override
    public void exportDataFile(org.vitrivr.adam.grpc.AdamGrpc.EntityNameMessage request,
        io.grpc.stub.StreamObserver<org.vitrivr.adam.grpc.AdamGrpc.ExportDataFileMessage> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_EXPORT_DATA_FILE, getCallOptions()), request, responseObserver);
    }

    @java.lang.Override
    public void listStorageHandlers(org.vitrivr.adam.grpc.AdamGrpc.EmptyMessage request,
        io.grpc.stub.StreamObserver<org.vitrivr.adam.grpc.AdamGrpc.StorageHandlersMessage> responseObserver) {
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
    public org.vitrivr.adam.grpc.AdamGrpc.AckMessage createEntity(org.vitrivr.adam.grpc.AdamGrpc.CreateEntityMessage request) {
      return blockingUnaryCall(
          getChannel(), METHOD_CREATE_ENTITY, getCallOptions(), request);
    }

    @java.lang.Override
    public org.vitrivr.adam.grpc.AdamGrpc.AvailableAttributeTypesMessage availableAttributeTypes(org.vitrivr.adam.grpc.AdamGrpc.EmptyMessage request) {
      return blockingUnaryCall(
          getChannel(), METHOD_AVAILABLE_ATTRIBUTE_TYPES, getCallOptions(), request);
    }

    @java.lang.Override
    public org.vitrivr.adam.grpc.AdamGrpc.AckMessage count(org.vitrivr.adam.grpc.AdamGrpc.EntityNameMessage request) {
      return blockingUnaryCall(
          getChannel(), METHOD_COUNT, getCallOptions(), request);
    }

    @java.lang.Override
    public org.vitrivr.adam.grpc.AdamGrpc.AckMessage dropEntity(org.vitrivr.adam.grpc.AdamGrpc.EntityNameMessage request) {
      return blockingUnaryCall(
          getChannel(), METHOD_DROP_ENTITY, getCallOptions(), request);
    }

    @java.lang.Override
    public org.vitrivr.adam.grpc.AdamGrpc.ExistsMessage existsEntity(org.vitrivr.adam.grpc.AdamGrpc.EntityNameMessage request) {
      return blockingUnaryCall(
          getChannel(), METHOD_EXISTS_ENTITY, getCallOptions(), request);
    }

    @java.lang.Override
    public org.vitrivr.adam.grpc.AdamGrpc.AckMessage delete(org.vitrivr.adam.grpc.AdamGrpc.DeleteMessage request) {
      return blockingUnaryCall(
          getChannel(), METHOD_DELETE, getCallOptions(), request);
    }

    @java.lang.Override
    public org.vitrivr.adam.grpc.AdamGrpc.AckMessage index(org.vitrivr.adam.grpc.AdamGrpc.IndexMessage request) {
      return blockingUnaryCall(
          getChannel(), METHOD_INDEX, getCallOptions(), request);
    }

    @java.lang.Override
    public org.vitrivr.adam.grpc.AdamGrpc.AckMessage generateAllIndexes(org.vitrivr.adam.grpc.AdamGrpc.IndexMessage request) {
      return blockingUnaryCall(
          getChannel(), METHOD_GENERATE_ALL_INDEXES, getCallOptions(), request);
    }

    @java.lang.Override
    public org.vitrivr.adam.grpc.AdamGrpc.ExistsMessage existsIndex(org.vitrivr.adam.grpc.AdamGrpc.IndexMessage request) {
      return blockingUnaryCall(
          getChannel(), METHOD_EXISTS_INDEX, getCallOptions(), request);
    }

    @java.lang.Override
    public org.vitrivr.adam.grpc.AdamGrpc.AckMessage dropIndex(org.vitrivr.adam.grpc.AdamGrpc.IndexNameMessage request) {
      return blockingUnaryCall(
          getChannel(), METHOD_DROP_INDEX, getCallOptions(), request);
    }

    @java.lang.Override
    public org.vitrivr.adam.grpc.AdamGrpc.IndexesMessage listIndexes(org.vitrivr.adam.grpc.AdamGrpc.EntityNameMessage request) {
      return blockingUnaryCall(
          getChannel(), METHOD_LIST_INDEXES, getCallOptions(), request);
    }

    @java.lang.Override
    public org.vitrivr.adam.grpc.AdamGrpc.AckMessage generateRandomData(org.vitrivr.adam.grpc.AdamGrpc.GenerateRandomDataMessage request) {
      return blockingUnaryCall(
          getChannel(), METHOD_GENERATE_RANDOM_DATA, getCallOptions(), request);
    }

    @java.lang.Override
    public org.vitrivr.adam.grpc.AdamGrpc.EntitiesMessage listEntities(org.vitrivr.adam.grpc.AdamGrpc.EmptyMessage request) {
      return blockingUnaryCall(
          getChannel(), METHOD_LIST_ENTITIES, getCallOptions(), request);
    }

    @java.lang.Override
    public org.vitrivr.adam.grpc.AdamGrpc.EntityPropertiesMessage getEntityProperties(org.vitrivr.adam.grpc.AdamGrpc.EntityNameMessage request) {
      return blockingUnaryCall(
          getChannel(), METHOD_GET_ENTITY_PROPERTIES, getCallOptions(), request);
    }

    @java.lang.Override
    public org.vitrivr.adam.grpc.AdamGrpc.AckMessage repartitionEntityData(org.vitrivr.adam.grpc.AdamGrpc.RepartitionMessage request) {
      return blockingUnaryCall(
          getChannel(), METHOD_REPARTITION_ENTITY_DATA, getCallOptions(), request);
    }

    @java.lang.Override
    public org.vitrivr.adam.grpc.AdamGrpc.AckMessage repartitionIndexData(org.vitrivr.adam.grpc.AdamGrpc.RepartitionMessage request) {
      return blockingUnaryCall(
          getChannel(), METHOD_REPARTITION_INDEX_DATA, getCallOptions(), request);
    }

    @java.lang.Override
    public org.vitrivr.adam.grpc.AdamGrpc.AckMessage resetScanWeights(org.vitrivr.adam.grpc.AdamGrpc.EntityNameMessage request) {
      return blockingUnaryCall(
          getChannel(), METHOD_RESET_SCAN_WEIGHTS, getCallOptions(), request);
    }

    @java.lang.Override
    public org.vitrivr.adam.grpc.AdamGrpc.AckMessage setScanWeight(org.vitrivr.adam.grpc.AdamGrpc.WeightMessage request) {
      return blockingUnaryCall(
          getChannel(), METHOD_SET_SCAN_WEIGHT, getCallOptions(), request);
    }

    @java.lang.Override
    public org.vitrivr.adam.grpc.AdamGrpc.AckMessage adaptScanMethods(org.vitrivr.adam.grpc.AdamGrpc.AdaptScanMethodsMessage request) {
      return blockingUnaryCall(
          getChannel(), METHOD_ADAPT_SCAN_METHODS, getCallOptions(), request);
    }

    @java.lang.Override
    public org.vitrivr.adam.grpc.AdamGrpc.AckMessage sparsifyEntity(org.vitrivr.adam.grpc.AdamGrpc.SparsifyEntityMessage request) {
      return blockingUnaryCall(
          getChannel(), METHOD_SPARSIFY_ENTITY, getCallOptions(), request);
    }

    @java.lang.Override
    public org.vitrivr.adam.grpc.AdamGrpc.AckMessage importData(org.vitrivr.adam.grpc.AdamGrpc.ImportMessage request) {
      return blockingUnaryCall(
          getChannel(), METHOD_IMPORT_DATA, getCallOptions(), request);
    }

    @java.lang.Override
    public org.vitrivr.adam.grpc.AdamGrpc.AckMessage importDataFile(org.vitrivr.adam.grpc.AdamGrpc.ImportDataFileMessage request) {
      return blockingUnaryCall(
          getChannel(), METHOD_IMPORT_DATA_FILE, getCallOptions(), request);
    }

    @java.lang.Override
    public org.vitrivr.adam.grpc.AdamGrpc.ExportDataFileMessage exportDataFile(org.vitrivr.adam.grpc.AdamGrpc.EntityNameMessage request) {
      return blockingUnaryCall(
          getChannel(), METHOD_EXPORT_DATA_FILE, getCallOptions(), request);
    }

    @java.lang.Override
    public org.vitrivr.adam.grpc.AdamGrpc.StorageHandlersMessage listStorageHandlers(org.vitrivr.adam.grpc.AdamGrpc.EmptyMessage request) {
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
    public com.google.common.util.concurrent.ListenableFuture<org.vitrivr.adam.grpc.AdamGrpc.AckMessage> createEntity(
        org.vitrivr.adam.grpc.AdamGrpc.CreateEntityMessage request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_CREATE_ENTITY, getCallOptions()), request);
    }

    @java.lang.Override
    public com.google.common.util.concurrent.ListenableFuture<org.vitrivr.adam.grpc.AdamGrpc.AvailableAttributeTypesMessage> availableAttributeTypes(
        org.vitrivr.adam.grpc.AdamGrpc.EmptyMessage request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_AVAILABLE_ATTRIBUTE_TYPES, getCallOptions()), request);
    }

    @java.lang.Override
    public com.google.common.util.concurrent.ListenableFuture<org.vitrivr.adam.grpc.AdamGrpc.AckMessage> count(
        org.vitrivr.adam.grpc.AdamGrpc.EntityNameMessage request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_COUNT, getCallOptions()), request);
    }

    @java.lang.Override
    public com.google.common.util.concurrent.ListenableFuture<org.vitrivr.adam.grpc.AdamGrpc.AckMessage> dropEntity(
        org.vitrivr.adam.grpc.AdamGrpc.EntityNameMessage request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_DROP_ENTITY, getCallOptions()), request);
    }

    @java.lang.Override
    public com.google.common.util.concurrent.ListenableFuture<org.vitrivr.adam.grpc.AdamGrpc.ExistsMessage> existsEntity(
        org.vitrivr.adam.grpc.AdamGrpc.EntityNameMessage request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_EXISTS_ENTITY, getCallOptions()), request);
    }

    @java.lang.Override
    public com.google.common.util.concurrent.ListenableFuture<org.vitrivr.adam.grpc.AdamGrpc.AckMessage> delete(
        org.vitrivr.adam.grpc.AdamGrpc.DeleteMessage request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_DELETE, getCallOptions()), request);
    }

    @java.lang.Override
    public com.google.common.util.concurrent.ListenableFuture<org.vitrivr.adam.grpc.AdamGrpc.AckMessage> index(
        org.vitrivr.adam.grpc.AdamGrpc.IndexMessage request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_INDEX, getCallOptions()), request);
    }

    @java.lang.Override
    public com.google.common.util.concurrent.ListenableFuture<org.vitrivr.adam.grpc.AdamGrpc.AckMessage> generateAllIndexes(
        org.vitrivr.adam.grpc.AdamGrpc.IndexMessage request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_GENERATE_ALL_INDEXES, getCallOptions()), request);
    }

    @java.lang.Override
    public com.google.common.util.concurrent.ListenableFuture<org.vitrivr.adam.grpc.AdamGrpc.ExistsMessage> existsIndex(
        org.vitrivr.adam.grpc.AdamGrpc.IndexMessage request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_EXISTS_INDEX, getCallOptions()), request);
    }

    @java.lang.Override
    public com.google.common.util.concurrent.ListenableFuture<org.vitrivr.adam.grpc.AdamGrpc.AckMessage> dropIndex(
        org.vitrivr.adam.grpc.AdamGrpc.IndexNameMessage request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_DROP_INDEX, getCallOptions()), request);
    }

    @java.lang.Override
    public com.google.common.util.concurrent.ListenableFuture<org.vitrivr.adam.grpc.AdamGrpc.IndexesMessage> listIndexes(
        org.vitrivr.adam.grpc.AdamGrpc.EntityNameMessage request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_LIST_INDEXES, getCallOptions()), request);
    }

    @java.lang.Override
    public com.google.common.util.concurrent.ListenableFuture<org.vitrivr.adam.grpc.AdamGrpc.AckMessage> generateRandomData(
        org.vitrivr.adam.grpc.AdamGrpc.GenerateRandomDataMessage request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_GENERATE_RANDOM_DATA, getCallOptions()), request);
    }

    @java.lang.Override
    public com.google.common.util.concurrent.ListenableFuture<org.vitrivr.adam.grpc.AdamGrpc.EntitiesMessage> listEntities(
        org.vitrivr.adam.grpc.AdamGrpc.EmptyMessage request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_LIST_ENTITIES, getCallOptions()), request);
    }

    @java.lang.Override
    public com.google.common.util.concurrent.ListenableFuture<org.vitrivr.adam.grpc.AdamGrpc.EntityPropertiesMessage> getEntityProperties(
        org.vitrivr.adam.grpc.AdamGrpc.EntityNameMessage request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_GET_ENTITY_PROPERTIES, getCallOptions()), request);
    }

    @java.lang.Override
    public com.google.common.util.concurrent.ListenableFuture<org.vitrivr.adam.grpc.AdamGrpc.AckMessage> repartitionEntityData(
        org.vitrivr.adam.grpc.AdamGrpc.RepartitionMessage request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_REPARTITION_ENTITY_DATA, getCallOptions()), request);
    }

    @java.lang.Override
    public com.google.common.util.concurrent.ListenableFuture<org.vitrivr.adam.grpc.AdamGrpc.AckMessage> repartitionIndexData(
        org.vitrivr.adam.grpc.AdamGrpc.RepartitionMessage request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_REPARTITION_INDEX_DATA, getCallOptions()), request);
    }

    @java.lang.Override
    public com.google.common.util.concurrent.ListenableFuture<org.vitrivr.adam.grpc.AdamGrpc.AckMessage> resetScanWeights(
        org.vitrivr.adam.grpc.AdamGrpc.EntityNameMessage request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_RESET_SCAN_WEIGHTS, getCallOptions()), request);
    }

    @java.lang.Override
    public com.google.common.util.concurrent.ListenableFuture<org.vitrivr.adam.grpc.AdamGrpc.AckMessage> setScanWeight(
        org.vitrivr.adam.grpc.AdamGrpc.WeightMessage request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_SET_SCAN_WEIGHT, getCallOptions()), request);
    }

    @java.lang.Override
    public com.google.common.util.concurrent.ListenableFuture<org.vitrivr.adam.grpc.AdamGrpc.AckMessage> adaptScanMethods(
        org.vitrivr.adam.grpc.AdamGrpc.AdaptScanMethodsMessage request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_ADAPT_SCAN_METHODS, getCallOptions()), request);
    }

    @java.lang.Override
    public com.google.common.util.concurrent.ListenableFuture<org.vitrivr.adam.grpc.AdamGrpc.AckMessage> sparsifyEntity(
        org.vitrivr.adam.grpc.AdamGrpc.SparsifyEntityMessage request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_SPARSIFY_ENTITY, getCallOptions()), request);
    }

    @java.lang.Override
    public com.google.common.util.concurrent.ListenableFuture<org.vitrivr.adam.grpc.AdamGrpc.AckMessage> importData(
        org.vitrivr.adam.grpc.AdamGrpc.ImportMessage request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_IMPORT_DATA, getCallOptions()), request);
    }

    @java.lang.Override
    public com.google.common.util.concurrent.ListenableFuture<org.vitrivr.adam.grpc.AdamGrpc.AckMessage> importDataFile(
        org.vitrivr.adam.grpc.AdamGrpc.ImportDataFileMessage request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_IMPORT_DATA_FILE, getCallOptions()), request);
    }

    @java.lang.Override
    public com.google.common.util.concurrent.ListenableFuture<org.vitrivr.adam.grpc.AdamGrpc.ExportDataFileMessage> exportDataFile(
        org.vitrivr.adam.grpc.AdamGrpc.EntityNameMessage request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_EXPORT_DATA_FILE, getCallOptions()), request);
    }

    @java.lang.Override
    public com.google.common.util.concurrent.ListenableFuture<org.vitrivr.adam.grpc.AdamGrpc.StorageHandlersMessage> listStorageHandlers(
        org.vitrivr.adam.grpc.AdamGrpc.EmptyMessage request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_LIST_STORAGE_HANDLERS, getCallOptions()), request);
    }
  }

  private static final int METHODID_CREATE_ENTITY = 0;
  private static final int METHODID_AVAILABLE_ATTRIBUTE_TYPES = 1;
  private static final int METHODID_COUNT = 2;
  private static final int METHODID_DROP_ENTITY = 3;
  private static final int METHODID_EXISTS_ENTITY = 4;
  private static final int METHODID_DELETE = 5;
  private static final int METHODID_INDEX = 6;
  private static final int METHODID_GENERATE_ALL_INDEXES = 7;
  private static final int METHODID_EXISTS_INDEX = 8;
  private static final int METHODID_DROP_INDEX = 9;
  private static final int METHODID_LIST_INDEXES = 10;
  private static final int METHODID_GENERATE_RANDOM_DATA = 11;
  private static final int METHODID_LIST_ENTITIES = 12;
  private static final int METHODID_GET_ENTITY_PROPERTIES = 13;
  private static final int METHODID_REPARTITION_ENTITY_DATA = 14;
  private static final int METHODID_REPARTITION_INDEX_DATA = 15;
  private static final int METHODID_RESET_SCAN_WEIGHTS = 16;
  private static final int METHODID_SET_SCAN_WEIGHT = 17;
  private static final int METHODID_ADAPT_SCAN_METHODS = 18;
  private static final int METHODID_SPARSIFY_ENTITY = 19;
  private static final int METHODID_IMPORT_DATA = 20;
  private static final int METHODID_IMPORT_DATA_FILE = 21;
  private static final int METHODID_EXPORT_DATA_FILE = 22;
  private static final int METHODID_LIST_STORAGE_HANDLERS = 23;
  private static final int METHODID_INSERT = 24;

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
          serviceImpl.createEntity((org.vitrivr.adam.grpc.AdamGrpc.CreateEntityMessage) request,
              (io.grpc.stub.StreamObserver<org.vitrivr.adam.grpc.AdamGrpc.AckMessage>) responseObserver);
          break;
        case METHODID_AVAILABLE_ATTRIBUTE_TYPES:
          serviceImpl.availableAttributeTypes((org.vitrivr.adam.grpc.AdamGrpc.EmptyMessage) request,
              (io.grpc.stub.StreamObserver<org.vitrivr.adam.grpc.AdamGrpc.AvailableAttributeTypesMessage>) responseObserver);
          break;
        case METHODID_COUNT:
          serviceImpl.count((org.vitrivr.adam.grpc.AdamGrpc.EntityNameMessage) request,
              (io.grpc.stub.StreamObserver<org.vitrivr.adam.grpc.AdamGrpc.AckMessage>) responseObserver);
          break;
        case METHODID_DROP_ENTITY:
          serviceImpl.dropEntity((org.vitrivr.adam.grpc.AdamGrpc.EntityNameMessage) request,
              (io.grpc.stub.StreamObserver<org.vitrivr.adam.grpc.AdamGrpc.AckMessage>) responseObserver);
          break;
        case METHODID_EXISTS_ENTITY:
          serviceImpl.existsEntity((org.vitrivr.adam.grpc.AdamGrpc.EntityNameMessage) request,
              (io.grpc.stub.StreamObserver<org.vitrivr.adam.grpc.AdamGrpc.ExistsMessage>) responseObserver);
          break;
        case METHODID_DELETE:
          serviceImpl.delete((org.vitrivr.adam.grpc.AdamGrpc.DeleteMessage) request,
              (io.grpc.stub.StreamObserver<org.vitrivr.adam.grpc.AdamGrpc.AckMessage>) responseObserver);
          break;
        case METHODID_INDEX:
          serviceImpl.index((org.vitrivr.adam.grpc.AdamGrpc.IndexMessage) request,
              (io.grpc.stub.StreamObserver<org.vitrivr.adam.grpc.AdamGrpc.AckMessage>) responseObserver);
          break;
        case METHODID_GENERATE_ALL_INDEXES:
          serviceImpl.generateAllIndexes((org.vitrivr.adam.grpc.AdamGrpc.IndexMessage) request,
              (io.grpc.stub.StreamObserver<org.vitrivr.adam.grpc.AdamGrpc.AckMessage>) responseObserver);
          break;
        case METHODID_EXISTS_INDEX:
          serviceImpl.existsIndex((org.vitrivr.adam.grpc.AdamGrpc.IndexMessage) request,
              (io.grpc.stub.StreamObserver<org.vitrivr.adam.grpc.AdamGrpc.ExistsMessage>) responseObserver);
          break;
        case METHODID_DROP_INDEX:
          serviceImpl.dropIndex((org.vitrivr.adam.grpc.AdamGrpc.IndexNameMessage) request,
              (io.grpc.stub.StreamObserver<org.vitrivr.adam.grpc.AdamGrpc.AckMessage>) responseObserver);
          break;
        case METHODID_LIST_INDEXES:
          serviceImpl.listIndexes((org.vitrivr.adam.grpc.AdamGrpc.EntityNameMessage) request,
              (io.grpc.stub.StreamObserver<org.vitrivr.adam.grpc.AdamGrpc.IndexesMessage>) responseObserver);
          break;
        case METHODID_GENERATE_RANDOM_DATA:
          serviceImpl.generateRandomData((org.vitrivr.adam.grpc.AdamGrpc.GenerateRandomDataMessage) request,
              (io.grpc.stub.StreamObserver<org.vitrivr.adam.grpc.AdamGrpc.AckMessage>) responseObserver);
          break;
        case METHODID_LIST_ENTITIES:
          serviceImpl.listEntities((org.vitrivr.adam.grpc.AdamGrpc.EmptyMessage) request,
              (io.grpc.stub.StreamObserver<org.vitrivr.adam.grpc.AdamGrpc.EntitiesMessage>) responseObserver);
          break;
        case METHODID_GET_ENTITY_PROPERTIES:
          serviceImpl.getEntityProperties((org.vitrivr.adam.grpc.AdamGrpc.EntityNameMessage) request,
              (io.grpc.stub.StreamObserver<org.vitrivr.adam.grpc.AdamGrpc.EntityPropertiesMessage>) responseObserver);
          break;
        case METHODID_REPARTITION_ENTITY_DATA:
          serviceImpl.repartitionEntityData((org.vitrivr.adam.grpc.AdamGrpc.RepartitionMessage) request,
              (io.grpc.stub.StreamObserver<org.vitrivr.adam.grpc.AdamGrpc.AckMessage>) responseObserver);
          break;
        case METHODID_REPARTITION_INDEX_DATA:
          serviceImpl.repartitionIndexData((org.vitrivr.adam.grpc.AdamGrpc.RepartitionMessage) request,
              (io.grpc.stub.StreamObserver<org.vitrivr.adam.grpc.AdamGrpc.AckMessage>) responseObserver);
          break;
        case METHODID_RESET_SCAN_WEIGHTS:
          serviceImpl.resetScanWeights((org.vitrivr.adam.grpc.AdamGrpc.EntityNameMessage) request,
              (io.grpc.stub.StreamObserver<org.vitrivr.adam.grpc.AdamGrpc.AckMessage>) responseObserver);
          break;
        case METHODID_SET_SCAN_WEIGHT:
          serviceImpl.setScanWeight((org.vitrivr.adam.grpc.AdamGrpc.WeightMessage) request,
              (io.grpc.stub.StreamObserver<org.vitrivr.adam.grpc.AdamGrpc.AckMessage>) responseObserver);
          break;
        case METHODID_ADAPT_SCAN_METHODS:
          serviceImpl.adaptScanMethods((org.vitrivr.adam.grpc.AdamGrpc.AdaptScanMethodsMessage) request,
              (io.grpc.stub.StreamObserver<org.vitrivr.adam.grpc.AdamGrpc.AckMessage>) responseObserver);
          break;
        case METHODID_SPARSIFY_ENTITY:
          serviceImpl.sparsifyEntity((org.vitrivr.adam.grpc.AdamGrpc.SparsifyEntityMessage) request,
              (io.grpc.stub.StreamObserver<org.vitrivr.adam.grpc.AdamGrpc.AckMessage>) responseObserver);
          break;
        case METHODID_IMPORT_DATA:
          serviceImpl.importData((org.vitrivr.adam.grpc.AdamGrpc.ImportMessage) request,
              (io.grpc.stub.StreamObserver<org.vitrivr.adam.grpc.AdamGrpc.AckMessage>) responseObserver);
          break;
        case METHODID_IMPORT_DATA_FILE:
          serviceImpl.importDataFile((org.vitrivr.adam.grpc.AdamGrpc.ImportDataFileMessage) request,
              (io.grpc.stub.StreamObserver<org.vitrivr.adam.grpc.AdamGrpc.AckMessage>) responseObserver);
          break;
        case METHODID_EXPORT_DATA_FILE:
          serviceImpl.exportDataFile((org.vitrivr.adam.grpc.AdamGrpc.EntityNameMessage) request,
              (io.grpc.stub.StreamObserver<org.vitrivr.adam.grpc.AdamGrpc.ExportDataFileMessage>) responseObserver);
          break;
        case METHODID_LIST_STORAGE_HANDLERS:
          serviceImpl.listStorageHandlers((org.vitrivr.adam.grpc.AdamGrpc.EmptyMessage) request,
              (io.grpc.stub.StreamObserver<org.vitrivr.adam.grpc.AdamGrpc.StorageHandlersMessage>) responseObserver);
          break;
        default:
          throw new AssertionError();
      }
    }

    @java.lang.SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<Req> invoke(
        io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_INSERT:
          return (io.grpc.stub.StreamObserver<Req>) serviceImpl.insert(
              (io.grpc.stub.StreamObserver<org.vitrivr.adam.grpc.AdamGrpc.AckMessage>) responseObserver);
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
              org.vitrivr.adam.grpc.AdamGrpc.CreateEntityMessage,
              org.vitrivr.adam.grpc.AdamGrpc.AckMessage>(
                serviceImpl, METHODID_CREATE_ENTITY)))
        .addMethod(
          METHOD_AVAILABLE_ATTRIBUTE_TYPES,
          asyncUnaryCall(
            new MethodHandlers<
              org.vitrivr.adam.grpc.AdamGrpc.EmptyMessage,
              org.vitrivr.adam.grpc.AdamGrpc.AvailableAttributeTypesMessage>(
                serviceImpl, METHODID_AVAILABLE_ATTRIBUTE_TYPES)))
        .addMethod(
          METHOD_COUNT,
          asyncUnaryCall(
            new MethodHandlers<
              org.vitrivr.adam.grpc.AdamGrpc.EntityNameMessage,
              org.vitrivr.adam.grpc.AdamGrpc.AckMessage>(
                serviceImpl, METHODID_COUNT)))
        .addMethod(
          METHOD_DROP_ENTITY,
          asyncUnaryCall(
            new MethodHandlers<
              org.vitrivr.adam.grpc.AdamGrpc.EntityNameMessage,
              org.vitrivr.adam.grpc.AdamGrpc.AckMessage>(
                serviceImpl, METHODID_DROP_ENTITY)))
        .addMethod(
          METHOD_EXISTS_ENTITY,
          asyncUnaryCall(
            new MethodHandlers<
              org.vitrivr.adam.grpc.AdamGrpc.EntityNameMessage,
              org.vitrivr.adam.grpc.AdamGrpc.ExistsMessage>(
                serviceImpl, METHODID_EXISTS_ENTITY)))
        .addMethod(
          METHOD_INSERT,
          asyncClientStreamingCall(
            new MethodHandlers<
              org.vitrivr.adam.grpc.AdamGrpc.InsertMessage,
              org.vitrivr.adam.grpc.AdamGrpc.AckMessage>(
                serviceImpl, METHODID_INSERT)))
        .addMethod(
          METHOD_DELETE,
          asyncUnaryCall(
            new MethodHandlers<
              org.vitrivr.adam.grpc.AdamGrpc.DeleteMessage,
              org.vitrivr.adam.grpc.AdamGrpc.AckMessage>(
                serviceImpl, METHODID_DELETE)))
        .addMethod(
          METHOD_INDEX,
          asyncUnaryCall(
            new MethodHandlers<
              org.vitrivr.adam.grpc.AdamGrpc.IndexMessage,
              org.vitrivr.adam.grpc.AdamGrpc.AckMessage>(
                serviceImpl, METHODID_INDEX)))
        .addMethod(
          METHOD_GENERATE_ALL_INDEXES,
          asyncUnaryCall(
            new MethodHandlers<
              org.vitrivr.adam.grpc.AdamGrpc.IndexMessage,
              org.vitrivr.adam.grpc.AdamGrpc.AckMessage>(
                serviceImpl, METHODID_GENERATE_ALL_INDEXES)))
        .addMethod(
          METHOD_EXISTS_INDEX,
          asyncUnaryCall(
            new MethodHandlers<
              org.vitrivr.adam.grpc.AdamGrpc.IndexMessage,
              org.vitrivr.adam.grpc.AdamGrpc.ExistsMessage>(
                serviceImpl, METHODID_EXISTS_INDEX)))
        .addMethod(
          METHOD_DROP_INDEX,
          asyncUnaryCall(
            new MethodHandlers<
              org.vitrivr.adam.grpc.AdamGrpc.IndexNameMessage,
              org.vitrivr.adam.grpc.AdamGrpc.AckMessage>(
                serviceImpl, METHODID_DROP_INDEX)))
        .addMethod(
          METHOD_LIST_INDEXES,
          asyncUnaryCall(
            new MethodHandlers<
              org.vitrivr.adam.grpc.AdamGrpc.EntityNameMessage,
              org.vitrivr.adam.grpc.AdamGrpc.IndexesMessage>(
                serviceImpl, METHODID_LIST_INDEXES)))
        .addMethod(
          METHOD_GENERATE_RANDOM_DATA,
          asyncUnaryCall(
            new MethodHandlers<
              org.vitrivr.adam.grpc.AdamGrpc.GenerateRandomDataMessage,
              org.vitrivr.adam.grpc.AdamGrpc.AckMessage>(
                serviceImpl, METHODID_GENERATE_RANDOM_DATA)))
        .addMethod(
          METHOD_LIST_ENTITIES,
          asyncUnaryCall(
            new MethodHandlers<
              org.vitrivr.adam.grpc.AdamGrpc.EmptyMessage,
              org.vitrivr.adam.grpc.AdamGrpc.EntitiesMessage>(
                serviceImpl, METHODID_LIST_ENTITIES)))
        .addMethod(
          METHOD_GET_ENTITY_PROPERTIES,
          asyncUnaryCall(
            new MethodHandlers<
              org.vitrivr.adam.grpc.AdamGrpc.EntityNameMessage,
              org.vitrivr.adam.grpc.AdamGrpc.EntityPropertiesMessage>(
                serviceImpl, METHODID_GET_ENTITY_PROPERTIES)))
        .addMethod(
          METHOD_REPARTITION_ENTITY_DATA,
          asyncUnaryCall(
            new MethodHandlers<
              org.vitrivr.adam.grpc.AdamGrpc.RepartitionMessage,
              org.vitrivr.adam.grpc.AdamGrpc.AckMessage>(
                serviceImpl, METHODID_REPARTITION_ENTITY_DATA)))
        .addMethod(
          METHOD_REPARTITION_INDEX_DATA,
          asyncUnaryCall(
            new MethodHandlers<
              org.vitrivr.adam.grpc.AdamGrpc.RepartitionMessage,
              org.vitrivr.adam.grpc.AdamGrpc.AckMessage>(
                serviceImpl, METHODID_REPARTITION_INDEX_DATA)))
        .addMethod(
          METHOD_RESET_SCAN_WEIGHTS,
          asyncUnaryCall(
            new MethodHandlers<
              org.vitrivr.adam.grpc.AdamGrpc.EntityNameMessage,
              org.vitrivr.adam.grpc.AdamGrpc.AckMessage>(
                serviceImpl, METHODID_RESET_SCAN_WEIGHTS)))
        .addMethod(
          METHOD_SET_SCAN_WEIGHT,
          asyncUnaryCall(
            new MethodHandlers<
              org.vitrivr.adam.grpc.AdamGrpc.WeightMessage,
              org.vitrivr.adam.grpc.AdamGrpc.AckMessage>(
                serviceImpl, METHODID_SET_SCAN_WEIGHT)))
        .addMethod(
          METHOD_ADAPT_SCAN_METHODS,
          asyncUnaryCall(
            new MethodHandlers<
              org.vitrivr.adam.grpc.AdamGrpc.AdaptScanMethodsMessage,
              org.vitrivr.adam.grpc.AdamGrpc.AckMessage>(
                serviceImpl, METHODID_ADAPT_SCAN_METHODS)))
        .addMethod(
          METHOD_SPARSIFY_ENTITY,
          asyncUnaryCall(
            new MethodHandlers<
              org.vitrivr.adam.grpc.AdamGrpc.SparsifyEntityMessage,
              org.vitrivr.adam.grpc.AdamGrpc.AckMessage>(
                serviceImpl, METHODID_SPARSIFY_ENTITY)))
        .addMethod(
          METHOD_IMPORT_DATA,
          asyncUnaryCall(
            new MethodHandlers<
              org.vitrivr.adam.grpc.AdamGrpc.ImportMessage,
              org.vitrivr.adam.grpc.AdamGrpc.AckMessage>(
                serviceImpl, METHODID_IMPORT_DATA)))
        .addMethod(
          METHOD_IMPORT_DATA_FILE,
          asyncUnaryCall(
            new MethodHandlers<
              org.vitrivr.adam.grpc.AdamGrpc.ImportDataFileMessage,
              org.vitrivr.adam.grpc.AdamGrpc.AckMessage>(
                serviceImpl, METHODID_IMPORT_DATA_FILE)))
        .addMethod(
          METHOD_EXPORT_DATA_FILE,
          asyncUnaryCall(
            new MethodHandlers<
              org.vitrivr.adam.grpc.AdamGrpc.EntityNameMessage,
              org.vitrivr.adam.grpc.AdamGrpc.ExportDataFileMessage>(
                serviceImpl, METHODID_EXPORT_DATA_FILE)))
        .addMethod(
          METHOD_LIST_STORAGE_HANDLERS,
          asyncUnaryCall(
            new MethodHandlers<
              org.vitrivr.adam.grpc.AdamGrpc.EmptyMessage,
              org.vitrivr.adam.grpc.AdamGrpc.StorageHandlersMessage>(
                serviceImpl, METHODID_LIST_STORAGE_HANDLERS)))
        .build();
  }
}
