package ch.unibas.cs.dbis.cineast.core.config;

import com.eclipsesource.json.JsonObject;

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
	
	/**
	 * 
	 * expects a json object of the follwing form:
	 * <pre>
	 * {
	 * 	"jsonApiPort" : (int)
	 * 	"allowExtraction" : (boolean)
	 * 	"enableCLI" : (boolean)
	 * }
	 * </pre>
	 * @throws NullPointerException in case provided JsonObject is null
	 * @throws IllegalArgumentException if any of the specified parameters does not have the expected type or is outside the valid range
	 */
	public static APIConfig parse(JsonObject obj) throws NullPointerException, IllegalArgumentException{
		if(obj == null){
			throw new NullPointerException("JsinObject was null");
		}
		
		int jsonApiPort = DEFAULT_JSON_API_PORT;
		if(obj.get("jsonApiPort") != null){
			try{
				jsonApiPort = obj.get("jsonApiPort").asInt();
			}catch(UnsupportedOperationException e){
				throw new IllegalArgumentException("'jsonApiPort' was not an integer in API configuration");
			}
			
			if(jsonApiPort <= 0 || jsonApiPort >= 65535){
				throw new IllegalArgumentException("'jsonApiPort' must be > 0 and < 65536");
			}
		}
		
		boolean allowExtraction = DEFAULT_ALLOW_EXTRACTION;
		if(obj.get("allowExtraction") != null){
			try{
				allowExtraction = obj.get("allowExtraction").asBoolean();
			}catch(UnsupportedOperationException e){
				throw new IllegalArgumentException("'allowExtraction' was not a boolean in API configuration");
			}
		}
		
		boolean enableCLI = DEFAULT_ENABLE_CLI;
		if(obj.get("enableCLI") != null){
			try{
				enableCLI = obj.get("enableCLI").asBoolean();
			}catch(UnsupportedOperationException e){
				throw new IllegalArgumentException("'enableCLI' was not a boolean in API configuration");
			}
		}
		
		return new APIConfig(jsonApiPort, allowExtraction, enableCLI);
		
	}
	
}
