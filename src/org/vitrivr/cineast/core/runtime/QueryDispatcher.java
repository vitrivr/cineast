package org.vitrivr.cineast.core.runtime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.config.Config;
import org.vitrivr.cineast.core.data.LimitedQueue;
import org.vitrivr.cineast.core.data.Pair;
import org.vitrivr.cineast.core.data.QueryContainer;
import org.vitrivr.cineast.core.data.StringDoublePair;
import org.vitrivr.cineast.core.features.retriever.Retriever;
import org.vitrivr.cineast.core.features.retriever.RetrieverInitializer;
import org.vitrivr.cineast.core.util.LogHelper;

import gnu.trove.map.hash.TObjectDoubleHashMap;

public class QueryDispatcher implements Callable<List<StringDoublePair>> {

	private static final Logger LOGGER = LogManager.getLogger();
	
	private static final int TASK_QUEUE_SIZE = Config.getRetrieverConfig().getTaskQueueSize();
	private static final int THREAD_COUNT = Config.getRetrieverConfig().getThreadPoolSize();
	private static final int MAX_RESULTS = Config.getRetrieverConfig().getMaxResults();
	
	private HashMap<Retriever, Double> retrieverWeights;
	private QueryContainer query = null;
	private ExecutorService executor;
	private RetrieverInitializer initializer;
	private String shotId = null;
	
	private QueryDispatcher(HashMap<Retriever, Double> featureWeights, RetrieverInitializer initializer){
		this.retrieverWeights = featureWeights;
		this.initializer = initializer;
		Set<Retriever> features = this.retrieverWeights.keySet();
		LinkedList<Retriever> removeList = new LinkedList<Retriever>();
		for(Retriever r : features){
			if(this.retrieverWeights.get(r) <= 0){
				removeList.add(r);
			}
		}
		for(Retriever r : removeList){
			this.retrieverWeights.remove(r);
		}
	}
	
	public QueryDispatcher(HashMap<Retriever, Double> featureWeights, RetrieverInitializer initializer, QueryContainer query){
		this(featureWeights, initializer);
		this.query = query;
	}
	
	public QueryDispatcher(HashMap<Retriever, Double> featureWeights, RetrieverInitializer initializer, String shotId){
		this(featureWeights, initializer);
		this.shotId = shotId;
	}
	
	public void startPool(){
		if(this.executor == null || this.executor.isShutdown()){
			LimitedQueue<Runnable> taskQueue = new LimitedQueue<>(TASK_QUEUE_SIZE);
			this.executor = new ThreadPoolExecutor(THREAD_COUNT, THREAD_COUNT, 60, TimeUnit.SECONDS, taskQueue);
		}
	}
	
	public void shutDownPool(){
		if(this.executor != null){
			this.executor.shutdown();
		}
	}
	
	public List<StringDoublePair> retirieve(QueryContainer query){
		LinkedList<Future<Pair<Retriever, List<StringDoublePair>>>> futures = new LinkedList<>();
		double weightSum = 0;
		Set<Retriever> features = this.retrieverWeights.keySet();
		for(Retriever r : features){
			
			this.initializer.initialize(r);
			
			futures.add(this.executor.submit(new RetrievalTask(r, query)));
		}		
		TObjectDoubleHashMap<String> result = new TObjectDoubleHashMap<>();
		
		while(!futures.isEmpty()){
			Iterator<Future<Pair<Retriever, List<StringDoublePair>>>> iter = futures.iterator();
			while(iter.hasNext()){
				Future<Pair<Retriever, List<StringDoublePair>>> future = iter.next();
				if(future.isDone()){
					try {
						Pair<Retriever, List<StringDoublePair>> pair = future.get();
						double weight = this.retrieverWeights.get(pair.first);
						weightSum += retrieverWeights.get(pair.first);
						List<StringDoublePair> list = pair.second;
						for(StringDoublePair sdp : list){
							if(Double.isInfinite(sdp.value) || Double.isNaN(sdp.value)){
								continue;
							}
							if(!result.containsKey(sdp.key)){
								result.put(sdp.key, 0d);
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
			} catch (InterruptedException e) {}
		}
		
		List<StringDoublePair> _return = new ArrayList<>(result.size());
		//Set<Entry<Long, Double>> entries = result.entrySet();
		String[] keys = (String[]) result.keys();
		for(String key : keys){
			_return.add(new StringDoublePair(key, result.get(key)));
		}
		
		Collections.sort(_return, StringDoublePair.COMPARATOR);
		
		 features = this.retrieverWeights.keySet();
		for(Retriever r : features){
			r.finish();
		}
		
		if(_return.size() > MAX_RESULTS){
			_return = _return.subList(0, MAX_RESULTS - 1);
		}

		if(weightSum <= 0d){
			weightSum = 1d;
		}
		
		for(StringDoublePair p : _return){
			p.value /= weightSum;
		}
		
		return _return;
	}
	
	public List<StringDoublePair> retirieve(String shotId){
		LinkedList<Future<Pair<Retriever, List<StringDoublePair>>>> futures = new LinkedList<>();
		double weightSum = 0;
		Set<Retriever> features = this.retrieverWeights.keySet();
		for(Retriever r : features){
			if(this.retrieverWeights.get(r) <= 0){
				continue;
			}
			this.initializer.initialize(r);
			
			futures.add(
					this.executor.submit(new RetrievalTask(r, shotId))
					);
		}		
		TObjectDoubleHashMap<String> result = new TObjectDoubleHashMap<>();
		
		while(!futures.isEmpty()){
			Iterator<Future<Pair<Retriever, List<StringDoublePair>>>> iter = futures.iterator();
			while(iter.hasNext()){
				Future<Pair<Retriever, List<StringDoublePair>>> future = iter.next();
				if(future.isDone()){
					try {
						Pair<Retriever, List<StringDoublePair>> pair = future.get();
						double weight = this.retrieverWeights.get(pair.first);
						weightSum += retrieverWeights.get(pair.first);
						List<StringDoublePair> list = pair.second;
						for(StringDoublePair sdp : list){
							if(Double.isInfinite(sdp.value) || Double.isNaN(sdp.value)){
								continue;
							}
							if(!result.containsKey(sdp.key)){
								result.put(sdp.key, 0d);
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
			} catch (InterruptedException e) {}
		}
		
		List<StringDoublePair> _return = new ArrayList<>(result.size());
		String[] keys = (String[]) result.keys();
		for(String key : keys){
			_return.add(new StringDoublePair(key, result.get(key)));
		}
		
		Collections.sort(_return, StringDoublePair.COMPARATOR);
		
		 features = this.retrieverWeights.keySet();
		for(Retriever r : features){
			r.finish();
		}
		
		if(_return.size() > MAX_RESULTS){
			_return = _return.subList(0, MAX_RESULTS - 1);
		}

		if(weightSum <= 0d){
			weightSum = 1d;
		}
		
		for(StringDoublePair p : _return){
			p.value /= weightSum;
		}
		
		return _return;
	}
	
	@Override
	public List<StringDoublePair> call(){
		LOGGER.entry();
		startPool();
		List<StringDoublePair> _return;
		if(this.query == null){
			_return = retirieve(this.shotId);
		}else{
			_return = retirieve(this.query);
		}
		shutDownPool();
		return LOGGER.exit(_return);
	}
}
