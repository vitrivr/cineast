package org.vitrivr.cineast.core.runtime;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
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
import org.vitrivr.cineast.core.data.query.containers.QueryContainer;
import org.vitrivr.cineast.core.data.score.BooleanSegmentScoreElement;
import org.vitrivr.cineast.core.data.score.ObjectScoreElement;
import org.vitrivr.cineast.core.data.score.ScoreElement;
import org.vitrivr.cineast.core.data.score.SegmentScoreElement;
import org.vitrivr.cineast.core.features.listener.RetrievalResultListener;
import org.vitrivr.cineast.core.features.retriever.Retriever;
import org.vitrivr.cineast.core.features.retriever.RetrieverInitializer;
import org.vitrivr.cineast.core.util.LogHelper;
import org.vitrivr.cineast.core.util.MathHelper;
import org.vitrivr.cineast.core.util.ScoreFusion;

import gnu.trove.iterator.TDoubleIterator;
import gnu.trove.map.TObjectDoubleMap;
import gnu.trove.map.hash.TObjectDoubleHashMap;

public class ContinuousQueryDispatcher {
  private static final Logger LOGGER = LogManager.getLogger();

  private static final String LISTENER_NULL_MESSAGE = "Retrieval result listener cannot be null.";
  private static final int TASK_QUEUE_SIZE = Config.sharedConfig().getRetriever()
      .getTaskQueueSize();
  private static final int THREAD_COUNT = Config.sharedConfig().getRetriever().getThreadPoolSize();
  private static final int MAX_RESULTS = Config.sharedConfig().getRetriever().getMaxResults();
  private static final int KEEP_ALIVE_TIME = 60;

  private static final List<RetrievalResultListener> resultListeners = new ArrayList<>();

  private static final LimitedQueue<Runnable> taskQueue = new LimitedQueue<>(TASK_QUEUE_SIZE);
  private static ExecutorService executor = new ThreadPoolExecutor(THREAD_COUNT, THREAD_COUNT,
      KEEP_ALIVE_TIME, TimeUnit.SECONDS, taskQueue);

  private final Function<Retriever, RetrievalTask> taskFactory;
  private final RetrieverInitializer initializer;
  private final TObjectDoubleMap<Retriever> retrieverWeights;
  private final double retrieverWeightSum;

  public static List<SegmentScoreElement> retrieve(QueryContainer query,
      TObjectDoubleHashMap<Retriever> retrievers,
      RetrieverInitializer initializer,
      ReadableQueryConfig config) {
    return new ContinuousQueryDispatcher(r -> new RetrievalTask(r, query, config), retrievers,
        initializer).doRetrieve();
  }

  public static List<SegmentScoreElement> retrieve(String segmentId,
      TObjectDoubleHashMap<Retriever> retrievers,
      RetrieverInitializer initializer,
      ReadableQueryConfig config) {
    return new ContinuousQueryDispatcher(r -> new RetrievalTask(r, segmentId, config), retrievers,
        initializer).doRetrieve();
  }

  public static void shutdown() {
    clearExecutor();
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

  private ContinuousQueryDispatcher(Function<Retriever, RetrievalTask> taskFactory,
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
    LOGGER.trace("Initialized continuous query dispatcher with retrievers {}", retrieverWeights);
  }

  private List<SegmentScoreElement> doRetrieve() {
    LOGGER.trace("Initializing executor with retrievers {}", retrieverWeights);
    initExecutor();
    LOGGER.trace("Starting tasks with retrievers {}", retrieverWeights);
    List<Future<Pair<RetrievalTask, List<ScoreElement>>>> futures = this.startTasks();
    LOGGER.trace("Extracting results with retrievers {}", retrieverWeights);
    List<SegmentScoreElement> segmentScores = this.extractResults(futures);
    LOGGER.trace("Retrieved {} results, finishing", segmentScores.size());
    this.finish();
    return segmentScores;
  }

  private static void initExecutor() {
    if (executor.isShutdown()) {
      clearExecutor();
    }
    if (executor == null) {
      executor = new ThreadPoolExecutor(THREAD_COUNT, THREAD_COUNT, KEEP_ALIVE_TIME,
          TimeUnit.SECONDS, taskQueue);
    }
  }

  private static void clearExecutor() {
    taskQueue.clear();
    if (executor != null) {
      executor.shutdown();
      executor = null;
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

    ScoreFusion.fuseObjectsIntoSegments(scoreBySegmentId, scoreByObjectId);
    return this.normalizeSortTruncate(scoreBySegmentId);
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
      } else if (element instanceof BooleanSegmentScoreElement) {
        scoreById = scoreBySegmentId; //TODO: Cleanup?
      } else {
        LOGGER.error(
            "Unknown subclass {} of ScoreElement in ContinuousQueryDispatcher.addRetrievalResult.",
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

    double weightedScore = score * weight;
    scoreById.adjustOrPutValue(id, weightedScore, weightedScore);
  }

  private List<SegmentScoreElement> normalizeSortTruncate(
      TObjectDoubleMap<String> scoreBySegmentId) {
    List<SegmentScoreElement> results = new ArrayList<>(scoreBySegmentId.size());
    scoreBySegmentId.forEachEntry((segmentId, score) -> {
      results.add(new SegmentScoreElement(segmentId, MathHelper.limit(score / this.retrieverWeightSum, 0d, 1d)));
      return true;
    });

    results.sort(ScoreElement.SCORE_COMPARATOR.reversed());
    if (results.size() > MAX_RESULTS) {
      return results.subList(0, MAX_RESULTS);
    } else {
      return results;
    }
  }

  private void finish() {
    for (Retriever r : this.retrieverWeights.keySet()) {
      r.finish();
    }
  }
}
