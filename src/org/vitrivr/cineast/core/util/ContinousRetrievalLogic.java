package org.vitrivr.cineast.core.util;

import gnu.trove.map.hash.TObjectDoubleHashMap;

import java.util.ArrayList;
import java.util.List;

import org.vitrivr.cineast.api.API;
import org.vitrivr.cineast.core.config.Config;
import org.vitrivr.cineast.core.config.ReadableQueryConfig;
import org.vitrivr.cineast.core.data.query.containers.QueryContainer;
import org.vitrivr.cineast.core.data.score.SegmentScoreElement;
import org.vitrivr.cineast.core.features.listener.RetrievalResultListener;
import org.vitrivr.cineast.core.features.retriever.Retriever;
import org.vitrivr.cineast.core.runtime.ContinousQueryDispatcher;

public class ContinousRetrievalLogic {

  public static List<SegmentScoreElement> retrieve(QueryContainer qc, String category,
      ReadableQueryConfig config) {
    TObjectDoubleHashMap<Retriever> retrievers = Config.sharedConfig().getRetriever()
        .getRetrieversByCategory(category);
    if (retrievers.isEmpty()) {
      return new ArrayList<SegmentScoreElement>(0);
    }
    return ContinousQueryDispatcher.retrieve(qc, retrievers, API.getInitializer(), config);
  }

  public static List<SegmentScoreElement> retrieve(String id, String category, ReadableQueryConfig config) {
    TObjectDoubleHashMap<Retriever> retrievers = Config.sharedConfig().getRetriever()
        .getRetrieversByCategory(category);
    if (retrievers.isEmpty()) {
      return new ArrayList<SegmentScoreElement>(0);
    }
    return ContinousQueryDispatcher.retrieve(id, retrievers, API.getInitializer(), config);
  }

  public static void addRetrievalResultListener(RetrievalResultListener listener) {
    ContinousQueryDispatcher.addRetrievalResultListener(listener);
  }

  public static void removeRetrievalResultListener(RetrievalResultListener listener) {
    ContinousQueryDispatcher.removeRetrievalResultListener(listener);
  }

  public static void shutdown() {
    // FIXME: Is shutdown() really needed anymore or just remove it?
    // ContinousQueryDispatcher.shutdown();
  }

}
