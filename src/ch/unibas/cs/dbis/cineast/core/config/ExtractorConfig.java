package ch.unibas.cs.dbis.cineast.core.config;

public final class ExtractorConfig {

	private final int shotQueueSize;
	private final int threadPoolSize;
	private final int taskQueueSize;
	
	public static final int DEFAULT_SHOT_QUEUE_SIZE = 5;
	public static final int DEFAULT_THREAD_POOL_SIZE = 4;
	public static final int DEFAULT_TASK_QUEUE_SIZE = 10;
	
	public ExtractorConfig(){
		this(DEFAULT_SHOT_QUEUE_SIZE, DEFAULT_THREAD_POOL_SIZE, DEFAULT_TASK_QUEUE_SIZE);
	}
	
	public ExtractorConfig(int shotQueueSize, int threadPoolSize, int taskQueueSize){
		this.shotQueueSize = shotQueueSize;
		this.threadPoolSize = threadPoolSize;
		this.taskQueueSize = taskQueueSize;
	}
	
	
	public int getShotQueueSize(){
		return this.shotQueueSize;
	}
	
	public int getThreadPoolSize(){
		return this.threadPoolSize;
	}

	public int getTaskQueueSize() {
		return this.taskQueueSize;
	}
}
