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
	
	static{ //for compatibility to properties file until it is replaced by JSON config.
		String property;
		int softLimit = 3096, hardLimit = 2048;
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
		
	}
	
	
	public static final UUID UNIQUE_ID = UUID.randomUUID();
	
	static{
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
	}
	
	public static String getDBLocation(){
		return properties.getProperty("database", "127.0.0.1:5434/cineast");
	}
	
	public static String getDBUser(){
		return properties.getProperty("user", "cineast");
	}
	
	public static String getDBPassword(){
		return properties.getProperty("pass", "ilikemovies");
	}
	
	public static int numbetOfPoolThreads(){
		String threads = properties.getProperty("numbetOfPoolThreads", "6");
		try{
			return Integer.parseInt(threads);
		}catch(Exception e){
			LOGGER.warn("error while parsing properties: {}", LogHelper.getStackTrace(e));
		}
		return 6;
	}
	
	public static int resultsPerModule(){
		String threads = properties.getProperty("resultsPerModule", "40");
		try{
			return Integer.parseInt(threads);
		}catch(Exception e){
			LOGGER.warn("error while parsing properties: {}", LogHelper.getStackTrace(e));
		}
		return 40;
	}
	
	public static int maxResults(){
		String threads = properties.getProperty("maxResults", "50");
		try{
			return Integer.parseInt(threads);
		}catch(Exception e){
			LOGGER.warn("error while parsing properties: {}", LogHelper.getStackTrace(e));
		}
		return 50;
	}
	

	public static int shotQueueSize() {
		String threads = properties.getProperty("shotQueueSize", "5");
		try{
			return Integer.parseInt(threads);
		}catch(Exception e){
			LOGGER.warn("error while parsing properties: {}", LogHelper.getStackTrace(e));
		}
		return 5;
	}
	
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
	
}
