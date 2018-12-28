package org.vitrivr.cineast.api.websocket.handlers.queries;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jetty.websocket.api.Session;
import org.vitrivr.cineast.core.config.Config;
import org.vitrivr.cineast.core.config.QueryConfig;
import org.vitrivr.cineast.core.data.StringDoublePair;
import org.vitrivr.cineast.core.data.entities.MediaObjectDescriptor;
import org.vitrivr.cineast.core.data.entities.MediaSegmentDescriptor;
import org.vitrivr.cineast.core.data.messages.query.MoreLikeThisQuery;
import org.vitrivr.cineast.core.data.messages.result.*;
import org.vitrivr.cineast.core.data.messages.result.MediaSegmentQueryResult;
import org.vitrivr.cineast.core.util.ContinuousRetrievalLogic;

/**
 * @author rgasser
 * @version 1.0
 * @created 27.04.17
 */
public class MoreLikeThisQueryMessageHandler extends AbstractQueryMessageHandler<MoreLikeThisQuery> {
    /**
     * Executes a {@link MoreLikeThisQuery} message. Performs a similarity query based on the segmentId specified
     * the {@link MoreLikeThisQuery} object.
     *
     * @param session WebSocket session the invocation is associated with.
     * @param qconf The {@link QueryConfig} that contains additional specifications.
     * @param message Instance of {@link MoreLikeThisQuery}
     */
    @Override
    public void execute(Session session, QueryConfig qconf, MoreLikeThisQuery message) throws Exception {
        /* Extract categories from MoreLikeThisQuery. */
        final String queryId = qconf.getQueryId().toString();
        final HashSet<String> categoryMap = new HashSet<>(message.getCategories());

        /* Retrieve per-category results and return them. */
        for (String category : categoryMap) {
            final List<StringDoublePair> results = ContinuousRetrievalLogic.retrieve(message.getSegmentId(), category, qconf).stream()
                    .map(score -> new StringDoublePair(score.getSegmentId(), score.getScore()))
                    .sorted(StringDoublePair.COMPARATOR)
                    .limit(Config.sharedConfig().getRetriever().getMaxResults())
                    .collect(Collectors.toList());

            /* Finalize and submit per-category results. */
            this.finalizeAndSubmitResults(session, queryId, category, results);
        }
    }

    /**
     * Fetches and submits all the data (e.g. {@link MediaObjectDescriptor}, {@link MediaSegmentDescriptor}) associated with the
     * raw results produced by a similarity search in a specific category.
     *
     * @param session The {@link Session} object used to transmit the results.
     * @param queryId ID of the running query.
     * @param category Name of the query category.
     * @param raw List of raw per-category results (segmentId -> score).
     */
    private void finalizeAndSubmitResults(Session session, String queryId, String category, List<StringDoublePair> raw) {
        final int stride = 1000;
        for (int i=0; i<Math.floorDiv(raw.size(), stride)+1; i++) {
            final List<StringDoublePair> sub = raw.subList(i*stride, Math.min((i+1)*stride, raw.size()));
            final List<String> segmentIds = sub.stream().map(s -> s.key).collect(Collectors.toList());

            /* Load segment & object information. */
            final List<MediaSegmentDescriptor> segments = this.loadSegments(segmentIds);
            final List<String> objectIds = segments.stream().map(MediaSegmentDescriptor::getObjectId).collect(Collectors.toList());
            final List<MediaObjectDescriptor> objects = this.loadObjects(objectIds);
            if (segments.size() == 0 || objects.size() == 0) {
                continue;
            }

            /* Write segments, objects and similarity search data to stream. */
            this.write(session, new MediaObjectQueryResult(queryId, objects));
            this.write(session, new MediaSegmentQueryResult(queryId, segments));
            this.write(session, new SimilarityQueryResult(queryId, category, sub));

            /* Load and transmit segment & object metadata. */
            this.loadAndWriteSegmentMetadata(session, queryId, segmentIds);
            this.loadAndWriteObjectMetadata(session, queryId, objectIds);
        }
    }
}
