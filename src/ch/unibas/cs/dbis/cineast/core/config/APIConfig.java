package ch.unibas.cs.dbis.cineast.core.config;

public final class APIConfig {

	private final int jsonApiPort;
	private final boolean allowExtraction;
	private final boolean enableCLI;
	
	public static final int DEFAULT_JSON_API_PORT = 12345;
	public static final boolean DEFAULT_ALLOW_EXTRACTION = false;
	public static final boolean DEFAULT_ENABLE_CLI = true;
	
	public APIConfig(int jsonApiPort, boolean allowExtraction, boolean enableCLI){
		if(jsonApiPort < 1){
			throw new IllegalArgumentException("jsonApiPort bust be > 0");
		}
		this.jsonApiPort = jsonApiPort;
		this.allowExtraction = allowExtraction;
		this.enableCLI = enableCLI;
	}
	
	public APIConfig(){
		this(DEFAULT_JSON_API_PORT, DEFAULT_ALLOW_EXTRACTION, DEFAULT_ENABLE_CLI);
	}
	
	public int getJsonApiPort(){
		return this.jsonApiPort;
	}
	
	public boolean getAllowExtraction(){
		return this.allowExtraction;
	}
	
	public boolean getEnableCli(){
		return this.enableCLI;
	}
	
}
