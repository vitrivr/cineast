package org.vitrivr.cineast.core.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.vitrivr.cineast.core.data.CorrespondenceFunction;

public class ReadableQueryConfig {

  private static final int DEFAULT_RESULTS_PER_MODULE = 250;
  protected final Set<Hints> hints;
  protected final Set<String> relevantSegmentIds = new HashSet<>();
  private final String queryId;
  protected Distance distance = null;
  protected float[] distanceWeights = null;
  protected float norm = Float.NaN;
  protected CorrespondenceFunction correspondence = null;
  protected int resultsPerModule = -1;
  protected Optional<Integer> maxResults = Optional.empty();
  /**
   * Constructor used to parse a {@link ReadableQueryConfig} from JSON.
   *
   * @param queryId The ID of the query. If not set, a new ID will be generated.
   * @param hints   List of query {@link Hints} used by the query. May be null or empty.
   */
  @JsonCreator
  public ReadableQueryConfig(@JsonProperty(value = "queryId", required = false) String queryId,
      @JsonProperty(value = "hints", required = false) List<Hints> hints) {

    this.queryId = queryId == null ? UUID.randomUUID().toString() : queryId;
    if (hints != null) {
      this.hints = new HashSet<>(hints);
    } else {
      this.hints = new HashSet<>();
    }
  }
  /**
   * Constructor used to create a {@link ReadableQueryConfig} from another {@link ReadableQueryConfig}.
   *
   * @param qc The {@link ReadableQueryConfig} that should be used as template
   */
  public ReadableQueryConfig(ReadableQueryConfig qc) {
    this(qc, qc == null ? null : qc.queryId);
  }

  /**
   * Internal constructor used to create a {@link ReadableQueryConfig} from another {@link ReadableQueryConfig}.
   *
   * @param qc      The {@link ReadableQueryConfig} that should be used. May be null.
   * @param queryId The queryId for the new {@link ReadableQueryConfig}. If null, a new UUID will be created.
   */
  protected ReadableQueryConfig(ReadableQueryConfig qc, String queryId) {
    this.queryId = (queryId == null) ? UUID.randomUUID().toString() : queryId;
    this.hints = new HashSet<>();
    if (qc == null) {
      return;
    }
    this.distance = qc.distance;
    this.distanceWeights = qc.distanceWeights;
    this.norm = qc.norm;
    this.correspondence = qc.correspondence;
    this.resultsPerModule = qc.resultsPerModule;
    this.maxResults = qc.maxResults;
    this.hints.addAll(qc.hints);
    this.relevantSegmentIds.addAll(qc.relevantSegmentIds);
  }

  public final String getQueryId() {
    return this.queryId;
  }

  /**
   * Will always be above 0; returns {@link #DEFAULT_RESULTS_PER_MODULE} if no value was set at creation
   */
  public int getResultsPerModule() {
    return this.resultsPerModule > 0 ? this.resultsPerModule : DEFAULT_RESULTS_PER_MODULE;
  }

  /**
   * Returns the actual underlying value of {@link #resultsPerModule}, might be below 0
   */
  public int getRawResultsPerModule() {
    return this.resultsPerModule;
  }

  public Optional<Integer> getMaxResults() {
    return this.maxResults;
  }

  public Optional<Distance> getDistance() {
    return Optional.ofNullable(this.distance);
  }

  public Optional<Float> getNorm() {
    return Optional.ofNullable(Float.isNaN(norm) ? null : norm);
  }

  public Optional<CorrespondenceFunction> getCorrespondenceFunction() {
    return Optional.ofNullable(this.correspondence);
  }

  public Optional<float[]> getDistanceWeights() {
    return Optional.ofNullable(this.distanceWeights);
  }

  public Set<Hints> getHints() {
    return this.hints;
  }

  /**
   * Specifies the segments to which a query result should be limited before any other evaluation. An empty set indicates no additional filtering
   *
   * @return set of relevant segment ids
   */
  public Set<String> getRelevantSegmentIds() {
    return Collections.unmodifiableSet(this.relevantSegmentIds);
  }

  /**
   * checks if the config has segment ids to which the result is to be limited without the need for allocation of immutable wrapper
   */
  public boolean hasRelevantSegmentIds() {
    return !this.relevantSegmentIds.isEmpty();
  }

  public ReadableQueryConfig withChangesFrom(ReadableQueryConfig other) {

    ReadableQueryConfig _return = new ReadableQueryConfig(this);

    if (other.distance != null) {
      this.distance = other.distance;
    }

    if (other.distanceWeights != null) {
      this.distanceWeights = other.distanceWeights;
    }

    if (!Float.isNaN(other.norm)) {
      this.norm = other.norm;
    }

    if (other.resultsPerModule > 0) {
      this.resultsPerModule = other.resultsPerModule;
    }

    this.hints.addAll(other.hints);

    this.relevantSegmentIds.addAll(other.relevantSegmentIds);

    return _return;

  }

  /**
   * Possible distance functions that can be configured in the {@link QueryConfig}. It's up to the implementing selector to support these distances and / or provide fallback options.
   */
  public enum Distance {
    chisquared, correlation, cosine, hamming, jaccard, kullbackleibler, chebyshev, euclidean, squaredeuclidean, manhattan, minkowski, spannorm, haversine
  }

  /**
   * List of Query-Hints that can be configured in the {@link QueryConfig}. It's up to the implementing selector to actually consider these hints.
   */
  public enum Hints {
    exact, /* Only exact lookup methods should be considered. */
    inexact, /* Inexact lookup methods can be used as well. */
    lsh, ecp, mi, pq, sh, va, vaf, vav, sequential, empirical
  }
}
