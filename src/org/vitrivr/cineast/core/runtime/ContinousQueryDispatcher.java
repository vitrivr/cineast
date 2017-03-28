package org.vitrivr.cineast.core.runtime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.config.Config;
import org.vitrivr.cineast.core.config.ReadableQueryConfig;
import org.vitrivr.cineast.core.data.LimitedQueue;
import org.vitrivr.cineast.core.data.Pair;
import org.vitrivr.cineast.core.data.StringDoublePair;
import org.vitrivr.cineast.core.data.query.containers.QueryContainer;
import org.vitrivr.cineast.core.features.listener.RetrievalResultListener;
import org.vitrivr.cineast.core.features.retriever.Retriever;
import org.vitrivr.cineast.core.features.retriever.RetrieverInitializer;
import org.vitrivr.cineast.core.util.LogHelper;

import gnu.trove.map.hash.TObjectDoubleHashMap;

public class ContinousQueryDispatcher {

	private static final Logger LOGGER = LogManager.getLogger();
	
	private static final int TASK_QUEUE_SIZE = Config.sharedConfig().getRetriever().getTaskQueueSize();
	private static final int THREAD_COUNT = Config.sharedConfig().getRetriever().getThreadPoolSize();
	private static final int MAX_RESULTS = Config.sharedConfig().getRetriever().getMaxResults();
	
	
	private static ExecutorService executor = null;
	private static LimitedQueue<Runnable> taskQueue = new LimitedQueue<>(TASK_QUEUE_SIZE);
	
	private static ArrayList<RetrievalResultListener> resultListeners = new ArrayList<>();
	
	
	public static void init(){
		if(executor != null){
			shutdown();
		}
		executor = new ThreadPoolExecutor(THREAD_COUNT, THREAD_COUNT, 60, TimeUnit.SECONDS, taskQueue);
	}
	
	public static void shutdown(){
		taskQueue.clear();
		if(executor != null){
			executor.shutdown();
			executor = null;
		}
		
	}
	
	public static List<StringDoublePair> retrieve(QueryContainer query, TObjectDoubleHashMap<Retriever> retrievers, RetrieverInitializer initializer, ReadableQueryConfig qc){
		if(executor == null || executor.isShutdown()){
			init();
		}
		LinkedList<Future<Pair<RetrievalTask, List<StringDoublePair>>>> futures = new LinkedList<>();
		double wheightSum = 0;
		Set<Retriever> features = retrievers.keySet();
		for(Retriever r : features){
			if(retrievers.get(r) > 0){
				wheightSum += retrievers.get(r);
				initializer.initialize(r);
				
				futures.add(executor.submit(new RetrievalTask(r, query, qc)));
			}
		}		
		
		return handleFutures(futures, retrievers, wheightSum);
	}
	
	public static List<StringDoublePair> retrieve(String shotId, TObjectDoubleHashMap<Retriever> retrievers, RetrieverInitializer initializer, ReadableQueryConfig qc){
		if(executor == null || executor.isShutdown()){
			init();
		}
		LinkedList<Future<Pair<RetrievalTask, List<StringDoublePair>>>> futures = new LinkedList<>();
		double wheightSum = 0;
		Set<Retriever> features = retrievers.keySet();
		for(Retriever r : features){
			if(retrievers.get(r) > 0){
				wheightSum += retrievers.get(r);
				initializer.initialize(r);
				
				futures.add(executor.submit(new RetrievalTask(r, shotId, qc)));
			}
		}		
		
		return handleFutures(futures, retrievers, wheightSum);
	}
	
	
	private static List<StringDoublePair> handleFutures(LinkedList<Future<Pair<RetrievalTask, List<StringDoublePair>>>> futures, TObjectDoubleHashMap<Retriever> retrievers, double wheightSum) {
		TObjectDoubleHashMap<String> result = new TObjectDoubleHashMap<>();

		while (!futures.isEmpty()) {
			Iterator<Future<Pair<RetrievalTask, List<StringDoublePair>>>> iter = futures.iterator();
			while (iter.hasNext()) {
				Future<Pair<RetrievalTask, List<StringDoublePair>>> future = iter.next();
				if (future.isDone()) {
					try {
						Pair<RetrievalTask, List<StringDoublePair>> pair = future.get();
						double weight = retrievers.get(pair.first.getRetriever());
						List<StringDoublePair> list = pair.second;
						if(list == null){
							continue;
						}
						for(int i = 0; i < resultListeners.size(); ++i){
						  resultListeners.get(i).notify(pair.second, pair.first);
						}
						for (StringDoublePair sdp : list) {
							if (Double.isInfinite(sdp.value) || Double.isNaN(sdp.value)) {
								continue;
							}
							if (!result.containsKey(sdp.key)) {
								result.put(sdp.key, (double) 0);
							}
							result.put(sdp.key, result.get(sdp.key) + (weight * sdp.value));
						}
					} catch (InterruptedException e) {
						LOGGER.warn(LogHelper.getStackTrace(e));
					} catch (ExecutionException e) {
						LOGGER.warn(LogHelper.getStackTrace(e));
					}
					iter.remove();
				}
			}
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
			}
		}
		
		List<StringDoublePair> _return = new ArrayList<>(result.size());
		Set<String> keys = result.keySet();
		for(String key : keys){
			_return.add(new StringDoublePair(key, result.get(key)));
		}
		
		Collections.sort(_return, StringDoublePair.COMPARATOR);
		
		Set<Retriever> features = retrievers.keySet();
		for(Retriever r : features){
			r.finish();
		}
		
		if(_return.size() > MAX_RESULTS){
			_return = _return.subList(0, MAX_RESULTS);
		}

		for(StringDoublePair p : _return){
			p.value /= wheightSum;
		}
		
		return _return;
	}
	
	public static void addRetrievalResultListener(RetrievalResultListener listener){
	  if(listener == null){
	    return;
	  }
	  if(!resultListeners.contains(listener)){
	    resultListeners.add(listener);
	  }
	}
	
	public static void removeRetrievalResultListener(RetrievalResultListener listener){
	  if(listener == null){
      return;
    }
	  resultListeners.remove(listener);
	}
}
