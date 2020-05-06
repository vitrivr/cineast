package org.vitrivr.cineast.standalone.util;

import gnu.trove.map.hash.TObjectDoubleHashMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.config.DatabaseConfig;
import org.vitrivr.cineast.core.config.ReadableQueryConfig;
import org.vitrivr.cineast.core.data.query.containers.QueryContainer;
import org.vitrivr.cineast.core.data.score.SegmentScoreElement;
import org.vitrivr.cineast.core.db.dao.reader.MediaSegmentReader;
import org.vitrivr.cineast.core.features.retriever.Retriever;
import org.vitrivr.cineast.core.features.retriever.RetrieverInitializer;
import org.vitrivr.cineast.standalone.config.Config;
import org.vitrivr.cineast.standalone.listener.RetrievalResultListener;
import org.vitrivr.cineast.standalone.runtime.ContinuousQueryDispatcher;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ContinuousRetrievalLogic {

  private static final Logger LOGGER = LogManager.getLogger();
  private final DatabaseConfig config;
  private final RetrieverInitializer initializer;
  private final MediaSegmentReader segmentReader;

  public ContinuousRetrievalLogic(DatabaseConfig config){
    this.config = config;
    this.initializer = r -> r.init(this.config.getSelectorSupplier());
    this.segmentReader = new MediaSegmentReader(this.config.getSelectorSupplier().get());
  }

  public List<SegmentScoreElement> retrieve(QueryContainer qc, String category,
      ReadableQueryConfig config) {
    TObjectDoubleHashMap<Retriever> retrievers = Config.sharedConfig().getRetriever()
        .getRetrieversByCategory(category);
    if (retrievers.isEmpty()) {
      LOGGER.warn("Empty retriever list for query {}, category {} and config {}, returning no results", qc, category, config);
      return new ArrayList<>(0);
    }
    return ContinuousQueryDispatcher.retrieve(qc, retrievers, initializer, config, this.segmentReader);
  }

  public List<SegmentScoreElement> retrieve(String segmentId, String category, ReadableQueryConfig config) {
    TObjectDoubleHashMap<Retriever> retrievers = Config.sharedConfig().getRetriever()
        .getRetrieversByCategory(category);
    if (retrievers.isEmpty()) {
      LOGGER.warn("Empty retriever list for segmentID {}, category {} and config {}, returning no results", segmentId, category, config);
      return new ArrayList<>(0);
    }
    return ContinuousQueryDispatcher.retrieve(segmentId, retrievers, initializer, config, this.segmentReader);
  }

  /**
   * Performs retrieval analogous to {@link #retrieve(String, String, ReadableQueryConfig)}
   *
   * @param retrieverName Name of the retriever as received by config
   */
  public List<SegmentScoreElement> retrieveByRetrieverName(String segmentId, String retrieverName,
      ReadableQueryConfig config) {
    Optional<Retriever> retriever = Config.sharedConfig().getRetriever()
        .getRetrieverByName(retrieverName);
    if (!retriever.isPresent()) {
      return new ArrayList<>(0);
    }
    return retrieveByRetriever(segmentId, retriever.get(), config);
  }

  public List<SegmentScoreElement> retrieveByRetriever(String segmentId, Retriever retriever,
      ReadableQueryConfig config) {
    TObjectDoubleHashMap<Retriever> map = new TObjectDoubleHashMap<>();
    map.put(retriever, 1d);
    return ContinuousQueryDispatcher.retrieve(segmentId, map, initializer, config, this.segmentReader);
  }

  public List<SegmentScoreElement> retrieveByRetriever(QueryContainer qc,
      Retriever retriever,
      ReadableQueryConfig config) {
    TObjectDoubleHashMap<Retriever> map = new TObjectDoubleHashMap<>();
    map.put(retriever, 1d);
    return ContinuousQueryDispatcher.retrieve(qc, map, initializer, config, this.segmentReader);
  }

  public List<SegmentScoreElement> retrieveByRetrieverName(QueryContainer qc,
      String retrieverName,
      ReadableQueryConfig config) {
    Optional<Retriever> retriever = Config.sharedConfig().getRetriever()
        .getRetrieverByName(retrieverName);
    if (!retriever.isPresent()) {
      return new ArrayList<>(0);
    }
    return retrieveByRetriever(qc, retriever.get(), config);
  }

  public void addRetrievalResultListener(RetrievalResultListener listener) {
    ContinuousQueryDispatcher.addRetrievalResultListener(listener);
  }

  public void removeRetrievalResultListener(RetrievalResultListener listener) {
    ContinuousQueryDispatcher.removeRetrievalResultListener(listener);
  }

  // TODO: Is this method actually needed?
  public void shutdown() {
    ContinuousQueryDispatcher.shutdown();
  }
}
