package ch.unibas.cs.dbis.cineast.core.runtime;

import gnu.trove.iterator.TLongIterator;
import gnu.trove.map.hash.TLongDoubleHashMap;
import gnu.trove.map.hash.TObjectDoubleHashMap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

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

public class MultiCombinationQueryDispatcher<K> {

private static final Logger LOGGER = LogManager.getLogger();
	
	private static final int TASK_QUEUE_SIZE = 10;
	private static final int THREAD_COUNT = Config.numbetOfPoolThreads();
	private static final int MAX_RESULTS = Config.maxResults();
	private static final Comparator<LongDoublePair> comparator = new Comparator<LongDoublePair>(){

		@Override
		public int compare(LongDoublePair o1, LongDoublePair o2) {
			return Double.compare(o2.value, o1.value);
		}
		
	};
	
	private HashSet<Retriever> retrievers = new HashSet<>();
	private TObjectDoubleHashMap<K> weightSums = new TObjectDoubleHashMap<K>();
	private RetrieverInitializer initializer;
	private HashMap<K, TObjectDoubleHashMap<Retriever>> retrieverCategories;
	private ExecutorService executor;
	
	public MultiCombinationQueryDispatcher(HashMap<K, TObjectDoubleHashMap<Retriever>> retrieverCategories, RetrieverInitializer initializer){
		this.retrieverCategories = retrieverCategories;
		this.initializer = initializer;
		
		Iterator<K> categoryNameIterator = retrieverCategories.keySet().iterator();
		while(categoryNameIterator.hasNext()){
			K categoryName = categoryNameIterator.next();
			TObjectDoubleHashMap<Retriever> categroy = retrieverCategories.get(categoryName);
			Set<Retriever> keyset = categroy.keySet();
			this.retrievers.addAll(keyset);
			
			double weightSum = 0;
			for(Retriever r : keyset){
				weightSum += categroy.get(r);
			}
			this.weightSums.put(categoryName, weightSum);
		}
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
	
	public synchronized ConcurrentHashMap<K, Future<List<LongDoublePair>>> retrieve(QueryContainer container){
		startPool();
		LinkedList<Future<Pair<Retriever, List<LongDoublePair>>>> futures = new LinkedList<>();
		for(Retriever r : this.retrievers){
			
			this.initializer.initialize(r);
			futures.add(
					this.executor.submit(
							new RetrievalTask(r, container)));
		}		
		
		return handleFutures(futures);
		
	}
	
	public synchronized ConcurrentHashMap<K, Future<List<LongDoublePair>>> retrieve(long shotId){
		startPool();
		LinkedList<Future<Pair<Retriever, List<LongDoublePair>>>> futures = new LinkedList<>();
		for(Retriever r : this.retrievers){
			
			this.initializer.initialize(r);
			futures.add(
					this.executor.submit(
							new RetrievalTask(r, shotId)));
		}		
		
		return handleFutures(futures);
		
	}
	
	private synchronized ConcurrentHashMap<K, Future<List<LongDoublePair>>> handleFutures(LinkedList<Future<Pair<Retriever, List<LongDoublePair>>>> futures){
		ConcurrentHashMap<K, Future<List<LongDoublePair>>> _return = new ConcurrentHashMap<K, Future<List<LongDoublePair>>>();
		ConcurrentHashMap<K, Set<Retriever>> retrieverSet = new ConcurrentHashMap<K, Set<Retriever>>();
		ConcurrentHashMap<K, List<LongDoublePair>> results = new ConcurrentHashMap<K, List<LongDoublePair>>();
		
		Set<K> keySet = retrieverCategories.keySet();
		
		for(K k : keySet){
			Set<Retriever> rset = new HashSet<Retriever>();
			rset.addAll(this.retrieverCategories.get(k).keySet());
			retrieverSet.put(k, rset);
		}
		
		for(K k : keySet){
			_return.put(k, new MultiCombinationQueryFuture(k, retrieverSet, results));
		}
		
		DispatcherThread thread = new DispatcherThread(futures, retrieverSet, results);
		
		for(K k : keySet){
			((MultiCombinationQueryFuture)_return.get(k)).thread = thread;
		}
		
		thread.start();
		
		return _return;
	}
	
	class DispatcherThread extends Thread{
		
		private LinkedList<Future<Pair<Retriever, List<LongDoublePair>>>> futures;
		private ConcurrentHashMap<K, Set<Retriever>> retrievers;
		private ConcurrentHashMap<K, List<LongDoublePair>> results;
		private Set<K> keySet;
		private HashMap<K, TLongDoubleHashMap> resultMap = new HashMap<K, TLongDoubleHashMap>();
		private boolean canceled = false;
		
		DispatcherThread(LinkedList<Future<Pair<Retriever, List<LongDoublePair>>>> futures, ConcurrentHashMap<K, Set<Retriever>> retrievers, ConcurrentHashMap<K, List<LongDoublePair>> results){
			this.futures = futures;
			this.retrievers = retrievers;
			this.results = results;
			this.keySet = this.retrievers.keySet();
			for(K k : this.keySet){
				this.resultMap.put(k, new TLongDoubleHashMap());
			}
		}
		
		public void run(){
			while(!canceled && !futures.isEmpty()){
				Iterator<Future<Pair<Retriever, List<LongDoublePair>>>> iter = futures.iterator();
				while(iter.hasNext()){
					Future<Pair<Retriever, List<LongDoublePair>>> future = iter.next();
					if(future.isDone()){
						iter.remove();
						Retriever r = null;
						try {
							Pair<Retriever, List<LongDoublePair>> pair = future.get();
							r = pair.first;
							List<LongDoublePair> list = pair.second;
							
							for(K k : this.keySet){
								double weightSum = weightSums.contains(k) ? weightSums.get(k) : 0d;
								weightSum += retrieverCategories.get(k).get(pair.first) * pair.first.getConfidenceWeight();
								weightSums.put(k, weightSum);
								
								TLongDoubleHashMap result = this.resultMap.get(k);
								double weight = MultiCombinationQueryDispatcher.this.retrieverCategories.get(k).get(r);
								for(LongDoublePair ldp : list){
									if(Double.isInfinite(ldp.value) || Double.isNaN(ldp.value)){
										continue;
									}
									if(!result.containsKey(ldp.key)){
										result.put(ldp.key, 0d);
									}
									result.put(ldp.key, result.get(ldp.key) + (weight * ldp.value));
								}
							}
							
							for(K k : this.keySet){
								
								Set<Retriever> retrieversToFinish = this.retrievers.get(k);
								
								if(retrieversToFinish.size() == 1 && retrieversToFinish.contains(r)){ //done, prepare result list
									TLongDoubleHashMap result = this.resultMap.get(k);
									List<LongDoublePair> resultList = new ArrayList<LongDoublePair>(result.size());
									TLongIterator keys = result.keySet().iterator();
									while(keys.hasNext()){
										long l = keys.next();
										resultList.add(new LongDoublePair(l, result.get(l)));
									}
									
									Collections.sort(resultList, comparator);
									
									if(resultList.size() > MAX_RESULTS){
										resultList = resultList.subList(0, MAX_RESULTS - 1);
									}

									double weightSum = weightSums.get(k);
									
									if(weightSum <= 0d){
										weightSum = 1d;
									}
									
									for(LongDoublePair p : resultList){
										p.value /= weightSum;
									}
									
									this.results.put(k, resultList);
									
								}
								
								retrieversToFinish.remove(r);
								r.finish();
							}
							
						} catch (InterruptedException e) {
							LOGGER.warn(LogHelper.getStackTrace(e));
						} catch (ExecutionException e) {
							LOGGER.warn(LogHelper.getStackTrace(e));
						} 
					}
				}
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {}
			}
			shutDownPool();
		}
		
	}
	
	class MultiCombinationQueryFuture implements Future<List<LongDoublePair>>{

		private K key;
		private ConcurrentHashMap<K, Set<Retriever>> retrievers;
		private ConcurrentHashMap<K, List<LongDoublePair>> results;
		private DispatcherThread thread = null;
		
		MultiCombinationQueryFuture(K key, ConcurrentHashMap<K, Set<Retriever>> retrievers, ConcurrentHashMap<K, List<LongDoublePair>> results){
			this.key = key;
			this.retrievers = retrievers;
			this.results = results;
		}
		
		@Override
		public boolean cancel(boolean mayInterruptIfRunning) {
			if(isDone()){
				return false;
			}
			if(this.thread.canceled){
				return false;
			}
			this.thread.canceled = true;
			return true;
		}

		@Override
		public boolean isCancelled() {
			return thread.canceled;
		}

		@Override
		public boolean isDone() {
			return this.retrievers.get(this.key).isEmpty();
		}

		@Override
		public List<LongDoublePair> get() throws InterruptedException,
				ExecutionException {
			while(!isDone()){
				if(isCancelled()){
					throw new CancellationException();
				}
				Thread.sleep(100);
			}
			return this.results.get(this.key);
		}

		@Override
		public List<LongDoublePair> get(long timeout, TimeUnit unit)
				throws InterruptedException, ExecutionException, TimeoutException {
			long remainingTime = unit.toMillis(timeout);
			while(remainingTime > 0 && !isDone()){
				if(isCancelled()){
					throw new CancellationException();
				}
				if(remainingTime < 100){
					Thread.sleep(remainingTime);
					remainingTime = 0;
				}else{
					Thread.sleep(100);
					remainingTime -= 100;
				}
				
			}
			if(!isDone()){
				throw new TimeoutException();
			}
			return this.results.get(this.key);
		}
		
	}
}

