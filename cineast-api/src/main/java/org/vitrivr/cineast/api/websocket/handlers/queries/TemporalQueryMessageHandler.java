package org.vitrivr.cineast.api.websocket.handlers.queries;

import io.netty.util.collection.IntObjectHashMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.websocket.api.Session;
import org.vitrivr.cineast.api.messages.query.QueryStage;
import org.vitrivr.cineast.api.messages.query.QueryTerm;
import org.vitrivr.cineast.api.messages.query.StagedSimilarityQuery;
import org.vitrivr.cineast.api.messages.query.TemporalQuery;
import org.vitrivr.cineast.core.config.QueryConfig;
import org.vitrivr.cineast.core.data.StringDoublePair;
import org.vitrivr.cineast.core.data.TemporalObject;
import org.vitrivr.cineast.core.data.entities.MediaSegmentDescriptor;
import org.vitrivr.cineast.core.data.query.containers.AbstractQueryTermContainer;
import org.vitrivr.cineast.core.data.score.SegmentScoreElement;
import org.vitrivr.cineast.core.temporal.TemporalScoring;
import org.vitrivr.cineast.standalone.config.Config;
import org.vitrivr.cineast.standalone.util.ContinuousRetrievalLogic;

public class TemporalQueryMessageHandler extends AbstractQueryMessageHandler<TemporalQuery> {

  private static final Logger LOGGER = LogManager.getLogger();

  private final ContinuousRetrievalLogic continuousRetrievalLogic;

  public TemporalQueryMessageHandler(ContinuousRetrievalLogic retrievalLogic) {
    this.continuousRetrievalLogic = retrievalLogic;
  }

