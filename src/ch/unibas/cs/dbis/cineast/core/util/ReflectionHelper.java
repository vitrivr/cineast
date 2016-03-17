package ch.unibas.cs.dbis.cineast.core.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.eclipsesource.json.JsonObject;

import ch.unibas.cs.dbis.cineast.core.features.abstracts.AbstractFeatureModule;
import ch.unibas.cs.dbis.cineast.core.features.extractor.Extractor;

public class ReflectionHelper {

	private static final Logger LOGGER = LogManager.getLogger();
	
	private static final String FEATURE_MODULE_PACKAGE = "ch.unibas.cs.dbis.cineast.core.features";
	private static final String EXPORTER_PACKAGE = "ch.unibas.cs.dbis.cineast.core.features.exporter";
	
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
	 * creates a new instance of an exporter as specified by the provided json.
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
	
	private static Class<?>[] getClassArray(Object... args) {
		Class<?>[] cls = new Class<?>[args.length];
		int i = 0;
		for (Object o : args) {
			cls[i++] = o.getClass();
		}
		return cls;
	}
	
	private static <T> T instanciate(Class<? extends T> cl, Object... args) {
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
	 * @return
	 * @throws IllegalArgumentException in case neither 'name' nor 'class' is specified
	 * @throws InstantiationException in case instantiation fails or the requested class does not match the expected super class
	 * @throws ClassNotFoundException in case the class to instantiate was not found
	 */
	@SuppressWarnings("unchecked")
	public static <T> T instanciateFromJson(JsonObject json, Class<T> expectedSuperClass, String expectedPackage) throws IllegalArgumentException, InstantiationException, ClassNotFoundException{
		if(json.get("name") == null && json.get("class") == null ){
			throw new IllegalArgumentException("Either 'name' or 'class' needs to be specified in json");
		}
		Class<T> targetClass = null;
		String classPath = "";
		if(json.get("name") != null){
			classPath = expectedPackage + "." + json.get("name").asString();
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
		
		if(targetClass == null && json.get("class") != null){
			classPath = json.get("class").asString();
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
		
		if(targetClass == null){
			throw new ClassNotFoundException("Class " + classPath + " was not found");
		}
		
		T instance = null;
		if(json.get("config") == null){
			instance = instanciate(targetClass);
		}else{
			instance = instanciate(targetClass, json.get("config").asObject());
		}
		
		if(instance == null){
			throw new InstantiationException();
		}
		
		return instance;
		
	}
	
}
