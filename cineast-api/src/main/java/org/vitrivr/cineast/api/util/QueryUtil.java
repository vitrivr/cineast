package org.vitrivr.cineast.api.util;

import static org.vitrivr.cineast.core.util.CineastConstants.FEATURE_COLUMN_QUALIFIER;
import static org.vitrivr.cineast.core.util.CineastConstants.GENERIC_ID_COLUMN_QUALIFIER;

import gnu.trove.map.hash.TObjectDoubleHashMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.vitrivr.cineast.api.messages.query.QueryComponent;
import org.vitrivr.cineast.api.messages.query.QueryTerm;
import org.vitrivr.cineast.api.messages.result.AllFeaturesByCategoryQueryResult;
import org.vitrivr.cineast.api.messages.result.FeaturesAllCategoriesQueryResult;
import org.vitrivr.cineast.core.config.ReadableQueryConfig;
import org.vitrivr.cineast.core.data.Pair;
import org.vitrivr.cineast.core.data.StringDoublePair;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;
import org.vitrivr.cineast.core.data.providers.primitive.StringTypeProvider;
import org.vitrivr.cineast.core.data.query.containers.QueryContainer;
import org.vitrivr.cineast.core.data.score.SegmentScoreElement;
import org.vitrivr.cineast.core.data.tag.Tag;
import org.vitrivr.cineast.core.db.DBSelector;
import org.vitrivr.cineast.core.db.dao.reader.TagReader;
import org.vitrivr.cineast.core.features.SegmentTags;
import org.vitrivr.cineast.core.util.MathHelper;
import org.vitrivr.cineast.standalone.config.Config;
import org.vitrivr.cineast.standalone.config.RetrievalRuntimeConfig;
import org.vitrivr.cineast.standalone.util.ContinuousRetrievalLogic;

//TODO maybe this should be moved to core?
public class QueryUtil {

  public static HashMap<String, ArrayList<QueryContainer>> groupComponentsByCategory(
      List<QueryComponent> queryComponents) {
    HashMap<String, ArrayList<QueryContainer>> categoryMap = new HashMap<>();
    for (QueryComponent component : queryComponents) {
      for (QueryTerm term : component.getTerms()) {
        if (term.getCategories() == null) {
          continue;
        }
        term.getCategories().forEach((String category) -> {
          if (!categoryMap.containsKey(category)) {
            categoryMap.put(category, new ArrayList<>());
          }
          categoryMap.get(category).add(term.toContainer());
        });
      }
    }
    return categoryMap;
  }

  public static HashMap<String, ArrayList<Pair<QueryContainer, ReadableQueryConfig>>> groupTermsByCategory(
      List<org.vitrivr.cineast.api.grpc.data.QueryTerm> terms) {
    HashMap<String, ArrayList<Pair<QueryContainer, ReadableQueryConfig>>> categoryMap = new HashMap<>();
    for (org.vitrivr.cineast.api.grpc.data.QueryTerm term : terms) {
      if (term.getCategories().isEmpty()) {
        continue;
      }
      term.getCategories().forEach((String category) -> {
        if (!categoryMap.containsKey(category)) {
          categoryMap.put(category, new ArrayList<>());
        }
        categoryMap.get(category).add(new Pair<>(term.getContainer(), term.getQueryConfig()));
      });

    }
    return categoryMap;
  }

