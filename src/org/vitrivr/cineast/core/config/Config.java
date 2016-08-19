package org.vitrivr.cineast.core.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;


public class Config {

	private Config(){}
	
	private static final Logger LOGGER = LogManager.getLogger();
	
	private static APIConfig apiConfig = new APIConfig();
	private static DatabaseConfig databaseConfig = new DatabaseConfig();
	private static DecoderConfig decoderConfig = new DecoderConfig();
	private static ExtractorConfig extractorConfig = new ExtractorConfig();
	private static ImageCacheConfig imageCacheConfig = new ImageCacheConfig();
	private static RetrieverConfig retrieverConfig = new RetrieverConfig();
	private static QueryConfig queryConfig = new QueryConfig();
	
	static{
		
		try{
			FileInputStream fin = new FileInputStream("cineast.json");
			parse(fin);
			fin.close();
			LOGGER.info("config loaded");
		}catch(FileNotFoundException e){
			LOGGER.warn("config file not found");
		} catch (IOException e) {
			LOGGER.warn("could not read config file");
		}
		
		
	}
	
	public static void parse(File configFile){
		if(configFile == null){
			LOGGER.error("config file cannot be null");
			return;
		}
		if(!configFile.exists() || configFile.isDirectory()){
			LOGGER.error("specified path is not a config file: {}", configFile.getAbsolutePath());
			return;
		}
		if(!configFile.canRead()){
			LOGGER.error("specified config file is not readable: {}", configFile.getAbsolutePath());
			return;
		}
		try{
			FileInputStream fin = new FileInputStream("cineast.json");
			parse(fin);
			fin.close();
		}catch(IOException e){
			LOGGER.error("an error occurred while reading the config file: {}", e.getMessage());
		}
	}
	
	public static void setDatabaseConfig(JsonObject obj){
		try{
			databaseConfig = DatabaseConfig.parse(obj);
		}catch(UnsupportedOperationException | IllegalArgumentException | NullPointerException e){
			LOGGER.warn("could not parse 'database' config: {}", e.getMessage());
		}
	}
	
	public static void setRetriverConfig(JsonObject obj){
		try{
			retrieverConfig = RetrieverConfig.parse(obj);
		}catch(UnsupportedOperationException | IllegalArgumentException | NullPointerException e){
			LOGGER.warn("could not parse 'retriever' config: {}", e.getMessage());
		}
	}
	
	public static void setDecoderConfig(JsonObject obj){
		try{
			decoderConfig = DecoderConfig.parse(obj);
		}catch(UnsupportedOperationException | IllegalArgumentException | NullPointerException e){
			LOGGER.warn("could not parse 'decoder' config: {}", e.getMessage());
		}
	}
	
	public static void setExtractorConfig(JsonObject obj){
		try{
			extractorConfig = ExtractorConfig.parse(obj);
		}catch(IllegalArgumentException | NullPointerException e){
			LOGGER.warn("could not parse 'extractor' config: {}", e.getMessage());
		}
	}
	
	public static void setImagecacheConfig(JsonObject obj){
		try{
			imageCacheConfig = ImageCacheConfig.parse(obj);
		}catch(UnsupportedOperationException | IllegalArgumentException | NullPointerException e){
			LOGGER.warn("could not parse 'imagecache' config: {}", e.getMessage());
		}
	}
	
	private static void parse(InputStream in) throws IOException{
		JsonObject obj = null;
		try{
			obj = JsonValue.readFrom(new InputStreamReader(in)).asObject();
		}catch(UnsupportedOperationException e){
			LOGGER.error("config file is not valid json");
			return;
		}
		
		for(String name : obj.names()){
			switch(name.toLowerCase()){
			case "database":{
				setDatabaseConfig(obj.get("database").asObject());
				break;
			}
			case "retriever":{
				setRetriverConfig(obj.get("retriever").asObject());
				break;
			}
			case "decoder":{
				setDecoderConfig(obj.get("decoder").asObject());
				break;
			}
			case "extractor":{
				setExtractorConfig(obj.get("extractor").asObject());
				break;
			}
			case "imagecache":{
				setImagecacheConfig(obj.get("imagecache").asObject());
				break;
			}
			case "api":{
				try{
					apiConfig = APIConfig.parse(obj.get("api").asObject());
				}catch(UnsupportedOperationException | IllegalArgumentException | NullPointerException e){
					LOGGER.warn("could not parse 'api' config: {}", e.getMessage());
				}
				break;
			}
			default: {
				LOGGER.info("unrecognized parameter in config: {}, ignoring", name);
			}
			}
		}
		
	}
	
	
	public static final UUID UNIQUE_ID = UUID.randomUUID();
	
	
	/**
	 * Returns the {@link ImageCacheConfig} as specified in the config file. If nothing is specified in the configuration file, the default values are returned, see {@link ImageCacheConfig#ImageMemoryConfig()}
	 * @return
	 */
	public static ImageCacheConfig getImageMemoryConfig(){
		return imageCacheConfig;
	}
	
	public static ExtractorConfig getExtractorConfig(){
		return extractorConfig;
	}
	
	public static RetrieverConfig getRetrieverConfig(){
		return retrieverConfig;
	}
	
	public static DecoderConfig getDecoderConfig(){
		return decoderConfig;
	}
	
	public static APIConfig getApiConfig(){
		return apiConfig;
	}
	
	public static DatabaseConfig getDatabaseConfig(){
		return databaseConfig;
	}
	
	public static QueryConfig getQueryConfig(){
		return queryConfig;
	}
}
