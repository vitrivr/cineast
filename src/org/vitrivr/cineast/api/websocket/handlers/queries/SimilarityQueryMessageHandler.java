package org.vitrivr.cineast.api.websocket.handlers.queries;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jetty.websocket.api.Session;
import org.vitrivr.cineast.core.config.Config;
import org.vitrivr.cineast.core.config.QueryConfig;
import org.vitrivr.cineast.core.data.StringDoublePair;
import org.vitrivr.cineast.core.data.entities.MediaObjectDescriptor;
import org.vitrivr.cineast.core.data.entities.MediaSegmentDescriptor;
import org.vitrivr.cineast.core.data.entities.MediaSegmentMetadataDescriptor;
import org.vitrivr.cineast.core.data.messages.query.QueryComponent;
import org.vitrivr.cineast.core.data.messages.query.SimilarityQuery;
import org.vitrivr.cineast.core.data.messages.result.*;
import org.vitrivr.cineast.core.data.messages.result.MediaSegmentQueryResult;
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
        final QueryConfig qconf = (message.getQueryConfig() == null) ? QueryConfig.newQueryConfigFromOther(Config.sharedConfig().getQuery()) : message.getQueryConfig();
        final String uuid = qconf.getQueryId().toString();

        /* Begin of Query: Send QueryStart Message to Client. */
        this.write(session, new QueryStart(uuid));

        try {
            /*  Prepare map that maps category  to QueryTerm components. */
            final HashMap<String, ArrayList<QueryContainer>> categoryMap = QueryComponent.toCategoryMap(message.getComponents());

            /*  Execute similarity queries for all Category -> QueryContainer combinations in the map. */
            for (String category : categoryMap.keySet()) {
                final TObjectDoubleHashMap<String> map = new TObjectDoubleHashMap<>();
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

                    /* Sort and limit the result set. */
                    final List<StringDoublePair> results = list.stream()
                            .sorted(StringDoublePair.COMPARATOR)
                            .limit(Config.sharedConfig().getRetriever().getMaxResults())
                            .collect(Collectors.toList());
                    final List<String> segmentIds = results.stream().map(s -> s.key)
                            .collect(Collectors.toList());

                    /* Fetch the List of SegmentDescriptors and MultimediaObjectDescriptors. */
                    final List<MediaSegmentDescriptor> segments = this.loadSegments(segmentIds);
                    final List<MediaObjectDescriptor> objects = this.loadObjects(segments.stream().map(MediaSegmentDescriptor::getObjectId).collect(Collectors.toList()));
                    final List<MediaSegmentMetadataDescriptor> segmentMetadata = this.loadSegmentMetadata(segmentIds);

                    /* Write partial results to stream. */
                    this.write(session, new MediaSegmentQueryResult(uuid, segments));
                    this.write(session, new MediaObjectQueryResult(uuid, objects));
                    this.write(session, new SimilarityQueryResult(uuid, category, results));
                    this.write(session, new MediaSegmentMetadataQueryResult(uuid, segmentMetadata));
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
