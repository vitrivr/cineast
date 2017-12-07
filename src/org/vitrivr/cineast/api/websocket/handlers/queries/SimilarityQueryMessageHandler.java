package org.vitrivr.cineast.api.websocket.handlers.queries;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.jetty.websocket.api.Session;
import org.vitrivr.cineast.core.config.Config;
import org.vitrivr.cineast.core.config.QueryConfig;
import org.vitrivr.cineast.core.data.StringDoublePair;
import org.vitrivr.cineast.core.data.messages.query.QueryComponent;
import org.vitrivr.cineast.core.data.messages.query.SimilarityQuery;
import org.vitrivr.cineast.core.data.messages.result.ObjectQueryResult;
import org.vitrivr.cineast.core.data.messages.result.QueryEnd;
import org.vitrivr.cineast.core.data.messages.result.QueryError;
import org.vitrivr.cineast.core.data.messages.result.QueryStart;
import org.vitrivr.cineast.core.data.messages.result.SegmentQueryResult;
import org.vitrivr.cineast.core.data.messages.result.SimilarityQueryResult;
import org.vitrivr.cineast.core.data.query.containers.QueryContainer;
import org.vitrivr.cineast.core.data.score.ScoreElement;
import org.vitrivr.cineast.core.util.ContinuousRetrievalLogic;
import org.vitrivr.cineast.core.util.LogHelper;

import gnu.trove.map.hash.TObjectDoubleHashMap;

/**
 * @author rgasser
 * @version 1.0
 * @created 12.01.17
 */
public class SimilarityQueryMessageHandler extends AbstractQueryMessageHandler<SimilarityQuery> {
    /**
     * Handles a {@link SimilarityQuery} message. Executes the similarity-query based on the {@link QueryContainer}
     * objects provided in the {@link SimilarityQuery}.
     *
     * @param session WebSocket session the invokation is associated with.
     * @param message Instance of {@link SimilarityQuery}
     */
    @Override
    public void handle(Session session, SimilarityQuery message) {
        /* Prepare QueryConfig (so as to obtain a QueryId). */
        final QueryConfig qconf = QueryConfig.newQueryConfigFromOther(Config.sharedConfig().getQuery());

        /* Begin of Query: Send QueryStart Message to Client. */
        this.write(session, new QueryStart(qconf.getQueryId().toString()));


        try {
            /*
             * Prepare map that maps category  to QueryTerm components.
             */
            final HashMap<String, ArrayList<QueryContainer>> categoryMap = QueryComponent.toCategoryMap(message.getComponents());

            /*
             * Execute similarity queries for all Category -> QueryContainer combinations in the map.
             */
            for (String category : categoryMap.keySet()) {
                TObjectDoubleHashMap<String> map = new TObjectDoubleHashMap<>();
                for (QueryContainer qc : categoryMap.get(category)) {
                    /* Merge partial results with score-map. */
                    float weight = qc.getWeight() > 0f ? 1f : -1f; //TODO better normalisation
                    ScoreElement.mergeWithScoreMap(ContinuousRetrievalLogic.retrieve(qc, category, qconf), map, weight);

                    /* Convert partial-results to list of StringDoublePairs. */
                    final List<StringDoublePair> list = new ArrayList<>(map.size());
                    map.forEachEntry((key, value) -> {
                        if (value > 0) {
                          list.add(new StringDoublePair(key, value));
                        }
                        return true;
                    });

                    /*
                     * Resize the list to the size constrained by MAX_RESULTS after sorting it
                     * in ascending order.
                     */
                    list.sort(StringDoublePair.COMPARATOR);
                    if (list.size() > MAX_RESULTS) {
                        list.subList(MAX_RESULTS, list.size()).clear();
                    }

                    /*
                     * Write query responses to WebSocket stream.
                     */
                    this.write(session, new SegmentQueryResult(qconf.getQueryId().toString(), this.loadSegments(list)));
                    this.write(session, new ObjectQueryResult(qconf.getQueryId().toString(), this.loadObjects(list)));
                    this.write(session, new SimilarityQueryResult(qconf.getQueryId().toString(), category, list));
                }
            }

            /* End of Query: Send QueryEnd Message to client. */
            this.write(session, new QueryEnd(qconf.getQueryId().toString()));

        } catch (Exception exception) {
            /* On exception: Send QueryError message to client. */
            this.write(session, new QueryError(qconf.getQueryId().toString(), exception.getMessage()));
            LOGGER.error("An exception occurred during execution of similarity query message {}.", LogHelper.getStackTrace(exception));
        }
    }
}
