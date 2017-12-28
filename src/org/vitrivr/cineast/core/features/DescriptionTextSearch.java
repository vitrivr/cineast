package org.vitrivr.cineast.core.features;

import org.vitrivr.cineast.core.data.segments.SegmentContainer;
import org.vitrivr.cineast.core.features.abstracts.SolrTextRetriever;

public class DescriptionTextSearch extends SolrTextRetriever {
    /**
     * Default constructor for {@link DescriptionTextSearch}
     */
    public DescriptionTextSearch() {
        super("features_densecap");
    }

    /**
     *
     * @param shot
     */
    @Override
    public void processSegment(SegmentContainer shot) {
        /* TODO: Not implemented because densecap extraction is not integrated into pipeline yet. */
    }
}
