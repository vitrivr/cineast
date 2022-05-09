package org.vitrivr.cineast.api.util;

import static org.vitrivr.cineast.core.util.CineastConstants.FEATURE_COLUMN_QUALIFIER;
import static org.vitrivr.cineast.core.util.CineastConstants.GENERIC_ID_COLUMN_QUALIFIER;

import gnu.trove.map.hash.TObjectDoubleHashMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.api.messages.query.QueryStage;
import org.vitrivr.cineast.api.messages.query.QueryTerm;
import org.vitrivr.cineast.api.messages.query.QueryTermType;
import org.vitrivr.cineast.api.messages.query.TemporalQuery;
import org.vitrivr.cineast.api.messages.result.FeaturesAllCategoriesQueryResult;
import org.vitrivr.cineast.api.messages.result.FeaturesByCategoryQueryResult;
import org.vitrivr.cineast.api.messages.result.FeaturesByEntityQueryResult;
import org.vitrivr.cineast.core.config.QueryConfig;
import org.vitrivr.cineast.core.config.ReadableQueryConfig;
import org.vitrivr.cineast.core.data.Pair;
import org.vitrivr.cineast.core.data.StringDoublePair;
import org.vitrivr.cineast.core.data.TemporalObject;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;
import org.vitrivr.cineast.core.data.providers.primitive.StringTypeProvider;
import org.vitrivr.cineast.core.data.query.containers.AbstractQueryTermContainer;
import org.vitrivr.cineast.core.data.score.SegmentScoreElement;
import org.vitrivr.cineast.core.data.tag.Tag;
import org.vitrivr.cineast.core.db.DBSelector;
import org.vitrivr.cineast.core.db.dao.reader.MediaSegmentReader;
import org.vitrivr.cineast.core.db.dao.reader.TagReader;
import org.vitrivr.cineast.core.features.SegmentTags;
import org.vitrivr.cineast.core.temporal.TemporalScoring;
import org.vitrivr.cineast.core.util.math.MathHelper;
import org.vitrivr.cineast.standalone.config.Config;
import org.vitrivr.cineast.standalone.config.RetrievalRuntimeConfig;
import org.vitrivr.cineast.standalone.util.ContinuousRetrievalLogic;

//TODO maybe this should be moved to core?
public class QueryUtil {

  private static final Logger LOGGER = LogManager.getLogger();

  /**
   * Executes a similarity query specified by the list of {@link QueryTerm}s.
   *
   * @param continuousRetrievalLogic The continuous retrieval logic to execute the query.
   * @param terms                    The terms specifying the query.
   * @param config                   The config to use for this query.
   * @return The query results as a map of query term categories to scored segment lists.
   */
  public static HashMap<String, List<StringDoublePair>> findSegmentsSimilar(ContinuousRetrievalLogic continuousRetrievalLogic, List<QueryTerm> terms, QueryConfig config) {
    HashMap<String, List<StringDoublePair>> returnMap = new HashMap<>();

    // Group terms by categories
    var categoryMap = QueryUtil.groupQueryTermsByCategory(terms);

    for (var category : categoryMap.keySet()) {
      var containerList = categoryMap.get(category).stream().map(x -> new Pair<>(x, (ReadableQueryConfig) config)).collect(Collectors.toList());
      var categoryResults = QueryUtil.retrieveCategory(continuousRetrievalLogic, containerList, category);
      returnMap.put(category, categoryResults);
    }

    return returnMap;
  }

  /**
   * Executes a staged similarity query specified by the list of {@link QueryStage}s.
   * <p>
   * Each {@link QueryTerm} category is expected to appear no more than once in the entire query.
   *
   * @param continuousRetrievalLogic The continuous retrieval logic to execute the query.
   * @param stages                   The stages specifying the query.
   * @param config                   The config to use for this query.
   * @return The query results as a map of query term categories to scored segment lists.
   */
  public static HashMap<String, List<StringDoublePair>> findSegmentsSimilarStaged(ContinuousRetrievalLogic continuousRetrievalLogic, List<QueryStage> stages, QueryConfig config) {
    var stageConfig = config.clone();

    var stagedQueryResults = new ArrayList<HashMap<String, List<StringDoublePair>>>();

    for (QueryStage stage : stages) {
      var stageResults = findSegmentsSimilar(continuousRetrievalLogic, stage.terms(), stageConfig);
      stagedQueryResults.add(stageResults);

      var relevantSegments = new HashSet<String>();
      for (var result : stageResults.values()) {
        relevantSegments.addAll(result.stream().map(pair -> pair.key).collect(Collectors.toList()));
      }

      // Return empty results if there are no more results in stage
      if (relevantSegments.isEmpty()) {
        return stageResults;
      }

      stageConfig.setRelevantSegmentIds(relevantSegments);
    }

    return mergeStagedQueryResults(stagedQueryResults);
  }

