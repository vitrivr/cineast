package org.vitrivr.cineast.api.websocket.handlers.queries;

import com.fasterxml.jackson.databind.ObjectMapper;
import gnu.trove.map.hash.TObjectDoubleHashMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.websocket.api.Session;
import org.vitrivr.cineast.api.messages.query.QueryTerm;
import org.vitrivr.cineast.api.messages.result.ExtendedSimilarityQueryResult;
import org.vitrivr.cineast.api.messages.result.MediaObjectQueryResult;
import org.vitrivr.cineast.api.messages.result.SimilarityQueryResult;
import org.vitrivr.cineast.core.config.QueryConfig;
import org.vitrivr.cineast.core.data.Pair;
import org.vitrivr.cineast.core.data.StringDoublePair;
import org.vitrivr.cineast.core.data.StringDoubleTriple;
import org.vitrivr.cineast.core.data.entities.MediaObjectDescriptor;
import org.vitrivr.cineast.core.data.entities.MediaSegmentDescriptor;
import org.vitrivr.cineast.api.messages.query.QueryComponent;
import org.vitrivr.cineast.api.messages.query.SimilarityQuery;
import org.vitrivr.cineast.api.messages.result.MediaSegmentQueryResult;
import org.vitrivr.cineast.core.data.query.containers.QueryContainer;
import org.vitrivr.cineast.core.data.score.ScoreElement;
import org.vitrivr.cineast.core.data.score.SegmentScoreElement;
import org.vitrivr.cineast.standalone.config.Config;
import org.vitrivr.cineast.standalone.util.ContinuousRetrievalLogic;

import java.util.ArrayList;
import java.util.Collection;
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
     * Executes a {@link SimilarityQuery}. Performs the similarity query based on the {@link QueryContainer}
     * objects provided in the {@link SimilarityQuery}.
     *
     * @param session WebSocket session the invocation is associated with.
     * @param qconf   The {@link QueryConfig} that contains additional specifications.
     * @param message Instance of {@link SimilarityQuery}
     */
    @Override
    public void execute(Session session, QueryConfig qconf, SimilarityQuery message) throws Exception {
        /* Prepare QueryConfig (so as to obtain a QueryId). */
        final String uuid = qconf.getQueryId().toString();

        /* Prepare map that maps QueryTerms (as QueryContainer, ready for retrieval) and their associated categories */
        final HashMap<QueryContainer, List<String>> containerCategoryMap = QueryComponent.toContainerMap(message.getComponents());

        /* Execute similarity queries for all QueryContainer -> Category combinations in the map */
        for (QueryContainer qc : containerCategoryMap.keySet()) {
            for (String category : containerCategoryMap.get(qc)) {
                /* Merge partial results with score-map */
                List<SegmentScoreElement> scores = continuousRetrievalLogic.retrieve(qc, category, qconf);
                /* Transform raw results into list of StringDoublePairs (segmentId -> score) */
                final int max = qconf.getMaxResults().orElse(Config.sharedConfig().getRetriever().getMaxResults());
                final List<StringDoublePair> results = scores.stream()
                        .map(elem -> new StringDoublePair(elem.getSegmentId(), elem.getScore()))
                        .filter(p -> p.value > 0d)
                        .sorted(StringDoublePair.COMPARATOR)
                        .limit(max)
                        .collect(Collectors.toList());

                /* Finalize and submit per-container results */
                this.finalizeAndSubmitResults(session, uuid, category, qc.getContainerId(),results);
            }
        }
    }


    /**
     * Fetches and submits all the data (e.g. {@link MediaObjectDescriptor}, {@link MediaSegmentDescriptor}) associated with the
     * raw results produced by a similarity search in a specific category.
     *q
     * @param session  The {@link Session} object used to transmit the results.
     * @param queryId  ID of the running query.
     * @param category Name of the query category.
     * @param raw      List of raw per-category results (segmentId -> score).
     */
    private void finalizeAndSubmitResults(Session session, String queryId, String category, String containerId, List<StringDoublePair> raw) {
        final int stride = 1000;
        for (int i = 0; i < Math.floorDiv(raw.size(), stride) + 1; i++) {
            final List<StringDoublePair> sub = raw.subList(i * stride, Math.min((i + 1) * stride, raw.size()));
            final List<String> segmentIds = sub.stream().map(s -> s.key).collect(Collectors.toList());

            /* Load segment & object information. */
            final List<MediaSegmentDescriptor> segments = this.loadSegments(segmentIds);
            final List<String> objectIds = segments.stream().map(MediaSegmentDescriptor::getObjectId).collect(Collectors.toList());
            final List<MediaObjectDescriptor> objects = this.loadObjects(objectIds);
            if (segments.isEmpty() || objects.isEmpty()) {
                continue;
            }

            /* Write segments, objects and similarity search data to stream. */
            this.write(session, new MediaObjectQueryResult(queryId, objects));
            this.write(session, new MediaSegmentQueryResult(queryId, segments));
            this.write(session, new SimilarityQueryResult.ContainerSimilarityQueryResult(queryId, category,containerId, sub));

            /* Load and transmit segment & object metadata. */
            this.loadAndWriteSegmentMetadata(session, queryId, segmentIds);
            this.loadAndWriteObjectMetadata(session, queryId, objectIds);
        }
    }
}
