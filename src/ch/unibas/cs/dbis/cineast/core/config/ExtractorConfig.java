package ch.unibas.cs.dbis.cineast.core.config;

import com.eclipsesource.json.JsonObject;

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
	
	
	/**
	 * 
	 * expects a json object of the follwing form:
	 * <pre>
	 * {
	 * 	"shotQueueSize" : (int)
	 * 	"threadPoolSize" : (int)
	 * 	"taskQueueSize" : (int)
	 * }
	 * </pre>
	 * 
	 * @throws NullPointerException in case the provided JsonObject is null
	 * @throws IllegalArgumentException if any of the specified values is not a positive integer
	 * 
	 */
	public static ExtractorConfig parse(JsonObject obj){
		if(obj == null){
			throw new NullPointerException("JsonObject was null");
		}
		
		int shotQueueSize = DEFAULT_SHOT_QUEUE_SIZE;
		if(obj.get("shotQueueSize") != null){
			try{
				shotQueueSize = obj.get("shotQueueSize").asInt();
			}catch(UnsupportedOperationException e){
				throw new IllegalArgumentException("'shotQueueSize' was not an integer in extractor configuration");
			}
			
			if(shotQueueSize <= 0){
				throw new IllegalArgumentException("'shotQueueSize' must be > 0");
			}
		}
		
		int threadPoolSize = DEFAULT_THREAD_POOL_SIZE;
		if(obj.get("threadPoolSize") != null){
			try{
				threadPoolSize = obj.get("threadPoolSize").asInt();
			}catch(UnsupportedOperationException e){
				throw new IllegalArgumentException("'threadPoolSize' was not an integer in extractor configuration");
			}
			
			if(shotQueueSize <= 0){
				throw new IllegalArgumentException("'threadPoolSize' must be > 0");
			}
		}
		
		int taskQueueSize = DEFAULT_TASK_QUEUE_SIZE;
		if(obj.get("taskQueueSize") != null){
			try{
				taskQueueSize = obj.get("taskQueueSize").asInt();
			}catch(UnsupportedOperationException e){
				throw new IllegalArgumentException("'taskQueueSize' was not an integer in extractor configuration");
			}
			
			if(shotQueueSize <= 0){
				throw new IllegalArgumentException("'taskQueueSize' must be > 0");
			}
		}
		
		return new ExtractorConfig(shotQueueSize, threadPoolSize, taskQueueSize);
		
	}
}
