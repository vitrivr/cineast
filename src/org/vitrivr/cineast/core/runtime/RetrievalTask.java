package org.vitrivr.cineast.core.runtime;

import java.util.List;
import java.util.concurrent.Callable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.config.ReadableQueryConfig;
import org.vitrivr.cineast.core.data.Pair;
import org.vitrivr.cineast.core.data.query.containers.QueryContainer;
import org.vitrivr.cineast.core.data.score.ScoreElement;
import org.vitrivr.cineast.core.features.retriever.Retriever;
import org.vitrivr.cineast.monitoring.RetrievalTaskMonitor;

public class RetrievalTask implements Callable<Pair<RetrievalTask, List<ScoreElement>>> {

  private final Retriever retriever;
  private final QueryContainer query;
  private final String shotId;
  private static final Logger LOGGER = LogManager.getLogger();
  private final ReadableQueryConfig config;


  public RetrievalTask(Retriever retriever, QueryContainer query, ReadableQueryConfig qc) {
    this.retriever = retriever;
    this.query = query;
    this.config = qc;
    this.shotId = null;
  }

  public RetrievalTask(Retriever retriever, QueryContainer query) {
    this(retriever, query, null);
  }


  public RetrievalTask(Retriever retriever, String segmentId, ReadableQueryConfig qc) {
    this.retriever = retriever;
    this.shotId = segmentId;
    this.config = qc;
    this.query = null;

  }

  public RetrievalTask(Retriever retriever, String segmentId) {
    this(retriever, segmentId, null);
  }

  @Override
  public Pair<RetrievalTask, List<ScoreElement>> call() throws Exception {
    LOGGER.traceEntry();
    long start = System.currentTimeMillis();
    LOGGER.debug("starting {}", retriever.getClass().getSimpleName());
    List<ScoreElement> result;
    if (this.query == null) {
      result = this.retriever.getSimilar(this.shotId, this.config);
    } else {
      result = this.retriever.getSimilar(this.query, this.config);
    }
    long stop = System.currentTimeMillis();
    RetrievalTaskMonitor.reportExecutionTime(retriever.getClass().getSimpleName(), stop - start);
    LOGGER.debug("{}.getSimilar() done in {} ms", retriever.getClass().getSimpleName(), stop - start);
    return LOGGER.traceExit(new Pair<RetrievalTask, List<ScoreElement>>(this, result));
  }

  public Retriever getRetriever() {
    return retriever;
  }

  public QueryContainer getQuery() {
    return query;
  }

  public String getSegmentId() {
    return shotId;
  }

  public ReadableQueryConfig getConfig() {
    return config;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((config == null) ? 0 : config.hashCode());
    result = prime * result + ((query == null) ? 0 : query.hashCode());
    result = prime * result + ((retriever == null) ? 0 : retriever.hashCode());
    result = prime * result + ((shotId == null) ? 0 : shotId.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    RetrievalTask other = (RetrievalTask) obj;
    if (config == null) {
      if (other.config != null) {
        return false;
      }
    } else if (!config.equals(other.config)) {
      return false;
    }
    if (query == null) {
      if (other.query != null) {
        return false;
      }
    } else if (!query.equals(other.query)) {
      return false;
    }
    if (retriever == null) {
      if (other.retriever != null) {
        return false;
      }
    } else if (!retriever.equals(other.retriever)) {
      return false;
    }
    if (shotId == null) {
      if (other.shotId != null) {
        return false;
      }
    } else if (!shotId.equals(other.shotId)) {
      return false;
    }
    return true;
  }


}
