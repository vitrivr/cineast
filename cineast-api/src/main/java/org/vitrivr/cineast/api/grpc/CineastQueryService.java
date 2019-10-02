package org.vitrivr.cineast.api.grpc;

import io.grpc.stub.StreamObserver;
import org.vitrivr.cineast.api.grpc.data.QueryTerm;
import org.vitrivr.cineast.api.grpc.util.MediaObjectUtil;
import org.vitrivr.cineast.api.grpc.util.MediaSegmentUtil;
import org.vitrivr.cineast.api.grpc.util.QueryContainerUtil;
import org.vitrivr.cineast.api.util.QueryUtil;
import org.vitrivr.cineast.core.config.ReadableQueryConfig;
import org.vitrivr.cineast.core.data.Pair;
import org.vitrivr.cineast.core.data.StringDoublePair;
import org.vitrivr.cineast.core.data.entities.MediaObjectDescriptor;
import org.vitrivr.cineast.core.data.entities.MediaObjectMetadataDescriptor;
import org.vitrivr.cineast.core.data.entities.MediaSegmentDescriptor;
import org.vitrivr.cineast.core.data.entities.MediaSegmentMetadataDescriptor;
import org.vitrivr.cineast.core.data.query.containers.QueryContainer;
import org.vitrivr.cineast.core.db.dao.reader.MediaObjectMetadataReader;
import org.vitrivr.cineast.core.db.dao.reader.MediaObjectReader;
import org.vitrivr.cineast.core.db.dao.reader.MediaSegmentMetadataReader;
import org.vitrivr.cineast.core.db.dao.reader.MediaSegmentReader;
import org.vitrivr.cineast.standalone.config.Config;
import org.vitrivr.cineast.standalone.util.ContinuousRetrievalLogic;

import java.util.*;
import java.util.stream.Collectors;

public class CineastQueryService extends CineastQueryGrpc.CineastQueryImplBase {

    private final ContinuousRetrievalLogic continuousRetrievalLogic;

    public CineastQueryService(ContinuousRetrievalLogic continuousRetrievalLogic){
        this.continuousRetrievalLogic = continuousRetrievalLogic;
    }

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
    public void getMediaSegmentScores(CineastGrpc.Query query, StreamObserver<CineastGrpc.SimilarityQueryResult> responseObserver) {
        List<QueryTerm> terms = QueryContainerUtil.query(query);

        for(QueryTerm term : terms) {

            for (String category : term.getCategories()) {
                List<StringDoublePair> results = QueryUtil.retrieve(continuousRetrievalLogic, term.getContainer(), term.getQueryConfig(), category);
                responseObserver.onNext(QueryContainerUtil.similarityQueryResult(
                        term.getQueryConfig().getQueryId().toString(),
                        category,
                        results
                ));
            }
        }

        responseObserver.onCompleted();
    }

