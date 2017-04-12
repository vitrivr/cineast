package org.vitrivr.cineast.core.runtime;

import com.google.common.collect.ListMultimap;
import gnu.trove.iterator.TDoubleIterator;
import gnu.trove.map.TObjectDoubleMap;
import gnu.trove.map.hash.TObjectDoubleHashMap;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.config.Config;
import org.vitrivr.cineast.core.config.ReadableQueryConfig;
import org.vitrivr.cineast.core.data.LimitedQueue;
import org.vitrivr.cineast.core.data.Pair;
import org.vitrivr.cineast.core.data.entities.SegmentDescriptor;
import org.vitrivr.cineast.core.data.query.containers.QueryContainer;
import org.vitrivr.cineast.core.data.score.ObjectScoreElement;
import org.vitrivr.cineast.core.data.score.ScoreElement;
import org.vitrivr.cineast.core.data.score.ScoreElements;
import org.vitrivr.cineast.core.data.score.SegmentScoreElement;
import org.vitrivr.cineast.core.db.dao.reader.SegmentLookup;
import org.vitrivr.cineast.core.features.listener.RetrievalResultListener;
import org.vitrivr.cineast.core.features.retriever.Retriever;
import org.vitrivr.cineast.core.features.retriever.RetrieverInitializer;
import org.vitrivr.cineast.core.util.LogHelper;

public class ContinousQueryDispatcher {
  private static final Logger LOGGER = LogManager.getLogger();

  private static final String LISTENER_NULL_MESSAGE = "Retrieval result listener cannot be null.";
  private static final int TASK_QUEUE_SIZE = Config.sharedConfig().getRetriever()
      .getTaskQueueSize();
  private static final int THREAD_COUNT = Config.sharedConfig().getRetriever().getThreadPoolSize();
  private static final int MAX_RESULTS = Config.sharedConfig().getRetriever().getMaxResults();
  private static final int KEEP_ALIVE_TIME = 60;

  private static final List<RetrievalResultListener> resultListeners = new ArrayList<>();
  private static final SegmentLookup segmentLookup = new SegmentLookup();

  private final LimitedQueue<Runnable> taskQueue = new LimitedQueue<>(TASK_QUEUE_SIZE);
  private ExecutorService executor = new ThreadPoolExecutor(THREAD_COUNT, THREAD_COUNT,
      KEEP_ALIVE_TIME, TimeUnit.SECONDS, taskQueue);

  private final Function<Retriever, RetrievalTask> taskFactory;
  private final RetrieverInitializer initializer;
  private final TObjectDoubleMap<Retriever> retrieverWeights;
  private final double retrieverWeightSum;

  public static List<SegmentScoreElement> retrieve(QueryContainer query,
      TObjectDoubleHashMap<Retriever> retrievers,
      RetrieverInitializer initializer,
      ReadableQueryConfig config) {
    return new ContinousQueryDispatcher(r -> new RetrievalTask(r, query, config), retrievers,
        initializer).retrieve();
  }

  public static List<SegmentScoreElement> retrieve(String segmentId,
      TObjectDoubleHashMap<Retriever> retrievers,
      RetrieverInitializer initializer,
      ReadableQueryConfig config) {
    return new ContinousQueryDispatcher(r -> new RetrievalTask(r, segmentId, config), retrievers,
        initializer).retrieve();
  }

  public static void addRetrievalResultListener(RetrievalResultListener listener) {
    Objects.requireNonNull(listener, LISTENER_NULL_MESSAGE);
    if (!resultListeners.contains(listener)) {
      resultListeners.add(listener);
    }
  }

  public static void removeRetrievalResultListener(RetrievalResultListener listener) {
    Objects.requireNonNull(listener, LISTENER_NULL_MESSAGE);
    resultListeners.remove(listener);
  }

  private ContinousQueryDispatcher(Function<Retriever, RetrievalTask> taskFactory,
      TObjectDoubleMap<Retriever> retrieverWeights,
      RetrieverInitializer initializer) {
    this.taskFactory = taskFactory;
    this.initializer = initializer;
    this.retrieverWeights = retrieverWeights;

    double weightSum = 0d;
    TDoubleIterator i = retrieverWeights.valueCollection().iterator();
    while (i.hasNext()) {
      weightSum += i.next();
    }
    this.retrieverWeightSum = weightSum;
  }

  private List<SegmentScoreElement> retrieve() {
    this.checkExecutor();
    List<Future<Pair<RetrievalTask, List<ScoreElement>>>> futures = this.startTasks();
    List<SegmentScoreElement> segmentScores = this.extractResults(futures);
    this.finish();
    return segmentScores;
  }

  private void checkExecutor() {
    if (this.executor == null || this.executor.isShutdown()) {
      throw new IllegalStateException(
          "Continuous query dispatcher can only be called once, but was already called.");
    }
  }

  private List<Future<Pair<RetrievalTask, List<ScoreElement>>>> startTasks() {
    List<Future<Pair<RetrievalTask, List<ScoreElement>>>> futures = new LinkedList<>();
    this.retrieverWeights.forEachEntry((r, weight) -> {
      if (weight > 0) {
        this.initializer.initialize(r);
        RetrievalTask task = taskFactory.apply(r);
        futures.add(executor.submit(task));
      }
      return true;
    });
    return futures;
  }

