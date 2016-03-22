package ch.unibas.cs.dbis.cineast.core.runtime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Callable;
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

public class QueryDispatcher implements Callable<List<LongDoublePair>> {

	private static final Logger LOGGER = LogManager.getLogger();
	
	private static final int TASK_QUEUE_SIZE = Config.getRetrieverConfig().getTaskQueueSize();
	private static final int THREAD_COUNT = Config.getRetrieverConfig().getThreadPoolSize();
	private static final int MAX_RESULTS = Config.getRetrieverConfig().getMaxResults();
	
	private HashMap<Retriever, Double> retrieverWeights;
	private QueryContainer query = null;
	private ExecutorService executor;
	private RetrieverInitializer initializer;
	private long shotId;
	
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
	
	public QueryDispatcher(HashMap<Retriever, Double> featureWeights, RetrieverInitializer initializer, long shotId){
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
	
	public List<LongDoublePair> retirieve(QueryContainer query){
		LinkedList<Future<Pair<Retriever, List<LongDoublePair>>>> futures = new LinkedList<>();
		double weightSum = 0;
		Set<Retriever> features = this.retrieverWeights.keySet();
		for(Retriever r : features){
			
			this.initializer.initialize(r);
			
			futures.add(this.executor.submit(new RetrievalTask(r, query)));
		}		
		TLongDoubleHashMap result = new TLongDoubleHashMap();
		
		while(!futures.isEmpty()){
			Iterator<Future<Pair<Retriever, List<LongDoublePair>>>> iter = futures.iterator();
			while(iter.hasNext()){
				Future<Pair<Retriever, List<LongDoublePair>>> future = iter.next();
				if(future.isDone()){
					try {
						Pair<Retriever, List<LongDoublePair>> pair = future.get();
						double weight = this.retrieverWeights.get(pair.first);
						weightSum += retrieverWeights.get(pair.first) * pair.first.getConfidenceWeight();
						List<LongDoublePair> list = pair.second;
						for(LongDoublePair ldp : list){
							if(Double.isInfinite(ldp.value) || Double.isNaN(ldp.value)){
								continue;
							}
							if(!result.containsKey(ldp.key)){
								result.put(ldp.key, 0d);
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
			} catch (InterruptedException e) {}
		}
		
		List<LongDoublePair> _return = new ArrayList<>(result.size());
		//Set<Entry<Long, Double>> entries = result.entrySet();
		long[] keys = result.keys();
		for(long key : keys){
			_return.add(new LongDoublePair(key, result.get(key)));
		}
		
		Collections.sort(_return, new Comparator<LongDoublePair>(){

			@Override
			public int compare(LongDoublePair o1, LongDoublePair o2) {
				return Double.compare(o2.value, o1.value);
			}
			
		});
		
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
		
		for(LongDoublePair p : _return){
			p.value /= weightSum;
		}
		
		return _return;
	}
	
	public List<LongDoublePair> retirieve(long shotId){
		LinkedList<Future<Pair<Retriever, List<LongDoublePair>>>> futures = new LinkedList<>();
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
		HashMap<Long, Double> result = new HashMap<>();
		
		while(!futures.isEmpty()){
			Iterator<Future<Pair<Retriever, List<LongDoublePair>>>> iter = futures.iterator();
			while(iter.hasNext()){
				Future<Pair<Retriever, List<LongDoublePair>>> future = iter.next();
				if(future.isDone()){
					try {
						Pair<Retriever, List<LongDoublePair>> pair = future.get();
						double weight = this.retrieverWeights.get(pair.first);
						weightSum += retrieverWeights.get(pair.first) * pair.first.getConfidenceWeight();
						List<LongDoublePair> list = pair.second;
						for(LongDoublePair ldp : list){
							if(Double.isInfinite(ldp.value) || Double.isNaN(ldp.value)){
								continue;
							}
							if(!result.containsKey(ldp.key)){
								result.put(ldp.key, 0d);
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
			} catch (InterruptedException e) {}
		}
		
		List<LongDoublePair> _return = new ArrayList<>(result.size());
		Set<Entry<Long, Double>> entries = result.entrySet();
		for(Entry<Long, Double> e : entries){
			_return.add(new LongDoublePair(e.getKey(), e.getValue()));
		}
		
		Collections.sort(_return, LongDoublePair.COMPARATOR);
		
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
		
		for(LongDoublePair p : _return){
			p.value /= weightSum;
		}
		
		return _return;
	}
	
	@Override
	public List<LongDoublePair> call(){
		LOGGER.entry();
		startPool();
		List<LongDoublePair> _return;
		if(this.query == null){
			_return = retirieve(this.shotId);
		}else{
			_return = retirieve(this.query);
		}
		shutDownPool();
		return LOGGER.exit(_return);
	}
}
