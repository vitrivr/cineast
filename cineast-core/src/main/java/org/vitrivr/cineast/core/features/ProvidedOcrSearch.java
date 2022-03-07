package org.vitrivr.cineast.core.features;

import org.vitrivr.cineast.core.features.abstracts.AbstractTextRetriever;

/**
 * Used when OCR is provided by an external API, e.g. Google Vision
 */
public class ProvidedOcrSearch extends AbstractTextRetriever {

  public static final String PROVIDED_OCR_SEARCH_TABLE_NAME = "features_providedOcr";

  /**
   * Default constructor for {@link ProvidedOcrSearch}.
   */
  public ProvidedOcrSearch() {
    super(ProvidedOcrSearch.PROVIDED_OCR_SEARCH_TABLE_NAME);
  }
}