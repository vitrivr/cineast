package org.vitrivr.cineast.core.features;

import org.vitrivr.cineast.core.config.ReadableQueryConfig;
import org.vitrivr.cineast.core.data.segments.SegmentContainer;
import org.vitrivr.cineast.core.features.abstracts.SolrTextRetriever;

import java.util.Arrays;

/**
 * Uses standard text support from Solr. OCR is handled by adding fuzziness / levenshtein-distance support to the query.
 * This makes sense here since we expect small errors from OCR sources
 */
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

    @Override
    protected String[] generateQuery(SegmentContainer sc, ReadableQueryConfig qc) {
        return Arrays.stream(sc.getText().split("\\s")).map(s -> s + "~0.5").toArray(String[]::new);
    }
}
