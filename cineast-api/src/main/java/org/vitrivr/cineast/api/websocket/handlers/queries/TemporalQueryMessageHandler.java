package org.vitrivr.cineast.api.websocket.handlers.queries;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.websocket.api.Session;
import org.vitrivr.cineast.api.messages.query.QueryStage;
import org.vitrivr.cineast.api.messages.query.QueryTerm;
import org.vitrivr.cineast.api.messages.query.StagedSimilarityQuery;
import org.vitrivr.cineast.api.messages.query.TemporalQuery;
import org.vitrivr.cineast.core.config.QueryConfig;
import org.vitrivr.cineast.core.data.StringDoublePair;
import org.vitrivr.cineast.core.data.query.containers.QueryContainer;
import org.vitrivr.cineast.core.data.score.SegmentScoreElement;
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
    StopWatch watch = StopWatch.createStarted();

    /* Prepare QueryConfig (so as to obtain a QueryId). */
    final String uuid = qconf.getQueryId().toString();
    final int max = qconf.getMaxResults().orElse(Config.sharedConfig().getRetriever().getMaxResults());
    qconf.setMaxResults(max);
    final int resultsPerModule = qconf.getRawResultsPerModule() == -1 ? Config.sharedConfig().getRetriever().getMaxResultsPerModule() : qconf.getResultsPerModule();
    qconf.setResultsPerModule(resultsPerModule);

    List<Thread> metadataRetrievalThreads = new ArrayList<>();

    /* We iterate over all components independently, because they have a temporal context.*/
    for (int containerIdx = 0; containerIdx < message.queries.size(); containerIdx++) {
      StagedSimilarityQuery stagedSimilarityQuery = message.queries.get(containerIdx);

      /* We make a new stagedQueryConfig per stage because the relevant segments will differ for each stage. This also resets the filter (relevant ids in the config)*/
      QueryConfig stageQConf = QueryConfig.clone(qconf);

      /* For the first stage, there will be no relevant segments when querying. This is ok because the retrieval engine handles this appropriately */
      HashSet<String> relevantSegments = new HashSet<>();

      /* Store for each queryterm per category all results to be sent at a later time */
      List<Map<String, List<StringDoublePair>>> cache = new ArrayList<>();

      /* For the terms of a stage, ordering matters. The assumption is that each term is used as a filter for its successor */
      for (int stageIndex = 0; stageIndex < stagedSimilarityQuery.stages.size(); stageIndex++) {
        /* Initalize stage with this hashmap */
        cache.add(stageIndex, new HashMap<>());

        QueryStage stage = stagedSimilarityQuery.stages.get(stageIndex);

        List<Thread> qtThreads = new ArrayList<>();

        /* We now iterate over all QueryTerms for this stage, simply adding their results to the list of relevant segments for the next querystage.
         * The list is only updated once we've iterated over all terms
         */
        for (int i = 0; i < stage.terms.size(); i++) {
          QueryTerm qt = stage.terms.get(i);

          final int finalContainerIdx = containerIdx;
          final int finalStageIndex = stageIndex;
          Thread qtRetrievalThread = new Thread(() -> {

            /* Prepare QueryTerm and perform sanity-checks */
            if (qt == null) {
              /* In rare instances, it is possible to have null as query stage. If this happens to you, please report this to the developers so we can try to fix it. */
              LOGGER.warn("QueryTerm was null for stage {}", stage);
              return;
            }
            QueryContainer qc = qt.toContainer();
            if (qc == null) {
              LOGGER.warn("Likely an empty query, as it could not be converted to a query container. Ignoring it");
              return;
            }
            qc.setContainerId(finalContainerIdx);

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
                LOGGER.warn("No results found for category {} and qt {} in stage with id {}. Full compoment: {}", category, qt.getType(), finalContainerIdx, stage);
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
              if (finalStageIndex == stagedSimilarityQuery.stages.size() - 1) {
                /* Finalize and submit per-container results */
                List<String> segmentIds = results.stream().map(el -> el.key).collect(Collectors.toList());
                List<String> objectIds = this.submitSegmentAndObjectInformation(session, uuid, segmentIds);
                this.finalizeAndSubmitResults(session, uuid, category, qc.getContainerId(), results);
                List<Thread> _threads = this.submitMetadata(session, uuid, segmentIds, objectIds, segmentIdsForWhichMetadataIsFetched, objectIdsForWhichMetadataIsFetched);
                metadataRetrievalThreads.addAll(_threads);
              }
            }
            /* We're done for this querycontainer */
          });
          qtRetrievalThread.setName("qt-stage" + stageIndex + "-" + qt.getType().name());
          qtThreads.add(qtRetrievalThread);
          qtRetrievalThread.start();
        }

        for (Thread thread : qtThreads) {
          thread.join();
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

      List<Thread> cleanupThreads = new ArrayList<>();
      /* At this point, we have iterated over all stages. Now, we need to go back for all stages and send the results for the relevant ids. */
      for (int stageIndex = 0; stageIndex < stagedSimilarityQuery.stages.size() - 1; stageIndex++) {
        int finalContainerIdx = containerIdx;
        int finalStageIndex = stageIndex;
        cache.get(stageIndex).forEach((category, results) -> {
          results.removeIf(pair -> !stageQConf.getRelevantSegmentIds().contains(pair.key));
          Thread thread = new Thread(() -> this.finalizeAndSubmitResults(session, uuid, category, finalContainerIdx, results));
          thread.setName("finalization-stage" + finalStageIndex + "-" + category);
          thread.start();
          cleanupThreads.add(thread);
        });
      }

      for (Thread cleanupThread : cleanupThreads) {
        cleanupThread.join();
      }
      /* There should be no carry-over from this block since temporal queries are executed independently */
    }

    for (Thread thread : metadataRetrievalThreads) {
      thread.join();
    }
    /* At this point, all StagedQueries have been executed for this TemporalQuery.
     * Since results have always been sent for the final stage or, when appropriate, in intermediate steps, there's nothing left to do.
     */
    watch.stop();
    LOGGER.debug("Query executed in {} ms", watch.getTime(TimeUnit.MILLISECONDS));
  }
}
