package org.vitrivr.cineast.core.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.data.MediaType;
import org.vitrivr.cineast.core.extraction.ExtractionContextProvider;
import org.vitrivr.cineast.core.extraction.decode.general.Converter;
import org.vitrivr.cineast.core.extraction.decode.general.Decoder;
import org.vitrivr.cineast.core.extraction.idgenerator.ObjectIdGenerator;
import org.vitrivr.cineast.core.extraction.metadata.MetadataExtractor;
import org.vitrivr.cineast.core.extraction.segmenter.general.Segmenter;
import org.vitrivr.cineast.core.features.codebook.CodebookGenerator;
import org.vitrivr.cineast.core.features.extractor.Extractor;
import org.vitrivr.cineast.core.features.retriever.Retriever;


public class ReflectionHelper {

  /**
   * Name of the package where {@link Extractor} and {@link Retriever} classes are located by default.
   */
  public static final String FEATURE_MODULE_PACKAGE = "org.vitrivr.cineast.core.features";
  private static final Logger LOGGER = LogManager.getLogger();
  /**
   * Name of the package where {@link CodebookGenerator} classes are located by default.
   */
  private static final String CODEBOOK_GENERATOR_PACKAGE = "org.vitrivr.cineast.core.features.codebook";

  /**
   * Name of the package where Exporter classes are located by default.
   */
  private static final String EXPORTER_PACKAGE = "org.vitrivr.cineast.core.features.exporter";

  /**
   * Name of the package where {@link ObjectIdGenerator} classes are located by default.
   */
  private static final String IDGENERATOR_PACKAGE = "org.vitrivr.cineast.core.extraction.idgenerator";

  /**
   * Name of the package where {@link MetadataExtractor} classes are located by default.
   */
  private static final String METADATA_PACKAGE = "org.vitrivr.cineast.core.extraction.metadata";

  /**
   * Name of the package where {@link Decoder} classes are located by default.
   */
  private static final String DECODER_PACKAGE = "org.vitrivr.cineast.core.extraction.decode";

  /**
   * Tries to instantiate a new, named ObjectIdGenerator object. If the methods succeeds to do so, that instance is returned by the method.
   * <p>
   * If the name contains dots (.), that name is treated as FQN. Otherwise, the IDGENERATOR_PACKAGE is assumed and the name is treated as simple name.
   *
   * @param name       Name of the {@link ObjectIdGenerator}.
   * @param properties Properties that should be used to configure new {@link ObjectIdGenerator}
   * @return Instance of ObjectIdGenerator or null, if instantiation fails.
   */
  @SuppressWarnings("unchecked")
  public static ObjectIdGenerator newIdGenerator(String name, Map<String, String> properties) {
    Class<ObjectIdGenerator> c = null;
    try {
      if (name.contains(".")) {
        c = (Class<ObjectIdGenerator>) Class.forName(name);
      } else {
        c = getClassFromName(name, ObjectIdGenerator.class, IDGENERATOR_PACKAGE);
      }

      if (properties == null || properties.isEmpty()) {
        return instantiate(c);
      } else {
        return instantiate(c, new Class[]{Map.class}, properties);
      }
    } catch (ClassNotFoundException | InstantiationException e) {
      LOGGER.fatal("Failed to create ObjectIdGenerator. Could not find class with name {} ({}).", name, LogHelper.getStackTrace(e));
      return null;
    }
  }

  /**
   * Tries to instantiate a new, named CodebookGenerator object. If the methods succeeds to do so, that instance is returned by the method.
   * <p>
   * If the name contains dots (.), that name is treated as FQN. Otherwise, the CODEBOOK_GENERATOR_PACKAGE is assumed and the name is treated as simple name.
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
      return instantiate(c);
    } catch (ClassNotFoundException | InstantiationException e) {
      LOGGER.fatal("Failed to create CodebookGenerator. Could not find or access class with name {} ({}).", name, LogHelper.getStackTrace(e));
      return null;
    }
  }

  /**
   * Tries to instantiate a new, named Covnerter object. If the methods succeeds to do so, that instance is returned by the method.
   *
   * @param fqn The fully-qualified name of the converter.
   * @return Instance of Converter or null, if instantiation fails.
   */
  public static Converter newConverter(String fqn) {
    try {
      Class<?> c = Class.forName(fqn);
      if (!c.isAssignableFrom(Converter.class)) {
        LOGGER.fatal("Failed to create Converter. Class '{}' is not a converter", fqn);
        return null;
      }
      @SuppressWarnings("unchecked")
      Class<Converter> cc = (Class<Converter>) c;
      return instantiate(cc);
    } catch (ClassNotFoundException e) {
      LOGGER.fatal("Failed to create Converter. Could not find or access class with name {} ({}).", fqn, LogHelper.getStackTrace(e));
      return null;
    }
  }

