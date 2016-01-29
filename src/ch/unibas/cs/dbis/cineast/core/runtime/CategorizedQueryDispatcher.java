package ch.unibas.cs.dbis.cineast.core.runtime;

import gnu.trove.map.hash.TLongDoubleHashMap;
import gnu.trove.map.hash.TObjectDoubleHashMap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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

import ch.unibas.cs.dbis.cineast.core.config.Config;
import ch.unibas.cs.dbis.cineast.core.data.LimitedQueue;
import ch.unibas.cs.dbis.cineast.core.data.LongDoublePair;
import ch.unibas.cs.dbis.cineast.core.data.Pair;
import ch.unibas.cs.dbis.cineast.core.data.QueryContainer;
import ch.unibas.cs.dbis.cineast.core.features.retriever.Retriever;
import ch.unibas.cs.dbis.cineast.core.features.retriever.RetrieverInitializer;
import ch.unibas.cs.dbis.cineast.core.util.LogHelper;

public class CategorizedQueryDispatcher implements Callable<HashMap<String, List<LongDoublePair>>> {

	private static final Logger LOGGER = LogManager.getLogger();

	private static final int TASK_QUEUE_SIZE = 10;
	private static final int THREAD_COUNT = Config.numbetOfPoolThreads();
	private static final int MAX_RESULTS = Config.maxResults();

	private HashMap<String, HashMap<Retriever, Double>> retrieverWeights;
	private RetrieverInitializer initializer;
	private ExecutorService executor;
	private QueryContainer query = null;
	private long shotId;

	private CategorizedQueryDispatcher(HashMap<String, HashMap<Retriever, Double>> featureWeights, RetrieverInitializer initializer){
		this.retrieverWeights = featureWeights;
		this.initializer = initializer;
	}
	
	public CategorizedQueryDispatcher(HashMap<String, HashMap<Retriever, Double>> featureWeights, RetrieverInitializer initializer, QueryContainer query){
		this(featureWeights, initializer);
		this.query = query;
	}
	
