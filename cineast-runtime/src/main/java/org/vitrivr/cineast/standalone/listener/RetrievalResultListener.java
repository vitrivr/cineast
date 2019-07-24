package org.vitrivr.cineast.standalone.listener;

import java.util.List;

import org.vitrivr.cineast.core.data.score.ScoreElement;
import org.vitrivr.cineast.core.features.retriever.Retriever;
import org.vitrivr.cineast.standalone.runtime.RetrievalTask;

/**
 * Listener which can be attached to retrieval logic to get notified on raw results of a {@link Retriever}
 */
public interface RetrievalResultListener {

  /**
   * gets called whenever a {@link Retriever} returns
   * @param resultList the retrieved results
   * @param task the {@link RetrievalTask} containing retriever and query
   */
  public void notify(List<ScoreElement> resultList, RetrievalTask task);
  
}
