package org.vitrivr.cineast.core.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.data.MediaType;
import org.vitrivr.cineast.core.extraction.decode.general.Converter;
import org.vitrivr.cineast.core.extraction.decode.general.Decoder;
import org.vitrivr.cineast.core.features.abstracts.AbstractFeatureModule;
import org.vitrivr.cineast.core.features.codebook.CodebookGenerator;
import org.vitrivr.cineast.core.features.extractor.Extractor;
import org.vitrivr.cineast.core.features.retriever.Retriever;
import org.vitrivr.cineast.core.extraction.idgenerator.ObjectIdGenerator;
import org.vitrivr.cineast.core.metadata.MetadataExtractor;
import org.vitrivr.cineast.core.extraction.ExtractionContextProvider;
import org.vitrivr.cineast.core.extraction.segmenter.general.Segmenter;

import com.eclipsesource.json.JsonObject;


public class ReflectionHelper {

	private static final Logger LOGGER = LogManager.getLogger();
	
	public static final String FEATURE_MODULE_PACKAGE = "org.vitrivr.cineast.core.features";

	/** Name of the package where CodebookGenerator classes are located by default. */
	private static final String CODEBOOK_GENERATOR_PACKAGE = "org.vitrivr.cineast.core.features.codebook";

	/** Name of the package where Exporter classes are located by default. */
	private static final String EXPORTER_PACKAGE = "org.vitrivr.cineast.core.features.exporter";

	/** Name of the package where ObjectIdGenerator classes are located by default. */
	private static final String IDGENERATOR_PACKAGE = "org.vitrivr.cineast.core.extraction.idgenerator";

	/** Name of the package where MetadataExtractor classes are located by default. */
	private static final String METADATA_PACKAGE = "org.vitrivr.cineast.core.metadata";

	private static final String DECODER_PACKAGE = "org.vitrivr.cineast.core.decode";

	/**
	 * Tries to instantiate a new, named ObjectIdGenerator object. If the methods succeeds to do so,
	 * that instance is returned by the method.
	 *
	 * If the name contains dots (.), that name is treated as FQN. Otherwise, the IDGENERATOR_PACKAGE
	 * is assumed and the name is treated as simple name.
	 *
	 * @param name Name of the {@link ObjectIdGenerator}.
	 * @param properties Properties that should be used to configure new {@link ObjectIdGenerator}
	 * @return Instance of ObjectIdGenerator or null, if instantiation fails.
	 */
	@SuppressWarnings("unchecked")
	public static ObjectIdGenerator newIdGenerator(String name, Map<String,String> properties) {
		Class<ObjectIdGenerator> c = null;
		try {
			if (name.contains(".")) {
				c = (Class<ObjectIdGenerator>) Class.forName(name);
			} else {
				c = getClassFromName(name, ObjectIdGenerator.class, IDGENERATOR_PACKAGE);
			}

			if (properties == null || properties.isEmpty()) {
				return instanciate(c);
			} else {
				return instanciate(c, new Class[]{Map.class}, properties);
			}
		} catch (ClassNotFoundException | InstantiationException  e) {
			LOGGER.fatal("Failed to create ObjectIdGenerator. Could not find class with name {} ({}).", name, LogHelper.getStackTrace(e));
			return null;
		}
	}

	/**
	 * Tries to instantiate a new, named CodebookGenerator object. If the methods succeeds to do so,
	 * that instance is returned by the method.
	 *
	 * If the name contains dots (.), that name is treated as FQN. Otherwise, the CODEBOOK_GENERATOR_PACKAGE
	 * is assumed and the name is treated as simple name.
	 *
	 * @param name Name of the CodebookGenerator.
	 * @return Instance of CodebookGenerator or null, if instantiation fails.
	 */
	@SuppressWarnings("unchecked")
	public static CodebookGenerator newCodebookGenerator(String name) {
		Class<CodebookGenerator> c = null;
		try {
			if (name.contains(".")) {
				c = (Class<CodebookGenerator>) Class.forName(name);
			} else {
				c = getClassFromName(name, CodebookGenerator.class, CODEBOOK_GENERATOR_PACKAGE);
			}
			return instanciate(c);
		} catch (ClassNotFoundException | InstantiationException e) {
			LOGGER.fatal("Failed to create CodebookGenerator. Could not find or access class with name {} ({}).", name, LogHelper.getStackTrace(e));
			return null;
		}
	}

