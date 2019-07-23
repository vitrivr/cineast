package org.vitrivr.cineast.core.features;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.RandomStringUtils;
import org.vitrivr.cineast.core.config.ReadableQueryConfig;
import org.vitrivr.cineast.core.data.entities.SimpleFulltextFeatureDescriptor;
import org.vitrivr.cineast.core.data.segments.SegmentContainer;
import org.vitrivr.cineast.core.features.abstracts.SolrTextRetriever;

/**
 * Uses standard text support from Solr. OCR is handled by adding fuzziness / levenshtein-distance support to the query. This makes sense here since we expect small errors from OCR sources
 */
public class OCRSearch extends SolrTextRetriever {

  public static final String OCR_TABLE_NAME = "features_ocr";

  /**
   * Default constructor for {@link OCRSearch}.
   */
  public OCRSearch() {
    super(OCR_TABLE_NAME);
  }


  @Override
  protected String[] generateQuery(SegmentContainer sc, ReadableQueryConfig qc) {
    String[] split = sc.getText().split(" ");
    String[] _return = new String[split.length];
    for (int i = 0; i < split.length; i++) {
      _return[i] = split[i] + "~0.5";
    }
    return _return;
  }
}
