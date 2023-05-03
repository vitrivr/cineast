package org.vitrivr.cineast.core.util.texturemodel.Viewpoint;

/**
 * For benchmark purposes. Since the Extractor does not support options, strategy should be implemented in a static way
 */
public enum ViewpointStrategy {
    /**
     * Randomly selects a viewpoint
     */
    RANDOM,
    /**
     * Selects the viewpoint from the front (0,0,1)
     */
    FRONT,
    /**
     * Selects the viewpoint from the upper left (-1,1,1)
     */
    UPPER_LEFT,
    /**
     * Runs the viewpoint entropy maximization algorithm to find the best viewpoint
     */
    VIEWPOINT_ENTROPY_MAXIMIZATION_RANDOMIZED,
    /**
     * Runs the viewpoint entropy maximization algorithm with y plane attraction to find the best viewpoint
     */
    VIEWPOINT_ENTROPY_MAXIMIZATION_RANDOMIZED_WEIGHTED,
    /**
     * Takes multiple images and aggregates the vectors using k-means
     */
    MULTI_IMAGE_KMEANS,
    /**
     * Takes multiple images and aggregates the vectors using the projected mean
     */
    MULTI_IMAGE_PROJECTEDMEAN,
    /**
     * Takes multiple images and embeds them as a video
     */
    MULTI_IMAGE_FRAME,
    /**
     * Creates a 2x2 image
     */
    MULTI_IMAGE_2_2,
}