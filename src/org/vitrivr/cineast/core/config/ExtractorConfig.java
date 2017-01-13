package org.vitrivr.cineast.core.config;

import java.io.File;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public final class ExtractorConfig {
	private int shotQueueSize = 5;
	private int threadPoolSize = 4;
	private int taskQueueSize = 10;
	private File outputLocation = new File(".");

	@JsonCreator
	public ExtractorConfig() {

	}

	@JsonProperty
	public int getShotQueueSize(){
		return this.shotQueueSize;
	}
	public void setShotQueueSize(int shotQueueSize) {
		this.shotQueueSize = shotQueueSize;
	}

	@JsonProperty
	public int getThreadPoolSize(){
		return this.threadPoolSize;
	}
	public void setThreadPoolSize(int threadPoolSize) {
		this.threadPoolSize = threadPoolSize;
	}

	@JsonProperty
	public int getTaskQueueSize() {
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
