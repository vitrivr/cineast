package org.vitrivr.cineast.core.features;

import org.vitrivr.cineast.core.features.abstracts.AbstractTextRetriever;

/**
 *  OCR is handled by adding fuzziness / levenshtein-distance support to the query if there are no quotes present (as quotes indicate precision)
 *  This makes sense here since we expect small errors from OCR sources
 */
public class OCRSearch extends AbstractTextRetriever {

  public static final String OCR_TABLE_NAME = "features_ocr";

  /**
   * Default constructor for {@link OCRSearch}.
   */
  public OCRSearch() {
    super(OCR_TABLE_NAME);
  }

  @Override
  protected String enrichQueryTerm(String queryTerm) {
    if (queryTerm.contains("\"")) {
      return queryTerm;
    }
    return queryTerm + "~1";
  }
}
