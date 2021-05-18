package org.vitrivr.cineast.api.websocket.handlers.queries;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.websocket.api.Session;
import org.vitrivr.cineast.api.messages.query.QueryComponent;
import org.vitrivr.cineast.api.messages.query.SimilarityQuery;
import org.vitrivr.cineast.core.config.QueryConfig;
import org.vitrivr.cineast.core.data.StringDoublePair;
import org.vitrivr.cineast.core.data.query.containers.QueryContainer;
import org.vitrivr.cineast.core.data.score.SegmentScoreElement;
import org.vitrivr.cineast.standalone.config.Config;
import org.vitrivr.cineast.standalone.util.ContinuousRetrievalLogic;

/**
 * This class extends the {@link AbstractQueryMessageHandler} abstract class and handles messages of type {@link SimilarityQuery}.
 */
public class SimilarityQueryMessageHandler extends AbstractQueryMessageHandler<SimilarityQuery> {

  private static final Logger LOGGER = LogManager.getLogger();

  private final ContinuousRetrievalLogic continuousRetrievalLogic;

  public SimilarityQueryMessageHandler(ContinuousRetrievalLogic retrievalLogic) {
    this.continuousRetrievalLogic = retrievalLogic;
  }

  /**
   * Executes a {@link SimilarityQuery}. Performs the similarity query based on the {@link QueryContainer} objects provided in the {@link SimilarityQuery}.
   *
   * @param session                             WebSocket session the invocation is associated with.
   * @param qconf                               The {@link QueryConfig} that contains additional specifications.
   * @param message                             Instance of {@link SimilarityQuery}
   * @param segmentIdsForWhichMetadataIsFetched Segment IDs for which metadata is fetched
   * @param objectIdsForWhichMetadataIsFetched  Object IDs for which metadata is fetched
   */
  @Override
  public void execute(Session session, QueryConfig qconf, SimilarityQuery message, Set<String> segmentIdsForWhichMetadataIsFetched, Set<String> objectIdsForWhichMetadataIsFetched) {

    /* Prepare QueryConfig (so as to obtain a QueryId). */
    final String uuid = qconf.getQueryId().toString();

    /* Prepare map that maps QueryTerms (as QueryContainer, ready for retrieval) and their associated categories */
    final HashMap<QueryContainer, List<String>> containerCategoryMap = QueryComponent.toContainerMap(message.getComponents());
    final int max = qconf.getMaxResults().orElse(Config.sharedConfig().getRetriever().getMaxResults());

    List<Thread> threads = new ArrayList<>();
    List<CompletableFuture<Void>> futures = new ArrayList<>();
    /* Execute similarity queries for all QueryContainer -> Category combinations in the map */
    for (QueryContainer qc : containerCategoryMap.keySet()) {
      for (String category : containerCategoryMap.get(qc)) {
        /* Merge partial results with score-map */
        List<SegmentScoreElement> scores = continuousRetrievalLogic.retrieve(qc, category, qconf);

        /* Transform raw results into list of StringDoublePairs (segmentId -> score) */
        final List<StringDoublePair> results = scores.stream()
            .map(elem -> new StringDoublePair(elem.getSegmentId(), elem.getScore()))
            .filter(p -> p.value > 0d)
            .sorted(StringDoublePair.COMPARATOR)
            .limit(max)
            .collect(Collectors.toList());

        /* Finalize and submit per-container results */
        List<String> segmentIds = results.stream().map(el -> el.key).collect(Collectors.toList());
        List<String> objectIds = this.submitSegmentAndObjectInformation(session, uuid, segmentIds);
        futures.addAll(this.finalizeAndSubmitResults(session, uuid, category, qc.getContainerId(), results));
        List<Thread> _threads = this.submitMetadata(session, uuid, segmentIds, objectIds, segmentIdsForWhichMetadataIsFetched, objectIdsForWhichMetadataIsFetched);
        threads.addAll(_threads);
      }
    }
    futures.forEach(CompletableFuture::join);
    for (Thread thread : threads) {
      try {
        thread.join();
      } catch (InterruptedException e) {
        LOGGER.error(e);
      }
    }

  }

}
