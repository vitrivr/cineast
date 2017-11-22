package org.vitrivr.cineast.core.util;

import gnu.trove.map.hash.TObjectDoubleHashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.vitrivr.cineast.api.API;
import org.vitrivr.cineast.core.config.Config;
import org.vitrivr.cineast.core.config.ReadableQueryConfig;
import org.vitrivr.cineast.core.data.query.containers.QueryContainer;
import org.vitrivr.cineast.core.data.score.SegmentScoreElement;
import org.vitrivr.cineast.core.features.listener.RetrievalResultListener;
import org.vitrivr.cineast.core.features.retriever.Retriever;
import org.vitrivr.cineast.core.runtime.ContinuousQueryDispatcher;

public class ContinuousRetrievalLogic {

  public static List<SegmentScoreElement> retrieve(QueryContainer qc, String category,
      ReadableQueryConfig config) {
    TObjectDoubleHashMap<Retriever> retrievers = Config.sharedConfig().getRetriever()
        .getRetrieversByCategory(category);
    if (retrievers.isEmpty()) {
      return new ArrayList<SegmentScoreElement>(0);
    }
    return ContinuousQueryDispatcher.retrieve(qc, retrievers, API.getInitializer(), config);
  }

  public static List<SegmentScoreElement> retrieve(String id, String category, ReadableQueryConfig config) {
    TObjectDoubleHashMap<Retriever> retrievers = Config.sharedConfig().getRetriever()
        .getRetrieversByCategory(category);
    if (retrievers.isEmpty()) {
      return new ArrayList<SegmentScoreElement>(0);
    }
    return ContinuousQueryDispatcher.retrieve(id, retrievers, API.getInitializer(), config);
  }

  /**
   * Performs retrieval analogous to {@link #retrieve(String, String, ReadableQueryConfig)}
   *
   * @param retrieverName Name of the retriever as received by config
   */
  public static List<SegmentScoreElement> retrieveByRetrieverName(String id, String retrieverName,
      ReadableQueryConfig config) {
    Optional<Retriever> retriever = Config.sharedConfig().getRetriever()
        .getRetrieverByName(retrieverName);
    if (!retriever.isPresent()) {
      return new ArrayList<>(0);
    }
    TObjectDoubleHashMap<Retriever> map = new TObjectDoubleHashMap<>();
    map.put(retriever.get(), 1d);
    return ContinuousQueryDispatcher.retrieve(id, map, API.getInitializer(), config);
  }

  public static void addRetrievalResultListener(RetrievalResultListener listener) {
    ContinuousQueryDispatcher.addRetrievalResultListener(listener);
  }

  public static void removeRetrievalResultListener(RetrievalResultListener listener) {
    ContinuousQueryDispatcher.removeRetrievalResultListener(listener);
  }

  // TODO: Is this method actually needed?
  public static void shutdown() {
    ContinuousQueryDispatcher.shutdown();
  }
}
