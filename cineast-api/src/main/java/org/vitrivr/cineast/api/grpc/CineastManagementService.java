package org.vitrivr.cineast.api.grpc;

import io.grpc.stub.StreamObserver;

public class CineastManagementService extends CineastManageGrpc.CineastManageImplBase {

    @Override
    public void ping(CineastGrpc.TimeStamp timeStamp, StreamObserver<CineastGrpc.TimeStamp> responseObserver) {
        responseObserver.onNext(CineastGrpc.TimeStamp.newBuilder().setTimestamp(System.currentTimeMillis()).build());
        responseObserver.onCompleted();
    }

}
