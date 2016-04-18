package ch.unibas.cs.dbis.cineast.core.config;

import com.eclipsesource.json.JsonObject;

public final class DatabaseConfig {
	
	private final String location;
	private final String user;
	private final String password;
	
	public static final String DEFAULT_LOCATION = "127.0.0.1:5432/cineast";
	public static final String DEFAULT_USER = "cineast";
	public static final String DEFAULT_PASSWORD = "ilikemovies";
	
	
	public DatabaseConfig(String location, String user, String password){
		if(location == null){
			throw new NullPointerException("Database location cannot be null");
		}
		if(user == null){
			throw new NullPointerException("Database user cannot be null");
		}
		if(password == null){
			throw new NullPointerException("Database password cannot be null");
		}
		
		this.location = location;
		this.user = user;
		this.password = password;
	}
	
	public DatabaseConfig(){
		this(DEFAULT_LOCATION, DEFAULT_USER, DEFAULT_PASSWORD);
	}
	
	
	public String getLocation(){
		return this.location;
	}
	
	public String getUser(){
		return this.user;
	}
	
	public String getPassword(){
		return this.password;
	}
	
	/**
	 * 
	 * expects a json object of the follwing form:
	 * <pre>
	 * {
	 * 	"location" : (string)
	 * 	"user" : (string)
	 * 	"password" : (string)
	 * }
	 * </pre>
	 * @throws NullPointerException in case provided JsonObject is null
	 * @throws IllegalArgumentException in case one of the specified parameters is not a string
	 */
	public static DatabaseConfig parse(JsonObject obj) throws NullPointerException, IllegalArgumentException{
		if(obj == null){
			throw new NullPointerException("JsonObject was null");
		}
		
		String location = DEFAULT_LOCATION;
		if(obj.get("location") != null){
			try{
				location = obj.get("location").asString();
			} catch(UnsupportedOperationException notastring){
				throw new IllegalArgumentException("'location' was not a string in database configuration");
			}
		}
		
		String user = DEFAULT_USER;
		if(obj.get("user") != null){
			try{
				user = obj.get("user").asString();
			} catch(UnsupportedOperationException notastring){
				throw new IllegalArgumentException("'user' was not a string in database configuration");
			}
		}
		
		String password = DEFAULT_PASSWORD;
		if(obj.get("password") != null){
			try{
				location = obj.get("password").asString();
			} catch(UnsupportedOperationException notastring){
				throw new IllegalArgumentException("'password' was not a string in database configuration");
			}
		}
		
		return new DatabaseConfig(location, user, password);
		
	}
}