  /**
   * Executes a temporal similarity query.
   *
   * @param continuousRetrievalLogic The continuous retrieval logic to execute the query.
   * @param query                    The temporal query to execute.
   * @param config                   The config to use for the execution of the query
   * @return The query results as a list of temporal objects.
   */
  public static List<TemporalObject> findSegmentsSimilarTemporal(ContinuousRetrievalLogic continuousRetrievalLogic, TemporalQuery query, QueryConfig config) {
    var stagedResults = query.queries().stream().map(stagedQuery -> findSegmentsSimilarStaged(continuousRetrievalLogic, stagedQuery.stages(), config)).collect(Collectors.toList());

    // TODO: New MediaSegmentReader for every request like FindSegmentByIdPostHandler or one persistent on per endpoint like AbstractQueryMessageHandler?
    try (var segmentReader = new MediaSegmentReader(Config.sharedConfig().getDatabase().getSelectorSupplier().get())) {
      var segmentIds = stagedResults.stream().flatMap(resultsMap -> resultsMap.values().stream().flatMap(pairs -> pairs.stream().map(pair -> pair.key))).distinct().collect(Collectors.toList());

      var segmentDescriptors = segmentReader.lookUpSegments(segmentIds, config.getQueryId());
      var stagedQueryResults = stagedResults.stream().map(resultsMap -> resultsMap.values().stream().flatMap(Collection::stream).collect(Collectors.toList())).collect(Collectors.toList());

      return TemporalScoring.score(segmentDescriptors, stagedQueryResults, query.getTimeDistances(), query.getMaxLength());
    }
  }

  /**
   * Merges staged query results into a single result map.
   *
   * @param stagedQueryResults List of staged query results by stage. The results are expected to be in order of the stages.
   * @return Map of merged results mapping categories to their results filtered to the final set of segments.
   */
  public static HashMap<String, List<StringDoublePair>> mergeStagedQueryResults(ArrayList<HashMap<String, List<StringDoublePair>>> stagedQueryResults) {
    var results = new HashMap<String, List<StringDoublePair>>();

    var relevantSegments = new HashSet<String>();
    for (var result : stagedQueryResults.get(stagedQueryResults.size() - 1).values()) {
      relevantSegments.addAll(result.stream().map(pair -> pair.key).collect(Collectors.toList()));
    }

    for (var stageResults : stagedQueryResults) {
      for (var category : stageResults.keySet()) {
        if (results.containsKey(category)) {
          LOGGER.warn("Staged query contained the category \"{}\" multiple times.", category);
        }
        var filteredResults = stageResults.get(category).stream().filter(pair -> relevantSegments.contains(pair.key)).collect(Collectors.toList());
        results.put(category, filteredResults);
      }
    }

    return results;
  }

  public static HashMap<String, ArrayList<AbstractQueryTermContainer>> groupQueryTermsByCategory(List<QueryTerm> queryTerms) {
    HashMap<String, ArrayList<AbstractQueryTermContainer>> categoryMap = new HashMap<>();
    for (QueryTerm term : queryTerms) {
      if (term.categories() == null) {
        LOGGER.warn("Encountered query term without categories. Ignoring: {}", term.toString());
        continue;
      }
      var qt = QueryTermType.createFromQueryTerm(term);
      term.categories().forEach((String category) -> {
        if (!categoryMap.containsKey(category)) {
          categoryMap.put(category, new ArrayList<>());
        }
        categoryMap.get(category).add(qt);
      });
    }
    return categoryMap;
  }

