package ch.unibas.cs.dbis.cineast.core.config;

import com.eclipsesource.json.JsonObject;

public final class DatabaseConfig {
	
	private final String host;
	private final int port;
	private final boolean plaintext;
	
	public static final String DEFAULT_HOST = "127.0.0.1";
	public static final int DEFAULT_PORT = 5890;
	public static final boolean DEFAULT_PLAINTEXT = false;
	
	
	public DatabaseConfig(String host, int port, boolean plaintext){
		if(host == null){
			throw new NullPointerException("Database location cannot be null");
		}
		if(port < 1 || port > 65535){
			throw new IllegalArgumentException(port + " is outside of valid port range");
		}
		
		this.host = host;
		this.port = port;
		this.plaintext = plaintext;
	}
	
	public DatabaseConfig(){
		this(DEFAULT_HOST, DEFAULT_PORT, DEFAULT_PLAINTEXT);
	}
	
	
	public String getHost(){
		return this.host;
	}
	
	public int getPort(){
		return this.port;
	}
	
	public boolean getPplaintext(){
		return this.plaintext;
	}
	
	/**
	 * 
	 * expects a json object of the follwing form:
	 * <pre>
	 * {
	 * 	"host" : (string)
	 * 	"port" : (int)
	 * 	"plaintext" : (boolean)
	 * }
	 * </pre>
	 * @throws NullPointerException in case provided JsonObject is null
	 * @throws IllegalArgumentException in case one of the parameters is not of the correct type or not within the valid range
	 */
	public static DatabaseConfig parse(JsonObject obj) throws NullPointerException, IllegalArgumentException{
		if(obj == null){
			throw new NullPointerException("JsonObject was null");
		}
		
		String host = DEFAULT_HOST;
		if(obj.get("host") != null){
			try{
				host = obj.get("host").asString();
			} catch(UnsupportedOperationException notastring){
				throw new IllegalArgumentException("'host' was not a string in database configuration");
			}
		}
		
		int port = DEFAULT_PORT;
		if(obj.get("port") != null){
			try{
				port = obj.get("port").asInt();
			} catch(UnsupportedOperationException notastring){
				throw new IllegalArgumentException("'port' was not an integer in database configuration");
			}
		}
		
		boolean plaintext = DEFAULT_PLAINTEXT;
		if(obj.get("plaintext") != null){
			try{
				plaintext = obj.get("plaintext").asBoolean();
			} catch(UnsupportedOperationException notastring){
				throw new IllegalArgumentException("'plaintext' was not a boolean in database configuration");
			}
		}
		
		return new DatabaseConfig(host, port, plaintext);
		
	}
}
