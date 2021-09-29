package org.vitrivr.cineast.core.features;

/**
 * An Extraction and Retrieval module for 3D models that leverages Spherical Harmonics as proposed in [1]. This version
 * uses high-resolution settings in terms of voxel-grid size and harmonics to use.
 *
 * [1] Funkhouser, T., Min, P., Kazhdan, M., Chen, J., Halderman, A., Dobkin, D., & Jacobs, D. (2003).
 *      A search engine for 3D models. ACM Trans. Graph., 22(1), 83–105. http://doi.org/10.1145/588272.588279

  */
public class SphericalHarmonicsHigh extends SphericalHarmonics {
    /**
     * Constructor for SphericalHarmonics feature module. Uses the values a higher
     * grid-size value than proposed in [1] and harmonics up to l=5
     */
    public SphericalHarmonicsHigh() {
        super("features_sphericalhhigh", 74, 1, 5);
    }
}
