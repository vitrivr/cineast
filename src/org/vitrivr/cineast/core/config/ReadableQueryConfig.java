package org.vitrivr.cineast.core.config;

import java.util.*;

import org.vitrivr.cineast.core.data.CorrespondenceFunction;

public class ReadableQueryConfig {
    /**
     * L
     */
    public enum Distance {
        chisquared, correlation, cosine, hamming, jaccard, kullbackleibler, chebyshev, euclidean, squaredeuclidean, manhattan, minkowski, spannorm, haversine
    }

    /**
     * List of Query-Hints that can be configured in the QueryConfig. It's up to the implementing
     * selector to actually consider these hints.
     */
    public enum Hints {
        exact, /* Only exact lookup methods should be considered. */
        inexact,lsh,ecp,mi,pq,sh,va,vaf,vav,sequential,empirical
    }

    private final UUID queryId;
    protected Distance distance = null;
    protected float[] distanceWeights = null;
    protected float norm = Float.NaN;
    protected CorrespondenceFunction correspondence = null;
    protected Set<Hints> hints = new HashSet<>();

    /**
     *
     * @param qc
     * @param uuid
     */
    protected ReadableQueryConfig(ReadableQueryConfig qc, UUID uuid) {
        this.queryId = (uuid == null) ? UUID.randomUUID() : uuid;
        if (qc == null) {
          return;
        }
        this.distance = qc.distance;
        this.distanceWeights = qc.distanceWeights;
        this.norm = qc.norm;
        this.hints.addAll(qc.hints);
    }

    public ReadableQueryConfig(ReadableQueryConfig qc) {
    this(qc, qc == null ? null : qc.queryId);
    }
    public Optional<Distance> getDistance() {
    return Optional.ofNullable(this.distance);
    }
    public Optional<Float> getNorm() {
    return Optional.ofNullable(Float.isNaN(norm) ? null : norm);
    }
    public Optional<CorrespondenceFunction> getCorrespondenceFunction() {return Optional.ofNullable(this.correspondence);}
    public final UUID getQueryId() {
        return this.queryId;
    }
    public Optional<float[]> getDistanceWeights() {
    return Optional.ofNullable(this.distanceWeights);
    }
    public Set<Hints> getHints() {
        return this.hints;
    }
}
