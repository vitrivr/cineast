package org.vitrivr.cineast.core.features;

/**
 * An Extraction and Retrieval module for 3D models that leverages Spherical Harmonics as proposed in [1]. This version
 * uses low-resolution settings in terms of harmonics to consider.
 *
 * [1] Funkhouser, T., Min, P., Kazhdan, M., Chen, J., Halderman, A., Dobkin, D., & Jacobs, D. (2003).
 *      A search engine for 3D models. ACM Trans. Graph., 22(1), 83–105. http://doi.org/10.1145/588272.588279

  */
public class SphericalHarmonicsLow extends SphericalHarmonics {
    /**
     * Constructor for SphericalHarmonics feature module. Uses the values
     * for grid_size proposed in [1] and harmonics up to l=5
     */
    public SphericalHarmonicsLow() {
        super("features_sphericalhlow", 64, 0,3);
    }
}
