package ch.unibas.cs.dbis.cineast.core.config;

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
	
	private static void parse(InputStream in) throws IOException{
		JsonObject obj = null;
		try{
			obj = JsonValue.readFrom(new InputStreamReader(in)).asObject();
		}catch(UnsupportedOperationException e){
			LOGGER.error("config file is not valid json");
			return;
		}
		
		for(String name : obj.names()){
			switch(name){
			case "database":{
				try{
					databaseConfig = DatabaseConfig.parse(obj.get("database").asObject());
				}catch(UnsupportedOperationException | IllegalArgumentException | NullPointerException e){
					LOGGER.warn("could not parse 'database' config: {}", e.getMessage());
				}
				break;
			}
			case "retriever":{
				try{
					retrieverConfig = RetrieverConfig.parse(obj.get("retriever").asObject());
				}catch(UnsupportedOperationException | IllegalArgumentException | NullPointerException e){
					LOGGER.warn("could not parse 'retriever' config: {}", e.getMessage());
				}
				break;
			}
			case "decoder":{
				try{
					decoderConfig = DecoderConfig.parse(obj.get("decoder").asObject());
				}catch(UnsupportedOperationException | IllegalArgumentException | NullPointerException e){
					LOGGER.warn("could not parse 'decoder' config: {}", e.getMessage());
				}
				break;
			}
			case "extractor":{
				try{
					extractorConfig = ExtractorConfig.parse(obj.get("extractor").asObject());
				}catch(IllegalArgumentException | NullPointerException e){
					LOGGER.warn("could not parse 'extractor' config: {}", e.getMessage());
				}
				break;
			}
			case "imagecache":{
				try{
					imageCacheConfig = ImageCacheConfig.parse(obj.get("imagecache").asObject());
				}catch(UnsupportedOperationException | IllegalArgumentException | NullPointerException e){
					LOGGER.warn("could not parse 'imagecache' config: {}", e.getMessage());
				}
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
}