	/**
	 * Tries to instantiate a new, named Covnerter object. If the methods succeeds to do so,
	 * that instance is returned by the method.
	 *
	 * @param fqn The fully-qualified name of the converter.
	 * @return Instance of Converter or null, if instantiation fails.
	 */
	public static Converter newConverter(String fqn) {
		try {
		  Class<?> c = Class.forName(fqn);
		  if(!c.isAssignableFrom(Converter.class)){
		    LOGGER.fatal("Failed to create Converter. Class '{}' is not a converter", fqn);
	      return null;
		  }
			@SuppressWarnings("unchecked")
      Class<Converter> cc = (Class<Converter>) c;
			return instanciate(cc);
		} catch (ClassNotFoundException e) {
			LOGGER.fatal("Failed to create Converter. Could not find or access class with name {} ({}).", fqn, LogHelper.getStackTrace(e));
			return null;
		}
	}

	/**
	 * Tries to instantiate a new, named Extractor object. If the methods succeeds to do so,
	 * that instance is returned by the method.
	 *
	 * If the name contains dots (.), that name is treated as FQN. Otherwise, the FEATURE_MODULE_PACKAGE
	 * is assumed and the name is treated as simple name.
	 *
	 * @param name Name of the Exporter.
	 * @return Instance of Exporter or null, if instantiation fails.
	 */
	@SuppressWarnings("unchecked")
	public static Extractor newExtractor(String name) {
		Class<Extractor> c = null;
		try {
			if (name.contains(".")) {
				c = (Class<Extractor>) Class.forName(name);
			} else {
				c = getClassFromName(name, Extractor.class, FEATURE_MODULE_PACKAGE);
			}
			return instanciate(c);
		} catch (ClassNotFoundException | InstantiationException e) {
			LOGGER.fatal("Failed to create Exporter. Could not find class with name {} ({}).", name, LogHelper.getStackTrace(e));
			return null;
		}
	}

	/**
	 * Tries to instantiate a new, named Exporter object. If the methods succeeds to do so,
	 * that instance is returned by the method.
	 *
	 * If the name contains dots (.), that name is treated as FQN. Otherwise, the EXPORTER_PACKAGE
	 * is assumed and the name is treated as simple name.
	 *
	 * @param name Name of the Exporter.
	 * @return Instance of Exporter or null, if instantiation fails.
	 */
	@SuppressWarnings("unchecked")
	public static Extractor newExporter(String name, Map<String, String> configuration) {
		Class<Extractor> c = null;
		try {
			if (name.contains(".")) {
				c = (Class<Extractor>) Class.forName(name);
			} else {
				c = getClassFromName(name, Extractor.class, EXPORTER_PACKAGE);
			}

			if (configuration == null || configuration.isEmpty()) {
				return instanciate(c);
			} else {
				return instanciate(c, configuration);
			}
		} catch (ClassNotFoundException | InstantiationException e) {
			LOGGER.fatal("Failed to create Exporter. Could not find or access class with name {} ({}).", name, LogHelper.getStackTrace(e));
			return null;
		}
	}

	/**
	 * Tries to instantiate a new, named {@link Decoder} object. If the methods succeeds to do so, that instance is returned by the method.
	 *
	 * If the name contains dots (.), that name is treated as FQN. Otherwise, the {@link #DECODER_PACKAGE}
	 * is assumed together with the {@link MediaType#getName()}  and the name is treated as simple name.
	 *
	 * @param name Name of the Decoder.
	 * @return Instance of Decoder or null, if instantiation fails.
	 */
	@SuppressWarnings("unchecked")
	public static Decoder newDecoder(String name, MediaType type){
		Class<Decoder> c = null;
		try {
			if (name.contains(".")) {
				c = (Class<Decoder>) Class.forName(name);
			} else {
				c = getClassFromName(name, Decoder.class, DECODER_PACKAGE+"."+type.getName());
			}
			return instanciate(c);
		} catch (ClassNotFoundException | InstantiationException e) {
			LOGGER.fatal("Failed to create Decoder. Could not find class with name {} ({}).", name, LogHelper.getStackTrace(e));
			return null;
		}
	}

