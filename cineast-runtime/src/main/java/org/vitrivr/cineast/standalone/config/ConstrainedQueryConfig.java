package org.vitrivr.cineast.standalone.config;

import org.vitrivr.cineast.core.config.QueryConfig;
import org.vitrivr.cineast.core.config.ReadableQueryConfig;

import java.util.List;
import java.util.Optional;

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
    public int getResultsPerModule() { return Math.min(Config.sharedConfig().getRetriever().getMaxResultsPerModule(), super.getResultsPerModule()); }

    @Override
    public Optional<Integer> getMaxResults() {
        if (super.getMaxResults().isPresent()) {
            return Optional.of(Math.min(Config.sharedConfig().getRetriever().getMaxResults(), super.getMaxResults().get()));
        }
        return Optional.of(Config.sharedConfig().getRetriever().getMaxResults());
    }



}
