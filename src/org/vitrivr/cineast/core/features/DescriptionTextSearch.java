package org.vitrivr.cineast.core.features;

import org.vitrivr.cineast.core.config.ReadableQueryConfig;
import org.vitrivr.cineast.core.data.segments.SegmentContainer;
import org.vitrivr.cineast.core.features.abstracts.SolrTextRetriever;

import java.util.Arrays;

public class DescriptionTextSearch extends SolrTextRetriever {
    /**
     * Default constructor for {@link DescriptionTextSearch}
     */
    public DescriptionTextSearch() {
        super("features_densecap");
    }

    @Override
    public void processSegment(SegmentContainer shot) {
        /* TODO: Not implemented because densecap extraction is not integrated into pipeline yet. */
    }

    @Override
    protected String[] generateQuery(SegmentContainer sc, ReadableQueryConfig qc) {
        return new String[]{"\""+sc.getText()+"\"~10"};
    }
}
