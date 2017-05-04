package org.vitrivr.cineast.api.websocket.handlers.queries;

import org.eclipse.jetty.websocket.api.Session;

import org.vitrivr.cineast.core.config.Config;
import org.vitrivr.cineast.core.config.QueryConfig;

import org.vitrivr.cineast.core.data.StringDoublePair;
import org.vitrivr.cineast.core.data.messages.query.MoreLikeThisQuery;
import org.vitrivr.cineast.core.data.messages.result.*;
import org.vitrivr.cineast.core.util.ContinuousRetrievalLogic;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author rgasser
 * @version 1.0
 * @created 27.04.17
 */
public class MoreLikeThisQueryMessageHandler extends AbstractQueryMessageHandler<MoreLikeThisQuery> {
    /**
     * Handles a {@link MoreLikeThisQuery} message. Executes the similarity-query based on the segmentId specified
     * the {@link MoreLikeThisQuery} object.
     *
     * @param session WebSocket session the invokation is associated with.
     * @param message Instance of {@link MoreLikeThisQuery}
     */
    @Override
    public void handle(Session session, MoreLikeThisQuery message) {
        /* Prepare QueryConfig (so as to obtain a QueryId). */
        final QueryConfig qconf = QueryConfig.newQueryConfigFromOther(Config.sharedConfig().getQuery());

        /* Begin of Query: Send QueryStart Message to Client. */
        final QueryStart startMarker = new QueryStart(qconf.getQueryId().toString());
        this.write(session, startMarker);

        /* Extract categories from MoreLikeThisQuery. */
        final HashSet<String> categoryMap = new HashSet<>();
        message.getCategories().forEach((String category) -> {
            if (!categoryMap.contains(category)) {
                categoryMap.add(category);
            }
        });

        /* Retrieve per-category results and return them. */
        for (String category : categoryMap) {
            List<StringDoublePair> results = ContinuousRetrievalLogic.retrieve(message.getSegmentId(), category, qconf).stream()
                    .map(score -> new StringDoublePair(score.getSegmentId(), score.getScore()))
                    .sorted(StringDoublePair.COMPARATOR)
                    .limit(MAX_RESULTS)
                    .collect(Collectors.toList());

            this.write(session, new SegmentQueryResult(startMarker.getQueryId(), this.loadSegments(results)));
            this.write(session, new ObjectQueryResult(startMarker.getQueryId(), this.loadObjects(results)));
            this.write(session, new SimilarityQueryResult(startMarker.getQueryId(), category, results));
        }

        /* End of Query: Send QueryEnd Message to Client. */
        this.write(session, new QueryEnd(startMarker.getQueryId()));
    }
}
