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
public class AdamDefinitionGrpc {

  private AdamDefinitionGrpc() {}

  public static final String SERVICE_NAME = "AdamDefinition";

  // Static method descriptors that strictly reflect the proto.
  @io.grpc.ExperimentalApi
  public static final io.grpc.MethodDescriptor<ch.unibas.dmi.dbis.adam.http.Adam.CreateEntityMessage,
      ch.unibas.dmi.dbis.adam.http.Adam.AckMessage> METHOD_CREATE_ENTITY =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "AdamDefinition", "CreateEntity"),
          io.grpc.protobuf.ProtoUtils.marshaller(ch.unibas.dmi.dbis.adam.http.Adam.CreateEntityMessage.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(ch.unibas.dmi.dbis.adam.http.Adam.AckMessage.getDefaultInstance()));
  @io.grpc.ExperimentalApi
  public static final io.grpc.MethodDescriptor<ch.unibas.dmi.dbis.adam.http.Adam.EntityNameMessage,
      ch.unibas.dmi.dbis.adam.http.Adam.AckMessage> METHOD_COUNT =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "AdamDefinition", "Count"),
          io.grpc.protobuf.ProtoUtils.marshaller(ch.unibas.dmi.dbis.adam.http.Adam.EntityNameMessage.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(ch.unibas.dmi.dbis.adam.http.Adam.AckMessage.getDefaultInstance()));
  @io.grpc.ExperimentalApi
  public static final io.grpc.MethodDescriptor<ch.unibas.dmi.dbis.adam.http.Adam.EntityNameMessage,
      ch.unibas.dmi.dbis.adam.http.Adam.AckMessage> METHOD_DROP_ENTITY =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "AdamDefinition", "DropEntity"),
          io.grpc.protobuf.ProtoUtils.marshaller(ch.unibas.dmi.dbis.adam.http.Adam.EntityNameMessage.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(ch.unibas.dmi.dbis.adam.http.Adam.AckMessage.getDefaultInstance()));
  @io.grpc.ExperimentalApi
  public static final io.grpc.MethodDescriptor<ch.unibas.dmi.dbis.adam.http.Adam.EntityNameMessage,
      ch.unibas.dmi.dbis.adam.http.Adam.ExistsMessage> METHOD_EXISTS_ENTITY =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "AdamDefinition", "ExistsEntity"),
          io.grpc.protobuf.ProtoUtils.marshaller(ch.unibas.dmi.dbis.adam.http.Adam.EntityNameMessage.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(ch.unibas.dmi.dbis.adam.http.Adam.ExistsMessage.getDefaultInstance()));
  @io.grpc.ExperimentalApi
  public static final io.grpc.MethodDescriptor<ch.unibas.dmi.dbis.adam.http.Adam.InsertMessage,
      ch.unibas.dmi.dbis.adam.http.Adam.AckMessage> METHOD_INSERT =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.CLIENT_STREAMING,
          generateFullMethodName(
              "AdamDefinition", "Insert"),
          io.grpc.protobuf.ProtoUtils.marshaller(ch.unibas.dmi.dbis.adam.http.Adam.InsertMessage.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(ch.unibas.dmi.dbis.adam.http.Adam.AckMessage.getDefaultInstance()));
  @io.grpc.ExperimentalApi
  public static final io.grpc.MethodDescriptor<ch.unibas.dmi.dbis.adam.http.Adam.IndexMessage,
      ch.unibas.dmi.dbis.adam.http.Adam.AckMessage> METHOD_INDEX =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "AdamDefinition", "Index"),
          io.grpc.protobuf.ProtoUtils.marshaller(ch.unibas.dmi.dbis.adam.http.Adam.IndexMessage.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(ch.unibas.dmi.dbis.adam.http.Adam.AckMessage.getDefaultInstance()));
  @io.grpc.ExperimentalApi
  public static final io.grpc.MethodDescriptor<ch.unibas.dmi.dbis.adam.http.Adam.IndexMessage,
      ch.unibas.dmi.dbis.adam.http.Adam.AckMessage> METHOD_GENERATE_ALL_INDEXES =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "AdamDefinition", "GenerateAllIndexes"),
          io.grpc.protobuf.ProtoUtils.marshaller(ch.unibas.dmi.dbis.adam.http.Adam.IndexMessage.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(ch.unibas.dmi.dbis.adam.http.Adam.AckMessage.getDefaultInstance()));
  @io.grpc.ExperimentalApi
  public static final io.grpc.MethodDescriptor<ch.unibas.dmi.dbis.adam.http.Adam.IndexMessage,
      ch.unibas.dmi.dbis.adam.http.Adam.ExistsMessage> METHOD_EXISTS_INDEX =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "AdamDefinition", "ExistsIndex"),
          io.grpc.protobuf.ProtoUtils.marshaller(ch.unibas.dmi.dbis.adam.http.Adam.IndexMessage.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(ch.unibas.dmi.dbis.adam.http.Adam.ExistsMessage.getDefaultInstance()));
  @io.grpc.ExperimentalApi
  public static final io.grpc.MethodDescriptor<ch.unibas.dmi.dbis.adam.http.Adam.IndexNameMessage,
      ch.unibas.dmi.dbis.adam.http.Adam.AckMessage> METHOD_DROP_INDEX =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "AdamDefinition", "DropIndex"),
          io.grpc.protobuf.ProtoUtils.marshaller(ch.unibas.dmi.dbis.adam.http.Adam.IndexNameMessage.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(ch.unibas.dmi.dbis.adam.http.Adam.AckMessage.getDefaultInstance()));
  @io.grpc.ExperimentalApi
  public static final io.grpc.MethodDescriptor<ch.unibas.dmi.dbis.adam.http.Adam.EntityNameMessage,
      ch.unibas.dmi.dbis.adam.http.Adam.IndexesMessage> METHOD_LIST_INDEXES =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "AdamDefinition", "ListIndexes"),
          io.grpc.protobuf.ProtoUtils.marshaller(ch.unibas.dmi.dbis.adam.http.Adam.EntityNameMessage.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(ch.unibas.dmi.dbis.adam.http.Adam.IndexesMessage.getDefaultInstance()));
  @io.grpc.ExperimentalApi
  public static final io.grpc.MethodDescriptor<ch.unibas.dmi.dbis.adam.http.Adam.GenerateRandomDataMessage,
      ch.unibas.dmi.dbis.adam.http.Adam.AckMessage> METHOD_GENERATE_RANDOM_DATA =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "AdamDefinition", "GenerateRandomData"),
          io.grpc.protobuf.ProtoUtils.marshaller(ch.unibas.dmi.dbis.adam.http.Adam.GenerateRandomDataMessage.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(ch.unibas.dmi.dbis.adam.http.Adam.AckMessage.getDefaultInstance()));
  @io.grpc.ExperimentalApi
  public static final io.grpc.MethodDescriptor<ch.unibas.dmi.dbis.adam.http.Adam.EmptyMessage,
      ch.unibas.dmi.dbis.adam.http.Adam.EntitiesMessage> METHOD_LIST_ENTITIES =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "AdamDefinition", "ListEntities"),
          io.grpc.protobuf.ProtoUtils.marshaller(ch.unibas.dmi.dbis.adam.http.Adam.EmptyMessage.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(ch.unibas.dmi.dbis.adam.http.Adam.EntitiesMessage.getDefaultInstance()));
  @io.grpc.ExperimentalApi
  public static final io.grpc.MethodDescriptor<ch.unibas.dmi.dbis.adam.http.Adam.EntityNameMessage,
      ch.unibas.dmi.dbis.adam.http.Adam.EntityPropertiesMessage> METHOD_GET_ENTITY_PROPERTIES =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "AdamDefinition", "GetEntityProperties"),
          io.grpc.protobuf.ProtoUtils.marshaller(ch.unibas.dmi.dbis.adam.http.Adam.EntityNameMessage.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(ch.unibas.dmi.dbis.adam.http.Adam.EntityPropertiesMessage.getDefaultInstance()));
  @io.grpc.ExperimentalApi
  public static final io.grpc.MethodDescriptor<ch.unibas.dmi.dbis.adam.http.Adam.RepartitionMessage,
      ch.unibas.dmi.dbis.adam.http.Adam.AckMessage> METHOD_REPARTITION_ENTITY_DATA =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "AdamDefinition", "RepartitionEntityData"),
          io.grpc.protobuf.ProtoUtils.marshaller(ch.unibas.dmi.dbis.adam.http.Adam.RepartitionMessage.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(ch.unibas.dmi.dbis.adam.http.Adam.AckMessage.getDefaultInstance()));
  @io.grpc.ExperimentalApi
  public static final io.grpc.MethodDescriptor<ch.unibas.dmi.dbis.adam.http.Adam.RepartitionMessage,
      ch.unibas.dmi.dbis.adam.http.Adam.AckMessage> METHOD_REPARTITION_INDEX_DATA =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "AdamDefinition", "RepartitionIndexData"),
          io.grpc.protobuf.ProtoUtils.marshaller(ch.unibas.dmi.dbis.adam.http.Adam.RepartitionMessage.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(ch.unibas.dmi.dbis.adam.http.Adam.AckMessage.getDefaultInstance()));
  @io.grpc.ExperimentalApi
  public static final io.grpc.MethodDescriptor<ch.unibas.dmi.dbis.adam.http.Adam.UpdateWeightsMessage,
      ch.unibas.dmi.dbis.adam.http.Adam.AckMessage> METHOD_ADJUST_SCAN_WEIGHTS =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "AdamDefinition", "AdjustScanWeights"),
          io.grpc.protobuf.ProtoUtils.marshaller(ch.unibas.dmi.dbis.adam.http.Adam.UpdateWeightsMessage.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(ch.unibas.dmi.dbis.adam.http.Adam.AckMessage.getDefaultInstance()));
  @io.grpc.ExperimentalApi
  public static final io.grpc.MethodDescriptor<ch.unibas.dmi.dbis.adam.http.Adam.EntityNameMessage,
      ch.unibas.dmi.dbis.adam.http.Adam.AckMessage> METHOD_RESET_SCAN_WEIGHTS =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "AdamDefinition", "ResetScanWeights"),
          io.grpc.protobuf.ProtoUtils.marshaller(ch.unibas.dmi.dbis.adam.http.Adam.EntityNameMessage.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(ch.unibas.dmi.dbis.adam.http.Adam.AckMessage.getDefaultInstance()));
  @io.grpc.ExperimentalApi
  public static final io.grpc.MethodDescriptor<ch.unibas.dmi.dbis.adam.http.Adam.WeightMessage,
      ch.unibas.dmi.dbis.adam.http.Adam.AckMessage> METHOD_SET_SCAN_WEIGHT =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "AdamDefinition", "SetScanWeight"),
          io.grpc.protobuf.ProtoUtils.marshaller(ch.unibas.dmi.dbis.adam.http.Adam.WeightMessage.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(ch.unibas.dmi.dbis.adam.http.Adam.AckMessage.getDefaultInstance()));
  @io.grpc.ExperimentalApi
  public static final io.grpc.MethodDescriptor<ch.unibas.dmi.dbis.adam.http.Adam.SparsifyEntityMessage,
      ch.unibas.dmi.dbis.adam.http.Adam.AckMessage> METHOD_SPARSIFY_ENTITY =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "AdamDefinition", "SparsifyEntity"),
          io.grpc.protobuf.ProtoUtils.marshaller(ch.unibas.dmi.dbis.adam.http.Adam.SparsifyEntityMessage.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(ch.unibas.dmi.dbis.adam.http.Adam.AckMessage.getDefaultInstance()));
  @io.grpc.ExperimentalApi
  public static final io.grpc.MethodDescriptor<ch.unibas.dmi.dbis.adam.http.Adam.ImportMessage,
      ch.unibas.dmi.dbis.adam.http.Adam.AckMessage> METHOD_IMPORT_DATA =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "AdamDefinition", "ImportData"),
          io.grpc.protobuf.ProtoUtils.marshaller(ch.unibas.dmi.dbis.adam.http.Adam.ImportMessage.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(ch.unibas.dmi.dbis.adam.http.Adam.AckMessage.getDefaultInstance()));

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

    public void createEntity(ch.unibas.dmi.dbis.adam.http.Adam.CreateEntityMessage request,
        io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.adam.http.Adam.AckMessage> responseObserver);

    public void count(ch.unibas.dmi.dbis.adam.http.Adam.EntityNameMessage request,
        io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.adam.http.Adam.AckMessage> responseObserver);

    public void dropEntity(ch.unibas.dmi.dbis.adam.http.Adam.EntityNameMessage request,
        io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.adam.http.Adam.AckMessage> responseObserver);

    public void existsEntity(ch.unibas.dmi.dbis.adam.http.Adam.EntityNameMessage request,
        io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.adam.http.Adam.ExistsMessage> responseObserver);

    public io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.adam.http.Adam.InsertMessage> insert(
        io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.adam.http.Adam.AckMessage> responseObserver);

    public void index(ch.unibas.dmi.dbis.adam.http.Adam.IndexMessage request,
        io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.adam.http.Adam.AckMessage> responseObserver);

    public void generateAllIndexes(ch.unibas.dmi.dbis.adam.http.Adam.IndexMessage request,
        io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.adam.http.Adam.AckMessage> responseObserver);

    public void existsIndex(ch.unibas.dmi.dbis.adam.http.Adam.IndexMessage request,
        io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.adam.http.Adam.ExistsMessage> responseObserver);

    public void dropIndex(ch.unibas.dmi.dbis.adam.http.Adam.IndexNameMessage request,
        io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.adam.http.Adam.AckMessage> responseObserver);

    public void listIndexes(ch.unibas.dmi.dbis.adam.http.Adam.EntityNameMessage request,
        io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.adam.http.Adam.IndexesMessage> responseObserver);

    public void generateRandomData(ch.unibas.dmi.dbis.adam.http.Adam.GenerateRandomDataMessage request,
        io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.adam.http.Adam.AckMessage> responseObserver);

    public void listEntities(ch.unibas.dmi.dbis.adam.http.Adam.EmptyMessage request,
        io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.adam.http.Adam.EntitiesMessage> responseObserver);

    public void getEntityProperties(ch.unibas.dmi.dbis.adam.http.Adam.EntityNameMessage request,
        io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.adam.http.Adam.EntityPropertiesMessage> responseObserver);

    public void repartitionEntityData(ch.unibas.dmi.dbis.adam.http.Adam.RepartitionMessage request,
        io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.adam.http.Adam.AckMessage> responseObserver);

    public void repartitionIndexData(ch.unibas.dmi.dbis.adam.http.Adam.RepartitionMessage request,
        io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.adam.http.Adam.AckMessage> responseObserver);

    public void adjustScanWeights(ch.unibas.dmi.dbis.adam.http.Adam.UpdateWeightsMessage request,
        io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.adam.http.Adam.AckMessage> responseObserver);

    public void resetScanWeights(ch.unibas.dmi.dbis.adam.http.Adam.EntityNameMessage request,
        io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.adam.http.Adam.AckMessage> responseObserver);

    public void setScanWeight(ch.unibas.dmi.dbis.adam.http.Adam.WeightMessage request,
        io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.adam.http.Adam.AckMessage> responseObserver);

    public void sparsifyEntity(ch.unibas.dmi.dbis.adam.http.Adam.SparsifyEntityMessage request,
        io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.adam.http.Adam.AckMessage> responseObserver);

    public void importData(ch.unibas.dmi.dbis.adam.http.Adam.ImportMessage request,
        io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.adam.http.Adam.AckMessage> responseObserver);
  }

  public static interface AdamDefinitionBlockingClient {

    public ch.unibas.dmi.dbis.adam.http.Adam.AckMessage createEntity(ch.unibas.dmi.dbis.adam.http.Adam.CreateEntityMessage request);

    public ch.unibas.dmi.dbis.adam.http.Adam.AckMessage count(ch.unibas.dmi.dbis.adam.http.Adam.EntityNameMessage request);

    public ch.unibas.dmi.dbis.adam.http.Adam.AckMessage dropEntity(ch.unibas.dmi.dbis.adam.http.Adam.EntityNameMessage request);

    public ch.unibas.dmi.dbis.adam.http.Adam.ExistsMessage existsEntity(ch.unibas.dmi.dbis.adam.http.Adam.EntityNameMessage request);

    public ch.unibas.dmi.dbis.adam.http.Adam.AckMessage index(ch.unibas.dmi.dbis.adam.http.Adam.IndexMessage request);

    public ch.unibas.dmi.dbis.adam.http.Adam.AckMessage generateAllIndexes(ch.unibas.dmi.dbis.adam.http.Adam.IndexMessage request);

    public ch.unibas.dmi.dbis.adam.http.Adam.ExistsMessage existsIndex(ch.unibas.dmi.dbis.adam.http.Adam.IndexMessage request);

    public ch.unibas.dmi.dbis.adam.http.Adam.AckMessage dropIndex(ch.unibas.dmi.dbis.adam.http.Adam.IndexNameMessage request);

    public ch.unibas.dmi.dbis.adam.http.Adam.IndexesMessage listIndexes(ch.unibas.dmi.dbis.adam.http.Adam.EntityNameMessage request);

    public ch.unibas.dmi.dbis.adam.http.Adam.AckMessage generateRandomData(ch.unibas.dmi.dbis.adam.http.Adam.GenerateRandomDataMessage request);

    public ch.unibas.dmi.dbis.adam.http.Adam.EntitiesMessage listEntities(ch.unibas.dmi.dbis.adam.http.Adam.EmptyMessage request);

    public ch.unibas.dmi.dbis.adam.http.Adam.EntityPropertiesMessage getEntityProperties(ch.unibas.dmi.dbis.adam.http.Adam.EntityNameMessage request);

    public ch.unibas.dmi.dbis.adam.http.Adam.AckMessage repartitionEntityData(ch.unibas.dmi.dbis.adam.http.Adam.RepartitionMessage request);

    public ch.unibas.dmi.dbis.adam.http.Adam.AckMessage repartitionIndexData(ch.unibas.dmi.dbis.adam.http.Adam.RepartitionMessage request);

    public ch.unibas.dmi.dbis.adam.http.Adam.AckMessage adjustScanWeights(ch.unibas.dmi.dbis.adam.http.Adam.UpdateWeightsMessage request);

    public ch.unibas.dmi.dbis.adam.http.Adam.AckMessage resetScanWeights(ch.unibas.dmi.dbis.adam.http.Adam.EntityNameMessage request);

    public ch.unibas.dmi.dbis.adam.http.Adam.AckMessage setScanWeight(ch.unibas.dmi.dbis.adam.http.Adam.WeightMessage request);

    public ch.unibas.dmi.dbis.adam.http.Adam.AckMessage sparsifyEntity(ch.unibas.dmi.dbis.adam.http.Adam.SparsifyEntityMessage request);

    public ch.unibas.dmi.dbis.adam.http.Adam.AckMessage importData(ch.unibas.dmi.dbis.adam.http.Adam.ImportMessage request);
  }

  public static interface AdamDefinitionFutureClient {

    public com.google.common.util.concurrent.ListenableFuture<ch.unibas.dmi.dbis.adam.http.Adam.AckMessage> createEntity(
        ch.unibas.dmi.dbis.adam.http.Adam.CreateEntityMessage request);

    public com.google.common.util.concurrent.ListenableFuture<ch.unibas.dmi.dbis.adam.http.Adam.AckMessage> count(
        ch.unibas.dmi.dbis.adam.http.Adam.EntityNameMessage request);

    public com.google.common.util.concurrent.ListenableFuture<ch.unibas.dmi.dbis.adam.http.Adam.AckMessage> dropEntity(
        ch.unibas.dmi.dbis.adam.http.Adam.EntityNameMessage request);

    public com.google.common.util.concurrent.ListenableFuture<ch.unibas.dmi.dbis.adam.http.Adam.ExistsMessage> existsEntity(
        ch.unibas.dmi.dbis.adam.http.Adam.EntityNameMessage request);

    public com.google.common.util.concurrent.ListenableFuture<ch.unibas.dmi.dbis.adam.http.Adam.AckMessage> index(
        ch.unibas.dmi.dbis.adam.http.Adam.IndexMessage request);

    public com.google.common.util.concurrent.ListenableFuture<ch.unibas.dmi.dbis.adam.http.Adam.AckMessage> generateAllIndexes(
        ch.unibas.dmi.dbis.adam.http.Adam.IndexMessage request);

    public com.google.common.util.concurrent.ListenableFuture<ch.unibas.dmi.dbis.adam.http.Adam.ExistsMessage> existsIndex(
        ch.unibas.dmi.dbis.adam.http.Adam.IndexMessage request);

    public com.google.common.util.concurrent.ListenableFuture<ch.unibas.dmi.dbis.adam.http.Adam.AckMessage> dropIndex(
        ch.unibas.dmi.dbis.adam.http.Adam.IndexNameMessage request);

    public com.google.common.util.concurrent.ListenableFuture<ch.unibas.dmi.dbis.adam.http.Adam.IndexesMessage> listIndexes(
        ch.unibas.dmi.dbis.adam.http.Adam.EntityNameMessage request);

    public com.google.common.util.concurrent.ListenableFuture<ch.unibas.dmi.dbis.adam.http.Adam.AckMessage> generateRandomData(
        ch.unibas.dmi.dbis.adam.http.Adam.GenerateRandomDataMessage request);

    public com.google.common.util.concurrent.ListenableFuture<ch.unibas.dmi.dbis.adam.http.Adam.EntitiesMessage> listEntities(
        ch.unibas.dmi.dbis.adam.http.Adam.EmptyMessage request);

    public com.google.common.util.concurrent.ListenableFuture<ch.unibas.dmi.dbis.adam.http.Adam.EntityPropertiesMessage> getEntityProperties(
        ch.unibas.dmi.dbis.adam.http.Adam.EntityNameMessage request);

    public com.google.common.util.concurrent.ListenableFuture<ch.unibas.dmi.dbis.adam.http.Adam.AckMessage> repartitionEntityData(
        ch.unibas.dmi.dbis.adam.http.Adam.RepartitionMessage request);

    public com.google.common.util.concurrent.ListenableFuture<ch.unibas.dmi.dbis.adam.http.Adam.AckMessage> repartitionIndexData(
        ch.unibas.dmi.dbis.adam.http.Adam.RepartitionMessage request);

    public com.google.common.util.concurrent.ListenableFuture<ch.unibas.dmi.dbis.adam.http.Adam.AckMessage> adjustScanWeights(
        ch.unibas.dmi.dbis.adam.http.Adam.UpdateWeightsMessage request);

    public com.google.common.util.concurrent.ListenableFuture<ch.unibas.dmi.dbis.adam.http.Adam.AckMessage> resetScanWeights(
        ch.unibas.dmi.dbis.adam.http.Adam.EntityNameMessage request);

    public com.google.common.util.concurrent.ListenableFuture<ch.unibas.dmi.dbis.adam.http.Adam.AckMessage> setScanWeight(
        ch.unibas.dmi.dbis.adam.http.Adam.WeightMessage request);

    public com.google.common.util.concurrent.ListenableFuture<ch.unibas.dmi.dbis.adam.http.Adam.AckMessage> sparsifyEntity(
        ch.unibas.dmi.dbis.adam.http.Adam.SparsifyEntityMessage request);

    public com.google.common.util.concurrent.ListenableFuture<ch.unibas.dmi.dbis.adam.http.Adam.AckMessage> importData(
        ch.unibas.dmi.dbis.adam.http.Adam.ImportMessage request);
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
    public void createEntity(ch.unibas.dmi.dbis.adam.http.Adam.CreateEntityMessage request,
        io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.adam.http.Adam.AckMessage> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_CREATE_ENTITY, getCallOptions()), request, responseObserver);
    }

    @java.lang.Override
    public void count(ch.unibas.dmi.dbis.adam.http.Adam.EntityNameMessage request,
        io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.adam.http.Adam.AckMessage> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_COUNT, getCallOptions()), request, responseObserver);
    }

    @java.lang.Override
    public void dropEntity(ch.unibas.dmi.dbis.adam.http.Adam.EntityNameMessage request,
        io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.adam.http.Adam.AckMessage> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_DROP_ENTITY, getCallOptions()), request, responseObserver);
    }

    @java.lang.Override
    public void existsEntity(ch.unibas.dmi.dbis.adam.http.Adam.EntityNameMessage request,
        io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.adam.http.Adam.ExistsMessage> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_EXISTS_ENTITY, getCallOptions()), request, responseObserver);
    }

    @java.lang.Override
    public io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.adam.http.Adam.InsertMessage> insert(
        io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.adam.http.Adam.AckMessage> responseObserver) {
      return asyncClientStreamingCall(
          getChannel().newCall(METHOD_INSERT, getCallOptions()), responseObserver);
    }

    @java.lang.Override
    public void index(ch.unibas.dmi.dbis.adam.http.Adam.IndexMessage request,
        io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.adam.http.Adam.AckMessage> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_INDEX, getCallOptions()), request, responseObserver);
    }

    @java.lang.Override
    public void generateAllIndexes(ch.unibas.dmi.dbis.adam.http.Adam.IndexMessage request,
        io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.adam.http.Adam.AckMessage> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_GENERATE_ALL_INDEXES, getCallOptions()), request, responseObserver);
    }

    @java.lang.Override
    public void existsIndex(ch.unibas.dmi.dbis.adam.http.Adam.IndexMessage request,
        io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.adam.http.Adam.ExistsMessage> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_EXISTS_INDEX, getCallOptions()), request, responseObserver);
    }

    @java.lang.Override
    public void dropIndex(ch.unibas.dmi.dbis.adam.http.Adam.IndexNameMessage request,
        io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.adam.http.Adam.AckMessage> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_DROP_INDEX, getCallOptions()), request, responseObserver);
    }

    @java.lang.Override
    public void listIndexes(ch.unibas.dmi.dbis.adam.http.Adam.EntityNameMessage request,
        io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.adam.http.Adam.IndexesMessage> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_LIST_INDEXES, getCallOptions()), request, responseObserver);
    }

    @java.lang.Override
    public void generateRandomData(ch.unibas.dmi.dbis.adam.http.Adam.GenerateRandomDataMessage request,
        io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.adam.http.Adam.AckMessage> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_GENERATE_RANDOM_DATA, getCallOptions()), request, responseObserver);
    }

    @java.lang.Override
    public void listEntities(ch.unibas.dmi.dbis.adam.http.Adam.EmptyMessage request,
        io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.adam.http.Adam.EntitiesMessage> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_LIST_ENTITIES, getCallOptions()), request, responseObserver);
    }

    @java.lang.Override
    public void getEntityProperties(ch.unibas.dmi.dbis.adam.http.Adam.EntityNameMessage request,
        io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.adam.http.Adam.EntityPropertiesMessage> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_GET_ENTITY_PROPERTIES, getCallOptions()), request, responseObserver);
    }

    @java.lang.Override
    public void repartitionEntityData(ch.unibas.dmi.dbis.adam.http.Adam.RepartitionMessage request,
        io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.adam.http.Adam.AckMessage> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_REPARTITION_ENTITY_DATA, getCallOptions()), request, responseObserver);
    }

    @java.lang.Override
    public void repartitionIndexData(ch.unibas.dmi.dbis.adam.http.Adam.RepartitionMessage request,
        io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.adam.http.Adam.AckMessage> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_REPARTITION_INDEX_DATA, getCallOptions()), request, responseObserver);
    }

    @java.lang.Override
    public void adjustScanWeights(ch.unibas.dmi.dbis.adam.http.Adam.UpdateWeightsMessage request,
        io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.adam.http.Adam.AckMessage> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_ADJUST_SCAN_WEIGHTS, getCallOptions()), request, responseObserver);
    }

    @java.lang.Override
    public void resetScanWeights(ch.unibas.dmi.dbis.adam.http.Adam.EntityNameMessage request,
        io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.adam.http.Adam.AckMessage> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_RESET_SCAN_WEIGHTS, getCallOptions()), request, responseObserver);
    }

    @java.lang.Override
    public void setScanWeight(ch.unibas.dmi.dbis.adam.http.Adam.WeightMessage request,
        io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.adam.http.Adam.AckMessage> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_SET_SCAN_WEIGHT, getCallOptions()), request, responseObserver);
    }

    @java.lang.Override
    public void sparsifyEntity(ch.unibas.dmi.dbis.adam.http.Adam.SparsifyEntityMessage request,
        io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.adam.http.Adam.AckMessage> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_SPARSIFY_ENTITY, getCallOptions()), request, responseObserver);
    }

    @java.lang.Override
    public void importData(ch.unibas.dmi.dbis.adam.http.Adam.ImportMessage request,
        io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.adam.http.Adam.AckMessage> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_IMPORT_DATA, getCallOptions()), request, responseObserver);
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
    public ch.unibas.dmi.dbis.adam.http.Adam.AckMessage createEntity(ch.unibas.dmi.dbis.adam.http.Adam.CreateEntityMessage request) {
      return blockingUnaryCall(
          getChannel(), METHOD_CREATE_ENTITY, getCallOptions(), request);
    }

    @java.lang.Override
    public ch.unibas.dmi.dbis.adam.http.Adam.AckMessage count(ch.unibas.dmi.dbis.adam.http.Adam.EntityNameMessage request) {
      return blockingUnaryCall(
          getChannel(), METHOD_COUNT, getCallOptions(), request);
    }

    @java.lang.Override
    public ch.unibas.dmi.dbis.adam.http.Adam.AckMessage dropEntity(ch.unibas.dmi.dbis.adam.http.Adam.EntityNameMessage request) {
      return blockingUnaryCall(
          getChannel(), METHOD_DROP_ENTITY, getCallOptions(), request);
    }

    @java.lang.Override
    public ch.unibas.dmi.dbis.adam.http.Adam.ExistsMessage existsEntity(ch.unibas.dmi.dbis.adam.http.Adam.EntityNameMessage request) {
      return blockingUnaryCall(
          getChannel(), METHOD_EXISTS_ENTITY, getCallOptions(), request);
    }

    @java.lang.Override
    public ch.unibas.dmi.dbis.adam.http.Adam.AckMessage index(ch.unibas.dmi.dbis.adam.http.Adam.IndexMessage request) {
      return blockingUnaryCall(
          getChannel(), METHOD_INDEX, getCallOptions(), request);
    }

    @java.lang.Override
    public ch.unibas.dmi.dbis.adam.http.Adam.AckMessage generateAllIndexes(ch.unibas.dmi.dbis.adam.http.Adam.IndexMessage request) {
      return blockingUnaryCall(
          getChannel(), METHOD_GENERATE_ALL_INDEXES, getCallOptions(), request);
    }

    @java.lang.Override
    public ch.unibas.dmi.dbis.adam.http.Adam.ExistsMessage existsIndex(ch.unibas.dmi.dbis.adam.http.Adam.IndexMessage request) {
      return blockingUnaryCall(
          getChannel(), METHOD_EXISTS_INDEX, getCallOptions(), request);
    }

    @java.lang.Override
    public ch.unibas.dmi.dbis.adam.http.Adam.AckMessage dropIndex(ch.unibas.dmi.dbis.adam.http.Adam.IndexNameMessage request) {
      return blockingUnaryCall(
          getChannel(), METHOD_DROP_INDEX, getCallOptions(), request);
    }

    @java.lang.Override
    public ch.unibas.dmi.dbis.adam.http.Adam.IndexesMessage listIndexes(ch.unibas.dmi.dbis.adam.http.Adam.EntityNameMessage request) {
      return blockingUnaryCall(
          getChannel(), METHOD_LIST_INDEXES, getCallOptions(), request);
    }

    @java.lang.Override
    public ch.unibas.dmi.dbis.adam.http.Adam.AckMessage generateRandomData(ch.unibas.dmi.dbis.adam.http.Adam.GenerateRandomDataMessage request) {
      return blockingUnaryCall(
          getChannel(), METHOD_GENERATE_RANDOM_DATA, getCallOptions(), request);
    }

    @java.lang.Override
    public ch.unibas.dmi.dbis.adam.http.Adam.EntitiesMessage listEntities(ch.unibas.dmi.dbis.adam.http.Adam.EmptyMessage request) {
      return blockingUnaryCall(
          getChannel(), METHOD_LIST_ENTITIES, getCallOptions(), request);
    }

    @java.lang.Override
    public ch.unibas.dmi.dbis.adam.http.Adam.EntityPropertiesMessage getEntityProperties(ch.unibas.dmi.dbis.adam.http.Adam.EntityNameMessage request) {
      return blockingUnaryCall(
          getChannel(), METHOD_GET_ENTITY_PROPERTIES, getCallOptions(), request);
    }

    @java.lang.Override
    public ch.unibas.dmi.dbis.adam.http.Adam.AckMessage repartitionEntityData(ch.unibas.dmi.dbis.adam.http.Adam.RepartitionMessage request) {
      return blockingUnaryCall(
          getChannel(), METHOD_REPARTITION_ENTITY_DATA, getCallOptions(), request);
    }

    @java.lang.Override
    public ch.unibas.dmi.dbis.adam.http.Adam.AckMessage repartitionIndexData(ch.unibas.dmi.dbis.adam.http.Adam.RepartitionMessage request) {
      return blockingUnaryCall(
          getChannel(), METHOD_REPARTITION_INDEX_DATA, getCallOptions(), request);
    }

    @java.lang.Override
    public ch.unibas.dmi.dbis.adam.http.Adam.AckMessage adjustScanWeights(ch.unibas.dmi.dbis.adam.http.Adam.UpdateWeightsMessage request) {
      return blockingUnaryCall(
          getChannel(), METHOD_ADJUST_SCAN_WEIGHTS, getCallOptions(), request);
    }

    @java.lang.Override
    public ch.unibas.dmi.dbis.adam.http.Adam.AckMessage resetScanWeights(ch.unibas.dmi.dbis.adam.http.Adam.EntityNameMessage request) {
      return blockingUnaryCall(
          getChannel(), METHOD_RESET_SCAN_WEIGHTS, getCallOptions(), request);
    }

    @java.lang.Override
    public ch.unibas.dmi.dbis.adam.http.Adam.AckMessage setScanWeight(ch.unibas.dmi.dbis.adam.http.Adam.WeightMessage request) {
      return blockingUnaryCall(
          getChannel(), METHOD_SET_SCAN_WEIGHT, getCallOptions(), request);
    }

    @java.lang.Override
    public ch.unibas.dmi.dbis.adam.http.Adam.AckMessage sparsifyEntity(ch.unibas.dmi.dbis.adam.http.Adam.SparsifyEntityMessage request) {
      return blockingUnaryCall(
          getChannel(), METHOD_SPARSIFY_ENTITY, getCallOptions(), request);
    }

    @java.lang.Override
    public ch.unibas.dmi.dbis.adam.http.Adam.AckMessage importData(ch.unibas.dmi.dbis.adam.http.Adam.ImportMessage request) {
      return blockingUnaryCall(
          getChannel(), METHOD_IMPORT_DATA, getCallOptions(), request);
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
    public com.google.common.util.concurrent.ListenableFuture<ch.unibas.dmi.dbis.adam.http.Adam.AckMessage> createEntity(
        ch.unibas.dmi.dbis.adam.http.Adam.CreateEntityMessage request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_CREATE_ENTITY, getCallOptions()), request);
    }

    @java.lang.Override
    public com.google.common.util.concurrent.ListenableFuture<ch.unibas.dmi.dbis.adam.http.Adam.AckMessage> count(
        ch.unibas.dmi.dbis.adam.http.Adam.EntityNameMessage request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_COUNT, getCallOptions()), request);
    }

    @java.lang.Override
    public com.google.common.util.concurrent.ListenableFuture<ch.unibas.dmi.dbis.adam.http.Adam.AckMessage> dropEntity(
        ch.unibas.dmi.dbis.adam.http.Adam.EntityNameMessage request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_DROP_ENTITY, getCallOptions()), request);
    }

    @java.lang.Override
    public com.google.common.util.concurrent.ListenableFuture<ch.unibas.dmi.dbis.adam.http.Adam.ExistsMessage> existsEntity(
        ch.unibas.dmi.dbis.adam.http.Adam.EntityNameMessage request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_EXISTS_ENTITY, getCallOptions()), request);
    }

    @java.lang.Override
    public com.google.common.util.concurrent.ListenableFuture<ch.unibas.dmi.dbis.adam.http.Adam.AckMessage> index(
        ch.unibas.dmi.dbis.adam.http.Adam.IndexMessage request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_INDEX, getCallOptions()), request);
    }

    @java.lang.Override
    public com.google.common.util.concurrent.ListenableFuture<ch.unibas.dmi.dbis.adam.http.Adam.AckMessage> generateAllIndexes(
        ch.unibas.dmi.dbis.adam.http.Adam.IndexMessage request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_GENERATE_ALL_INDEXES, getCallOptions()), request);
    }

    @java.lang.Override
    public com.google.common.util.concurrent.ListenableFuture<ch.unibas.dmi.dbis.adam.http.Adam.ExistsMessage> existsIndex(
        ch.unibas.dmi.dbis.adam.http.Adam.IndexMessage request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_EXISTS_INDEX, getCallOptions()), request);
    }

    @java.lang.Override
    public com.google.common.util.concurrent.ListenableFuture<ch.unibas.dmi.dbis.adam.http.Adam.AckMessage> dropIndex(
        ch.unibas.dmi.dbis.adam.http.Adam.IndexNameMessage request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_DROP_INDEX, getCallOptions()), request);
    }

    @java.lang.Override
    public com.google.common.util.concurrent.ListenableFuture<ch.unibas.dmi.dbis.adam.http.Adam.IndexesMessage> listIndexes(
        ch.unibas.dmi.dbis.adam.http.Adam.EntityNameMessage request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_LIST_INDEXES, getCallOptions()), request);
    }

    @java.lang.Override
    public com.google.common.util.concurrent.ListenableFuture<ch.unibas.dmi.dbis.adam.http.Adam.AckMessage> generateRandomData(
        ch.unibas.dmi.dbis.adam.http.Adam.GenerateRandomDataMessage request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_GENERATE_RANDOM_DATA, getCallOptions()), request);
    }

    @java.lang.Override
    public com.google.common.util.concurrent.ListenableFuture<ch.unibas.dmi.dbis.adam.http.Adam.EntitiesMessage> listEntities(
        ch.unibas.dmi.dbis.adam.http.Adam.EmptyMessage request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_LIST_ENTITIES, getCallOptions()), request);
    }

    @java.lang.Override
    public com.google.common.util.concurrent.ListenableFuture<ch.unibas.dmi.dbis.adam.http.Adam.EntityPropertiesMessage> getEntityProperties(
        ch.unibas.dmi.dbis.adam.http.Adam.EntityNameMessage request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_GET_ENTITY_PROPERTIES, getCallOptions()), request);
    }

    @java.lang.Override
    public com.google.common.util.concurrent.ListenableFuture<ch.unibas.dmi.dbis.adam.http.Adam.AckMessage> repartitionEntityData(
        ch.unibas.dmi.dbis.adam.http.Adam.RepartitionMessage request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_REPARTITION_ENTITY_DATA, getCallOptions()), request);
    }

    @java.lang.Override
    public com.google.common.util.concurrent.ListenableFuture<ch.unibas.dmi.dbis.adam.http.Adam.AckMessage> repartitionIndexData(
        ch.unibas.dmi.dbis.adam.http.Adam.RepartitionMessage request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_REPARTITION_INDEX_DATA, getCallOptions()), request);
    }

    @java.lang.Override
    public com.google.common.util.concurrent.ListenableFuture<ch.unibas.dmi.dbis.adam.http.Adam.AckMessage> adjustScanWeights(
        ch.unibas.dmi.dbis.adam.http.Adam.UpdateWeightsMessage request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_ADJUST_SCAN_WEIGHTS, getCallOptions()), request);
    }

    @java.lang.Override
    public com.google.common.util.concurrent.ListenableFuture<ch.unibas.dmi.dbis.adam.http.Adam.AckMessage> resetScanWeights(
        ch.unibas.dmi.dbis.adam.http.Adam.EntityNameMessage request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_RESET_SCAN_WEIGHTS, getCallOptions()), request);
    }

    @java.lang.Override
    public com.google.common.util.concurrent.ListenableFuture<ch.unibas.dmi.dbis.adam.http.Adam.AckMessage> setScanWeight(
        ch.unibas.dmi.dbis.adam.http.Adam.WeightMessage request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_SET_SCAN_WEIGHT, getCallOptions()), request);
    }

    @java.lang.Override
    public com.google.common.util.concurrent.ListenableFuture<ch.unibas.dmi.dbis.adam.http.Adam.AckMessage> sparsifyEntity(
        ch.unibas.dmi.dbis.adam.http.Adam.SparsifyEntityMessage request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_SPARSIFY_ENTITY, getCallOptions()), request);
    }

    @java.lang.Override
    public com.google.common.util.concurrent.ListenableFuture<ch.unibas.dmi.dbis.adam.http.Adam.AckMessage> importData(
        ch.unibas.dmi.dbis.adam.http.Adam.ImportMessage request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_IMPORT_DATA, getCallOptions()), request);
    }
  }

  private static final int METHODID_CREATE_ENTITY = 0;
  private static final int METHODID_COUNT = 1;
  private static final int METHODID_DROP_ENTITY = 2;
  private static final int METHODID_EXISTS_ENTITY = 3;
  private static final int METHODID_INDEX = 4;
  private static final int METHODID_GENERATE_ALL_INDEXES = 5;
  private static final int METHODID_EXISTS_INDEX = 6;
  private static final int METHODID_DROP_INDEX = 7;
  private static final int METHODID_LIST_INDEXES = 8;
  private static final int METHODID_GENERATE_RANDOM_DATA = 9;
  private static final int METHODID_LIST_ENTITIES = 10;
  private static final int METHODID_GET_ENTITY_PROPERTIES = 11;
  private static final int METHODID_REPARTITION_ENTITY_DATA = 12;
  private static final int METHODID_REPARTITION_INDEX_DATA = 13;
  private static final int METHODID_ADJUST_SCAN_WEIGHTS = 14;
  private static final int METHODID_RESET_SCAN_WEIGHTS = 15;
  private static final int METHODID_SET_SCAN_WEIGHT = 16;
  private static final int METHODID_SPARSIFY_ENTITY = 17;
  private static final int METHODID_IMPORT_DATA = 18;
  private static final int METHODID_INSERT = 19;

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
          serviceImpl.createEntity((ch.unibas.dmi.dbis.adam.http.Adam.CreateEntityMessage) request,
              (io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.adam.http.Adam.AckMessage>) responseObserver);
          break;
        case METHODID_COUNT:
          serviceImpl.count((ch.unibas.dmi.dbis.adam.http.Adam.EntityNameMessage) request,
              (io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.adam.http.Adam.AckMessage>) responseObserver);
          break;
        case METHODID_DROP_ENTITY:
          serviceImpl.dropEntity((ch.unibas.dmi.dbis.adam.http.Adam.EntityNameMessage) request,
              (io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.adam.http.Adam.AckMessage>) responseObserver);
          break;
        case METHODID_EXISTS_ENTITY:
          serviceImpl.existsEntity((ch.unibas.dmi.dbis.adam.http.Adam.EntityNameMessage) request,
              (io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.adam.http.Adam.ExistsMessage>) responseObserver);
          break;
        case METHODID_INDEX:
          serviceImpl.index((ch.unibas.dmi.dbis.adam.http.Adam.IndexMessage) request,
              (io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.adam.http.Adam.AckMessage>) responseObserver);
          break;
        case METHODID_GENERATE_ALL_INDEXES:
          serviceImpl.generateAllIndexes((ch.unibas.dmi.dbis.adam.http.Adam.IndexMessage) request,
              (io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.adam.http.Adam.AckMessage>) responseObserver);
          break;
        case METHODID_EXISTS_INDEX:
          serviceImpl.existsIndex((ch.unibas.dmi.dbis.adam.http.Adam.IndexMessage) request,
              (io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.adam.http.Adam.ExistsMessage>) responseObserver);
          break;
        case METHODID_DROP_INDEX:
          serviceImpl.dropIndex((ch.unibas.dmi.dbis.adam.http.Adam.IndexNameMessage) request,
              (io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.adam.http.Adam.AckMessage>) responseObserver);
          break;
        case METHODID_LIST_INDEXES:
          serviceImpl.listIndexes((ch.unibas.dmi.dbis.adam.http.Adam.EntityNameMessage) request,
              (io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.adam.http.Adam.IndexesMessage>) responseObserver);
          break;
        case METHODID_GENERATE_RANDOM_DATA:
          serviceImpl.generateRandomData((ch.unibas.dmi.dbis.adam.http.Adam.GenerateRandomDataMessage) request,
              (io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.adam.http.Adam.AckMessage>) responseObserver);
          break;
        case METHODID_LIST_ENTITIES:
          serviceImpl.listEntities((ch.unibas.dmi.dbis.adam.http.Adam.EmptyMessage) request,
              (io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.adam.http.Adam.EntitiesMessage>) responseObserver);
          break;
        case METHODID_GET_ENTITY_PROPERTIES:
          serviceImpl.getEntityProperties((ch.unibas.dmi.dbis.adam.http.Adam.EntityNameMessage) request,
              (io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.adam.http.Adam.EntityPropertiesMessage>) responseObserver);
          break;
        case METHODID_REPARTITION_ENTITY_DATA:
          serviceImpl.repartitionEntityData((ch.unibas.dmi.dbis.adam.http.Adam.RepartitionMessage) request,
              (io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.adam.http.Adam.AckMessage>) responseObserver);
          break;
        case METHODID_REPARTITION_INDEX_DATA:
          serviceImpl.repartitionIndexData((ch.unibas.dmi.dbis.adam.http.Adam.RepartitionMessage) request,
              (io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.adam.http.Adam.AckMessage>) responseObserver);
          break;
        case METHODID_ADJUST_SCAN_WEIGHTS:
          serviceImpl.adjustScanWeights((ch.unibas.dmi.dbis.adam.http.Adam.UpdateWeightsMessage) request,
              (io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.adam.http.Adam.AckMessage>) responseObserver);
          break;
        case METHODID_RESET_SCAN_WEIGHTS:
          serviceImpl.resetScanWeights((ch.unibas.dmi.dbis.adam.http.Adam.EntityNameMessage) request,
              (io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.adam.http.Adam.AckMessage>) responseObserver);
          break;
        case METHODID_SET_SCAN_WEIGHT:
          serviceImpl.setScanWeight((ch.unibas.dmi.dbis.adam.http.Adam.WeightMessage) request,
              (io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.adam.http.Adam.AckMessage>) responseObserver);
          break;
        case METHODID_SPARSIFY_ENTITY:
          serviceImpl.sparsifyEntity((ch.unibas.dmi.dbis.adam.http.Adam.SparsifyEntityMessage) request,
              (io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.adam.http.Adam.AckMessage>) responseObserver);
          break;
        case METHODID_IMPORT_DATA:
          serviceImpl.importData((ch.unibas.dmi.dbis.adam.http.Adam.ImportMessage) request,
              (io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.adam.http.Adam.AckMessage>) responseObserver);
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
              (io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.adam.http.Adam.AckMessage>) responseObserver);
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
              ch.unibas.dmi.dbis.adam.http.Adam.CreateEntityMessage,
              ch.unibas.dmi.dbis.adam.http.Adam.AckMessage>(
                serviceImpl, METHODID_CREATE_ENTITY)))
        .addMethod(
          METHOD_COUNT,
          asyncUnaryCall(
            new MethodHandlers<
              ch.unibas.dmi.dbis.adam.http.Adam.EntityNameMessage,
              ch.unibas.dmi.dbis.adam.http.Adam.AckMessage>(
                serviceImpl, METHODID_COUNT)))
        .addMethod(
          METHOD_DROP_ENTITY,
          asyncUnaryCall(
            new MethodHandlers<
              ch.unibas.dmi.dbis.adam.http.Adam.EntityNameMessage,
              ch.unibas.dmi.dbis.adam.http.Adam.AckMessage>(
                serviceImpl, METHODID_DROP_ENTITY)))
        .addMethod(
          METHOD_EXISTS_ENTITY,
          asyncUnaryCall(
            new MethodHandlers<
              ch.unibas.dmi.dbis.adam.http.Adam.EntityNameMessage,
              ch.unibas.dmi.dbis.adam.http.Adam.ExistsMessage>(
                serviceImpl, METHODID_EXISTS_ENTITY)))
        .addMethod(
          METHOD_INSERT,
          asyncClientStreamingCall(
            new MethodHandlers<
              ch.unibas.dmi.dbis.adam.http.Adam.InsertMessage,
              ch.unibas.dmi.dbis.adam.http.Adam.AckMessage>(
                serviceImpl, METHODID_INSERT)))
        .addMethod(
          METHOD_INDEX,
          asyncUnaryCall(
            new MethodHandlers<
              ch.unibas.dmi.dbis.adam.http.Adam.IndexMessage,
              ch.unibas.dmi.dbis.adam.http.Adam.AckMessage>(
                serviceImpl, METHODID_INDEX)))
        .addMethod(
          METHOD_GENERATE_ALL_INDEXES,
          asyncUnaryCall(
            new MethodHandlers<
              ch.unibas.dmi.dbis.adam.http.Adam.IndexMessage,
              ch.unibas.dmi.dbis.adam.http.Adam.AckMessage>(
                serviceImpl, METHODID_GENERATE_ALL_INDEXES)))
        .addMethod(
          METHOD_EXISTS_INDEX,
          asyncUnaryCall(
            new MethodHandlers<
              ch.unibas.dmi.dbis.adam.http.Adam.IndexMessage,
              ch.unibas.dmi.dbis.adam.http.Adam.ExistsMessage>(
                serviceImpl, METHODID_EXISTS_INDEX)))
        .addMethod(
          METHOD_DROP_INDEX,
          asyncUnaryCall(
            new MethodHandlers<
              ch.unibas.dmi.dbis.adam.http.Adam.IndexNameMessage,
              ch.unibas.dmi.dbis.adam.http.Adam.AckMessage>(
                serviceImpl, METHODID_DROP_INDEX)))
        .addMethod(
          METHOD_LIST_INDEXES,
          asyncUnaryCall(
            new MethodHandlers<
              ch.unibas.dmi.dbis.adam.http.Adam.EntityNameMessage,
              ch.unibas.dmi.dbis.adam.http.Adam.IndexesMessage>(
                serviceImpl, METHODID_LIST_INDEXES)))
        .addMethod(
          METHOD_GENERATE_RANDOM_DATA,
          asyncUnaryCall(
            new MethodHandlers<
              ch.unibas.dmi.dbis.adam.http.Adam.GenerateRandomDataMessage,
              ch.unibas.dmi.dbis.adam.http.Adam.AckMessage>(
                serviceImpl, METHODID_GENERATE_RANDOM_DATA)))
        .addMethod(
          METHOD_LIST_ENTITIES,
          asyncUnaryCall(
            new MethodHandlers<
              ch.unibas.dmi.dbis.adam.http.Adam.EmptyMessage,
              ch.unibas.dmi.dbis.adam.http.Adam.EntitiesMessage>(
                serviceImpl, METHODID_LIST_ENTITIES)))
        .addMethod(
          METHOD_GET_ENTITY_PROPERTIES,
          asyncUnaryCall(
            new MethodHandlers<
              ch.unibas.dmi.dbis.adam.http.Adam.EntityNameMessage,
              ch.unibas.dmi.dbis.adam.http.Adam.EntityPropertiesMessage>(
                serviceImpl, METHODID_GET_ENTITY_PROPERTIES)))
        .addMethod(
          METHOD_REPARTITION_ENTITY_DATA,
          asyncUnaryCall(
            new MethodHandlers<
              ch.unibas.dmi.dbis.adam.http.Adam.RepartitionMessage,
              ch.unibas.dmi.dbis.adam.http.Adam.AckMessage>(
                serviceImpl, METHODID_REPARTITION_ENTITY_DATA)))
        .addMethod(
          METHOD_REPARTITION_INDEX_DATA,
          asyncUnaryCall(
            new MethodHandlers<
              ch.unibas.dmi.dbis.adam.http.Adam.RepartitionMessage,
              ch.unibas.dmi.dbis.adam.http.Adam.AckMessage>(
                serviceImpl, METHODID_REPARTITION_INDEX_DATA)))
        .addMethod(
          METHOD_ADJUST_SCAN_WEIGHTS,
          asyncUnaryCall(
            new MethodHandlers<
              ch.unibas.dmi.dbis.adam.http.Adam.UpdateWeightsMessage,
              ch.unibas.dmi.dbis.adam.http.Adam.AckMessage>(
                serviceImpl, METHODID_ADJUST_SCAN_WEIGHTS)))
        .addMethod(
          METHOD_RESET_SCAN_WEIGHTS,
          asyncUnaryCall(
            new MethodHandlers<
              ch.unibas.dmi.dbis.adam.http.Adam.EntityNameMessage,
              ch.unibas.dmi.dbis.adam.http.Adam.AckMessage>(
                serviceImpl, METHODID_RESET_SCAN_WEIGHTS)))
        .addMethod(
          METHOD_SET_SCAN_WEIGHT,
          asyncUnaryCall(
            new MethodHandlers<
              ch.unibas.dmi.dbis.adam.http.Adam.WeightMessage,
              ch.unibas.dmi.dbis.adam.http.Adam.AckMessage>(
                serviceImpl, METHODID_SET_SCAN_WEIGHT)))
        .addMethod(
          METHOD_SPARSIFY_ENTITY,
          asyncUnaryCall(
            new MethodHandlers<
              ch.unibas.dmi.dbis.adam.http.Adam.SparsifyEntityMessage,
              ch.unibas.dmi.dbis.adam.http.Adam.AckMessage>(
                serviceImpl, METHODID_SPARSIFY_ENTITY)))
        .addMethod(
          METHOD_IMPORT_DATA,
          asyncUnaryCall(
            new MethodHandlers<
              ch.unibas.dmi.dbis.adam.http.Adam.ImportMessage,
              ch.unibas.dmi.dbis.adam.http.Adam.AckMessage>(
                serviceImpl, METHODID_IMPORT_DATA)))
        .build();
  }
}