  /**
   * Tries to instantiate a new, named Extractor object. If the methods succeeds to do so, that instance is returned by the method.
   * <p>
   * If the name contains dots (.), that name is treated as FQN. Otherwise, the FEATURE_MODULE_PACKAGE is assumed and the name is treated as simple name.
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
      return instantiate(c);
    } catch (ClassNotFoundException | InstantiationException e) {
      LOGGER.fatal("Failed to create Exporter. Could not find class with name {} ({}).", name, LogHelper.getStackTrace(e));
      return null;
    }
  }

  /**
   * Tries to instantiate a new, named Exporter object. If the methods succeeds to do so, that instance is returned by the method.
   * <p>
   * If the name contains dots (.), that name is treated as FQN. Otherwise, the EXPORTER_PACKAGE is assumed and the name is treated as simple name.
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
        return instantiate(c);
      } else {
        return instantiate(c, configuration);
      }
    } catch (ClassNotFoundException | InstantiationException e) {
      LOGGER.fatal("Failed to create Exporter. Could not find or access class with name {} ({}).", name, LogHelper.getStackTrace(e));
      return null;
    }
  }

  /**
   * Tries to instantiate a new, named {@link Decoder} object. If the methods succeeds to do so, that instance is returned by the method.
   * <p>
   * If the name contains dots (.), that name is treated as FQN. Otherwise, the {@link #DECODER_PACKAGE} is assumed together with the {@link MediaType#getName()}  and the name is treated as simple name.
   *
   * @param name Name of the Decoder.
   * @return Instance of Decoder or null, if instantiation fails.
   */
  @SuppressWarnings("unchecked")
  public static Decoder newDecoder(String name, MediaType type) {
    Class<Decoder> c = null;
    try {
      if (name.contains(".")) {
        c = (Class<Decoder>) Class.forName(name);
      } else {
        c = getClassFromName(name, Decoder.class, DECODER_PACKAGE + "." + type.getName());
      }
      return instantiate(c);
    } catch (ClassNotFoundException | InstantiationException e) {
      LOGGER.fatal("Failed to create Decoder. Could not find class with name {} ({}).", name, LogHelper.getStackTrace(e));
      return null;
    }
  }

  /**
   * Tries to instantiate a new, named MetadataExtractor object. If the methods succeeds to do so, that instance is returned by the method.
   * <p>
   * If the name contains dots (.), that name is treated as FQN. Otherwise, the METADATA_PACKAGE is assumed and the name is treated as simple name.
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

      return instantiate(c);
    } catch (ClassNotFoundException | InstantiationException | ClassCastException e) {
      LOGGER.fatal("Failed to create MetadataExtractor. Could not find or access class with name {} ({}).", name, LogHelper.getStackTrace(e));
      return null;
    }
  }


  /**
   * Tries to instantiate a new, named MetadataExtractor object. If the methods succeeds to do so, that instance is returned by the method.
   * <p>
   * If the name contains dots (.), that name is treated as FQN. Otherwise, the METADATA_PACKAGE is assumed and the name is treated as simple name.
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
        return instantiate(c, new Class[]{ExtractionContextProvider.class}, provider);
      } else {
        return instantiate(c, new Class[]{ExtractionContextProvider.class, Map.class}, provider, configuration);
      }
    } catch (ClassNotFoundException | ClassCastException e) {
      LOGGER.fatal("Failed to create Segmenter. Could not find or access class with name {} ({}).", name, LogHelper.getStackTrace(e));
      return null;
    }
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
   * @param cl   The class that should be instantiated.
   * @param args The arguments that should be passed to the constructor. The constructor signature will be inferred from this list.
   * @return Instance of the class or null, if instantiation failed.
   */
  public static <T> T instantiate(Class<? extends T> cl, Object... args) {
    return instantiate(cl, getClassArray(args), args);
  }

  /**
   * Convenience method to instantiate an object of a given class using a defined constructor.
   *
   * @param cl    The class that should be instantiated.
   * @param types An array of types that defines the expected signature of the class's constructor.
   * @param args  The arguments that should be passed to the constructor.
   * @return Instance of the class or null, if instantiation failed.
   */
  public static <T> T instantiate(Class<? extends T> cl, Class[] types, Object... args) {
    try {
      // for constructors expecting maps.
      for (int i = 0, typesLength = types.length; i < typesLength; i++) {
        if (Arrays.stream(types[0].getInterfaces()).anyMatch(c -> c == Map.class)) {
          types[i] = Map.class;
        }
      }

      Constructor<? extends T> con = cl.getConstructor(types);
      return con.newInstance(args);
    } catch (InvocationTargetException e) {
      LOGGER.error("InvocationTargetException: {}", LogHelper.getStackTrace(e.getCause()));
    } catch (Exception e) {
      LOGGER.error(LogHelper.getStackTrace(e));
    }
    return null;
  }

  @SuppressWarnings("unchecked")
  public static <T> Class<T> getClassFromName(String className, Class<T> expectedSuperClass, String expectedPackage) throws IllegalArgumentException, ClassNotFoundException, InstantiationException {
    Class<T> targetClass = null;
    try {
      String classPath = expectedPackage + "." + className;
      Class<?> c = Class.forName(classPath);
      if (!expectedSuperClass.isAssignableFrom(c)) {
        throw new InstantiationException(classPath + " is not a sub-class of " + expectedSuperClass.getName());
      }
      targetClass = (Class<T>) c;
    } catch (UnsupportedOperationException e) {
      LOGGER.warn("'name' was not a string during class instantiation in instantiateFromJson");
    } catch (ClassNotFoundException e) {
      LOGGER.warn("Class {} was not found", className);
    }
    return targetClass;
  }
}
