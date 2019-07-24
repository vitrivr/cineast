package org.vitrivr.cineast.standalone.config;

import java.io.File;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public final class ExtractionPipelineConfig {

	/** Default value for size of thread-pool. */
	public static final int DEFAULT_THREADPOOL_SIZE = 4;

	/** Default value for size of task-queue. */
	public static final int DEFAULT_TASKQUEUE_SIZE = 10;

	/** Default value for size of segment-queue. */
	public static final int DEFAULT_SEGMENTQUEUE_SIZE = 10;

	/** */
	private Integer shotQueueSize = DEFAULT_THREADPOOL_SIZE;

	/** */
	private Integer threadPoolSize = DEFAULT_TASKQUEUE_SIZE;

	/** */
	private Integer taskQueueSize = DEFAULT_SEGMENTQUEUE_SIZE;

	private File outputLocation = new File(".");

	@JsonCreator
	public ExtractionPipelineConfig() {
	}

	@JsonProperty
	public Integer getShotQueueSize(){
		return this.shotQueueSize;
	}
	public void setShotQueueSize(Integer shotQueueSize) {
		this.shotQueueSize = shotQueueSize;
	}

	@JsonProperty
	public Integer getThreadPoolSize(){
		return this.threadPoolSize;
	}
	public void setThreadPoolSize(int threadPoolSize) {
		this.threadPoolSize = threadPoolSize;
	}

	@JsonProperty
	public Integer getTaskQueueSize() {
		return this.taskQueueSize;
	}
	public void setTaskQueueSize(int taskQueueSize) {
		this.taskQueueSize = taskQueueSize;
	}

	@JsonProperty
	public File getOutputLocation(){
		return this.outputLocation;
	}
	public void setOutputLocation(String outputLocation) {
		this.outputLocation = new File(outputLocation);
	}
}
