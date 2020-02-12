package org.vitrivr.cineast.api.websocket.handlers.queries;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.websocket.api.Session;
import org.vitrivr.cineast.api.messages.query.QueryComponent;
import org.vitrivr.cineast.api.messages.query.SimilarityQuery;
import org.vitrivr.cineast.api.messages.query.StagedSimilarityQuery;
import org.vitrivr.cineast.core.config.QueryConfig;
import org.vitrivr.cineast.core.data.StringDoublePair;
import org.vitrivr.cineast.core.data.query.containers.QueryContainer;
import org.vitrivr.cineast.core.data.score.SegmentScoreElement;
import org.vitrivr.cineast.standalone.config.Config;
import org.vitrivr.cineast.standalone.util.ContinuousRetrievalLogic;

public class StagedSimilarityQueryMessageHandler extends AbstractQueryMessageHandler<StagedSimilarityQuery> {

  private static final Logger LOGGER = LogManager.getLogger();

  private final ContinuousRetrievalLogic continuousRetrievalLogic;

  public StagedSimilarityQueryMessageHandler(ContinuousRetrievalLogic retrievalLogic) {
    this.continuousRetrievalLogic = retrievalLogic;
  }

  @Override
  public void execute(Session session, QueryConfig qconf, StagedSimilarityQuery message) throws Exception {
    /* Prepare QueryConfig (so as to obtain a QueryId). */
    final String uuid = qconf.getQueryId().toString();

    HashSet<String> relevantSegments = new HashSet<>();

    for (int i = 0; i < message.getStages().size(); i++) {

      SimilarityQuery stage = message.getStages().get(i);
      boolean stageIsLastStage = i == message.getStages().size() - 1;
      QueryConfig stageQConf = QueryConfig.clone(stage.getQueryConfig() != null ? stage.getQueryConfig() : qconf);
      /* For the first stage, this list will be empty. This is ok because... TODO */
      stageQConf.addRelevantSegmentIds(relevantSegments);
      relevantSegments.clear();

      /* Prepare map that maps QueryTerms (as QueryContainer, ready for retrieval) and their associated categories */
      final HashMap<QueryContainer, List<String>> containerCategoryMap = QueryComponent.toContainerMap(stage.getComponents());
      final int max = stageQConf.getMaxResults().orElse(Config.sharedConfig().getRetriever().getMaxResults());

      /* Execute similarity queries for all QueryContainer -> Category combinations in the map */
      for (QueryContainer qc : containerCategoryMap.keySet()) {
        for (String category : containerCategoryMap.get(qc)) {
          /* Merge partial results with score-map */
          List<SegmentScoreElement> scores = continuousRetrievalLogic.retrieve(qc, category, stageQConf);
          /* Transform raw results into list of StringDoublePairs (segmentId -> score) */

          final List<StringDoublePair> results = scores.stream()
              .map(elem -> new StringDoublePair(elem.getSegmentId(), elem.getScore()))
              .filter(p -> p.value > 0d)
              .sorted(StringDoublePair.COMPARATOR)
              .limit(max)
              .collect(Collectors.toList());

          if (stageIsLastStage) {
            /* Finalize and submit per-container results */
            this.finalizeAndSubmitResults(session, uuid, category, qc.getContainerId(), results);
          } else {
            if (results.isEmpty()) { //no more results left
              return;
            }
            results.forEach(x -> relevantSegments.add(x.key));
          }
        }
      }

    }
  }
}
