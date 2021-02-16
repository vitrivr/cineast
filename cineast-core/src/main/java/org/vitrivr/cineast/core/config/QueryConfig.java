package org.vitrivr.cineast.core.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.vitrivr.cineast.core.data.CorrespondenceFunction;

public class QueryConfig extends ReadableQueryConfig {

  /**
   * Constructor used to parse a {@link QueryConfig} from JSON.
   *
   * @param queryId The ID of the query. If not set, a new ID will be generated.
   * @param hints List of query {@link Hints} used by the query. May be null or empty.
   */
  @JsonCreator
  public QueryConfig(@JsonProperty(value = "queryId", required = false) String queryId,
      @JsonProperty(value = "hints", required = false) List<Hints> hints) {
    super(queryId, hints);
  }

  public QueryConfig(ReadableQueryConfig qc) {
    super(qc);
  }

  private QueryConfig(ReadableQueryConfig qc, UUID uuid) {
    super(qc, uuid);
  }

  public QueryConfig setDistanceWeights(float[] weights) {
    this.distanceWeights = weights;
    return this;
  }

  public QueryConfig setDistanceIfEmpty(Distance distance) {
    if (this.distance == null) {
      return setDistance(distance);
    }
    return this;
  }

  public QueryConfig setDistanceWeightsIfEmpty(float[] weights) {
    if (this.distanceWeights == null) {
      return setDistanceWeights(weights);
    }
    return this;
  }

  public QueryConfig setResultsPerModule(int k) {
    if (k > 0) {
      this.resultsPerModule = k;
    }
    return this;
  }

  public QueryConfig setMaxResults(int max) {
    if (max >= 0) {
      this.maxResults = Optional.of(max);
    }
    return this;
  }

  public QueryConfig setNormIfEmpty(float norm) {
    if (Float.isNaN(this.norm)) {
      return setNorm(norm);
    }
    return this;
  }

  public QueryConfig setCorrespondenceFunctionIfEmpty(CorrespondenceFunction f) {
    if (this.correspondence == null) {
      return setCorrespondenceFunction(f);
    }
    return this;
  }

  /**
   * Adds the provided hint to the list of hints.
   *
   * @param hint Hint to be added.
   * @return this
   */
  public QueryConfig addHint(Hints hint) {
    this.hints.add(hint);
    return this;
  }

  /**
   * Adds the provided hints and thereby replaces all hints that may have been set previously.
   *
   * @param hint Collection of hints to be added.
   * @return this
   */
  public QueryConfig setHints(Collection<Hints> hint) {
    this.hints.clear();
    this.hints.addAll(hint);
    return this;
  }

  @Override
  public QueryConfig clone() {
    return new QueryConfig(this);
  }

  public static QueryConfig clone(ReadableQueryConfig qc) {
    return new QueryConfig(qc);
  }

  /**
   * Creates and returns a new {@link QueryConfig} which is identical to the provided one except for the query ID.
   *
   * @return New {@link QueryConfig}
   */
  public static QueryConfig newQueryConfigFromOther(QueryConfig qc) {
    return new QueryConfig(qc, null);
  }

  protected QueryConfig setDistance(Distance distance) {
    this.distance = distance;
    if (distance == Distance.euclidean) {
      this.norm = 2f;
    } else if (distance == Distance.manhattan) {
      this.norm = 1f;
    } else if (distance == Distance.chebyshev) {
      this.norm = Float.POSITIVE_INFINITY;
    }
    return this;
  }

  protected QueryConfig setNorm(float norm) {
    this.norm = norm;
    if (Math.abs(norm - 2f) < 1e-6f) {
      this.distance = Distance.euclidean;
    } else if (Math.abs(norm - 1f) < 1e-6f) {
      this.distance = Distance.manhattan;
    } else if (Float.isInfinite(norm) && norm > 0) {
      this.distance = Distance.chebyshev;
    }
    return this;
  }

  protected QueryConfig setCorrespondenceFunction(CorrespondenceFunction f) {
    Objects.requireNonNull(f, "Correspondence function cannot be null");
    this.correspondence = f;
    return this;
  }

  public void addRelevantSegmentIds(Collection<String> ids) {
    this.relevantSegmentIds.addAll(ids);
  }

  public void addRelevantSegmentIds(String... ids) {
    if (ids != null) {
      for (int i = 0; i < ids.length; ++i) {
        if (ids[i] != null) {
          this.relevantSegmentIds.add(ids[i]);
        }
      }
    }
  }

  public void setRelevantSegmentIds(Collection<String> relevantSegments) {
    this.relevantSegmentIds.clear();
    this.addRelevantSegmentIds(relevantSegments);
  }

  @Override
  public String toString() {
    return "QueryConfig{" +
        "distance=" + distance +
        ", distanceWeights=" + Arrays.toString(distanceWeights) +
        ", norm=" + norm +
        ", correspondence=" + correspondence +
        ", resultsPerModule=" + resultsPerModule +
        ", maxResults=" + maxResults +
        ", hints=" + hints +
        ", relevantSegmentIds (Excerpt)=" + Arrays.toString(relevantSegmentIds.stream().limit(5).toArray()) +
        '}';
  }
}