	public CategorizedQueryDispatcher(HashMap<String, HashMap<Retriever, Double>> featureWeights, RetrieverInitializer initializer, long shotId){
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
	
	
	public HashMap<String, List<LongDoublePair>> retirieve(QueryContainer query){
		LinkedList<Future<Pair<Retriever, List<LongDoublePair>>>> futures = new LinkedList<>();
		Set<String> categories = this.retrieverWeights.keySet();
		TObjectDoubleHashMap<String> categoryWheightSum = new TObjectDoubleHashMap<String>();
		HashMap<String, TLongDoubleHashMap> resultMap = new HashMap<String, TLongDoubleHashMap>();
		for(String category : categories){
			double wheightSum = 0;
			HashMap<Retriever, Double> retrieverMap = this.retrieverWeights.get(category);
			Set<Retriever> features = retrieverMap.keySet();
			for(Retriever r : features){
				double weight = retrieverMap.get(r);
				if(weight == 0d){
					continue;
				}
				wheightSum += weight;
				this.initializer.initialize(r);
				
				futures.add(this.executor.submit(new RetrievalTask(r, query)));
			}
			categoryWheightSum.put(category, wheightSum);
			resultMap.put(category, new TLongDoubleHashMap());
		}
			
		
		
		while(!futures.isEmpty()){
			Iterator<Future<Pair<Retriever, List<LongDoublePair>>>> iter = futures.iterator();
			while(iter.hasNext()){
				Future<Pair<Retriever, List<LongDoublePair>>> future = iter.next();
				if(future.isDone()){
					try {
						Pair<Retriever, List<LongDoublePair>> pair = future.get();
						
						//find category
						Retriever retriever = pair.first;
						String category = null;
						for(String c : categories){
							if(this.retrieverWeights.get(c).containsKey(retriever)){
								category = c;
								break;
							}
						}
						if(category == null){
							LOGGER.error("could not determine category of retriever {}", retriever);
							continue;
						}
						double weight = this.retrieverWeights.get(category).get(pair.first);
						TLongDoubleHashMap result = resultMap.get(category);
						
						List<LongDoublePair> list = pair.second;
						for(LongDoublePair ldp : list){
							if(Double.isInfinite(ldp.value) || Double.isNaN(ldp.value)){
								continue;
							}
							if(!result.containsKey(ldp.key)){
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
			} catch (InterruptedException e) {}
		}
		
		HashMap<String, List<LongDoublePair>> _return = new HashMap<String, List<LongDoublePair>>();
		
		for(String category : categories){
			TLongDoubleHashMap result = resultMap.get(category);
			
			List<LongDoublePair> resultList = new ArrayList<>(result.size());
			//Set<Entry<Long, Double>> entries = result.entrySet();
			long[] keys = result.keys();
			for(long key : keys){
				resultList.add(new LongDoublePair(key, result.get(key)));
			}
			
			Collections.sort(resultList, LongDoublePair.COMPARATOR);
			
			Set<Retriever> features = this.retrieverWeights.get(category).keySet();
			for(Retriever r : features){
				r.finish();
			}
			
			if(resultList.size() > MAX_RESULTS){
				resultList = resultList.subList(0, MAX_RESULTS - 1);
			}
	
			for(LongDoublePair p : resultList){
				p.value /= categoryWheightSum.get(category);
			}
			_return.put(category, resultList);
		}
		
		return _return;
	}
	
	public HashMap<String, List<LongDoublePair>> retirieve(long shotId){
		LinkedList<Future<Pair<Retriever, List<LongDoublePair>>>> futures = new LinkedList<>();
		Set<String> categories = this.retrieverWeights.keySet();
		TObjectDoubleHashMap<String> categoryWheightSum = new TObjectDoubleHashMap<String>();
		HashMap<String, TLongDoubleHashMap> resultMap = new HashMap<String, TLongDoubleHashMap>();
		for(String category : categories){
			double wheightSum = 0;
			HashMap<Retriever, Double> retrieverMap = this.retrieverWeights.get(category);
			Set<Retriever> features = retrieverMap.keySet();
			for(Retriever r : features){
				double weight = retrieverMap.get(r);
				if(weight == 0d){
					continue;
				}
				wheightSum += weight;
				this.initializer.initialize(r);
				
				futures.add(this.executor.submit(new RetrievalTask(r, shotId)));
			}
			categoryWheightSum.put(category, wheightSum);
			resultMap.put(category, new TLongDoubleHashMap());
		}
			
		
		
		while(!futures.isEmpty()){
			Iterator<Future<Pair<Retriever, List<LongDoublePair>>>> iter = futures.iterator();
			while(iter.hasNext()){
				Future<Pair<Retriever, List<LongDoublePair>>> future = iter.next();
				if(future.isDone()){
					try {
						Pair<Retriever, List<LongDoublePair>> pair = future.get();
						
						//find category
						Retriever retriever = pair.first;
						String category = null;
						for(String c : categories){
							if(this.retrieverWeights.get(c).containsKey(retriever)){
								category = c;
								break;
							}
						}
						if(category == null){
							LOGGER.error("could not determine category of retriever {}", retriever);
							continue;
						}
						double weight = this.retrieverWeights.get(category).get(pair.first);
						TLongDoubleHashMap result = resultMap.get(category);
						
						List<LongDoublePair> list = pair.second;
						for(LongDoublePair ldp : list){
							if(Double.isInfinite(ldp.value) || Double.isNaN(ldp.value)){
								continue;
							}
							if(!result.containsKey(ldp.key)){
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
			} catch (InterruptedException e) {}
		}
		
		HashMap<String, List<LongDoublePair>> _return = new HashMap<String, List<LongDoublePair>>();
		
		for(String category : categories){
			TLongDoubleHashMap result = resultMap.get(category);
			
			List<LongDoublePair> resultList = new ArrayList<>(result.size());
			//Set<Entry<Long, Double>> entries = result.entrySet();
			long[] keys = result.keys();
			for(long key : keys){
				resultList.add(new LongDoublePair(key, result.get(key)));
			}
			
			Collections.sort(resultList, new Comparator<LongDoublePair>(){
	
				@Override
				public int compare(LongDoublePair o1, LongDoublePair o2) {
					return Double.compare(o2.value, o1.value);
				}
				
			});
			
			Set<Retriever> features = this.retrieverWeights.get(category).keySet();
			for(Retriever r : features){
				r.finish();
			}
			
			if(resultList.size() > MAX_RESULTS){
				resultList = resultList.subList(0, MAX_RESULTS - 1);
			}
	
			for(LongDoublePair p : resultList){
				p.value /= categoryWheightSum.get(category);
			}
			_return.put(category, resultList);
		}
		
		return _return;
	}
	
	
	@Override
	public HashMap<String, List<LongDoublePair>> call(){
		LOGGER.entry();
		startPool();
		HashMap<String, List<LongDoublePair>> _return;
		if(this.query == null){
			_return = retirieve(this.shotId);
		}else{
			_return = retirieve(this.query);
		}
		shutDownPool();
		return LOGGER.exit(_return);
	}

}
