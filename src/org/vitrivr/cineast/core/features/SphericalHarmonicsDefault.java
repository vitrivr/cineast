package org.vitrivr.cineast.core.features;

/**
 * An Extraction and Retrieval module for 3D models that leverages Spherical Harmonics as proposed in [1]. This version
 * uses mid-resolution settings in terms of harmonics to consider.
 *
 * [1] Funkhouser, T., Min, P., Kazhdan, M., Chen, J., Halderman, A., Dobkin, D., & Jacobs, D. (2003).
 *      A search engine for 3D models. ACM Trans. Graph., 22(1), 83–105. http://doi.org/10.1145/588272.588279

 * @author rgasser
 * @version 1.0
 * @created 16.02.17
 */
public class SphericalHarmonicsDefault extends SphericalHarmonics {
    /**
     * Constructor for SphericalHarmonics feature module. Uses the values
     * for grid_size proposed in [1] and harmonics up to l=6
     */
    public SphericalHarmonicsDefault() {
        super("features_sphericalhdefault", 64, 0, 4);
    }
}
