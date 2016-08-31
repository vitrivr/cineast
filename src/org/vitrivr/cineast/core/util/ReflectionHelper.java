package org.vitrivr.cineast.core.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.features.abstracts.AbstractFeatureModule;
import org.vitrivr.cineast.core.features.extractor.Extractor;
import org.vitrivr.cineast.core.features.retriever.Retriever;

import com.eclipsesource.json.JsonObject;

public class ReflectionHelper {

	private static final Logger LOGGER = LogManager.getLogger();
	
	public static final String FEATURE_MODULE_PACKAGE = "org.vitrivr.cineast.core.features";
	public static final String EXPORTER_PACKAGE = "org.vitrivr.cineast.core.features.exporter";
	
	/**
	 * creates a new instance of an exporter as specified by the provided json.
	 * @param json see {@link #instanciateFromJson(JsonObject, Class, String)}}
	 * @return an instance of the requested class or null in case of error
	 */
	public static final Extractor newExporter(JsonObject json){
		try {
			return instanciateFromJson(json, Extractor.class, EXPORTER_PACKAGE);
		} catch (IllegalArgumentException | InstantiationException | ClassNotFoundException e) {
			LOGGER.error(LogHelper.getStackTrace(e));
		}
		return null;
	}
	
	/**
	 * creates a new instance of an {@link AbstractFeatureModule} as specified by the provided json.
	 * @param json see {@link #instanciateFromJson(JsonObject, Class, String)}}
	 * @return an instance of the requested class or null in case of error
	 */
	public static final AbstractFeatureModule newFeatureModule(JsonObject json){
		try {
			return instanciateFromJson(json, AbstractFeatureModule.class, FEATURE_MODULE_PACKAGE);
		} catch (IllegalArgumentException | InstantiationException | ClassNotFoundException e) {
			LOGGER.error(LogHelper.getStackTrace(e));
		}
		return null;
	}
	
	/**
	 * creates a new instance of an {@link AbstractFeatureModule} as specified by the provided json.
	 * @param json see {@link #instanciateFromJson(JsonObject, Class, String)}}
	 * @return an instance of the requested class or null in case of error
	 */
	public static final Extractor newExtractor(JsonObject json){
		try {
			return instanciateFromJson(json, Extractor.class, FEATURE_MODULE_PACKAGE);
		} catch (IllegalArgumentException | InstantiationException | ClassNotFoundException e) {
			LOGGER.error(LogHelper.getStackTrace(e));
		}
		return null;
	}
	
	/**
	 * creates a new instance of a {@link Retriever}} as specified by the provided json.
	 * @param json see {@link #instanciateFromJson(JsonObject, Class, String)}}
	 * @return an instance of the requested class or null in case of error
	 */
	public static final Retriever newRetriever(JsonObject json){
		try {
			return instanciateFromJson(json, Retriever.class, FEATURE_MODULE_PACKAGE);
		} catch (IllegalArgumentException | InstantiationException | ClassNotFoundException e) {
			LOGGER.error(LogHelper.getStackTrace(e));
		}
		return null;
	}
	
	private static Class<?>[] getClassArray(Object... args) {
		Class<?>[] cls = new Class<?>[args.length];
		int i = 0;
		for (Object o : args) {
			cls[i++] = o.getClass();
		}
		return cls;
	}
	
	public static <T> T instanciate(Class<? extends T> cl, Object... args) {
		try {
			Constructor<? extends T> con = cl.getConstructor(getClassArray(args));
			return con.newInstance(args);
		} catch (Exception e) {
			if (e instanceof InvocationTargetException) {
				LOGGER.error("InvocationTargetException: {}", LogHelper.getStackTrace(((InvocationTargetException) e).getCause()));
			} else {
				LOGGER.error(LogHelper.getStackTrace(e));
			}
		}
		return null;
	}
	
	
	/**
	 * Instantiates an object from a provided JSON with the following structure:
	 * {
	 * 		"name" : the name of the class, (optional)
	 * 		"class" : the class name including package name of the class, (optional)
	 * 		"config" : JSON object containing configuration which is passed as is to the constructor (optional)
	 * }
	 * 
	 * Either 'name' or 'class' needs to be set, 'name' has priority over 'class'.
	 * 
	 * @param json
	 * @param expectedSuperClass
	 * @param expectedPackage
	 * @throws IllegalArgumentException in case neither 'name' nor 'class' is specified
	 * @throws InstantiationException in case instantiation fails or the requested class does not match the expected super class
	 * @throws ClassNotFoundException in case the class to instantiate was not found
	 */
	public static <T> T instanciateFromJson(JsonObject json, Class<T> expectedSuperClass, String expectedPackage) throws IllegalArgumentException, InstantiationException, ClassNotFoundException{
		
		Class<T> targetClass = getClassFromJson(json, expectedSuperClass, expectedPackage);
		
		if(targetClass == null){
			throw new ClassNotFoundException("No class found matching specification in " + json.toString());
		}
		
		T instance = null;
		if(json.get("config") == null){
			instance = instanciate(targetClass);
		}else{
			try{
				instance = instanciate(targetClass, json.get("config").asObject());
			}catch(UnsupportedOperationException notAnObject){
				LOGGER.warn("'config' was not an object during class instanciation in instanciateFromJson");
			}
		}
		
		if(instance == null){
			throw new InstantiationException();
		}
		
		return instance;
		
	}
	
	@SuppressWarnings("unchecked")
	public static <T> Class<T> getClassFromJson(JsonObject json, Class<T> expectedSuperClass, String expectedPackage) throws IllegalArgumentException, ClassNotFoundException, InstantiationException{
		if(json.get("name") == null && json.get("class") == null ){
			throw new IllegalArgumentException("Either 'name' or 'class' needs to be specified in json");
		}
		Class<T> targetClass = null;
		String classPath = null;
		if(json.get("name") != null){
			try{
				classPath = expectedPackage + "." + json.get("name").asString();
				Class<?> c =  Class.forName(classPath);
				if(!expectedSuperClass.isAssignableFrom(c)){
					throw new InstantiationException(classPath + " is not a sub-class of " + expectedSuperClass.getName());
				}
				targetClass = (Class<T>) c;
			}catch(ClassNotFoundException e){
				//can be ignored at this point
			}catch(UnsupportedOperationException notAString){
				LOGGER.warn("'name' was not a string during class instanciation in instanciateFromJson");
			}
		}
		
		if(targetClass != null){
			return targetClass;
		}
		
		if(json.get("class") != null){
			try{
				classPath = json.get("class").asString();
			}catch(UnsupportedOperationException notAString){
				LOGGER.warn("'class' was not a string during class instanciation in instanciateFromJson");
			}
			try{
				Class<?> c =  Class.forName(classPath);
				if(!expectedSuperClass.isAssignableFrom(c)){
					throw new InstantiationException(classPath + " is not a sub-class of " + expectedSuperClass.getName());
				}
				targetClass = (Class<T>) c;
			}catch(ClassNotFoundException e){
				//can be ignored at this point
			}
		}
		
		return targetClass;
		
	}
	
}
