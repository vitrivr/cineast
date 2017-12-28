package org.vitrivr.cineast.core.features;

import org.vitrivr.cineast.core.data.segments.SegmentContainer;
import org.vitrivr.cineast.core.features.abstracts.SolrTextRetriever;

public class VideoMetadata extends SolrTextRetriever {

    /**
     * Default constructor for {@link VideoMetadata}.
     */
    public VideoMetadata() {
        super("features_meta");
    }

    /**
     *
     * @param shot
     */
    @Override
    public void processSegment(SegmentContainer shot) {
        /* TODO: Not implemented because video metadata extraction is not integrated into pipeline yet. */
    }
}