  public static List<StringDoublePair> retrieveCategory(ContinuousRetrievalLogic continuousRetrievalLogic, List<Pair<AbstractQueryTermContainer, ReadableQueryConfig>> queryContainers, String category) {
    TObjectDoubleHashMap<String> scoreBySegmentId = new TObjectDoubleHashMap<>();
    for (Pair<AbstractQueryTermContainer, ReadableQueryConfig> pair : queryContainers) {

      if (pair == null) {
        continue;
      }

      AbstractQueryTermContainer qc = pair.first;
      ReadableQueryConfig qconf = pair.second;

      float weight = MathHelper.limit(qc.getWeight(), -1f, 1f);

      retrieveAndWeight(continuousRetrievalLogic, category, scoreBySegmentId, qc, qconf, weight);

    }
    final List<StringDoublePair> list = new ArrayList<>(scoreBySegmentId.size());
    scoreBySegmentId.forEachEntry((segmentId, score) -> {
      if (score > 0) {
        list.add(new StringDoublePair(segmentId, score));
      }
      return true;
    });

    list.sort(StringDoublePair.COMPARATOR);

    // FIXME: Using an arbitrary query config to limit results is prone to errors
    final int MAX_RESULTS = queryContainers.get(0).second.getMaxResults().orElse(Config.sharedConfig().getRetriever().getMaxResults());
    List<StringDoublePair> resultList = list;
    if (list.size() > MAX_RESULTS) {
      resultList = resultList.subList(0, MAX_RESULTS);
    }
    return resultList;
  }

  public static List<StringDoublePair> retrieve(ContinuousRetrievalLogic continuousRetrievalLogic, AbstractQueryTermContainer queryTermContainer, ReadableQueryConfig config, String category) {
    float weight = MathHelper.limit(queryTermContainer.getWeight(), -1f, 1f);
    TObjectDoubleHashMap<String> scoreBySegmentId = new TObjectDoubleHashMap<>();

    retrieveAndWeight(continuousRetrievalLogic, category, scoreBySegmentId, queryTermContainer, config, weight);

    final List<StringDoublePair> list = new ArrayList<>(scoreBySegmentId.size());
    scoreBySegmentId.forEachEntry((segmentId, score) -> {
      if (score > 0) {
        list.add(new StringDoublePair(segmentId, score));
      }
      return true;
    });

    return list;
  }

  private static void retrieveAndWeight(ContinuousRetrievalLogic continuousRetrievalLogic, String category, TObjectDoubleHashMap<String> scoreBySegmentId, AbstractQueryTermContainer qc, ReadableQueryConfig qconf, float weight) {
    List<SegmentScoreElement> scoreResults;
    if (qc.hasId()) {
      scoreResults = continuousRetrievalLogic.retrieve(qc.getId(), category, qconf);
    } else {
      scoreResults = continuousRetrievalLogic.retrieve(qc, category, qconf);
    }

    for (SegmentScoreElement element : scoreResults) {
      String segmentId = element.getSegmentId();
      double score = element.getScore();
      if (Double.isInfinite(score) || Double.isNaN(score)) {
        continue;
      }
      double weightedScore = score * weight;
      scoreBySegmentId.adjustOrPutValue(segmentId, weightedScore, weightedScore);
    }
  }

  /**
   * Retrieves all tag ids belong to certain elements. duplicates are a feature
   *
   * @param ids element ids for which the tags table is to be searched.
   * @return a list of {@link Tag#getId()} ids
   */
  public static List<String> retrieveTagIDs(List<String> ids) {
    List<String> _return = new ArrayList<>();
    DBSelector selector = Config.sharedConfig().getDatabase().getSelectorSupplier().get();
    selector.open(SegmentTags.SEGMENT_TAGS_TABLE_NAME);
    try {
      List<Map<String, PrimitiveTypeProvider>> rows = selector.getRows(GENERIC_ID_COLUMN_QUALIFIER, ids);
      rows.forEach(row -> _return.add(row.get(SegmentTags.TAG_ID_QUALIFIER).getString()));
      return _return;
    } catch (Exception e) {
      LOGGER.error("Exception while looking up tags", e);
      return _return;
    }
  }

  /**
   * Simply assumes all elements retrieved by {@link #retrieveFeaturesForIDByCategory(String, String)} are strings.
   */
  public static List<String> retrieveTextFeatureByID(String id, String category) {
    return retrieveFeaturesForIDByCategory(id, category).stream().map(Object::toString).collect(Collectors.toList());
  }

