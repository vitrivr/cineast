package org.vitrivr.cineast.core.features;

import org.vitrivr.cineast.core.data.segments.SegmentContainer;
import org.vitrivr.cineast.core.features.abstracts.SolrTextRetriever;

public class TagsFulltext extends SolrTextRetriever {
    /**
     * Name of the entity associated wiht {@link VideoMetadata}.
     */
    public static final String ENTITY_NAME = "features_tagsft";


    /**
     * Default constructor for {@link VideoMetadata}.
     */
    public TagsFulltext() {
        super(ENTITY_NAME);
    }

    /**
     * @param shot
     */
    @Override
    public void processSegment(SegmentContainer shot) {
        /* TODO: Not implemented because video metadata extraction is not integrated into pipeline yet. */
    }
}