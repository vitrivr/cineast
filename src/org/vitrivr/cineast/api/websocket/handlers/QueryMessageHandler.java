package org.vitrivr.cineast.api.websocket.handlers;

import java.util.*;
import java.util.stream.Collectors;

import org.eclipse.jetty.websocket.api.Session;
import org.vitrivr.cineast.api.websocket.handlers.abstracts.StatelessWebsocketMessageHandler;
import org.vitrivr.cineast.core.config.Config;
import org.vitrivr.cineast.core.config.QueryConfig;
import org.vitrivr.cineast.core.data.StringDoublePair;
import org.vitrivr.cineast.core.data.entities.MediaObjectDescriptor;
import org.vitrivr.cineast.core.data.entities.MediaSegmentMetadataDescriptor;
import org.vitrivr.cineast.core.data.entities.MediaSegmentDescriptor;
import org.vitrivr.cineast.core.data.messages.query.QueryComponent;
import org.vitrivr.cineast.core.data.messages.query.QueryTerm;
import org.vitrivr.cineast.core.data.messages.query.SimilarityQuery;
import org.vitrivr.cineast.core.data.messages.result.*;
import org.vitrivr.cineast.core.data.query.containers.QueryContainer;
import org.vitrivr.cineast.core.data.score.SegmentScoreElement;
import org.vitrivr.cineast.core.db.dao.reader.MediaObjectReader;
import org.vitrivr.cineast.core.db.dao.reader.MediaSegmentReader;
import org.vitrivr.cineast.core.db.dao.reader.MediaSegmentMetadataReader;
import org.vitrivr.cineast.core.util.ContinuousRetrievalLogic;

import gnu.trove.map.hash.TObjectDoubleHashMap;

/**
 * @author rgasser
 * @version 1.0
 * @created 12.01.17
 */
public class QueryMessageHandler extends StatelessWebsocketMessageHandler<SimilarityQuery> {
  /**
   *
   * @param session
   * @param message
   */
  @Override
  public void handle(Session session, SimilarityQuery message) {
    /* Prepare QueryConfig (so as to obtain a QueryId). */
    final QueryConfig qconf = QueryConfig.newQueryConfigFromOther(Config.sharedConfig().getQuery());

    /*
     * Begin of Query: Send QueryStart Message to Client.
     */
    final QueryStart startMarker = new QueryStart(qconf.getQueryId().toString());
    this.write(session, startMarker);


    // TODO: Remove code duplication shared with FindObjectSimilarActionHandler
    /*
     * Prepare map that maps categories to QueryTerm components.
     */
    HashMap<String, ArrayList<QueryContainer>> categoryMap = new HashMap<>();
    for (QueryComponent component : message.getComponents()) {
      for (QueryTerm term : component.getTerms()) {
        if (term.getCategories() == null) {
          continue;
        }
        term.getCategories().forEach((String category) -> {
          if (!categoryMap.containsKey(category)) {
            categoryMap.put(category, new ArrayList<QueryContainer>());
          }
          categoryMap.get(category).add(term.toContainer());
        });
      }
    }

    List<SegmentScoreElement> result;
    for (String category : categoryMap.keySet()) {
      final TObjectDoubleHashMap<String> map = new TObjectDoubleHashMap<>();
      for (QueryContainer qc : categoryMap.get(category)) {

        float weight = qc.getWeight() > 0f ? 1f : -1f; //TODO better normalisation

        if (qc.hasId()) {
          result = ContinuousRetrievalLogic.retrieve(qc.getId(), category, qconf);
        } else {
          result = ContinuousRetrievalLogic.retrieve(qc, category, qconf);
        }

        for (SegmentScoreElement element : result) {
          String segmentId = element.getSegmentId();
          double score = element.getScore();
          if (Double.isInfinite(score) || Double.isNaN(score)) {
            continue;
          }
          double weightedScore = score * weight;
          map.adjustOrPutValue(segmentId, weightedScore, weightedScore);
        }

        /* Transform raw results into list of StringDoublePair's (segmentId -> score). */
        final int max = Config.sharedConfig().getRetriever().getMaxResults();
        final List<StringDoublePair> results = map.keySet().stream()
                .map(key -> new StringDoublePair(key, map.get(key)))
                .filter(p -> p.value > 0.0)
                .sorted(StringDoublePair.COMPARATOR)
                .limit(max)
                .collect(Collectors.toList());

        /* Finalize and submit per-category results. */
        this.finalizeAndSubmitResults(session, startMarker.getQueryId(), category, results);
      }
    }

    /* End of Query: Send QueryEnd Message to Client. */
    this.write(session, new QueryEnd(startMarker.getQueryId()));
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
    final List<String> segmentIds = raw.stream().map(s -> s.key).collect(Collectors.toList());

    /* Load segment information. */
    final List<MediaSegmentDescriptor> segments = this.loadSegments(segmentIds);
    final List<MediaSegmentMetadataDescriptor> segmentMetadata = this.loadSegmentMetadata(segmentIds);

    /* Fetch object IDs. */
    final List<String> objectIds = segments.stream().map(MediaSegmentDescriptor::getObjectId).collect(Collectors.toList());
    final List<MediaObjectDescriptor> objects = this.loadObjects(objectIds);

    this.write(session, new MediaObjectQueryResult(queryId, objects));
    this.write(session, new MediaSegmentQueryResult(queryId, segments));
    this.write(session, new SimilarityQueryResult(queryId, category, raw));
    this.write(session, new MediaSegmentMetadataQueryResult(queryId, segmentMetadata));
  }

  /**
   * Fetches a list of {@link MediaSegmentDescriptor}s using the {@link MediaSegmentReader} class.
   *
   * @param segmentIds List of segment ID's for which to fetch {@link MediaSegmentDescriptor}s.
   * @return List of {@link MediaSegmentDescriptor}. Number of entries can be smaller then the number of IDs provided by the caller.
   */
  private List<MediaSegmentDescriptor> loadSegments(final List<String> segmentIds) {
    try (final MediaSegmentReader sl = new MediaSegmentReader()) {
      final Map<String, MediaSegmentDescriptor> map = sl.lookUpSegments(segmentIds);
      return segmentIds.stream().map(map::get).filter(Objects::nonNull).collect(Collectors.toList());
    }
  }

  /**
   * Fetches a list of {@link MediaSegmentMetadataDescriptor}s using the {@link MediaSegmentMetadataReader} class.
   *
   * @param segmentIds List of segment ID's for which to fetch {@link MediaSegmentMetadataReader}s.
   * @return List of {@link MediaSegmentMetadataReader}. Number of entries can be smaller then the number of IDs provided by the caller.
   */
  private List<MediaSegmentMetadataDescriptor> loadSegmentMetadata(List<String> segmentIds) {
    try (final MediaSegmentMetadataReader reader = new MediaSegmentMetadataReader()) {
      return reader.lookupMultimediaMetadata(segmentIds);
    }
  }

  /**
   * Fetches a list of {@link MediaObjectDescriptor}s using the {@link MediaObjectDescriptor} class.
   *
   * @param objectIds List of object ID's for which to fetch {@link MediaObjectDescriptor}s.
   * @return List of {@link MediaObjectDescriptor}. Number of entries can be smaller then the number of IDs provided by the caller.
   */
  private List<MediaObjectDescriptor> loadObjects(List<String> objectIds) {
    try (final MediaObjectReader vl = new MediaObjectReader()) {
      final Map<String, MediaObjectDescriptor> map = vl.lookUpObjects(objectIds);
      return objectIds.stream().map(map::get).filter(Objects::nonNull).collect(Collectors.toList());
    }
  }
}
