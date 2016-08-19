package org.vitrivr.cineast.core.features.exporter;

import java.awt.image.BufferedImage;

import org.vitrivr.cineast.core.data.FloatVector;

public interface FeatureVisualizer {

	/**
	 * Visualises the vector representation of a feature
	 * @param fv feature information
	 * @param img the image to draw the visualisation into. If img is null, a new {@link BufferedImage} is created.
	 * @return the image containing the visualisation
	 */
	BufferedImage visualize(FloatVector fv, BufferedImage img);
	
}
