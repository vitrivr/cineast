package ch.unibas.cs.dbis.cineast.core.config;

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
	
}
