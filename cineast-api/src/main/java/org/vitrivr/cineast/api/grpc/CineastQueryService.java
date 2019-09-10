package org.vitrivr.cineast.api.grpc;

import io.grpc.stub.StreamObserver;

public class CineastQueryService extends CineastQueryGrpc.CineastQueryImplBase {

    @Override
    public void getMediaObjects(CineastGrpc.MediaObjectIdList request, StreamObserver<CineastGrpc.MediaObjectQueryResult> responseObserver) {
        //TODO
        responseObserver.onCompleted();
    }

    @Override
    public void getMediaSegments(CineastGrpc.MediaSegmentIdList request, StreamObserver<CineastGrpc.MediaSegmentQueryResult> responseObserver) {
        //TODO
        responseObserver.onCompleted();
    }

    @Override
    public void getMediaSegmentScores(CineastGrpc.Query request, StreamObserver<CineastGrpc.SimilarityQueryResult> responseObserver) {
        //TODO
        responseObserver.onCompleted();
    }

    @Override
    public void getSimilar(CineastGrpc.Query request, StreamObserver<CineastGrpc.QueryResult> responseObserver) {
        //TODO
        responseObserver.onCompleted();
    }
}
