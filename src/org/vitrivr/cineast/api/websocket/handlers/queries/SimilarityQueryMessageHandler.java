package org.vitrivr.cineast.api.websocket.handlers.queries;

import gnu.trove.map.hash.TObjectDoubleHashMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.eclipse.jetty.websocket.api.Session;
import org.vitrivr.cineast.api.websocket.handlers.abstracts.StatelessWebsocketMessageHandler;
import org.vitrivr.cineast.core.config.Config;
import org.vitrivr.cineast.core.config.QueryConfig;
import org.vitrivr.cineast.core.data.StringDoublePair;
import org.vitrivr.cineast.core.data.entities.MultimediaObjectDescriptor;
import org.vitrivr.cineast.core.data.entities.SegmentDescriptor;
import org.vitrivr.cineast.core.data.messages.query.SimilarityQuery;
import org.vitrivr.cineast.core.data.messages.query.QueryComponent;
import org.vitrivr.cineast.core.data.messages.query.QueryTerm;
import org.vitrivr.cineast.core.data.messages.result.ObjectQueryResult;
import org.vitrivr.cineast.core.data.messages.result.QueryEnd;
import org.vitrivr.cineast.core.data.messages.result.QueryStart;
import org.vitrivr.cineast.core.data.messages.result.SegmentQueryResult;
import org.vitrivr.cineast.core.data.messages.result.SimilarityQueryResult;
import org.vitrivr.cineast.core.data.query.containers.QueryContainer;
import org.vitrivr.cineast.core.data.score.ScoreElement;
import org.vitrivr.cineast.core.data.score.SegmentScoreElement;
import org.vitrivr.cineast.core.db.dao.reader.MultimediaObjectLookup;
import org.vitrivr.cineast.core.db.dao.reader.SegmentLookup;
import org.vitrivr.cineast.core.util.ContinuousRetrievalLogic;

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
    /* Begin of Query: Send QueryStart Message to Client. */
        QueryStart startMarker = new QueryStart();
        this.write(session, startMarker);

        /*
         * Prepare map that maps category  to QueryTerm components.
         */
        HashMap<String, ArrayList<QueryContainer>> categoryMap = QueryComponent.toCategoryMap(message.getComponents());

        /*
         * Execute similarity queries for all Category -> QueryContainer combinations in the map.
         */
        QueryConfig qconf = QueryConfig.newQueryConfigFromOther(Config.sharedConfig().getQuery());
        for (String category : categoryMap.keySet()) {
            TObjectDoubleHashMap<String> map = new TObjectDoubleHashMap<>();
            for (QueryContainer qc : categoryMap.get(category)) {
                /* Merge partial results with score-map. */
                float weight = qc.getWeight() > 0f ? 1f : -1f; //TODO better normalisation
                ScoreElement.mergeWithScoreMap(ContinuousRetrievalLogic.retrieve(qc, category, qconf), map, weight);

                /* Convert partial-results to list of StringDoublePairs. */
                final List<StringDoublePair> list = new ArrayList<>(map.size());
                map.forEachEntry((key, value) -> {
                    if (value > 0) list.add(new StringDoublePair(key, value));
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
                this.write(session, new SegmentQueryResult(startMarker.getQueryId(), this.loadSegments(list)));
                this.write(session, new ObjectQueryResult(startMarker.getQueryId(), this.loadObjects(list)));
                this.write(session, new SimilarityQueryResult(startMarker.getQueryId(), category, list));
            }
        }

        /* End of Query: Send QueryEnd Message to Client. */
        this.write(session, new QueryEnd(startMarker.getQueryId()));
    }
}
