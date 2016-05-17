package ch.unibas.cs.dbis.cineast.core.runtime;

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

import ch.unibas.cs.dbis.cineast.core.config.Config;
import ch.unibas.cs.dbis.cineast.core.config.QueryConfig;
import ch.unibas.cs.dbis.cineast.core.data.LimitedQueue;
import ch.unibas.cs.dbis.cineast.core.data.Pair;
import ch.unibas.cs.dbis.cineast.core.data.QueryContainer;
import ch.unibas.cs.dbis.cineast.core.data.StringDoublePair;
import ch.unibas.cs.dbis.cineast.core.features.retriever.Retriever;
import ch.unibas.cs.dbis.cineast.core.features.retriever.RetrieverInitializer;
import ch.unibas.cs.dbis.cineast.core.util.LogHelper;
import gnu.trove.map.hash.TObjectDoubleHashMap;

public class ContinousQueryDispatcher {

	private static final Logger LOGGER = LogManager.getLogger();
	
	private static final int TASK_QUEUE_SIZE = Config.getRetrieverConfig().getTaskQueueSize();
	private static final int THREAD_COUNT = Config.getRetrieverConfig().getThreadPoolSize();
	private static final int MAX_RESULTS = Config.getRetrieverConfig().getMaxResults();
	
	
	private static ExecutorService executor = null;
	private static LimitedQueue<Runnable> taskQueue = new LimitedQueue<>(TASK_QUEUE_SIZE);
	
	
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
	
	public static List<StringDoublePair> retirieve(QueryContainer query, TObjectDoubleHashMap<Retriever> retrievers, RetrieverInitializer initializer, QueryConfig qc){
		if(executor == null || executor.isShutdown()){
			init();
		}
		LinkedList<Future<Pair<Retriever, List<StringDoublePair>>>> futures = new LinkedList<>();
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
	
	public static List<StringDoublePair> retirieve(String shotId, TObjectDoubleHashMap<Retriever> retrievers, RetrieverInitializer initializer, QueryConfig qc){
		if(executor == null || executor.isShutdown()){
			init();
		}
		LinkedList<Future<Pair<Retriever, List<StringDoublePair>>>> futures = new LinkedList<>();
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
	
	
	private static List<StringDoublePair> handleFutures(LinkedList<Future<Pair<Retriever, List<StringDoublePair>>>> futures, TObjectDoubleHashMap<Retriever> retrievers, double wheightSum) {
		TObjectDoubleHashMap<String> result = new TObjectDoubleHashMap<>();

		while (!futures.isEmpty()) {
			Iterator<Future<Pair<Retriever, List<StringDoublePair>>>> iter = futures.iterator();
			while (iter.hasNext()) {
				Future<Pair<Retriever, List<StringDoublePair>>> future = iter.next();
				if (future.isDone()) {
					try {
						Pair<Retriever, List<StringDoublePair>> pair = future.get();
						double weight = retrievers.get(pair.first);
						List<StringDoublePair> list = pair.second;
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
		String[] keys = (String[]) result.keys();
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
}
