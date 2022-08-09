package org.vitrivr.cineast.standalone.runtime;

import com.carrotsearch.hppc.ObjectDoubleHashMap;
import com.carrotsearch.hppc.ObjectDoubleMap;
import com.carrotsearch.hppc.cursors.DoubleCursor;
import com.carrotsearch.hppc.cursors.ObjectDoubleCursor;
import com.carrotsearch.hppc.predicates.ObjectDoublePredicate;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.config.ReadableQueryConfig;
import org.vitrivr.cineast.core.data.LimitedQueue;
import org.vitrivr.cineast.core.data.Pair;
import org.vitrivr.cineast.core.data.QueryResultCacheKey;
import org.vitrivr.cineast.core.data.query.containers.AbstractQueryTermContainer;
import org.vitrivr.cineast.core.data.score.BooleanSegmentScoreElement;
import org.vitrivr.cineast.core.data.score.ObjectScoreElement;
import org.vitrivr.cineast.core.data.score.ScoreElement;
import org.vitrivr.cineast.core.data.score.SegmentScoreElement;
import org.vitrivr.cineast.core.db.dao.reader.MediaSegmentReader;
import org.vitrivr.cineast.core.features.retriever.Retriever;
import org.vitrivr.cineast.core.features.retriever.RetrieverInitializer;
import org.vitrivr.cineast.core.util.LogHelper;
import org.vitrivr.cineast.core.util.ScoreFusion;
import org.vitrivr.cineast.core.util.math.MathHelper;
import org.vitrivr.cineast.standalone.config.Config;

public class ContinuousQueryDispatcher {

  private static final Logger LOGGER = LogManager.getLogger();

  private static final int TASK_QUEUE_SIZE = Config.sharedConfig().getRetriever().getTaskQueueSize();
  private static final int THREAD_COUNT = Config.sharedConfig().getRetriever().getThreadPoolSize();
  private static final int MAX_RESULTS = Config.sharedConfig().getRetriever().getMaxResults();
  private static final int KEEP_ALIVE_TIME = 60;

  private static final LimitedQueue<Runnable> taskQueue = new LimitedQueue<>(TASK_QUEUE_SIZE);
  private static ExecutorService executor = new ThreadPoolExecutor(THREAD_COUNT, THREAD_COUNT, KEEP_ALIVE_TIME, TimeUnit.SECONDS, taskQueue);

  private final Function<Retriever, RetrievalTask> taskFactory;
  private final RetrieverInitializer initializer;
  private final ObjectDoubleMap<Retriever> retrieverWeights;
  private final MediaSegmentReader mediaSegmentReader;
  private final double retrieverWeightSum;

  private static final boolean QUERY_CACHE_ENABLED = Config.sharedConfig().getCache().isEnableQueryCaching();
  private static final int QUERY_CACHE_SIZE = QUERY_CACHE_ENABLED ? Config.sharedConfig().getCache().getQueryCacheSize() : 0;
  private static final long QUERY_CACHE_LIFE = QUERY_CACHE_ENABLED ? Config.sharedConfig().getCache().getQueryCacheDuration() : 0L;

  private static final Cache<QueryResultCacheKey, List<SegmentScoreElement>> queryCache =
      QUERY_CACHE_ENABLED ? CacheBuilder.newBuilder()
          .maximumSize(QUERY_CACHE_SIZE)
          .expireAfterWrite(QUERY_CACHE_LIFE, TimeUnit.SECONDS)
          .build() : null;


  private ContinuousQueryDispatcher(Function<Retriever, RetrievalTask> taskFactory, ObjectDoubleMap<Retriever> retrieverWeights, RetrieverInitializer initializer, MediaSegmentReader mediaSegmentReader) {
    this.taskFactory = taskFactory;
    this.initializer = initializer;
    this.retrieverWeights = retrieverWeights;
    this.mediaSegmentReader = mediaSegmentReader;

    double weightSum = 0d;
    for (DoubleCursor doubleCursor : retrieverWeights.values()) {
      weightSum += doubleCursor.value;
    }
    this.retrieverWeightSum = weightSum;
    LOGGER.trace("Initialized continuous query dispatcher with retrievers {}", retrieverWeights);

  }

  public static List<SegmentScoreElement> retrieve(AbstractQueryTermContainer query, ObjectDoubleHashMap<Retriever> retrievers, RetrieverInitializer initializer, ReadableQueryConfig config, MediaSegmentReader mediaSegmentReader) {

    if (QUERY_CACHE_ENABLED) {

      QueryResultCacheKey cacheKey = new QueryResultCacheKey(query, retrievers, config);

      List<SegmentScoreElement> result = queryCache.getIfPresent(cacheKey);

      if (result == null) {
        result = new ContinuousQueryDispatcher(r -> new RetrievalTask(r, query, config), retrievers, initializer, mediaSegmentReader).doRetrieve();
        queryCache.put(cacheKey, result);
      }

      return result;

    } else {
      return new ContinuousQueryDispatcher(r -> new RetrievalTask(r, query, config), retrievers, initializer, mediaSegmentReader).doRetrieve();
    }
  }

