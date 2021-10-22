package org.vitrivr.cineast.standalone.runtime;

import java.util.List;
import java.util.concurrent.Callable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.config.ReadableQueryConfig;
import org.vitrivr.cineast.core.data.Pair;
import org.vitrivr.cineast.core.data.query.containers.AbstractQueryTermContainer;
import org.vitrivr.cineast.core.data.score.ScoreElement;
import org.vitrivr.cineast.core.features.retriever.Retriever;
import org.vitrivr.cineast.standalone.monitoring.RetrievalTaskMonitor;

public class RetrievalTask implements Callable<Pair<RetrievalTask, List<ScoreElement>>> {

  private final Retriever retriever;
  private final AbstractQueryTermContainer query;
  private final String segmentId;
  private static final Logger LOGGER = LogManager.getLogger();
  private final ReadableQueryConfig config;


  public RetrievalTask(Retriever retriever, AbstractQueryTermContainer query, ReadableQueryConfig qc) {
    this.retriever = retriever;
    this.query = query;
    this.config = qc;
    this.segmentId = null;
  }

  public RetrievalTask(Retriever retriever, AbstractQueryTermContainer query) {
    this(retriever, query, null);
  }


  public RetrievalTask(Retriever retriever, String segmentId, ReadableQueryConfig qc) {
    this.retriever = retriever;
    this.segmentId = segmentId;
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
    nameThread();
    LOGGER.debug("starting {}", retriever.getClass().getSimpleName());
    List<ScoreElement> result;
    if (this.query == null) {
      result = this.retriever.getSimilar(this.segmentId, this.config);
    } else {
      result = this.retriever.getSimilar(this.query, this.config);
    }
    long stop = System.currentTimeMillis();
    RetrievalTaskMonitor.reportExecutionTime(retriever.getClass().getSimpleName(), stop - start);
    LOGGER.debug("{}.getSimilar() done in {} ms, {} results", retriever.getClass().getSimpleName(), stop - start, result.size());
    return LOGGER.traceExit(new Pair<RetrievalTask, List<ScoreElement>>(this, result));
  }

  private void nameThread() {
    String currentThreadName = Thread.currentThread().getName();
    if(!currentThreadName.endsWith(retriever.getClass().getSimpleName())){
      Thread.currentThread().setName(currentThreadName.substring(0, currentThreadName.lastIndexOf('-'))+"-"+retriever.getClass().getSimpleName());
    }
  }


  public Retriever getRetriever() {
    return retriever;
  }

  public AbstractQueryTermContainer getQuery() {
    return query;
  }

  public String getSegmentId() {
    return segmentId;
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
    result = prime * result + ((segmentId == null) ? 0 : segmentId.hashCode());
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
    if (segmentId == null) {
      if (other.segmentId != null) {
        return false;
      }
    } else if (!segmentId.equals(other.segmentId)) {
      return false;
    }
    return true;
  }


}
