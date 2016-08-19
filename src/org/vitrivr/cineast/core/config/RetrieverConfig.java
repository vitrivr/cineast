package org.vitrivr.cineast.core.config;

import com.eclipsesource.json.JsonObject;

public final class RetrieverConfig {

	private final int threadPoolSize;
	private final int taskQueueSize;
	private final int maxResults;
	private final int resultsPerModule;
	
	public static final int DEFAULT_THREAD_POOL_SIZE = 4;
	public static final int DEFAULT_TASK_QUEUE_SIZE = 10;
	public static final int DEFAULT_MAX_RESULTS = 100;
	public static final int DEFAULT_RESULTS_PER_MODULE = 50;
	
	public RetrieverConfig(){
		this(DEFAULT_THREAD_POOL_SIZE, DEFAULT_TASK_QUEUE_SIZE, DEFAULT_MAX_RESULTS, DEFAULT_RESULTS_PER_MODULE);
	}
	
	public RetrieverConfig(int threadPoolSize, int taskQueueSize, int maxResults, int resultsPerModule){
		this.threadPoolSize = threadPoolSize;
		this.taskQueueSize = taskQueueSize;
		this.maxResults = maxResults;
		this.resultsPerModule = resultsPerModule;
	}
	
	public int getThreadPoolSize(){
		return this.threadPoolSize;
	}

	public int getTaskQueueSize() {
		return this.taskQueueSize;
	}
	
	public int getMaxResults(){
		return this.maxResults;
	}
	
	public int getMaxResultsPerModule(){
		return this.resultsPerModule;
	}
	
	/**
	 * 
	 * expects a json object of the follwing form:
	 * <pre>
	 * {
	 * 	"threadPoolSize" : (int)
	 * 	"taskQueueSize" : (int)
	 * 	"maxResults" : (int)
	 * 	"resultsPerModule" : (int)
	 * }
	 * </pre>
	 * @throws NullPointerException in case provided JsonObject is null
	 * @throws IllegalArgumentException if any of the specified parameters does not have the expected type or is outside the valid range
	 */
	public static RetrieverConfig parse(JsonObject obj) throws NullPointerException, IllegalArgumentException{
		if(obj == null){
			throw new NullPointerException("JsonObject was null");
		}
		
		int threadPoolSize = DEFAULT_THREAD_POOL_SIZE;
		if(obj.get("threadPoolSize") != null){
			try{
				threadPoolSize = obj.get("threadPoolSize").asInt();
			}catch(UnsupportedOperationException e){
				throw new IllegalArgumentException("'threadPoolSize' was not an integer in retriever configuration");
			}
			
			if(threadPoolSize <= 0){
				throw new IllegalArgumentException("'threadPoolSize' must be > 0");
			}
		}
		
		int taskQueueSize = DEFAULT_TASK_QUEUE_SIZE;
		if(obj.get("taskQueueSize") != null){
			try{
				taskQueueSize = obj.get("taskQueueSize").asInt();
			}catch(UnsupportedOperationException e){
				throw new IllegalArgumentException("'taskQueueSize' was not an integer in retriever configuration");
			}
			
			if(taskQueueSize <= 0){
				throw new IllegalArgumentException("'threadPoolSize' must be > 0");
			}
		}
		
		int maxResults = DEFAULT_MAX_RESULTS;
		if(obj.get("maxResults") != null){
			try{
				maxResults = obj.get("maxResults").asInt();
			}catch(UnsupportedOperationException e){
				throw new IllegalArgumentException("'maxResults' was not an integer in retriever configuration");
			}
			
			if(maxResults <= 0){
				throw new IllegalArgumentException("'maxResults' must be > 0");
			}
		}
		
		int resultsPerModule = DEFAULT_RESULTS_PER_MODULE;
		if(obj.get("resultsPerModule") != null){
			try{
				resultsPerModule = obj.get("resultsPerModule").asInt();
			}catch(UnsupportedOperationException e){
				throw new IllegalArgumentException("'resultsPerModule' was not an integer in retriever configuration");
			}
			
			if(resultsPerModule <= 0){
				throw new IllegalArgumentException("'resultsPerModule' must be > 0");
			}
		}
		
		return new RetrieverConfig(threadPoolSize, taskQueueSize, maxResults, resultsPerModule);
	}
}
