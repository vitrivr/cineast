package org.vitrivr.cineast.api.messages.query;

import org.vitrivr.cineast.core.data.query.containers.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Optional;

/**
 * @author rgasser
 * @version 1.0
 * @created 27.01.17
 */
public enum QueryTermType {
    /** Denotes a {@link QueryTerm} that should be regarded as still image (either sketch or photograph).*/
    IMAGE(ImageQueryContainer.class),

    /** Denotes a {@link QueryTerm} that should be regarded as audio snippet.*/
    AUDIO(AudioQueryContainer.class),

    /** Denotes a {@link QueryTerm} that should be regarded as motion query.*/
    MOTION(MotionQueryContainer.class),

    /** Denotes a {@link QueryTerm} that should be regarded as 3D model (example).*/
    MODEL3D(ModelQueryContainer.class),

    /** Denotes a {@link QueryTerm} that should be regarded as location based query. */
    LOCATION(LocationQueryContainer.class),

    /** Denotes a {@link QueryTerm} that should be regarded as location based query. */
    TIME(InstantQueryContainer.class),

    /** Denotes a {@link QueryTerm} that should be regarded as text query. */
    TEXT(TextQueryContainer.class),

    /** Denotes a {@link QueryTerm} that should be regarded as tag query. */
    TAG(TagQueryContainer.class),

    /** Denotes a {@link QueryTerm} that should be regarded as a semantic sketch query. */
    SEMANTIC(SemanticMapQueryContainer.class),

    /**
     * Denotes a {@link QueryTerm} containing an Id for a 'More-Like-This' query.
     * This is used over the @link {@link MoreLikeThisQuery} in REST calls.
     */
    ID(IdQueryContainer.class),

    BOOLEAN(BooleanQueryContainer.class);

    /** Instance of the {@link QueryContainer} class that represents this {@link QueryTermType}. */
    private final Class<? extends QueryContainer> c;

    /**
     * Constructor for {@link QueryTermType}.
     *
     * @param clazz
     */
    QueryTermType(Class<? extends QueryContainer> clazz) {
        this.c = clazz;
    }

    /**
     * Returns the class of the {@link QueryContainer} associated with the current {@link QueryTermType}.
     *
     * @return Class of the {@link QueryContainer}
     */
    public Class<? extends QueryContainer> getContainerClass() {
        return this.c;
    }

    /**
     * Constructs a new instance of the {@link QueryContainer} associated with the current {@link QueryTermType}
     * using the provided raw data (usually base 64 encoded).
     *
     * @param data Data from which to construct a {@link QueryContainer}
     */
    public Optional<QueryContainer> getQueryContainer(String data) {
        try {
            Constructor<? extends QueryContainer> constructor = this.c.getConstructor(String.class);
            return Optional.of(constructor.newInstance(data));
        } catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
            return Optional.empty();
        }
    }
}
