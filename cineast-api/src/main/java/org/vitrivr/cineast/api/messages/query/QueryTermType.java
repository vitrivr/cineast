package org.vitrivr.cineast.api.messages.query;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.data.query.containers.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Optional;

/**
 * A {@link QueryTermType} represents the types of query terms used.
 */
public enum QueryTermType {

  IMAGE(ImageQueryTermContainer.class),
  AUDIO(AudioQueryTermContainer.class),
  MOTION(MotionQueryTermContainer.class),
  MODEL3D(ModelQueryTermContainer.class),
  LOCATION(LocationQueryTermContainer.class),
  PARAMETERISED_LOCATION(ParameterisedLocationQueryTermContainer.class),
  TIME(InstantQueryTermContainer.class),
  TEXT(TextQueryTermContainer.class),
  TAG(TagQueryTermContainer.class),
  SEMANTIC(SemanticMapQueryTermContainer.class),
  SKELETON(SkeletonQueryTermContainer.class),

  /**
   * Denotes a {@link QueryTerm} containing an Id for a 'More-Like-This' query. This is used over the @link {@link MoreLikeThisQuery} in REST calls.
   */
  ID(IdQueryTermContainer.class),
  BOOLEAN(BooleanQueryTermContainer.class);

  private static final Logger LOGGER = LogManager.getLogger();

  /**
   * Instance of the {@link AbstractQueryTermContainer} class that represents this {@link QueryTermType}.
   */
  private final Class<? extends AbstractQueryTermContainer> c;

  QueryTermType(Class<? extends AbstractQueryTermContainer> clazz) {
    this.c = clazz;
  }

  public Class<? extends AbstractQueryTermContainer> getContainerClass() {
    return this.c;
  }

  /**
   * Constructs a new instance of the {@link AbstractQueryTermContainer} associated with the current {@link QueryTermType} using the provided raw data (usually base 64 encoded).
   *
   * @param data Data from which to construct a {@link AbstractQueryTermContainer}
   */
  public Optional<AbstractQueryTermContainer> getQueryContainer(String data) {
    try {
      Constructor<? extends AbstractQueryTermContainer> constructor = this.c.getConstructor(String.class);
      return Optional.of(constructor.newInstance(data));
    } catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
      LOGGER.error("Error while constructing query container", e);
      return Optional.empty();
    }
  }
}
