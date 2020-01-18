package org.vitrivr.cineast.api.websocket.handlers.queries;

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

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author rgasser
 * @version 1.0
 * @created 12.01.17
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
   * @param session WebSocket session the invocation is associated with.
   * @param qconf The {@link QueryConfig} that contains additional specifications.
   * @param message Instance of {@link SimilarityQuery}
   */
  @Override
  public void execute(Session session, QueryConfig qconf, SimilarityQuery message) {
    /* Prepare QueryConfig (so as to obtain a QueryId). */
    final String uuid = qconf.getQueryId().toString();

    /* Prepare map that maps QueryTerms (as QueryContainer, ready for retrieval) and their associated categories */
    final HashMap<QueryContainer, List<String>> containerCategoryMap = QueryComponent.toContainerMap(message.getComponents());
    final int max = qconf.getMaxResults().orElse(Config.sharedConfig().getRetriever().getMaxResults());

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
        this.finalizeAndSubmitResults(session, uuid, category, qc.getContainerId(), results);
      }
    }
  }

}
