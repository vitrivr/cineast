package org.vitrivr.cineast.core.features;

import org.vitrivr.cineast.core.data.segments.SegmentContainer;
import org.vitrivr.cineast.core.features.abstracts.SolrTextRetriever;

public class OCRSearch extends SolrTextRetriever {

    /**
     * Default constructor for {@link OCRSearch}.
     */
    public OCRSearch() {
        super("features_ocr");
    }

    /**
     *
     * @param shot
     */
    @Override
    public void processSegment(SegmentContainer shot) {
        /* TODO: Not implemented because OCR extraction is not integrated into pipeline yet. */
    }
}
