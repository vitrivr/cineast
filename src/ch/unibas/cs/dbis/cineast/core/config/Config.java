package ch.unibas.cs.dbis.cineast.core.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ch.unibas.cs.dbis.cineast.core.config.ImageMemoryConfig.Policy;
import ch.unibas.cs.dbis.cineast.core.util.LogHelper;


public class Config {

	private Config(){}
	
	private static Properties properties = new Properties();
	private static final Logger LOGGER = LogManager.getLogger();
	
	private static ImageMemoryConfig imageMemoryConfig;
	private static DecoderConfig decoderConfig;
	private static ExtractorConfig extractorConfig;
	private static RetrieverConfig retrieverConfig;
	
	static{ //for compatibility to properties file until it is replaced by JSON config.
		
		try{
			FileInputStream fin = new FileInputStream("cineast.properties");
			properties.load(fin);
			fin.close();
			LOGGER.info("prpoerties loaded");
		}catch(FileNotFoundException e){
			LOGGER.warn("properties file not found");
		} catch (IOException e) {
			LOGGER.warn("could not read properties file");
		}
		
		
		String property;
		int softLimit = ImageMemoryConfig.DEFAULT_SOFT_LIMIT, hardLimit = ImageMemoryConfig.DEFAULT_HARD_LIMIT;
		File cacheLocation = new File(".");
		property = properties.getProperty("softImageMemoryLimit", "" + softLimit);
		try{
			softLimit = Integer.parseInt(property);
		}catch(Exception e){
			//ignore
		}
		
		property = properties.getProperty("hardImageMemoryLimit", "" + hardLimit);
		try{
			hardLimit = Integer.parseInt(property);
		}catch(Exception e){
			//ignore
		}
		
		String path = properties.getProperty("frameCacheFolder", ".");
		File folder = new File(path);
		if((folder.exists() && folder.isDirectory()) || folder.mkdirs()){
			cacheLocation = folder;
		}
		
		imageMemoryConfig = new ImageMemoryConfig(softLimit, hardLimit, Policy.AUTOMATIC, cacheLocation);
		
		
		int poolthreads = ExtractorConfig.DEFAULT_THREAD_POOL_SIZE;
		property = properties.getProperty("numbetOfPoolThreads", "" + poolthreads);
		try{
			poolthreads = Integer.parseInt(property);
		}catch(Exception e){
			LOGGER.warn("error while parsing properties: {}", LogHelper.getStackTrace(e));
		}
		
		int shotQueueSize = ExtractorConfig.DEFAULT_SHOT_QUEUE_SIZE;
		property = properties.getProperty("shotQueueSize", "" + shotQueueSize);
		try{
			shotQueueSize = Integer.parseInt(property);
		}catch(Exception e){
			LOGGER.warn("error while parsing properties: {}", LogHelper.getStackTrace(e));
		}
		
		extractorConfig = new ExtractorConfig(shotQueueSize, poolthreads, ExtractorConfig.DEFAULT_TASK_QUEUE_SIZE);
		
		
		int resultsPerModule = RetrieverConfig.DEFAULT_RESULTS_PER_MODULE, maxResults = RetrieverConfig.DEFAULT_MAX_RESULTS;
		
		property = properties.getProperty("resultsPerModule", "" + resultsPerModule);
		try{
			resultsPerModule = Integer.parseInt(property);
		}catch(Exception e){
			LOGGER.warn("error while parsing properties: {}", LogHelper.getStackTrace(e));
		}
		
		property = properties.getProperty("maxResults", "" + maxResults);
		try{
			maxResults = Integer.parseInt(property);
		}catch(Exception e){
			LOGGER.warn("error while parsing properties: {}", LogHelper.getStackTrace(e));
		}
		
		retrieverConfig = new RetrieverConfig(poolthreads, RetrieverConfig.DEFAULT_TASK_QUEUE_SIZE, maxResults, resultsPerModule);
		
		
		int maxFrameWidth = DecoderConfig.DEFAULT_MAX_FRAME_WIDTH, maxFrameHeight = DecoderConfig.DEFAULT_MAX_FRAME_HEIGHT;
		
		property = properties.getProperty("maxFrameWidth", "" + maxFrameWidth);
		try{
			maxFrameWidth = Integer.parseInt(property);
		}catch(Exception e){
			LOGGER.warn("error while parsing properties: {}", LogHelper.getStackTrace(e));
		}
		
		String threads = properties.getProperty("maxFrameHeight", "" + maxFrameHeight);
		try{
			maxFrameHeight = Integer.parseInt(threads);
		}catch(Exception e){
			LOGGER.warn("error while parsing properties: {}", LogHelper.getStackTrace(e));
		}
		
		decoderConfig = new DecoderConfig(maxFrameWidth, maxFrameHeight);
		
	}
	
	
	public static final UUID UNIQUE_ID = UUID.randomUUID();
	
	
	
	public static String getDBLocation(){
		return properties.getProperty("database", "127.0.0.1:5434/cineast");
	}
	
	public static String getDBUser(){
		return properties.getProperty("user", "cineast");
	}
	
	public static String getDBPassword(){
		return properties.getProperty("pass", "ilikemovies");
	}
/*
	public static int maxFrameWidth(){
		String threads = properties.getProperty("maxFrameWidth", "" + Integer.MAX_VALUE);
		try{
			return Integer.parseInt(threads);
		}catch(Exception e){
			LOGGER.warn("error while parsing properties: {}", LogHelper.getStackTrace(e));
		}
		return Integer.MAX_VALUE;
	}
	
	public static int maxFrameHeight(){
		String threads = properties.getProperty("maxFrameHeight", "" + Integer.MAX_VALUE);
		try{
			return Integer.parseInt(threads);
		}catch(Exception e){
			LOGGER.warn("error while parsing properties: {}", LogHelper.getStackTrace(e));
		}
		return Integer.MAX_VALUE;
	}
*/
	
	public static int getAPIPort(){
		String port = properties.getProperty("apiPort", "" + 12345);
		try{
			return Integer.parseInt(port);
		}catch(Exception e){
			LOGGER.warn("error while parsing properties: {}", LogHelper.getStackTrace(e));
		}
		return 12345;
	}
	
	/**
	 * Returns the {@link ImageMemoryConfig} as specified in the config file. If nothing is specified in the configuration file, the default values are returned, see {@link ImageMemoryConfig#ImageMemoryConfig()}
	 * @return
	 */
	public static ImageMemoryConfig getImageMemoryConfig(){
		return imageMemoryConfig;
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
}
