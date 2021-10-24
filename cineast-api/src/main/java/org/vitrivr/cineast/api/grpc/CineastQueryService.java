package org.vitrivr.cineast.api.grpc;

import io.grpc.stub.StreamObserver;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.api.grpc.data.QueryStage;
import org.vitrivr.cineast.api.grpc.data.QueryTerm;
import org.vitrivr.cineast.api.grpc.util.MediaObjectUtil;
import org.vitrivr.cineast.api.grpc.util.MediaSegmentUtil;
import org.vitrivr.cineast.api.grpc.util.QueryContainerUtil;
import org.vitrivr.cineast.api.util.QueryUtil;
import org.vitrivr.cineast.core.config.QueryConfig;
import org.vitrivr.cineast.core.config.ReadableQueryConfig;
import org.vitrivr.cineast.core.data.StringDoublePair;
import org.vitrivr.cineast.core.data.entities.MediaObjectDescriptor;
import org.vitrivr.cineast.core.data.entities.MediaObjectMetadataDescriptor;
import org.vitrivr.cineast.core.data.entities.MediaSegmentDescriptor;
import org.vitrivr.cineast.core.data.entities.MediaSegmentMetadataDescriptor;
import org.vitrivr.cineast.core.data.query.containers.AbstractQueryTermContainer;
import org.vitrivr.cineast.core.data.score.SegmentScoreElement;
import org.vitrivr.cineast.core.db.dao.reader.MediaObjectMetadataReader;
import org.vitrivr.cineast.core.db.dao.reader.MediaObjectReader;
import org.vitrivr.cineast.core.db.dao.reader.MediaSegmentMetadataReader;
import org.vitrivr.cineast.core.db.dao.reader.MediaSegmentReader;
import org.vitrivr.cineast.standalone.config.Config;
import org.vitrivr.cineast.standalone.util.ContinuousRetrievalLogic;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class CineastQueryService extends CineastQueryGrpc.CineastQueryImplBase {

    private static final int DEFAULT_NEIGHBORING_SEGMENTS = 10;

    private final ContinuousRetrievalLogic continuousRetrievalLogic;

    private static final Logger LOGGER = LogManager.getLogger();

    public CineastQueryService(ContinuousRetrievalLogic continuousRetrievalLogic) {
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

        List<QueryStage> stages = QueryContainerUtil.query(query);

        HashSet<String> relevantSegments = new HashSet<>();

        stages:
        for (int i = 0; i < stages.size(); ++i) {

            QueryStage stage = stages.get(i);
            boolean lastStage = i == stages.size() - 1;

            List<QueryTerm> terms = stage.getQueryTerms();
            QueryConfig stageConfig = QueryConfig.clone(stage.getQueryConfig());
            stageConfig.addRelevantSegmentIds(relevantSegments);
            relevantSegments.clear();

            for (QueryTerm term : terms) {

                for (String category : term.getCategories()) {

                    ReadableQueryConfig queryConfig = stageConfig.withChangesFrom(term.getQueryConfig());

                    List<StringDoublePair> results = QueryUtil.retrieve(continuousRetrievalLogic, term.getContainer(), queryConfig, category);

                    if (lastStage) {
                        responseObserver.onNext(QueryContainerUtil.similarityQueryResult(
                                term.getQueryConfig().getQueryId().toString(),
                                category,
                                results
                        ));
                    } else {

                        if (results.isEmpty()) { //no more results left
                            break stages;
                        }

                        results.stream().forEach(x -> relevantSegments.add(x.key));
                    }
                }
            }
        }

        responseObserver.onCompleted();
    }

    //TODO This has enormous code duplication with the TemporalQueryMessageHandler

    @Override
    public void getSimilar(CineastGrpc.TemporalQuery query, StreamObserver<CineastGrpc.QueryResult> responseObserver) {
        StopWatch watch = StopWatch.createStarted();

        MediaSegmentReader mediaSegmentReader = new MediaSegmentReader(Config.sharedConfig().getDatabase().getSelectorSupplier().get());
        MediaObjectReader mediaObjectReader = new MediaObjectReader(Config.sharedConfig().getDatabase().getSelectorSupplier().get());
        MediaSegmentMetadataReader segmentMetadataReader = new MediaSegmentMetadataReader(Config.sharedConfig().getDatabase().getSelectorSupplier().get());
        MediaObjectMetadataReader objectMetadataReader = new MediaObjectMetadataReader(Config.sharedConfig().getDatabase().getSelectorSupplier().get());

        Set<String> sentSegmentIds = new HashSet<>(), sentObjectIds = new HashSet<>();

        CineastGrpc.QueryConfig config = query.getQueryList().get(0).getConfig();
        ReadableQueryConfig rqconf = QueryContainerUtil.queryConfig(config);
        QueryConfig qconf = new QueryConfig(rqconf);

        /* Prepare QueryConfig (so as to obtain a QueryId). */
        final String uuid = qconf.getQueryId().toString();
        final int max = qconf.getMaxResults().orElse(Config.sharedConfig().getRetriever().getMaxResults());
        qconf.setMaxResults(max);
        final int resultsPerModule = qconf.getRawResultsPerModule() == -1 ? Config.sharedConfig().getRetriever().getMaxResultsPerModule() : qconf.getResultsPerModule();
        qconf.setResultsPerModule(resultsPerModule);

        List<Thread> metadataRetrievalThreads = new ArrayList<>();

        /* We iterate over all components independently, because they have a temporal context.*/
        for (int containerIdx = 0; containerIdx < query.getQueryCount(); containerIdx++) {
            List<QueryStage> stages = QueryContainerUtil.query(query.getQueryList().get(containerIdx));

            /* We make a new stagedQueryConfig per stage because the relevant segments will differ for each stage. This also resets the filter (relevant ids in the config)*/
            QueryConfig stageQConf = QueryConfig.clone(qconf);

            /* For the first stage, there will be no relevant segments when querying. This is ok because the retrieval engine handles this appropriately */
            HashSet<String> relevantSegments = new HashSet<>();

            /* Store for each queryterm per category all results to be sent at a later time */
            List<Map<String, List<StringDoublePair>>> cache = new ArrayList<>();

            /* For the terms of a stage, ordering matters. The assumption is that each term is used as a filter for its successor */
            for (int stageIndex = 0; stageIndex < stages.size(); stageIndex++) {
                /* Initalize stage with this hashmap */
                cache.add(stageIndex, new HashMap<>());

                QueryStage stage = stages.get(stageIndex);

                List<Thread> qtThreads = new ArrayList<>();

                /* We now iterate over all QueryTerms for this stage, simply adding their results to the list of relevant segments for the next querystage.
                 * The list is only updated once we've iterated over all terms
                 */
                for (int i = 0; i < stage.getQueryTerms().size(); i++) {
                    QueryTerm qt = stage.getQueryTerms().get(i);

                    final int finalContainerIdx = containerIdx;
                    final int finalStageIndex = stageIndex;
                    Thread qtRetrievalThread = new Thread(() -> {

                        /* Prepare QueryTerm and perform sanity-checks */
                        if (qt == null) {
                            /* In rare instances, it is possible to have null as query stage. If this happens to you, please report this to the developers so we can try to fix it. */
                            LOGGER.warn("QueryTerm was null for stage {}", stage);
                            return;
                        }
                        AbstractQueryTermContainer qc = qt.getContainer();
                        if (qc == null) {
                            LOGGER.warn("Likely an empty query, as it could not be converted to a query container. Ignoring it");
                            return;
                        }

                        List<Thread> categoryThreads = new ArrayList<>();

                        /* For each category of a specific queryterm, we actually go and retrieve. Be aware that we do not change the relevant ids after this call */
                        for (String category : qt.getCategories()) {
                            /* Merge partial results with score-map */
                            List<SegmentScoreElement> scores = continuousRetrievalLogic.retrieve(qc, category, stageQConf);

                            /* Transform raw results into list of StringDoublePairs (segmentId -> score) */
                            final List<StringDoublePair> results = scores.stream()
                                .map(elem -> new StringDoublePair(elem.getSegmentId(), elem.getScore()))
                                .filter(p -> p.value > 0d)
                                .sorted(StringDoublePair.COMPARATOR)
                                .limit(max)
                                .collect(Collectors.toList());

                            if (results.isEmpty()) {
                                LOGGER.warn("No results found for category {} and qt {} in stage with id {}. Full compoment: {}", category, qt, finalContainerIdx, stage);
                            }
                            if (cache.get(finalStageIndex).containsKey(category)) {
                                LOGGER.error("Category {} was used twice in stage {}. This erases the results of the previous category... ", category, finalStageIndex);
                            }
                            cache.get(finalStageIndex).put(category, results);
                            results.forEach(res -> relevantSegments.add(res.key));
                            LOGGER.trace("Category {} at stage {} executed @ {} ms", category, finalStageIndex, watch.getTime(TimeUnit.MILLISECONDS));

                            /* If this is the last stage, we can send relevant results per category back to the UI.
                             * Otherwise, we cannot since we might send results to the UI which would be filtered at a later stage
                             */
                            if (finalStageIndex == stages.size() - 1) {
                                /* Finalize and submit per-container results */
                                responseObserver.onNext(
                                    QueryContainerUtil.queryResult(
                                        QueryContainerUtil.similarityQueryResult(
                                            qt.getQueryConfig().getQueryId().toString(),
                                            category,
                                            results
                                        )));

                                List<String> segmentIds = results.stream().map(x -> x.key).filter(x -> !sentSegmentIds.contains(x)).collect(Collectors.toList());
                                if (segmentIds.isEmpty()) {
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
                                if (objectIds.isEmpty()) {
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
                        /* We're done for this querycontainer */
                    });
                    qtRetrievalThread.setName("qt-stage" + stageIndex + "-" + qt.getCategories()); //TODO Better name
                    qtThreads.add(qtRetrievalThread);
                    qtRetrievalThread.start();
                }

                for (Thread thread : qtThreads) {
                    try {
                        thread.join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                /* After we are done with a stage, we add all relevant segments to the config for the next stage. */
                if (relevantSegments.size() == 0) {
                    LOGGER.warn("No relevant segments anymore, aborting staged querying");
                    /* Clear relevant segments (there are none) */
                    stageQConf.setRelevantSegmentIds(relevantSegments);
                    break;
                }
                stageQConf.setRelevantSegmentIds(relevantSegments);
                relevantSegments.clear();
            }

            /* At this point, we have iterated over all stages. Now, we need to go back for all stages and send the results for the relevant ids. */
            for (int stageIndex = 0; stageIndex < stages.size()-1; stageIndex++) {
                cache.get(stageIndex).forEach((category, results) -> {
                    results.removeIf(pair -> !stageQConf.getRelevantSegmentIds().contains(pair.key));

                    responseObserver.onNext(
                        QueryContainerUtil.queryResult(
                            QueryContainerUtil.similarityQueryResult(
                                uuid,   //TODO This assumes that all queries in a temporalquery have the same uuid
                                category,
                                results
                            )));
                });
            }

            /* There should be no carry-over from this block since temporal queries are executed independently */
        }

        /* At this point, all StagedQueries have been executed for this TemporalQuery.
         * Since results have always been sent for the final stage or, when appropriate, in intermediate steps, there's nothing left to do.
         */

        responseObserver.onCompleted();

        mediaSegmentReader.close();
        mediaObjectReader.close();
        segmentMetadataReader.close();
        watch.stop();
        LOGGER.debug("Query executed in {} ms", watch.getTime(TimeUnit.MILLISECONDS));

    }

    @Override
    public void getNeighboringSegments(CineastGrpc.MediaSegmentIdList request, StreamObserver<CineastGrpc.MediaSegmentQueryResult> responseObserver) {

        MediaSegmentReader mediaSegmentReader = new MediaSegmentReader(Config.sharedConfig().getDatabase().getSelectorSupplier().get());
        Set<String> ids = request.getIdsList().stream().map(CineastGrpc.MediaSegmentId::getId).collect(Collectors.toSet());
        Map<String, MediaSegmentDescriptor> descriptors = mediaSegmentReader.lookUpSegments(ids);

        int range = QueryContainerUtil.queryConfig(request.getQueryConfig()).getMaxResults().orElse(DEFAULT_NEIGHBORING_SEGMENTS) / 2;

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