  public static List<StringDoublePair> retrieveCategory(
      ContinuousRetrievalLogic continuousRetrievalLogic,
      List<Pair<QueryContainer, ReadableQueryConfig>> queryContainers, String category) {
    TObjectDoubleHashMap<String> scoreBySegmentId = new TObjectDoubleHashMap<>();
    for (Pair<QueryContainer, ReadableQueryConfig> pair : queryContainers) {

      if (pair == null) {
        continue;
      }

      QueryContainer qc = pair.first;
      ReadableQueryConfig qconf = pair.second;

      float weight = MathHelper.limit(qc.getWeight(), -1f, 1f);

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
    final List<StringDoublePair> list = new ArrayList<>(scoreBySegmentId.size());
    scoreBySegmentId.forEachEntry((segmentId, score) -> {
      if (score > 0) {
        list.add(new StringDoublePair(segmentId, score));
      }
      return true;
    });

    Collections.sort(list, StringDoublePair.COMPARATOR);

    final int MAX_RESULTS = queryContainers.get(0).second.getMaxResults()
        .orElse(Config.sharedConfig().getRetriever().getMaxResults());
    List<StringDoublePair> resultList = list;
    if (list.size() > MAX_RESULTS) {
      resultList = resultList.subList(0, MAX_RESULTS);
    }
    return resultList;
  }

  public static List<StringDoublePair> retrieve(ContinuousRetrievalLogic continuousRetrievalLogic,
      QueryContainer queryContainer, ReadableQueryConfig config, String category) {
    float weight = MathHelper.limit(queryContainer.getWeight(), -1f, 1f);
    TObjectDoubleHashMap<String> scoreBySegmentId = new TObjectDoubleHashMap<>();

    List<SegmentScoreElement> scoreResults;
    if (queryContainer.hasId()) {
      scoreResults = continuousRetrievalLogic.retrieve(queryContainer.getId(), category, config);
    } else {
      scoreResults = continuousRetrievalLogic.retrieve(queryContainer, category, config);
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

    final List<StringDoublePair> list = new ArrayList<>(scoreBySegmentId.size());
    scoreBySegmentId.forEachEntry((segmentId, score) -> {
      if (score > 0) {
        list.add(new StringDoublePair(segmentId, score));
      }
      return true;
    });

    return list;
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
    List<Map<String, PrimitiveTypeProvider>> rows = selector.getRows(GENERIC_ID_COLUMN_QUALIFIER, ids);

    rows.forEach(row -> _return.add(row.get(SegmentTags.TAG_ID_QUALIFIER).getString()));
    return _return;
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
        rows.stream().map(row ->
            row.get(FEATURE_COLUMN_QUALIFIER).toObject()
        ).forEach(_return::add);
      });
      return true; // TODO Verify this.
    });
    return _return;
  }

  /**
   * Returns all tags for a given list of tagsids
   */
  public static List<Tag> resolveTagsById(
      List<String> tagIds) {
    DBSelector selector = Config.sharedConfig().getDatabase().getSelectorSupplier().get();
    TagReader tagReader = new TagReader(selector);
    return tagReader.getTagsById(tagIds.toArray(new String[0]));
  }

  public static FeaturesAllCategoriesQueryResult retrieveFeaturesForAllCategories(String id) {
    Map<String, Object[]> features = new HashMap<>();
    final RetrievalRuntimeConfig retrievalRuntimeConfig = Config.sharedConfig().getRetriever();
    final DBSelector selector = Config.sharedConfig().getDatabase().getSelectorSupplier().get();

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

  /**
   * Retrieves features by category for all stored/available IDs.
   *
   * @param category The category of features to retrieve.
   * @return A feature map containing a list of IDs/feature array for every object ID for every feature in the category.
   */
  private static Map<String, ArrayList<HashMap<String, Object>>> getAllFeaturesInCategory(String category) {
    // TODO Simplify return data type.
    final RetrievalRuntimeConfig retrievalRuntimeConfig = Config.sharedConfig().getRetriever();
    final DBSelector selector = Config.sharedConfig().getDatabase().getSelectorSupplier().get();
    Map<String, ArrayList<HashMap<String, Object>>> _return = new HashMap<>();

    retrievalRuntimeConfig.getRetrieversByCategory(category).forEach(retriever -> {

      retriever.getTableNames().forEach(tableName -> {

        ArrayList<HashMap<String, Object>> currList = new ArrayList<>();
        selector.open(tableName);
        List<Map<String, PrimitiveTypeProvider>> rows = selector.getAll();

        for (Map<String, PrimitiveTypeProvider> row : rows) {
          HashMap<String, Object> tempMap = new HashMap<>();

          tempMap.put(FEATURE_COLUMN_QUALIFIER, row.get(FEATURE_COLUMN_QUALIFIER).toObject());
          tempMap.put(GENERIC_ID_COLUMN_QUALIFIER, row.get(GENERIC_ID_COLUMN_QUALIFIER).toObject());

          currList.add(tempMap);
        }

        _return.put(tableName, currList);

      });

      return true;

    });

    return _return;
  }

  public static AllFeaturesByCategoryQueryResult retrieveAllFeaturesByCategory(String category) {
    Map<String, ArrayList<HashMap<String, Object>>> features = getAllFeaturesInCategory(category);
    return new AllFeaturesByCategoryQueryResult("", features, category);
  }

}