  private List<SegmentScoreElement> extractResults(
      List<Future<Pair<RetrievalTask, List<ScoreElement>>>> futures) {
    TObjectDoubleMap<String> scoreByObjectId = new TObjectDoubleHashMap<>();
    TObjectDoubleMap<String> scoreBySegmentId = new TObjectDoubleHashMap<>();
    while (!futures.isEmpty()) {
      Iterator<Future<Pair<RetrievalTask, List<ScoreElement>>>> iterator = futures.iterator();
      while (iterator.hasNext()) {
        Future<Pair<RetrievalTask, List<ScoreElement>>> future = iterator.next();
        if (!future.isDone()) {
          continue;
        }

        try {
          Pair<RetrievalTask, List<ScoreElement>> pair = future.get();
          this.addRetrievalResult(scoreByObjectId, scoreBySegmentId, pair.first, pair.second);
        } catch (InterruptedException | ExecutionException e) {
          LOGGER.warn(LogHelper.getStackTrace(e));
        }
        iterator.remove();
      }
      try {
        Thread.sleep(10);
      } catch (InterruptedException e) {
        // Ignore
      }
    }

    List<SegmentScoreElement> merged =
        this.mergeObjectSegmentElements(scoreByObjectId, scoreBySegmentId);
    return this.sortAndTruncate(merged);
  }

  private void addRetrievalResult(TObjectDoubleMap<String> scoreByObjectId,
      TObjectDoubleMap<String> scoreBySegmentId, RetrievalTask task,
      List<ScoreElement> scoreElements) {
    if (scoreElements == null) {
      LOGGER.warn("Retrieval task {} returned 'null' results.", task);
      return;
    }

    for (RetrievalResultListener listener : resultListeners) {
      listener.notify(scoreElements, task);
    }

    double retrieverWeight = this.retrieverWeights.get(task.getRetriever());
    for (ScoreElement element : scoreElements) {
      TObjectDoubleMap<String> scoreById;
      if (element instanceof ObjectScoreElement) {
        scoreById = scoreByObjectId;
      } else if (element instanceof SegmentScoreElement) {
        scoreById = scoreBySegmentId;
      } else {
        LOGGER.error(
            "Unknown subclass {} of ScoreElement in ContinousQueryDispatcher.addRetrievalResult.",
            element.getClass().getSimpleName());
        continue;
      }
      this.addScoreElement(scoreById, element, retrieverWeight);
    }
  }

  private void addScoreElement(TObjectDoubleMap<String> scoreById, ScoreElement next,
      double weight) {
    String id = next.getId();
    double score = next.getScore();
    if (score < 0 || score > 1) {
      LOGGER.warn("Score of retrieval task should be between [0,1], but was: {}, ignoring {}...",
          score, next);
      return;
    }

    // Normalize already here because of ObjectScoreElement merge strategy
    // TODO: Discuss with Luca whether normalization here is ok or not
    double updatedScore = score * weight / this.retrieverWeightSum;
    scoreById.adjustOrPutValue(id, updatedScore, updatedScore);
  }

  /**
   * Merges the object and segment scores into one final list of {@link SegmentScoreElement}s. Every
   * object score is added to the scores of its segments. If an object without any of its segments
   * was found, the first segment is added and used instead.
   *
   * @param scoreByObjectId Map containing the found object ids with their score.
   * @param scoreBySegmentId Map containing the found segment ids with their score.
   * @return A final, merged list of {@link SegmentScoreElement}s.
   */
  private List<SegmentScoreElement> mergeObjectSegmentElements(
      TObjectDoubleMap<String> scoreByObjectId,
      TObjectDoubleMap<String> scoreBySegmentId) {
    Set<String> objectIds = scoreByObjectId.keySet();
    ListMultimap<String, SegmentDescriptor> segmentsByObjectId =
        segmentLookup.lookUpSegmentsOfObjects(objectIds);
    for (String objectId : segmentsByObjectId.keySet()) {
      assert scoreByObjectId.containsKey(objectId);
      double objectScore = scoreByObjectId.get(objectId);
      List<SegmentDescriptor> segments = segmentsByObjectId.get(objectId);
      assert !segments.isEmpty();

      boolean objectSegmentsFoundInResults = false;
      for (SegmentDescriptor segment : segments) {
        String segmentId = segment.getSegmentId();
        boolean foundElement = scoreBySegmentId.adjustValue(segmentId, objectScore);
        if (foundElement) {
          objectSegmentsFoundInResults = true;
        }
      }

      if (!objectSegmentsFoundInResults) {
        SegmentDescriptor firstSegment = segments.get(0);
        String firstId = firstSegment.getSegmentId();
        scoreBySegmentId.put(firstId, objectScore);
      }
    }

    return ScoreElements.segmentsFromSegmentsMap(scoreBySegmentId);
  }

  private List<SegmentScoreElement> sortAndTruncate(List<SegmentScoreElement> results) {
    results.sort(ScoreElements.SCORE_COMPARATOR.reversed());
    if (results.size() > MAX_RESULTS) {
      results = results.subList(0, MAX_RESULTS);
    }
    return results;
  }

  private void finish() {
    for (Retriever r : this.retrieverWeights.keySet()) {
      r.finish();
    }

    this.taskQueue.clear();
    if (this.executor != null) {
      this.executor.shutdown();
      this.executor = null;
    }
  }
}
