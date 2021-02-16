package org.vitrivr.cineast.core.features.retriever;

/**
 * A marker interface to allow multiple instances of such a retriever.
 * These have to ensure that they are distinguishable by {@see #hashCode} and {@see equals}.
 */
public interface MultipleInstantiatableRetriever extends Retriever{


    int hashCode();
    boolean equals(Object o);

}
