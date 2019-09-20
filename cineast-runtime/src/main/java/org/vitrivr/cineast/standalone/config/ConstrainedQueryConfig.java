package org.vitrivr.cineast.standalone.config;

import org.vitrivr.cineast.core.config.QueryConfig;
import org.vitrivr.cineast.core.config.ReadableQueryConfig;

import java.util.List;

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
    public int getMaxResults() { return Math.min(Config.sharedConfig().getRetriever().getMaxResults(), super.getMaxResults()); }



}