  @Override
  public void execute(Session session, QueryConfig qconf, TemporalQuery message, Set<String> segmentIdsForWhichMetadataIsFetched, Set<String> objectIdsForWhichMetadataIsFetched) throws Exception {

    /* Prepare the query config and get the QueryId */
    final String uuid = qconf.getQueryId().toString();
    String qid = uuid.substring(0, 3);
    final int max = Math.min(qconf.getMaxResults().orElse(Config.sharedConfig().getRetriever().getMaxResults()), Config.sharedConfig().getRetriever().getMaxResults());
    qconf.setMaxResults(max);
    final int resultsPerModule = Math.min(qconf.getRawResultsPerModule() == -1 ? Config.sharedConfig().getRetriever().getMaxResultsPerModule() : qconf.getResultsPerModule(), Config.sharedConfig().getRetriever().getMaxResultsPerModule());
    qconf.setResultsPerModule(resultsPerModule);

    List<Thread> metadataRetrievalThreads = new ArrayList<>();
    List<CompletableFuture<Void>> futures = new ArrayList<>();
    List<Thread> cleanupThreads = new ArrayList<>();

    /* We need a set of segments and objects to be used for temporal scoring as well as a storage of all container results where are the index of the outer list is where container i was scored */
    Map<Integer, List<StringDoublePair>> containerResults = new IntObjectHashMap<>();
    Set<MediaSegmentDescriptor> segments = new HashSet<>();

    Set<String> sentSegmentIds = new HashSet<>();
    Set<String> sentObjectIds = new HashSet<>();

    /* Each container can be evaluated in parallel, provided resouces are available */
    List<Thread> ssqThreads = new ArrayList<>();

    /* Iterate over all temporal query containers independently */
    for (int containerIdx = 0; containerIdx < message.getQueries().size(); containerIdx++) {
      StagedSimilarityQuery stagedSimilarityQuery = message.getQueries().get(containerIdx);

      /* Make a new Query config for this container because the relevant segments from the previous stage will differ within this container from stage to stage.  */
      QueryConfig stageQConf = QueryConfig.clone(qconf);
      QueryConfig limitedStageQConf = QueryConfig.clone(qconf);

      /* The first stage of a container will have no relevant segments from a previous stage. The retrieval engine will handle this case. */
      HashSet<String> relevantSegments = new HashSet<>();
      HashSet<String> limitedRelevantSegments = new HashSet<>();

      /*
       * Store for each query term per category all results to be sent at a later time
       */
      List<Map<String, List<StringDoublePair>>> cache = new ArrayList<>();

      /* For the temporal scoring, we need to store the relevant results of the stage to be saved to the containerResults */
      List<StringDoublePair> stageResults = new ArrayList<>();

      int lambdaFinalContainerIdx = containerIdx;

      /*
       * The lightweight, but blocking logic of waiting for retrieval results is launched as a thread.
       * The results of this thread will be awaited after all containers have started their retrieval process
       */
      Thread ssqThread = new Thread(() -> {
        /* Iterate over all stages in their respective order as each term of one stage will be used as a filter for its successors */
        for (int stageIndex = 0; stageIndex < stagedSimilarityQuery.getStages().size(); stageIndex++) {
          /* Create hashmap for this stage as cache */
          cache.add(stageIndex, new HashMap<>());

          QueryStage stage = stagedSimilarityQuery.getStages().get(stageIndex);

          /*
           * Iterate over all QueryTerms for this stage and add their results to the list of relevant segments for the next query stage.
           * Only update the list of relevant query terms once we iterated over all terms
           */
          for (int i = 0; i < stage.terms.size(); i++) {
            QueryTerm qt = stage.terms.get(i);

            /* Prepare the QueryTerm and perform sanity checks */
            if (qt == null) {
              /* There are edge cases in which we have a null as a query stage. If this happens please report this to the developers  */
              LOGGER.warn("QueryTerm was null for stage {}", stage);
              return;
            }
            AbstractQueryTermContainer qc = qt.toContainer();
            if (qc == null) {
              LOGGER.warn(
                  "Likely an empty query, as it could not be converted to a query container. Ignoring it");
              return;
            }

            /* We retrieve the results for each category of a QueryTerm independently. The relevant ids will not yet be changed after this call as we are still in the same stage. */
            for (String category : qt.getCategories()) {
              List<SegmentScoreElement> scores = continuousRetrievalLogic.retrieve(qc, category, stageQConf);

              final List<StringDoublePair> results = scores.stream()
                  .map(elem -> new StringDoublePair(elem.getSegmentId(), elem.getScore()))
                  .filter(p -> p.value > 0d)
                  .sorted(StringDoublePair.COMPARATOR)
                  .collect(Collectors.toList());

              if (results.isEmpty()) {
                LOGGER.warn("No results found for category {} and qt {} in stage with id {}. Full component: {}", category, qt.getType(), lambdaFinalContainerIdx, stage);
              }
              if (cache.get(stageIndex).containsKey(category)) {
                LOGGER.error("Category {} was used twice in stage {}. This erases the results of the previous category... ", category, stageIndex);
              }

              cache.get(stageIndex).put(category, results);
              results.forEach(res -> relevantSegments.add(res.key));

              /*
               * If this is the last stage, we can collect the results and send relevant results per category back the requester.
               * Otherwise we shouldn't yet send since we might send results to the requester that would be filtered at a later stage.
               */
              if (stageIndex == stagedSimilarityQuery.getStages().size() - 1) {

                /* We limit the results to be sent back to the requester to the max limit. This is so that the original view is not affected by the changes of temporal query version 2 */
                List<StringDoublePair> limitedResults = results.stream()
                    .limit(max)
                    .collect(Collectors.toList());
                results.forEach(res -> limitedRelevantSegments.add(res.key));
                List<String> limitedSegmentIds = limitedResults.stream()
                    .map(el -> el.key)
                    .collect(Collectors.toList());
                sentSegmentIds.addAll(limitedSegmentIds);

                List<MediaSegmentDescriptor> limitedSegmentDescriptors = this.loadSegments(limitedSegmentIds, qid);

                /* Store the segments and results for this staged query to be used in the temporal querying. */
                segments.addAll(limitedSegmentDescriptors);
                stageResults.addAll(results);

                List<String> limitedObjectIds = this.submitPrefetchedSegmentAndObjectInformation(session, uuid, limitedSegmentDescriptors);
                sentObjectIds.addAll(limitedObjectIds);
                LOGGER.trace("Queueing finalization and result submission for last stage, container {}", lambdaFinalContainerIdx);
                futures.addAll(this.finalizeAndSubmitResults(session, uuid, category, lambdaFinalContainerIdx, limitedResults));
                List<Thread> _threads = this.submitMetadata(session, uuid, limitedSegmentIds, limitedObjectIds, segmentIdsForWhichMetadataIsFetched, objectIdsForWhichMetadataIsFetched, message.getMetadataAccessSpec());
                metadataRetrievalThreads.addAll(_threads);
              }
            }
          }

          /* After having finished a stage, we add all relevant segments to the config of the next stage. */
          if (relevantSegments.size() == 0) {
            LOGGER.warn("No relevant segments anymore, aborting staged querying");
            /* Clear the relevant segments are there are none */
            stageQConf.setRelevantSegmentIds(relevantSegments);
            break;
          }
          stageQConf.setRelevantSegmentIds(relevantSegments);
          relevantSegments.clear();
        }
        limitedStageQConf.setRelevantSegmentIds(limitedRelevantSegments);

        /* At this point, we have iterated over all stages. Now, we need to go back for all stages and send the results for the relevant ids. */
        for (int stageIndex = 0; stageIndex < stagedSimilarityQuery.getStages().size() - 1; stageIndex++) {
          int finalStageIndex = stageIndex;
          /* Add the results from the last filter from all previous stages also to the list of results */
          cache.get(stageIndex).forEach((category, results) -> {
            results.removeIf(pair -> !stageQConf.getRelevantSegmentIds().contains(pair.key));
            stageResults.addAll(results);
          });
          /* Return the limited results from all stages that are within the filter */
          cache.get(stageIndex).forEach((category, results) -> {
            results.removeIf(pair -> !limitedStageQConf.getRelevantSegmentIds().contains(pair.key));
            Thread thread = new Thread(() -> {
              LOGGER.trace("Queuing finalization & result submission for stage {} and container {}", finalStageIndex, lambdaFinalContainerIdx);
              futures.addAll(this.finalizeAndSubmitResults(session, uuid, category, lambdaFinalContainerIdx, results));
            });
            thread.setName("finalization-stage" + finalStageIndex + "-" + category);
            thread.start();
            cleanupThreads.add(thread);
          });
        }

        /* There should be no carry-over from this block since temporal queries are executed independently */
        containerResults.put(lambdaFinalContainerIdx, stageResults);
      });
      ssqThread.setName("ssq-" + containerIdx);
      ssqThreads.add(ssqThread);
      ssqThread.start();
    }
    for (Thread ssqThread : ssqThreads) {
      ssqThread.join();
    }

    /* You can skip the computation of temporal objects in the config if you wish simply to execute all queries independently (e.g. for evaluation)*/
    if (!message.getTemporalQueryConfig().computeTemporalObjects) {
      LOGGER.debug("Not computing temporal objects due to query config");
      finish(metadataRetrievalThreads, cleanupThreads);
      return;
    }

    LOGGER.debug("Starting fusion for temporal context");
    long start = System.currentTimeMillis();
    /* Retrieve the MediaSegmentDescriptors needed for the temporal scoring retrieval */
    Map<String, MediaSegmentDescriptor> segmentMap = segments.stream().distinct()
        .collect(Collectors.toMap(MediaSegmentDescriptor::getSegmentId, x -> x, (x1, x2) -> x1));
    /* Initialise the temporal scoring algorithms depending on timeDistances list */
    List<List<StringDoublePair>> tmpContainerResults = new ArrayList<>();

    IntStream.range(0, message.getQueries().size()).forEach(idx -> tmpContainerResults.add(containerResults.getOrDefault(idx, new ArrayList<>())));

    /* Score and retrieve the results */
    List<TemporalObject> results = TemporalScoring.score(segmentMap, tmpContainerResults, message.getTimeDistances(), message.getMaxLength());

    List<TemporalObject> finalResults = results.stream()
        .sorted(TemporalObject.COMPARATOR.reversed())
        .limit(max)
        .collect(Collectors.toList());

    LOGGER.debug("Temporal scoring done in {} ms, {} results", System.currentTimeMillis() - start, finalResults.size());

    /* Retrieve the segment Ids of the newly scored segments */
    List<String> segmentIds = finalResults.stream().map(TemporalObject::getSegments).flatMap(List::stream).collect(Collectors.toList());

    /* Send potential information not already sent  */
    /* Maybe change from list to set? */
    segmentIds = segmentIds.stream().filter(s -> !sentSegmentIds.contains(s)).collect(Collectors.toList());
    List<String> objectIds = segments.stream().map(MediaSegmentDescriptor::getObjectId).collect(Collectors.toList());
    objectIds = objectIds.stream().filter(s -> !sentObjectIds.contains(s)).collect(Collectors.toList());

    /* If necessary, send to the UI */
    if (segmentIds.size() != 0 && objectIds.size() != 0) {
      this.submitSegmentAndObjectInformationFromIds(session, uuid, segmentIds, objectIds);

      /* Retrieve and send metadata for items not already sent */
      List<Thread> _threads = this.submitMetadata(session, uuid, segmentIds, objectIds, segmentIdsForWhichMetadataIsFetched, objectIdsForWhichMetadataIsFetched, message.getMetadataAccessSpec());
      metadataRetrievalThreads.addAll(_threads);
    }

    /* Send scoring results to the frontend */
    if (finalResults.size() > 0) {
      futures.addAll(this.finalizeAndSubmitTemporalResults(session, uuid, finalResults));
      futures.forEach(CompletableFuture::join);
    }

    finish(metadataRetrievalThreads, cleanupThreads);
  }

  private void finish(List<Thread> metadataRetrievalThreads, List<Thread> cleanupThreads) throws InterruptedException {
    for (Thread cleanupThread : cleanupThreads) {
      cleanupThread.join();
    }

    for (Thread thread : metadataRetrievalThreads) {
      thread.join();
    }
  }

}
