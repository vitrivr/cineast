package ch.unibas.cs.dbis.cineast.core.features.exporter;

import java.awt.image.BufferedImage;

import ch.unibas.cs.dbis.cineast.core.data.FloatVector;

public interface FeatureVisualizer {

	/**
	 * Visualises the vector representation of a feature
	 * @param fv feature information
	 * @param img the image to draw the visualisation into. If img is null, a new {@link BufferedImage} is created.
	 * @return the image containing the visualisation
	 */
	BufferedImage visualize(FloatVector fv, BufferedImage img);
	
}
