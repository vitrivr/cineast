package org.vitrivr.cineast.core.features;

/**
 * An Extraction and Retrieval module that leverages HPCP CENS shingles according to [1]. The particular module focuses on melodic frequencies following an idea found in [2].
 * <p>
 * [1] Grosche, P., & Muller, M. (2012). Toward characteristic audio shingles for efficient cross-version music retrieval. In 2012 IEEE International Conference on Acoustics, Speech and Signal Processing (ICASSP) (pp. 473–476). IEEE. http://doi.org/10.1109/ICASSP.2012.6287919
 * <p>
 * [2] Salamon, J., Serrà, J., & Gómez, E. (2012). Tonal representations for music retrieval: from version identification to query-by-humming. International Journal of Multimedia Information Retrieval, 2(1), 45–58. http://doi.org/10.1007/s13735-012-0026-0
 */
public class CENS12MelodyShingle extends CENS {

  private final static float MIN_FREQUENCY = 262.0f;

  private final static float MAX_FREQUENCY = 5000.0f;

  /**
   * Default constructor; CENS Shingle using frequencies between 262.0f and 5000.0f Hz.
   */
  public CENS12MelodyShingle() {
    super("features_cens12melody", MIN_FREQUENCY, MAX_FREQUENCY);
  }
}
