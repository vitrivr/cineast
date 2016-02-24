package ch.unibas.cs.dbis.cineast.core.runtime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
import ch.unibas.cs.dbis.cineast.core.data.LimitedQueue;
import ch.unibas.cs.dbis.cineast.core.data.LongDoublePair;
import ch.unibas.cs.dbis.cineast.core.data.Pair;
import ch.unibas.cs.dbis.cineast.core.data.QueryContainer;
import ch.unibas.cs.dbis.cineast.core.features.retriever.Retriever;
import ch.unibas.cs.dbis.cineast.core.features.retriever.RetrieverInitializer;
import ch.unibas.cs.dbis.cineast.core.util.LogHelper;
import gnu.trove.map.hash.TLongDoubleHashMap;
import gnu.trove.map.hash.TObjectDoubleHashMap;

public class ContinousQueryDispatcher {

	private static final Logger LOGGER = LogManager.getLogger();
	
	private static final int TASK_QUEUE_SIZE = 10;
	private static final int THREAD_COUNT = Config.numbetOfPoolThreads();
	private static final int MAX_RESULTS = Config.maxResults();
	private static final Comparator<LongDoublePair> COMPARATOR = new Comparator<LongDoublePair>(){

		@Override
		public int compare(LongDoublePair o1, LongDoublePair o2) {
			return Double.compare(o2.value, o1.value);
		}
		
	};
	
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
	
	public static List<LongDoublePair> retirieve(QueryContainer query, TObjectDoubleHashMap<Retriever> retrievers, RetrieverInitializer initializer, String resultCacheName){
		if(executor == null || executor.isShutdown()){
			init();
		}
		LinkedList<Future<Pair<Retriever, List<LongDoublePair>>>> futures = new LinkedList<>();
		double wheightSum = 0;
		Set<Retriever> features = retrievers.keySet();
		for(Retriever r : features){
			if(retrievers.get(r) > 0){
				wheightSum += retrievers.get(r);
				initializer.initialize(r);
				
				futures.add(executor.submit(new RetrievalTask(r, query, resultCacheName)));
			}
		}		
		
		return handleFutures(futures, retrievers, wheightSum);
	}
	
	public static List<LongDoublePair> retirieve(long shotId, TObjectDoubleHashMap<Retriever> retrievers, RetrieverInitializer initializer, String resultCacheName){
		if(executor == null || executor.isShutdown()){
			init();
		}
		LinkedList<Future<Pair<Retriever, List<LongDoublePair>>>> futures = new LinkedList<>();
		double wheightSum = 0;
		Set<Retriever> features = retrievers.keySet();
		for(Retriever r : features){
			if(retrievers.get(r) > 0){
				wheightSum += retrievers.get(r);
				initializer.initialize(r);
				
				futures.add(executor.submit(new RetrievalTask(r, shotId, resultCacheName)));
			}
		}		
		
		return handleFutures(futures, retrievers, wheightSum);
	}
	
	
	private static List<LongDoublePair> handleFutures(LinkedList<Future<Pair<Retriever, List<LongDoublePair>>>> futures, TObjectDoubleHashMap<Retriever> retrievers, double wheightSum) {
		TLongDoubleHashMap result = new TLongDoubleHashMap();

		while (!futures.isEmpty()) {
			Iterator<Future<Pair<Retriever, List<LongDoublePair>>>> iter = futures.iterator();
			while (iter.hasNext()) {
				Future<Pair<Retriever, List<LongDoublePair>>> future = iter.next();
				if (future.isDone()) {
					try {
						Pair<Retriever, List<LongDoublePair>> pair = future.get();
						double weight = retrievers.get(pair.first);
						List<LongDoublePair> list = pair.second;
						for (LongDoublePair ldp : list) {
							if (Double.isInfinite(ldp.value) || Double.isNaN(ldp.value)) {
								continue;
							}
							if (!result.containsKey(ldp.key)) {
								result.put(ldp.key, (double) 0);
							}
							result.put(ldp.key, result.get(ldp.key) + (weight * ldp.value));
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
		
		List<LongDoublePair> _return = new ArrayList<>(result.size());
		long[] keys = result.keys();
		for(long key : keys){
			_return.add(new LongDoublePair(key, result.get(key)));
		}
		
		Collections.sort(_return, COMPARATOR);
		
		Set<Retriever> features = retrievers.keySet();
		for(Retriever r : features){
			r.finish();
		}
		
		if(_return.size() > MAX_RESULTS){
			_return = _return.subList(0, MAX_RESULTS);
		}

		for(LongDoublePair p : _return){
			p.value /= wheightSum;
		}
		
		return _return;
	}
}
