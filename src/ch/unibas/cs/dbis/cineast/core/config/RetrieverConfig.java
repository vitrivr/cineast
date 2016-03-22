package ch.unibas.cs.dbis.cineast.core.config;

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
}