  public static List<SegmentScoreElement> retrieve(String segmentId, ObjectDoubleHashMap<Retriever> retrievers, RetrieverInitializer initializer, ReadableQueryConfig config, MediaSegmentReader mediaSegmentReader) {

    if (QUERY_CACHE_ENABLED) {

      QueryResultCacheKey cacheKey = new QueryResultCacheKey(segmentId, retrievers, config);

      List<SegmentScoreElement> result = queryCache.getIfPresent(cacheKey);

      if (result == null) {
        result = new ContinuousQueryDispatcher(r -> new RetrievalTask(r, segmentId, config), retrievers, initializer, mediaSegmentReader).doRetrieve();
        queryCache.put(cacheKey, result);
      }

      return result;
    } else {
      return new ContinuousQueryDispatcher(r -> new RetrievalTask(r, segmentId, config), retrievers, initializer, mediaSegmentReader).doRetrieve();
    }
  }

  public static void shutdown() {
    clearExecutor();
  }

  private static void initExecutor() { //FIXME this should be somewhere else
    if (executor.isShutdown()) {
      clearExecutor();
    }
    if (executor == null) {
      executor = new ThreadPoolExecutor(THREAD_COUNT, THREAD_COUNT, KEEP_ALIVE_TIME, TimeUnit.SECONDS, taskQueue);
    }
  }

  private static void clearExecutor() {
    taskQueue.clear();
    if (executor != null) {
      executor.shutdown();
      try {
        executor.awaitTermination(10, TimeUnit.SECONDS);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      List<Runnable> runnables = executor.shutdownNow();
      if (runnables.size() > 0) {
        LOGGER.warn("{} threads terminated prematurely", runnables.size());
      }
      executor = null;
    }
  }

  private List<SegmentScoreElement> doRetrieve() {
    LOGGER.trace("Initializing executor with retrievers {}", retrieverWeights);
    initExecutor();
    LOGGER.trace("Starting tasks with retrievers {}", retrieverWeights);
    List<Future<Pair<RetrievalTask, List<ScoreElement>>>> futures = this.startTasks();
    LOGGER.trace("Extracting results with retrievers {}", retrieverWeights);
    List<SegmentScoreElement> segmentScores = this.extractResults(futures, this.mediaSegmentReader);
    LOGGER.trace("Retrieved {} results, finishing", segmentScores.size());
    this.finish();
    return segmentScores;
  }

  private List<Future<Pair<RetrievalTask, List<ScoreElement>>>> startTasks() {
    List<Future<Pair<RetrievalTask, List<ScoreElement>>>> futures = new LinkedList<>();
    this.retrieverWeights.forEach((ObjectDoublePredicate<? super Retriever>) (r, weight) -> {
      if (weight > 0) {
        this.initializer.initialize(r);
        RetrievalTask task = taskFactory.apply(r);
        futures.add(executor.submit(task));
      }
      return true;
    });
    return futures;
  }

  private List<SegmentScoreElement> extractResults(List<Future<Pair<RetrievalTask, List<ScoreElement>>>> futures, MediaSegmentReader mediaSegmentReader) {
    ObjectDoubleMap<String> scoreByObjectId = new ObjectDoubleHashMap<>();
    ObjectDoubleMap<String> scoreBySegmentId = new ObjectDoubleHashMap<>();
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

    ScoreFusion.fuseObjectsIntoSegments(scoreBySegmentId, scoreByObjectId, mediaSegmentReader);
    return this.normalizeSortTruncate(scoreBySegmentId);
  }

  private void addRetrievalResult(ObjectDoubleMap<String> scoreByObjectId, ObjectDoubleMap<String> scoreBySegmentId, RetrievalTask task, List<ScoreElement> scoreElements) {
    if (scoreElements == null) {
      LOGGER.warn("Retrieval task {} returned 'null' results.", task);
      return;
    }

    double retrieverWeight = this.retrieverWeights.get(task.getRetriever());
    for (ScoreElement element : scoreElements) {
      ObjectDoubleMap<String> scoreById;
      if (element instanceof ObjectScoreElement) {
        scoreById = scoreByObjectId;
      } else if (element instanceof SegmentScoreElement) {
        scoreById = scoreBySegmentId;
      } else if (element instanceof BooleanSegmentScoreElement) {
        scoreById = scoreBySegmentId; //TODO: Cleanup?
      } else {
        LOGGER.error("Unknown subclass {} of ScoreElement in ContinuousQueryDispatcher.addRetrievalResult.", element.getClass().getSimpleName());
        continue;
      }
      this.addScoreElement(scoreById, element, retrieverWeight);
    }
  }

  private void addScoreElement(ObjectDoubleMap<String> scoreById, ScoreElement next, double weight) {
    String id = next.getId();
    double score = next.getScore();
    if (score < 0 || score > 1) {
      LOGGER.warn("Score of retrieval task should be between [0,1], but was: {}, ignoring {}...", score, next);
      return;
    }

    double weightedScore = score * weight;
    scoreById.putOrAdd(id, weightedScore, weightedScore);
  }

  private List<SegmentScoreElement> normalizeSortTruncate(ObjectDoubleMap<String> scoreBySegmentId) {
    List<SegmentScoreElement> results = new ArrayList<>(scoreBySegmentId.size());
    scoreBySegmentId.forEach((ObjectDoublePredicate<? super String>) (segmentId, score) -> {
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

    for (ObjectDoubleCursor<Retriever> retrieverWeight : this.retrieverWeights) {
      retrieverWeight.key.finish();
    }

  }
}
