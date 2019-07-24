package org.vitrivr.cineast.standalone.run;

/**
 * An {@link ExtractionItemProcessor} processes {@link ExtractionItemContainer}s.
 *
 * @author silvan on 16.04.18.
 */
public interface ExtractionItemProcessor {

  public void addExtractionCompleteListener(ExtractionCompleteListener listener);

}
