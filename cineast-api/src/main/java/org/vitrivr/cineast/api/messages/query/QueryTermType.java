package org.vitrivr.cineast.api.messages.query;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Optional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.data.query.containers.AudioQueryContainer;
import org.vitrivr.cineast.core.data.query.containers.BooleanQueryContainer;
import org.vitrivr.cineast.core.data.query.containers.IdQueryContainer;
import org.vitrivr.cineast.core.data.query.containers.ImageQueryContainer;
import org.vitrivr.cineast.core.data.query.containers.InstantQueryContainer;
import org.vitrivr.cineast.core.data.query.containers.LocationQueryContainer;
import org.vitrivr.cineast.core.data.query.containers.ModelQueryContainer;
import org.vitrivr.cineast.core.data.query.containers.MotionQueryContainer;
import org.vitrivr.cineast.core.data.query.containers.QueryContainer;
import org.vitrivr.cineast.core.data.query.containers.SemanticMapQueryContainer;
import org.vitrivr.cineast.core.data.query.containers.TagQueryContainer;
import org.vitrivr.cineast.core.data.query.containers.TextQueryContainer;

public enum QueryTermType {

  IMAGE(ImageQueryContainer.class),
  AUDIO(AudioQueryContainer.class),
  MOTION(MotionQueryContainer.class),
  MODEL3D(ModelQueryContainer.class),
  LOCATION(LocationQueryContainer.class),
  TIME(InstantQueryContainer.class),
  TEXT(TextQueryContainer.class),
  TAG(TagQueryContainer.class),
  SEMANTIC(SemanticMapQueryContainer.class),

  /**
   * Denotes a {@link QueryTerm} containing an Id for a 'More-Like-This' query. This is used over the @link {@link MoreLikeThisQuery} in REST calls.
   */
  ID(IdQueryContainer.class),
  BOOLEAN(BooleanQueryContainer.class);

  private static final Logger LOGGER = LogManager.getLogger();

  /**
   * Instance of the {@link QueryContainer} class that represents this {@link QueryTermType}.
   */
  private final Class<? extends QueryContainer> c;

  QueryTermType(Class<? extends QueryContainer> clazz) {
    this.c = clazz;
  }

  public Class<? extends QueryContainer> getContainerClass() {
    return this.c;
  }

  /**
   * Constructs a new instance of the {@link QueryContainer} associated with the current {@link QueryTermType} using the provided raw data (usually base 64 encoded).
   *
   * @param data Data from which to construct a {@link QueryContainer}
   */
  public Optional<QueryContainer> getQueryContainer(String data) {
    try {
      Constructor<? extends QueryContainer> constructor = this.c.getConstructor(String.class);
      return Optional.of(constructor.newInstance(data));
    } catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
      LOGGER.error("Error while constructing query container", e);
      return Optional.empty();
    }
  }
}
