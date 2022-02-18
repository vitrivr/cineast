package org.vitrivr.cineast.standalone.config;

import java.util.List;
import java.util.Optional;
import org.vitrivr.cineast.core.config.QueryConfig;
import org.vitrivr.cineast.core.config.ReadableQueryConfig;

public class ConstrainedQueryConfig extends QueryConfig {

  public ConstrainedQueryConfig(String queryId, List<Hints> hints) {
    super(queryId, hints);
  }

  public ConstrainedQueryConfig(ReadableQueryConfig qc) {
    super(qc);
  }

  public ConstrainedQueryConfig() {
    super(null);
  }

  @Override
  public int getResultsPerModule() {
    return Math.min(Config.sharedConfig().getRetriever().getMaxResultsPerModule(), super.getResultsPerModule());
  }

  @Override
  public Optional<Integer> getMaxResults() {
    if (super.getMaxResults().isPresent()) {
      return Optional.of(Math.min(Config.sharedConfig().getRetriever().getMaxResults(), super.getMaxResults().get()));
    }
    return Optional.of(Config.sharedConfig().getRetriever().getMaxResults());
  }

  public static ConstrainedQueryConfig getApplyingConfig(QueryConfig config) {
    ConstrainedQueryConfig queryConfig = new ConstrainedQueryConfig(config);
    if (config == null) {
      final int max = Math.min(queryConfig.getMaxResults().orElse(Config.sharedConfig().getRetriever().getMaxResults()), Config.sharedConfig().getRetriever().getMaxResults());
      queryConfig.setMaxResults(max);
      final int resultsPerModule = Math.min(queryConfig.getRawResultsPerModule() == -1 ? Config.sharedConfig().getRetriever().getMaxResultsPerModule() : queryConfig.getResultsPerModule(), Config.sharedConfig().getRetriever().getMaxResultsPerModule());
      queryConfig.setResultsPerModule(resultsPerModule);
    }

    return queryConfig;
  }
}