	/**
	 * Tries to instantiate a new, named MetadataExtractor object. If the methods succeeds to do so,
	 * that instance is returned by the method.
	 *
	 * If the name contains dots (.), that name is treated as FQN. Otherwise, the METADATA_PACKAGE
	 * is assumed and the name is treated as simple name.
	 *
	 * @param name Name of the MetadataExtractor.
	 * @return Instance of MetadataExtractor or null, if instantiation fails.
	 */
	@SuppressWarnings("unchecked")
	public static MetadataExtractor newMetadataExtractor(String name) {
		Class<MetadataExtractor> c = null;
		try {
			if (name.contains(".")) {
				c = (Class<MetadataExtractor>) Class.forName(name);
			} else {
				c = getClassFromName(name, MetadataExtractor.class, METADATA_PACKAGE);
			}

			return instanciate(c);
		} catch (ClassNotFoundException | InstantiationException | ClassCastException e) {
			LOGGER.fatal("Failed to create MetadataExtractor. Could not find or access class with name {} ({}).", name, LogHelper.getStackTrace(e));
			return null;
		}
	}


	/**
	 * Tries to instantiate a new, named MetadataExtractor object. If the methods succeeds to do so,
	 * that instance is returned by the method.
	 *
	 * If the name contains dots (.), that name is treated as FQN. Otherwise, the METADATA_PACKAGE
	 * is assumed and the name is treated as simple name.
	 *
	 * @param name Name of the MetadataExtractor.
	 * @return Instance of MetadataExtractor or null, if instantiation fails.
	 */
	@SuppressWarnings("unchecked")
	public static <T> Segmenter<T> newSegmenter(String name, Map<String, String> configuration, ExtractionContextProvider provider) {
		Class<Segmenter<T>> c = null;
		try {
			 c = (Class<Segmenter<T>>) Class.forName(name);
			if (configuration == null) {
				return instanciate(c, new Class[]{ExtractionContextProvider.class}, provider);
			} else {
				return instanciate(c, new Class[]{ExtractionContextProvider.class, Map.class}, provider, configuration);
			}
		} catch (ClassNotFoundException | ClassCastException e) {
			LOGGER.fatal("Failed to create Segmenter. Could not find or access class with name {} ({}).", name, LogHelper.getStackTrace(e));
			return null;
		}
	}

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

	/**
	 * Convenience method to instantiate an object of a given class using a specific constructor.
	 *
	 * @param cl The class that should be instantiated.
	 * @param args The arguments that should be passed to the constructor. The constructor signature will be inferred from this list.
	 * @param <T>
	 * @return Instance of the class or null, if instantiation failed.
	 */
	public static <T> T instanciate(Class<? extends T> cl, Object... args) {
		return instanciate(cl, getClassArray(args), args);
	}

	/**
	 * Convenience method to instantiate an object of a given class using a defined constructor.
	 *
	 * @param cl The class that should be instantiated.
	 * @param types An array of types that defines the expected signature of the class's constructor.
	 * @param args The arguments that should be passed to the constructor.
	 * @param <T>
	 * @return Instance of the class or null, if instantiation failed.
	 */
	public static <T> T instanciate(Class<? extends T> cl, Class[] types, Object... args) {
		try {
			Constructor<? extends T> con = cl.getConstructor(types);
			return con.newInstance(args);
		} catch (InvocationTargetException e) {
			LOGGER.error("InvocationTargetException: {}", LogHelper.getStackTrace(e.getCause()));
		} catch (Exception e) {
			LOGGER.error(LogHelper.getStackTrace(e));
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
		  targetClass = getClassFromName(json.get("name").asString(), expectedSuperClass, expectedPackage);
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
	
	@SuppressWarnings("unchecked")
	public static <T> Class<T> getClassFromName(String className, Class<T> expectedSuperClass, String expectedPackage) throws IllegalArgumentException, ClassNotFoundException, InstantiationException {
	  	Class<T> targetClass = null;
	  	try {
			String classPath = expectedPackage + "." + className;
	  		Class<?> c =  Class.forName(classPath);
	  		if(!expectedSuperClass.isAssignableFrom(c)){
				throw new InstantiationException(classPath + " is not a sub-class of " + expectedSuperClass.getName());
	  		}
	  		targetClass = (Class<T>) c;
		} catch(UnsupportedOperationException e){
		 	LOGGER.warn("'name' was not a string during class instantiation in instantiateFromJson");
		}
		  return targetClass;
	}
}