  /**
   * Retrieves all features for a given id (i.e. segment, object id) and a given category.
   */
  public static List<Object> retrieveFeaturesForIDByCategory(String id, String category) {
    final RetrievalRuntimeConfig retrievalRuntimeConfig = Config.sharedConfig().getRetriever();
    final DBSelector selector = Config.sharedConfig().getDatabase().getSelectorSupplier().get();
    List<Object> _return = new ArrayList<>();
    retrievalRuntimeConfig.getRetrieversByCategory(category).forEachKey(retriever -> {
      retriever.getTableNames().forEach(tableName -> {
        selector.open(tableName);
        List<Map<String, PrimitiveTypeProvider>> rows = selector.getRows(GENERIC_ID_COLUMN_QUALIFIER, new StringTypeProvider(id));
        rows.stream().map(row -> row.get(FEATURE_COLUMN_QUALIFIER).toObject()).forEach(_return::add);
      });
      return true; // Return value false would break the foreEachKey
    });
    return _return;
  }

  /**
   * Returns all tags for a given list of tagsids
   */
  public static List<Tag> resolveTagsById(List<String> tagIds) {
    DBSelector selector = Config.sharedConfig().getDatabase().getSelectorSupplier().get();
    TagReader tagReader = new TagReader(selector);
    return tagReader.getTagsById(tagIds);
  }

  public static FeaturesAllCategoriesQueryResult retrieveFeaturesForAllCategories(String id) {
    Map<String, Object[]> features = new HashMap<>();
    final RetrievalRuntimeConfig retrievalRuntimeConfig = Config.sharedConfig().getRetriever();

    retrievalRuntimeConfig.getRetrieverCategories().forEach(cat -> {
      List<Object> _features = retrieveFeaturesForIDByCategory(id, cat);
      if (_features.size() == 0) {
        return;
      }

      if (_features.get(0) != null) {
        features.put(cat, _features.toArray());
      }
    });

    return new FeaturesAllCategoriesQueryResult("", features, id);
  }

  private static ArrayList<HashMap<String, Object>> getFeaturesFromEntity(String entityName, List<String> ids) {
    final DBSelector selector = Config.sharedConfig().getDatabase().getSelectorSupplier().get();

    ArrayList<HashMap<String, Object>> currList = new ArrayList<>();
    selector.open(entityName);

    List<Map<String, PrimitiveTypeProvider>> rows;

    if (ids == null || ids.isEmpty()) {
      rows = selector.getAll();
    } else {
      rows = selector.getRows(GENERIC_ID_COLUMN_QUALIFIER, ids);
    }

    for (Map<String, PrimitiveTypeProvider> row : rows) {
      HashMap<String, Object> tempMap = new HashMap<>();

      tempMap.put(FEATURE_COLUMN_QUALIFIER, row.get(FEATURE_COLUMN_QUALIFIER).toObject());
      tempMap.put(GENERIC_ID_COLUMN_QUALIFIER, row.get(GENERIC_ID_COLUMN_QUALIFIER).toObject());

      currList.add(tempMap);
    }

    return currList;
  }

  private static Map<String, ArrayList<HashMap<String, Object>>> getFeaturesForCategory(String category, List<String> ids) {
    final RetrievalRuntimeConfig retrievalRuntimeConfig = Config.sharedConfig().getRetriever();
    Map<String, ArrayList<HashMap<String, Object>>> _return = new HashMap<>();

    retrievalRuntimeConfig.getRetrieversByCategory(category).forEach(retriever -> {
      retriever.getTableNames().forEach(tableName -> _return.put(tableName, getFeaturesFromEntity(tableName, ids)));
      return true;
    });

    return _return;
  }

  public static FeaturesByEntityQueryResult retrieveFeaturesForEntity(String entityName, List<String> ids) {
    ArrayList<HashMap<String, Object>> features = getFeaturesFromEntity(entityName, ids);
    return new FeaturesByEntityQueryResult("", features, entityName);
  }

  public static FeaturesByCategoryQueryResult retrieveFeaturesForCategory(String category, List<String> ids) {
    Map<String, ArrayList<HashMap<String, Object>>> features = getFeaturesForCategory(category, ids);
    return new FeaturesByCategoryQueryResult("", features, category);
  }

}
