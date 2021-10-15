package org.vitrivr.cineast.standalone.run;

/**
 * An {@link ExtractionItemProcessor} processes {@link ExtractionItemContainer}s.
 *
 */
public interface ExtractionItemProcessor {

  void addExtractionCompleteListener(ExtractionCompleteListener listener);

}