    @Override
    public void getSimilar(CineastGrpc.Query query, StreamObserver<CineastGrpc.QueryResult> responseObserver) {
        List<QueryTerm> terms = QueryContainerUtil.query(query);

        Set<String> sentSegmentIds = new HashSet<>(), sentObjectIds = new HashSet<>();

        MediaSegmentReader mediaSegmentReader = new MediaSegmentReader(Config.sharedConfig().getDatabase().getSelectorSupplier().get());
        MediaObjectReader mediaObjectReader = new MediaObjectReader(Config.sharedConfig().getDatabase().getSelectorSupplier().get());
        MediaSegmentMetadataReader segmentMetadataReader = new MediaSegmentMetadataReader(Config.sharedConfig().getDatabase().getSelectorSupplier().get());
        MediaObjectMetadataReader objectMetadataReader = new MediaObjectMetadataReader(Config.sharedConfig().getDatabase().getSelectorSupplier().get());

        for(QueryTerm term : terms) {

            for (String category : term.getCategories()) {
                List<StringDoublePair> results = QueryUtil.retrieve(continuousRetrievalLogic, term.getContainer(), term.getQueryConfig(), category);
                responseObserver.onNext(
                        QueryContainerUtil.queryResult(
                            QueryContainerUtil.similarityQueryResult(
                            term.getQueryConfig().getQueryId().toString(),
                            category,
                            results
                )));

                List<String> segmentIds = results.stream().map(x -> x.key).filter(x -> !sentSegmentIds.contains(x)).collect(Collectors.toList());
                if (segmentIds.isEmpty()){
                    continue;
                }

                Map<String, MediaSegmentDescriptor> segments = mediaSegmentReader.lookUpSegments(segmentIds);

                responseObserver.onNext(
                        QueryContainerUtil.queryResult(
                                CineastGrpc.MediaSegmentQueryResult.newBuilder().addAllSegments(
                        segments.values().stream().map(MediaSegmentUtil::fromMediaSegmentDescriptor).collect(Collectors.toList())
                                ).build()
                        )
                );

                List<MediaSegmentMetadataDescriptor> segmentMetaData = segmentMetadataReader.lookupMultimediaMetadata(segmentIds);
                responseObserver.onNext(
                        QueryContainerUtil.queryResult(
                                CineastGrpc.MediaSegmentMetaDataQueryResult.newBuilder().addAllSegmentMetaData(
                                        segmentMetaData.stream().map(QueryContainerUtil::mediaSegmentMetaData).collect(Collectors.toList())
                                ).build()
                        )
                );

                sentSegmentIds.addAll(segmentIds);

                List<String> objectIds = segments.values().stream().map(MediaSegmentDescriptor::getObjectId).filter(x -> !sentObjectIds.contains(x)).collect(Collectors.toList());
                if (objectIds.isEmpty()){
                    continue;
                }
                Map<String, MediaObjectDescriptor> objects = mediaObjectReader.lookUpObjects(objectIds);

                responseObserver.onNext(
                        QueryContainerUtil.queryResult(
                                CineastGrpc.MediaObjectQueryResult.newBuilder().addAllObjects(
                                        objects.values().stream().map(MediaObjectUtil::fromMediaObjectDescriptor).collect(Collectors.toList())
                                ).build()
                        )
                );

                List<MediaObjectMetadataDescriptor> objectMetaData = objectMetadataReader.lookupMultimediaMetadata(objectIds);
                responseObserver.onNext(
                        QueryContainerUtil.queryResult(
                                CineastGrpc.MediaObjectMetaDataQueryResult.newBuilder().addAllObjectMetaData(
                                        objectMetaData.stream().map(QueryContainerUtil::mediaObjectMetaData).collect(Collectors.toList())
                                ).build()
                        )
                );

                sentObjectIds.addAll(objectIds);
            }
        }

        responseObserver.onCompleted();

        mediaSegmentReader.close();
        mediaObjectReader.close();
        segmentMetadataReader.close();

    }

    @Override
    public void getNeighboringSegments(CineastGrpc.MediaSegmentIdList request, StreamObserver<CineastGrpc.MediaSegmentQueryResult> responseObserver) {

        MediaSegmentReader mediaSegmentReader = new MediaSegmentReader(Config.sharedConfig().getDatabase().getSelectorSupplier().get());
        Set<String> ids = request.getIdsList().stream().map(CineastGrpc.MediaSegmentId::getId).collect(Collectors.toSet());
        Map<String, MediaSegmentDescriptor> descriptors = mediaSegmentReader.lookUpSegments(ids);

        int range = QueryContainerUtil.queryConfig(request.getQueryConfig()).getMaxResults().orElse(10) / 2;

        if (range > 0){
            Set<MediaSegmentDescriptor> results = new HashSet<>( 2 * range * descriptors.size());

            for (MediaSegmentDescriptor d : descriptors.values()) {
                results.addAll(mediaSegmentReader.lookUpSegmentsByNumberRange(d.getObjectId(), d.getSequenceNumber() - range, d.getSequenceNumber() + range));
            }

            CineastGrpc.MediaSegmentQueryResult result = CineastGrpc.MediaSegmentQueryResult.newBuilder().addAllSegments(
                    results.stream().map(MediaSegmentUtil::fromMediaSegmentDescriptor).collect(Collectors.toList())
            ).build();
            responseObserver.onNext(result);

        }

        responseObserver.onCompleted();
        mediaSegmentReader.close();
    }
}
