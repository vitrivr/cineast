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
  public static final io.grpc.MethodDescriptor<ch.unibas.dmi.dbis.adam.http.Grpc.CreateEntityMessage,
      ch.unibas.dmi.dbis.adam.http.Grpc.AckMessage> METHOD_CREATE_ENTITY =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "AdamDefinition", "CreateEntity"),
          io.grpc.protobuf.ProtoUtils.marshaller(ch.unibas.dmi.dbis.adam.http.Grpc.CreateEntityMessage.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(ch.unibas.dmi.dbis.adam.http.Grpc.AckMessage.getDefaultInstance()));
  @io.grpc.ExperimentalApi
  public static final io.grpc.MethodDescriptor<ch.unibas.dmi.dbis.adam.http.Grpc.EntityNameMessage,
      ch.unibas.dmi.dbis.adam.http.Grpc.AckMessage> METHOD_DROP_ENTITY =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "AdamDefinition", "DropEntity"),
          io.grpc.protobuf.ProtoUtils.marshaller(ch.unibas.dmi.dbis.adam.http.Grpc.EntityNameMessage.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(ch.unibas.dmi.dbis.adam.http.Grpc.AckMessage.getDefaultInstance()));
  @io.grpc.ExperimentalApi
  public static final io.grpc.MethodDescriptor<ch.unibas.dmi.dbis.adam.http.Grpc.InsertMessage,
      ch.unibas.dmi.dbis.adam.http.Grpc.AckMessage> METHOD_INSERT =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.CLIENT_STREAMING,
          generateFullMethodName(
              "AdamDefinition", "Insert"),
          io.grpc.protobuf.ProtoUtils.marshaller(ch.unibas.dmi.dbis.adam.http.Grpc.InsertMessage.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(ch.unibas.dmi.dbis.adam.http.Grpc.AckMessage.getDefaultInstance()));
  @io.grpc.ExperimentalApi
  public static final io.grpc.MethodDescriptor<ch.unibas.dmi.dbis.adam.http.Grpc.IndexMessage,
      ch.unibas.dmi.dbis.adam.http.Grpc.AckMessage> METHOD_INDEX =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "AdamDefinition", "Index"),
          io.grpc.protobuf.ProtoUtils.marshaller(ch.unibas.dmi.dbis.adam.http.Grpc.IndexMessage.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(ch.unibas.dmi.dbis.adam.http.Grpc.AckMessage.getDefaultInstance()));
  @io.grpc.ExperimentalApi
  public static final io.grpc.MethodDescriptor<ch.unibas.dmi.dbis.adam.http.Grpc.IndexMessage,
      ch.unibas.dmi.dbis.adam.http.Grpc.AckMessage> METHOD_GENERATE_ALL_INDEXES =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "AdamDefinition", "GenerateAllIndexes"),
          io.grpc.protobuf.ProtoUtils.marshaller(ch.unibas.dmi.dbis.adam.http.Grpc.IndexMessage.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(ch.unibas.dmi.dbis.adam.http.Grpc.AckMessage.getDefaultInstance()));
  @io.grpc.ExperimentalApi
  public static final io.grpc.MethodDescriptor<ch.unibas.dmi.dbis.adam.http.Grpc.IndexNameMessage,
      ch.unibas.dmi.dbis.adam.http.Grpc.AckMessage> METHOD_DROP_INDEX =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "AdamDefinition", "DropIndex"),
          io.grpc.protobuf.ProtoUtils.marshaller(ch.unibas.dmi.dbis.adam.http.Grpc.IndexNameMessage.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(ch.unibas.dmi.dbis.adam.http.Grpc.AckMessage.getDefaultInstance()));
  @io.grpc.ExperimentalApi
  public static final io.grpc.MethodDescriptor<ch.unibas.dmi.dbis.adam.http.Grpc.EntityNameMessage,
      ch.unibas.dmi.dbis.adam.http.Grpc.AckMessage> METHOD_COUNT =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "AdamDefinition", "Count"),
          io.grpc.protobuf.ProtoUtils.marshaller(ch.unibas.dmi.dbis.adam.http.Grpc.EntityNameMessage.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(ch.unibas.dmi.dbis.adam.http.Grpc.AckMessage.getDefaultInstance()));
  @io.grpc.ExperimentalApi
  public static final io.grpc.MethodDescriptor<ch.unibas.dmi.dbis.adam.http.Grpc.GenerateRandomDataMessage,
      ch.unibas.dmi.dbis.adam.http.Grpc.AckMessage> METHOD_GENERATE_RANDOM_DATA =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "AdamDefinition", "GenerateRandomData"),
          io.grpc.protobuf.ProtoUtils.marshaller(ch.unibas.dmi.dbis.adam.http.Grpc.GenerateRandomDataMessage.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(ch.unibas.dmi.dbis.adam.http.Grpc.AckMessage.getDefaultInstance()));
  @io.grpc.ExperimentalApi
  public static final io.grpc.MethodDescriptor<ch.unibas.dmi.dbis.adam.http.Grpc.Empty,
      ch.unibas.dmi.dbis.adam.http.Grpc.EntitiesMessage> METHOD_LIST_ENTITIES =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "AdamDefinition", "ListEntities"),
          io.grpc.protobuf.ProtoUtils.marshaller(ch.unibas.dmi.dbis.adam.http.Grpc.Empty.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(ch.unibas.dmi.dbis.adam.http.Grpc.EntitiesMessage.getDefaultInstance()));
  @io.grpc.ExperimentalApi
  public static final io.grpc.MethodDescriptor<ch.unibas.dmi.dbis.adam.http.Grpc.EntityNameMessage,
      ch.unibas.dmi.dbis.adam.http.Grpc.EntityPropertiesMessage> METHOD_GET_ENTITY_PROPERTIES =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "AdamDefinition", "GetEntityProperties"),
          io.grpc.protobuf.ProtoUtils.marshaller(ch.unibas.dmi.dbis.adam.http.Grpc.EntityNameMessage.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(ch.unibas.dmi.dbis.adam.http.Grpc.EntityPropertiesMessage.getDefaultInstance()));
  @io.grpc.ExperimentalApi
  public static final io.grpc.MethodDescriptor<ch.unibas.dmi.dbis.adam.http.Grpc.RepartitionMessage,
      ch.unibas.dmi.dbis.adam.http.Grpc.AckMessage> METHOD_REPARTITION_INDEX_DATA =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "AdamDefinition", "RepartitionIndexData"),
          io.grpc.protobuf.ProtoUtils.marshaller(ch.unibas.dmi.dbis.adam.http.Grpc.RepartitionMessage.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(ch.unibas.dmi.dbis.adam.http.Grpc.AckMessage.getDefaultInstance()));
  @io.grpc.ExperimentalApi
  public static final io.grpc.MethodDescriptor<ch.unibas.dmi.dbis.adam.http.Grpc.IndexWeightMessage,
      ch.unibas.dmi.dbis.adam.http.Grpc.AckMessage> METHOD_SET_INDEX_WEIGHT =
      io.grpc.MethodDescriptor.create(
          io.grpc.MethodDescriptor.MethodType.UNARY,
          generateFullMethodName(
              "AdamDefinition", "SetIndexWeight"),
          io.grpc.protobuf.ProtoUtils.marshaller(ch.unibas.dmi.dbis.adam.http.Grpc.IndexWeightMessage.getDefaultInstance()),
          io.grpc.protobuf.ProtoUtils.marshaller(ch.unibas.dmi.dbis.adam.http.Grpc.AckMessage.getDefaultInstance()));

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

    public void createEntity(ch.unibas.dmi.dbis.adam.http.Grpc.CreateEntityMessage request,
        io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.adam.http.Grpc.AckMessage> responseObserver);

    public void dropEntity(ch.unibas.dmi.dbis.adam.http.Grpc.EntityNameMessage request,
        io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.adam.http.Grpc.AckMessage> responseObserver);

    public io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.adam.http.Grpc.InsertMessage> insert(
        io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.adam.http.Grpc.AckMessage> responseObserver);

    public void index(ch.unibas.dmi.dbis.adam.http.Grpc.IndexMessage request,
        io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.adam.http.Grpc.AckMessage> responseObserver);

    public void generateAllIndexes(ch.unibas.dmi.dbis.adam.http.Grpc.IndexMessage request,
        io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.adam.http.Grpc.AckMessage> responseObserver);

    public void dropIndex(ch.unibas.dmi.dbis.adam.http.Grpc.IndexNameMessage request,
        io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.adam.http.Grpc.AckMessage> responseObserver);

    public void count(ch.unibas.dmi.dbis.adam.http.Grpc.EntityNameMessage request,
        io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.adam.http.Grpc.AckMessage> responseObserver);

    public void generateRandomData(ch.unibas.dmi.dbis.adam.http.Grpc.GenerateRandomDataMessage request,
        io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.adam.http.Grpc.AckMessage> responseObserver);

    public void listEntities(ch.unibas.dmi.dbis.adam.http.Grpc.Empty request,
        io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.adam.http.Grpc.EntitiesMessage> responseObserver);

    public void getEntityProperties(ch.unibas.dmi.dbis.adam.http.Grpc.EntityNameMessage request,
        io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.adam.http.Grpc.EntityPropertiesMessage> responseObserver);

    public void repartitionIndexData(ch.unibas.dmi.dbis.adam.http.Grpc.RepartitionMessage request,
        io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.adam.http.Grpc.AckMessage> responseObserver);

    public void setIndexWeight(ch.unibas.dmi.dbis.adam.http.Grpc.IndexWeightMessage request,
        io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.adam.http.Grpc.AckMessage> responseObserver);
  }

  public static interface AdamDefinitionBlockingClient {

    public ch.unibas.dmi.dbis.adam.http.Grpc.AckMessage createEntity(ch.unibas.dmi.dbis.adam.http.Grpc.CreateEntityMessage request);

    public ch.unibas.dmi.dbis.adam.http.Grpc.AckMessage dropEntity(ch.unibas.dmi.dbis.adam.http.Grpc.EntityNameMessage request);

    public ch.unibas.dmi.dbis.adam.http.Grpc.AckMessage index(ch.unibas.dmi.dbis.adam.http.Grpc.IndexMessage request);

    public ch.unibas.dmi.dbis.adam.http.Grpc.AckMessage generateAllIndexes(ch.unibas.dmi.dbis.adam.http.Grpc.IndexMessage request);

    public ch.unibas.dmi.dbis.adam.http.Grpc.AckMessage dropIndex(ch.unibas.dmi.dbis.adam.http.Grpc.IndexNameMessage request);

    public ch.unibas.dmi.dbis.adam.http.Grpc.AckMessage count(ch.unibas.dmi.dbis.adam.http.Grpc.EntityNameMessage request);

    public ch.unibas.dmi.dbis.adam.http.Grpc.AckMessage generateRandomData(ch.unibas.dmi.dbis.adam.http.Grpc.GenerateRandomDataMessage request);

    public ch.unibas.dmi.dbis.adam.http.Grpc.EntitiesMessage listEntities(ch.unibas.dmi.dbis.adam.http.Grpc.Empty request);

    public ch.unibas.dmi.dbis.adam.http.Grpc.EntityPropertiesMessage getEntityProperties(ch.unibas.dmi.dbis.adam.http.Grpc.EntityNameMessage request);

    public ch.unibas.dmi.dbis.adam.http.Grpc.AckMessage repartitionIndexData(ch.unibas.dmi.dbis.adam.http.Grpc.RepartitionMessage request);

    public ch.unibas.dmi.dbis.adam.http.Grpc.AckMessage setIndexWeight(ch.unibas.dmi.dbis.adam.http.Grpc.IndexWeightMessage request);
  }

  public static interface AdamDefinitionFutureClient {

    public com.google.common.util.concurrent.ListenableFuture<ch.unibas.dmi.dbis.adam.http.Grpc.AckMessage> createEntity(
        ch.unibas.dmi.dbis.adam.http.Grpc.CreateEntityMessage request);

    public com.google.common.util.concurrent.ListenableFuture<ch.unibas.dmi.dbis.adam.http.Grpc.AckMessage> dropEntity(
        ch.unibas.dmi.dbis.adam.http.Grpc.EntityNameMessage request);

    public com.google.common.util.concurrent.ListenableFuture<ch.unibas.dmi.dbis.adam.http.Grpc.AckMessage> index(
        ch.unibas.dmi.dbis.adam.http.Grpc.IndexMessage request);

    public com.google.common.util.concurrent.ListenableFuture<ch.unibas.dmi.dbis.adam.http.Grpc.AckMessage> generateAllIndexes(
        ch.unibas.dmi.dbis.adam.http.Grpc.IndexMessage request);

    public com.google.common.util.concurrent.ListenableFuture<ch.unibas.dmi.dbis.adam.http.Grpc.AckMessage> dropIndex(
        ch.unibas.dmi.dbis.adam.http.Grpc.IndexNameMessage request);

    public com.google.common.util.concurrent.ListenableFuture<ch.unibas.dmi.dbis.adam.http.Grpc.AckMessage> count(
        ch.unibas.dmi.dbis.adam.http.Grpc.EntityNameMessage request);

    public com.google.common.util.concurrent.ListenableFuture<ch.unibas.dmi.dbis.adam.http.Grpc.AckMessage> generateRandomData(
        ch.unibas.dmi.dbis.adam.http.Grpc.GenerateRandomDataMessage request);

    public com.google.common.util.concurrent.ListenableFuture<ch.unibas.dmi.dbis.adam.http.Grpc.EntitiesMessage> listEntities(
        ch.unibas.dmi.dbis.adam.http.Grpc.Empty request);

    public com.google.common.util.concurrent.ListenableFuture<ch.unibas.dmi.dbis.adam.http.Grpc.EntityPropertiesMessage> getEntityProperties(
        ch.unibas.dmi.dbis.adam.http.Grpc.EntityNameMessage request);

    public com.google.common.util.concurrent.ListenableFuture<ch.unibas.dmi.dbis.adam.http.Grpc.AckMessage> repartitionIndexData(
        ch.unibas.dmi.dbis.adam.http.Grpc.RepartitionMessage request);

    public com.google.common.util.concurrent.ListenableFuture<ch.unibas.dmi.dbis.adam.http.Grpc.AckMessage> setIndexWeight(
        ch.unibas.dmi.dbis.adam.http.Grpc.IndexWeightMessage request);
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
    public void createEntity(ch.unibas.dmi.dbis.adam.http.Grpc.CreateEntityMessage request,
        io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.adam.http.Grpc.AckMessage> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_CREATE_ENTITY, getCallOptions()), request, responseObserver);
    }

    @java.lang.Override
    public void dropEntity(ch.unibas.dmi.dbis.adam.http.Grpc.EntityNameMessage request,
        io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.adam.http.Grpc.AckMessage> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_DROP_ENTITY, getCallOptions()), request, responseObserver);
    }

    @java.lang.Override
    public io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.adam.http.Grpc.InsertMessage> insert(
        io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.adam.http.Grpc.AckMessage> responseObserver) {
      return asyncClientStreamingCall(
          getChannel().newCall(METHOD_INSERT, getCallOptions()), responseObserver);
    }

    @java.lang.Override
    public void index(ch.unibas.dmi.dbis.adam.http.Grpc.IndexMessage request,
        io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.adam.http.Grpc.AckMessage> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_INDEX, getCallOptions()), request, responseObserver);
    }

    @java.lang.Override
    public void generateAllIndexes(ch.unibas.dmi.dbis.adam.http.Grpc.IndexMessage request,
        io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.adam.http.Grpc.AckMessage> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_GENERATE_ALL_INDEXES, getCallOptions()), request, responseObserver);
    }

    @java.lang.Override
    public void dropIndex(ch.unibas.dmi.dbis.adam.http.Grpc.IndexNameMessage request,
        io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.adam.http.Grpc.AckMessage> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_DROP_INDEX, getCallOptions()), request, responseObserver);
    }

    @java.lang.Override
    public void count(ch.unibas.dmi.dbis.adam.http.Grpc.EntityNameMessage request,
        io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.adam.http.Grpc.AckMessage> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_COUNT, getCallOptions()), request, responseObserver);
    }

    @java.lang.Override
    public void generateRandomData(ch.unibas.dmi.dbis.adam.http.Grpc.GenerateRandomDataMessage request,
        io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.adam.http.Grpc.AckMessage> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_GENERATE_RANDOM_DATA, getCallOptions()), request, responseObserver);
    }

    @java.lang.Override
    public void listEntities(ch.unibas.dmi.dbis.adam.http.Grpc.Empty request,
        io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.adam.http.Grpc.EntitiesMessage> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_LIST_ENTITIES, getCallOptions()), request, responseObserver);
    }

    @java.lang.Override
    public void getEntityProperties(ch.unibas.dmi.dbis.adam.http.Grpc.EntityNameMessage request,
        io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.adam.http.Grpc.EntityPropertiesMessage> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_GET_ENTITY_PROPERTIES, getCallOptions()), request, responseObserver);
    }

    @java.lang.Override
    public void repartitionIndexData(ch.unibas.dmi.dbis.adam.http.Grpc.RepartitionMessage request,
        io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.adam.http.Grpc.AckMessage> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_REPARTITION_INDEX_DATA, getCallOptions()), request, responseObserver);
    }

    @java.lang.Override
    public void setIndexWeight(ch.unibas.dmi.dbis.adam.http.Grpc.IndexWeightMessage request,
        io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.adam.http.Grpc.AckMessage> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(METHOD_SET_INDEX_WEIGHT, getCallOptions()), request, responseObserver);
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
    public ch.unibas.dmi.dbis.adam.http.Grpc.AckMessage createEntity(ch.unibas.dmi.dbis.adam.http.Grpc.CreateEntityMessage request) {
      return blockingUnaryCall(
          getChannel(), METHOD_CREATE_ENTITY, getCallOptions(), request);
    }

    @java.lang.Override
    public ch.unibas.dmi.dbis.adam.http.Grpc.AckMessage dropEntity(ch.unibas.dmi.dbis.adam.http.Grpc.EntityNameMessage request) {
      return blockingUnaryCall(
          getChannel(), METHOD_DROP_ENTITY, getCallOptions(), request);
    }

    @java.lang.Override
    public ch.unibas.dmi.dbis.adam.http.Grpc.AckMessage index(ch.unibas.dmi.dbis.adam.http.Grpc.IndexMessage request) {
      return blockingUnaryCall(
          getChannel(), METHOD_INDEX, getCallOptions(), request);
    }

    @java.lang.Override
    public ch.unibas.dmi.dbis.adam.http.Grpc.AckMessage generateAllIndexes(ch.unibas.dmi.dbis.adam.http.Grpc.IndexMessage request) {
      return blockingUnaryCall(
          getChannel(), METHOD_GENERATE_ALL_INDEXES, getCallOptions(), request);
    }

    @java.lang.Override
    public ch.unibas.dmi.dbis.adam.http.Grpc.AckMessage dropIndex(ch.unibas.dmi.dbis.adam.http.Grpc.IndexNameMessage request) {
      return blockingUnaryCall(
          getChannel(), METHOD_DROP_INDEX, getCallOptions(), request);
    }

    @java.lang.Override
    public ch.unibas.dmi.dbis.adam.http.Grpc.AckMessage count(ch.unibas.dmi.dbis.adam.http.Grpc.EntityNameMessage request) {
      return blockingUnaryCall(
          getChannel(), METHOD_COUNT, getCallOptions(), request);
    }

    @java.lang.Override
    public ch.unibas.dmi.dbis.adam.http.Grpc.AckMessage generateRandomData(ch.unibas.dmi.dbis.adam.http.Grpc.GenerateRandomDataMessage request) {
      return blockingUnaryCall(
          getChannel(), METHOD_GENERATE_RANDOM_DATA, getCallOptions(), request);
    }

    @java.lang.Override
    public ch.unibas.dmi.dbis.adam.http.Grpc.EntitiesMessage listEntities(ch.unibas.dmi.dbis.adam.http.Grpc.Empty request) {
      return blockingUnaryCall(
          getChannel(), METHOD_LIST_ENTITIES, getCallOptions(), request);
    }

    @java.lang.Override
    public ch.unibas.dmi.dbis.adam.http.Grpc.EntityPropertiesMessage getEntityProperties(ch.unibas.dmi.dbis.adam.http.Grpc.EntityNameMessage request) {
      return blockingUnaryCall(
          getChannel(), METHOD_GET_ENTITY_PROPERTIES, getCallOptions(), request);
    }

    @java.lang.Override
    public ch.unibas.dmi.dbis.adam.http.Grpc.AckMessage repartitionIndexData(ch.unibas.dmi.dbis.adam.http.Grpc.RepartitionMessage request) {
      return blockingUnaryCall(
          getChannel(), METHOD_REPARTITION_INDEX_DATA, getCallOptions(), request);
    }

    @java.lang.Override
    public ch.unibas.dmi.dbis.adam.http.Grpc.AckMessage setIndexWeight(ch.unibas.dmi.dbis.adam.http.Grpc.IndexWeightMessage request) {
      return blockingUnaryCall(
          getChannel(), METHOD_SET_INDEX_WEIGHT, getCallOptions(), request);
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
    public com.google.common.util.concurrent.ListenableFuture<ch.unibas.dmi.dbis.adam.http.Grpc.AckMessage> createEntity(
        ch.unibas.dmi.dbis.adam.http.Grpc.CreateEntityMessage request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_CREATE_ENTITY, getCallOptions()), request);
    }

    @java.lang.Override
    public com.google.common.util.concurrent.ListenableFuture<ch.unibas.dmi.dbis.adam.http.Grpc.AckMessage> dropEntity(
        ch.unibas.dmi.dbis.adam.http.Grpc.EntityNameMessage request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_DROP_ENTITY, getCallOptions()), request);
    }

    @java.lang.Override
    public com.google.common.util.concurrent.ListenableFuture<ch.unibas.dmi.dbis.adam.http.Grpc.AckMessage> index(
        ch.unibas.dmi.dbis.adam.http.Grpc.IndexMessage request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_INDEX, getCallOptions()), request);
    }

    @java.lang.Override
    public com.google.common.util.concurrent.ListenableFuture<ch.unibas.dmi.dbis.adam.http.Grpc.AckMessage> generateAllIndexes(
        ch.unibas.dmi.dbis.adam.http.Grpc.IndexMessage request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_GENERATE_ALL_INDEXES, getCallOptions()), request);
    }

    @java.lang.Override
    public com.google.common.util.concurrent.ListenableFuture<ch.unibas.dmi.dbis.adam.http.Grpc.AckMessage> dropIndex(
        ch.unibas.dmi.dbis.adam.http.Grpc.IndexNameMessage request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_DROP_INDEX, getCallOptions()), request);
    }

    @java.lang.Override
    public com.google.common.util.concurrent.ListenableFuture<ch.unibas.dmi.dbis.adam.http.Grpc.AckMessage> count(
        ch.unibas.dmi.dbis.adam.http.Grpc.EntityNameMessage request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_COUNT, getCallOptions()), request);
    }

    @java.lang.Override
    public com.google.common.util.concurrent.ListenableFuture<ch.unibas.dmi.dbis.adam.http.Grpc.AckMessage> generateRandomData(
        ch.unibas.dmi.dbis.adam.http.Grpc.GenerateRandomDataMessage request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_GENERATE_RANDOM_DATA, getCallOptions()), request);
    }

    @java.lang.Override
    public com.google.common.util.concurrent.ListenableFuture<ch.unibas.dmi.dbis.adam.http.Grpc.EntitiesMessage> listEntities(
        ch.unibas.dmi.dbis.adam.http.Grpc.Empty request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_LIST_ENTITIES, getCallOptions()), request);
    }

    @java.lang.Override
    public com.google.common.util.concurrent.ListenableFuture<ch.unibas.dmi.dbis.adam.http.Grpc.EntityPropertiesMessage> getEntityProperties(
        ch.unibas.dmi.dbis.adam.http.Grpc.EntityNameMessage request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_GET_ENTITY_PROPERTIES, getCallOptions()), request);
    }

    @java.lang.Override
    public com.google.common.util.concurrent.ListenableFuture<ch.unibas.dmi.dbis.adam.http.Grpc.AckMessage> repartitionIndexData(
        ch.unibas.dmi.dbis.adam.http.Grpc.RepartitionMessage request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_REPARTITION_INDEX_DATA, getCallOptions()), request);
    }

    @java.lang.Override
    public com.google.common.util.concurrent.ListenableFuture<ch.unibas.dmi.dbis.adam.http.Grpc.AckMessage> setIndexWeight(
        ch.unibas.dmi.dbis.adam.http.Grpc.IndexWeightMessage request) {
      return futureUnaryCall(
          getChannel().newCall(METHOD_SET_INDEX_WEIGHT, getCallOptions()), request);
    }
  }

  private static final int METHODID_CREATE_ENTITY = 0;
  private static final int METHODID_DROP_ENTITY = 1;
  private static final int METHODID_INDEX = 2;
  private static final int METHODID_GENERATE_ALL_INDEXES = 3;
  private static final int METHODID_DROP_INDEX = 4;
  private static final int METHODID_COUNT = 5;
  private static final int METHODID_GENERATE_RANDOM_DATA = 6;
  private static final int METHODID_LIST_ENTITIES = 7;
  private static final int METHODID_GET_ENTITY_PROPERTIES = 8;
  private static final int METHODID_REPARTITION_INDEX_DATA = 9;
  private static final int METHODID_SET_INDEX_WEIGHT = 10;
  private static final int METHODID_INSERT = 11;

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
          serviceImpl.createEntity((ch.unibas.dmi.dbis.adam.http.Grpc.CreateEntityMessage) request,
              (io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.adam.http.Grpc.AckMessage>) responseObserver);
          break;
        case METHODID_DROP_ENTITY:
          serviceImpl.dropEntity((ch.unibas.dmi.dbis.adam.http.Grpc.EntityNameMessage) request,
              (io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.adam.http.Grpc.AckMessage>) responseObserver);
          break;
        case METHODID_INDEX:
          serviceImpl.index((ch.unibas.dmi.dbis.adam.http.Grpc.IndexMessage) request,
              (io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.adam.http.Grpc.AckMessage>) responseObserver);
          break;
        case METHODID_GENERATE_ALL_INDEXES:
          serviceImpl.generateAllIndexes((ch.unibas.dmi.dbis.adam.http.Grpc.IndexMessage) request,
              (io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.adam.http.Grpc.AckMessage>) responseObserver);
          break;
        case METHODID_DROP_INDEX:
          serviceImpl.dropIndex((ch.unibas.dmi.dbis.adam.http.Grpc.IndexNameMessage) request,
              (io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.adam.http.Grpc.AckMessage>) responseObserver);
          break;
        case METHODID_COUNT:
          serviceImpl.count((ch.unibas.dmi.dbis.adam.http.Grpc.EntityNameMessage) request,
              (io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.adam.http.Grpc.AckMessage>) responseObserver);
          break;
        case METHODID_GENERATE_RANDOM_DATA:
          serviceImpl.generateRandomData((ch.unibas.dmi.dbis.adam.http.Grpc.GenerateRandomDataMessage) request,
              (io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.adam.http.Grpc.AckMessage>) responseObserver);
          break;
        case METHODID_LIST_ENTITIES:
          serviceImpl.listEntities((ch.unibas.dmi.dbis.adam.http.Grpc.Empty) request,
              (io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.adam.http.Grpc.EntitiesMessage>) responseObserver);
          break;
        case METHODID_GET_ENTITY_PROPERTIES:
          serviceImpl.getEntityProperties((ch.unibas.dmi.dbis.adam.http.Grpc.EntityNameMessage) request,
              (io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.adam.http.Grpc.EntityPropertiesMessage>) responseObserver);
          break;
        case METHODID_REPARTITION_INDEX_DATA:
          serviceImpl.repartitionIndexData((ch.unibas.dmi.dbis.adam.http.Grpc.RepartitionMessage) request,
              (io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.adam.http.Grpc.AckMessage>) responseObserver);
          break;
        case METHODID_SET_INDEX_WEIGHT:
          serviceImpl.setIndexWeight((ch.unibas.dmi.dbis.adam.http.Grpc.IndexWeightMessage) request,
              (io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.adam.http.Grpc.AckMessage>) responseObserver);
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
              (io.grpc.stub.StreamObserver<ch.unibas.dmi.dbis.adam.http.Grpc.AckMessage>) responseObserver);
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
              ch.unibas.dmi.dbis.adam.http.Grpc.CreateEntityMessage,
              ch.unibas.dmi.dbis.adam.http.Grpc.AckMessage>(
                serviceImpl, METHODID_CREATE_ENTITY)))
        .addMethod(
          METHOD_DROP_ENTITY,
          asyncUnaryCall(
            new MethodHandlers<
              ch.unibas.dmi.dbis.adam.http.Grpc.EntityNameMessage,
              ch.unibas.dmi.dbis.adam.http.Grpc.AckMessage>(
                serviceImpl, METHODID_DROP_ENTITY)))
        .addMethod(
          METHOD_INSERT,
          asyncClientStreamingCall(
            new MethodHandlers<
              ch.unibas.dmi.dbis.adam.http.Grpc.InsertMessage,
              ch.unibas.dmi.dbis.adam.http.Grpc.AckMessage>(
                serviceImpl, METHODID_INSERT)))
        .addMethod(
          METHOD_INDEX,
          asyncUnaryCall(
            new MethodHandlers<
              ch.unibas.dmi.dbis.adam.http.Grpc.IndexMessage,
              ch.unibas.dmi.dbis.adam.http.Grpc.AckMessage>(
                serviceImpl, METHODID_INDEX)))
        .addMethod(
          METHOD_GENERATE_ALL_INDEXES,
          asyncUnaryCall(
            new MethodHandlers<
              ch.unibas.dmi.dbis.adam.http.Grpc.IndexMessage,
              ch.unibas.dmi.dbis.adam.http.Grpc.AckMessage>(
                serviceImpl, METHODID_GENERATE_ALL_INDEXES)))
        .addMethod(
          METHOD_DROP_INDEX,
          asyncUnaryCall(
            new MethodHandlers<
              ch.unibas.dmi.dbis.adam.http.Grpc.IndexNameMessage,
              ch.unibas.dmi.dbis.adam.http.Grpc.AckMessage>(
                serviceImpl, METHODID_DROP_INDEX)))
        .addMethod(
          METHOD_COUNT,
          asyncUnaryCall(
            new MethodHandlers<
              ch.unibas.dmi.dbis.adam.http.Grpc.EntityNameMessage,
              ch.unibas.dmi.dbis.adam.http.Grpc.AckMessage>(
                serviceImpl, METHODID_COUNT)))
        .addMethod(
          METHOD_GENERATE_RANDOM_DATA,
          asyncUnaryCall(
            new MethodHandlers<
              ch.unibas.dmi.dbis.adam.http.Grpc.GenerateRandomDataMessage,
              ch.unibas.dmi.dbis.adam.http.Grpc.AckMessage>(
                serviceImpl, METHODID_GENERATE_RANDOM_DATA)))
        .addMethod(
          METHOD_LIST_ENTITIES,
          asyncUnaryCall(
            new MethodHandlers<
              ch.unibas.dmi.dbis.adam.http.Grpc.Empty,
              ch.unibas.dmi.dbis.adam.http.Grpc.EntitiesMessage>(
                serviceImpl, METHODID_LIST_ENTITIES)))
        .addMethod(
          METHOD_GET_ENTITY_PROPERTIES,
          asyncUnaryCall(
            new MethodHandlers<
              ch.unibas.dmi.dbis.adam.http.Grpc.EntityNameMessage,
              ch.unibas.dmi.dbis.adam.http.Grpc.EntityPropertiesMessage>(
                serviceImpl, METHODID_GET_ENTITY_PROPERTIES)))
        .addMethod(
          METHOD_REPARTITION_INDEX_DATA,
          asyncUnaryCall(
            new MethodHandlers<
              ch.unibas.dmi.dbis.adam.http.Grpc.RepartitionMessage,
              ch.unibas.dmi.dbis.adam.http.Grpc.AckMessage>(
                serviceImpl, METHODID_REPARTITION_INDEX_DATA)))
        .addMethod(
          METHOD_SET_INDEX_WEIGHT,
          asyncUnaryCall(
            new MethodHandlers<
              ch.unibas.dmi.dbis.adam.http.Grpc.IndexWeightMessage,
              ch.unibas.dmi.dbis.adam.http.Grpc.AckMessage>(
                serviceImpl, METHODID_SET_INDEX_WEIGHT)))
        .build();
  }
}
