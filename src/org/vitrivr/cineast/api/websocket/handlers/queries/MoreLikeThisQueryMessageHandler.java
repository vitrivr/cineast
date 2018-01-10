package org.vitrivr.cineast.api.websocket.handlers.queries;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jetty.websocket.api.Session;
import org.vitrivr.cineast.core.config.Config;
import org.vitrivr.cineast.core.config.QueryConfig;
import org.vitrivr.cineast.core.data.StringDoublePair;
import org.vitrivr.cineast.core.data.entities.MultimediaObjectDescriptor;
import org.vitrivr.cineast.core.data.entities.SegmentDescriptor;
import org.vitrivr.cineast.core.data.messages.query.MoreLikeThisQuery;
import org.vitrivr.cineast.core.data.messages.result.ObjectQueryResult;
import org.vitrivr.cineast.core.data.messages.result.QueryEnd;
import org.vitrivr.cineast.core.data.messages.result.QueryStart;
import org.vitrivr.cineast.core.data.messages.result.SegmentQueryResult;
import org.vitrivr.cineast.core.data.messages.result.SimilarityQueryResult;
import org.vitrivr.cineast.core.util.ContinuousRetrievalLogic;

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
        final QueryConfig qconf = (message.getQueryConfig() == null) ? QueryConfig.newQueryConfigFromOther(Config.sharedConfig().getQuery()) : message.getQueryConfig();
        final String uuid = qconf.getQueryId().toString();

        /* Begin of Query: Send QueryStart Message to Client. */
        this.write(session, new QueryStart(uuid));

        /* Extract categories from MoreLikeThisQuery. */
        final HashSet<String> categoryMap = new HashSet<>();
        message.getCategories().forEach((String category) -> {
            if (!categoryMap.contains(category)) {
                categoryMap.add(category);
            }
        });

        /* Retrieve per-category results and return them. */
        for (String category : categoryMap) {
            final List<StringDoublePair> results = ContinuousRetrievalLogic.retrieve(message.getSegmentId(), category, qconf).stream()
                    .map(score -> new StringDoublePair(score.getSegmentId(), score.getScore()))
                    .sorted(StringDoublePair.COMPARATOR)
                    .limit(MAX_RESULTS)
                    .collect(Collectors.toList());


            /* Fetch the List of SegmentDescriptors and MultimediaObjectDescriptors. */
            final List<SegmentDescriptor> descriptors = this.loadSegments(results.stream().map(s -> s.key).collect(Collectors.toList()));
            final List<MultimediaObjectDescriptor> objects = this.loadObjects(descriptors.stream().map(SegmentDescriptor::getObjectId).collect(Collectors.toList()));

            /* Write partial results to stream. */
            this.write(session, new SegmentQueryResult(uuid, descriptors));
            this.write(session, new ObjectQueryResult(uuid, objects));
            this.write(session, new SimilarityQueryResult(uuid, category, results));
        }

        /* End of Query: Send QueryEnd Message to Client. */
        this.write(session, new QueryEnd(uuid));
    }
}
