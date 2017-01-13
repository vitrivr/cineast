package org.vitrivr.cineast.core.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public final class APIConfig {
	private int jsonApiPort = 12345;
	private boolean enableJson = false;
	private boolean enableRest = true;
	private boolean allowExtraction = true;
	private boolean enableCLI = false;

	private enum APIMode {
		LEGACY,WEBSOCKET,NONE
	}

	@JsonCreator
	public APIConfig() {

	}

	@JsonProperty
	public int getJsonApiPort(){
		return this.jsonApiPort;
	}
	public void setJsonApiPort(int jsonApiPort) {
		if(jsonApiPort < 1){
			throw new IllegalArgumentException("jsonApiPort bust be > 0");
		}
		this.jsonApiPort = jsonApiPort;
	}

	@JsonProperty
	public boolean getEnableJsonAPI(){
		return this.enableJson;
	}
	public void setEnableJson(boolean enableJson) {
		this.enableJson = enableJson;
	}

	@JsonProperty
	public boolean getEnableRestAPI() {return this.enableRest;}
	public void setEnableRest(boolean enableRest) {
		this.enableRest = enableRest;
	}

	@JsonProperty
	public boolean getAllowExtraction(){
		return this.allowExtraction;
	}
	public void setAllowExtraction(boolean allowExtraction) {
		this.allowExtraction = allowExtraction;
	}

	@JsonProperty
	public boolean getEnableCli(){
		return this.enableCLI;
	}
	public void setEnableCLI(boolean enableCLI) {
		this.enableCLI = enableCLI;
	}
}
