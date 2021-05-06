package org.vitrivr.cineast.api.websocket.handlers.queries;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.websocket.api.Session;
import org.vitrivr.cineast.api.messages.query.QueryStage;
import org.vitrivr.cineast.api.messages.query.QueryTerm;
import org.vitrivr.cineast.api.messages.query.StagedSimilarityQuery;
import org.vitrivr.cineast.api.messages.query.TemporalQueryV2;
import org.vitrivr.cineast.core.config.QueryConfig;
import org.vitrivr.cineast.core.data.StringDoublePair;
import org.vitrivr.cineast.core.data.TemporalObject;
import org.vitrivr.cineast.core.data.entities.MediaObjectDescriptor;
import org.vitrivr.cineast.core.data.entities.MediaSegmentDescriptor;
import org.vitrivr.cineast.core.data.query.containers.QueryContainer;
import org.vitrivr.cineast.core.data.score.SegmentScoreElement;
import org.vitrivr.cineast.core.temporal.TemporalScoring;
import org.vitrivr.cineast.standalone.config.Config;
import org.vitrivr.cineast.standalone.util.ContinuousRetrievalLogic;

public class TemporalQueryMessageHandlerV2 extends AbstractQueryMessageHandler<TemporalQueryV2> {

  private static final Logger LOGGER = LogManager.getLogger();

  private final ContinuousRetrievalLogic continuousRetrievalLogic;

  public TemporalQueryMessageHandlerV2(ContinuousRetrievalLogic retrievalLogic) {
    this.continuousRetrievalLogic = retrievalLogic;
  }

  @Override
  public void execute(Session session, QueryConfig qconf, TemporalQueryV2 message,
      Set<String> segmentIdsForWhichMetadataIsFetched,
      Set<String> objectIdsForWhichMetadataIsFetched) throws Exception {

    final String uuid = qconf.getQueryId().toString();
    final int max = qconf.getMaxResults()
        .orElse(Config.sharedConfig().getRetriever().getMaxResults());
    qconf.setMaxResults(max);
    final int resultsPerModule =
        qconf.getRawResultsPerModule() == -1 ? Config.sharedConfig().getRetriever()
            .getMaxResultsPerModule() : qconf.getResultsPerModule();
    qconf.setResultsPerModule(resultsPerModule);

    List<Thread> metadataRetrievalThreads = new ArrayList<>();
    List<List<StringDoublePair>> containerResults = new ArrayList<>();
    Set<MediaSegmentDescriptor> segments = new HashSet<>();
    Set<MediaObjectDescriptor> objects = new HashSet<>();

    for (int containerIdx = 0; containerIdx < message.getQueries().size(); containerIdx++) {
      StagedSimilarityQuery stagedSimilarityQuery = message.getQueries().get(containerIdx);
      QueryConfig stageQConf = QueryConfig.clone(qconf);

      HashSet<String> relevantSegments = new HashSet<>();

      List<Map<String, List<StringDoublePair>>> cache = new ArrayList<>();
      List<StringDoublePair> stageResults = new ArrayList<>();

      for (int stageIndex = 0; stageIndex < stagedSimilarityQuery.stages.size(); stageIndex++) {
        cache.add(stageIndex, new HashMap<>());

        QueryStage stage = stagedSimilarityQuery.stages.get(stageIndex);

        for (int i = 0; i < stage.terms.size(); i++) {
          QueryTerm qt = stage.terms.get(i);

          if (qt == null) {
            LOGGER.warn("QueryTerm was null for stage {}", stage);
            return;
          }
          QueryContainer qc = qt.toContainer();
          if (qc == null) {
            LOGGER.warn(
                "Likely an empty query, as it could not be converted to a query container. Ignoring it");
            return;
          }
          qc.setContainerId(containerIdx);

          for (String category : qt.getCategories()) {
            List<SegmentScoreElement> scores = continuousRetrievalLogic
                .retrieve(qc, category, stageQConf);

            final List<StringDoublePair> results = scores.stream()
                .map(elem -> new StringDoublePair(elem.getSegmentId(), elem.getScore()))
                .filter(p -> p.value > 0d)
                .collect(Collectors.toList());

            if (results.isEmpty()) {
              LOGGER.warn(
                  "No results found for category {} and qt {} in stage with id {}. Full compoment: {}",
                  category, qt.getType(), containerIdx, stage);
            }
            if (cache.get(stageIndex).containsKey(category)) {
              LOGGER.error(
                  "Category {} was used twice in stage {}. This erases the results of the previous category... ",
                  category, stageIndex);
            }
            cache.get(stageIndex).put(category, results);
            results.forEach(res -> relevantSegments.add(res.key));

            List<String> stageSegmentIds = results.stream().map(el -> el.key)
                .collect(Collectors.toList());
            List<MediaSegmentDescriptor> stageSegments = this.loadSegments(stageSegmentIds);
            List<String> stageObjectIds = stageSegments.stream()
                .map(MediaSegmentDescriptor::getObjectId).collect(Collectors.toList());
            List<MediaObjectDescriptor> stageObjects = this.loadObjects(stageObjectIds);

            segments.addAll(stageSegments);
            objects.addAll(stageObjects);
            stageResults.addAll(results);
            this.submitSegmentAndObjectDescriptors(session, uuid, stageObjects, stageSegments);

            List<StringDoublePair> limitedResults = results.stream()
                .sorted(StringDoublePair.COMPARATOR)
                .limit(max)
                .collect(Collectors.toList());
            this.finalizeAndSubmitResults(session, uuid, category, qc.getContainerId(),
                limitedResults);
            List<Thread> _threads = this
                .submitMetadata(session, uuid, stageSegmentIds, stageObjectIds,
                    segmentIdsForWhichMetadataIsFetched, objectIdsForWhichMetadataIsFetched);
            metadataRetrievalThreads.addAll(_threads);
          }
        }

        if (relevantSegments.size() == 0) {
          LOGGER.warn("No relevant segments anymore, aborting staged querying");
          stageQConf.setRelevantSegmentIds(relevantSegments);
          break;
        }

        stageQConf.setRelevantSegmentIds(relevantSegments);
        relevantSegments.clear();
      }

      containerResults.add(containerIdx, stageResults);
    }

    for (Thread thread : metadataRetrievalThreads) {
      thread.join();
    }

    Map<String, MediaObjectDescriptor> objectMap = objects.stream()
        .collect(Collectors.toMap(MediaObjectDescriptor::getObjectId, x -> x));
    Map<String, MediaSegmentDescriptor> segmentMap = segments.stream()
        .collect(Collectors.toMap(MediaSegmentDescriptor::getSegmentId, x -> x));
    TemporalScoring temporalScoring = new TemporalScoring(objectMap, segmentMap, containerResults,
        qconf, message.getTimeDistances(), message.getMaxLength());

    List<TemporalObject> results = temporalScoring.score();

    this.finalizeAndSubmitTemporalResults(session, uuid, results);
  }

}
