package org.vitrivr.cineast.api.grpc;

import io.grpc.stub.StreamObserver;
import org.vitrivr.cineast.api.grpc.util.MediaObjectUtil;
import org.vitrivr.cineast.api.grpc.util.MediaSegmentUtil;
import org.vitrivr.cineast.core.data.entities.MediaObjectDescriptor;
import org.vitrivr.cineast.core.data.entities.MediaSegmentDescriptor;
import org.vitrivr.cineast.core.db.dao.reader.MediaObjectReader;
import org.vitrivr.cineast.core.db.dao.reader.MediaSegmentReader;
import org.vitrivr.cineast.standalone.config.Config;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class CineastQueryService extends CineastQueryGrpc.CineastQueryImplBase {

    @Override
    public void getMediaObjects(CineastGrpc.MediaObjectIdList request, StreamObserver<CineastGrpc.MediaObjectQueryResult> responseObserver) {

        Set<String> ids = request.getIdsList().stream().map(CineastGrpc.MediaObjectId::getId).collect(Collectors.toSet());
        MediaObjectReader reader = new MediaObjectReader(Config.sharedConfig().getDatabase().getSelectorSupplier().get());
        Map<String, MediaObjectDescriptor> objects = reader.lookUpObjects(ids);

        CineastGrpc.MediaObjectQueryResult result = CineastGrpc.MediaObjectQueryResult.newBuilder().addAllObjects(
                objects.values().stream().map(MediaObjectUtil::fromMediaObjectDescriptor).collect(Collectors.toList())
        ).build();

        responseObserver.onNext(result);
        responseObserver.onCompleted();
    }

    @Override
    public void getMediaSegments(CineastGrpc.MediaSegmentIdList request, StreamObserver<CineastGrpc.MediaSegmentQueryResult> responseObserver) {

        Set<String> ids = request.getIdsList().stream().map(CineastGrpc.MediaSegmentId::getId).collect(Collectors.toSet());
        MediaSegmentReader reader = new MediaSegmentReader(Config.sharedConfig().getDatabase().getSelectorSupplier().get());
        Map<String, MediaSegmentDescriptor> segments = reader.lookUpSegments(ids);

        CineastGrpc.MediaSegmentQueryResult result = CineastGrpc.MediaSegmentQueryResult.newBuilder().addAllSegments(
            segments.values().stream().map(MediaSegmentUtil::fromMediaSegmentDescriptor).collect(Collectors.toList())
        ).build();

        responseObserver.onNext(result);
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

    @Override
    public void getNeighboringSegments(CineastGrpc.MediaSegmentIdList request, StreamObserver<CineastGrpc.MediaSegmentQueryResult> responseObserver) {
        //TODO
        responseObserver.onCompleted();
    }
}
