package org.vitrivr.cineast.core.features.exporter;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.function.Supplier;

import javax.imageio.ImageIO;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.vitrivr.cineast.core.data.segments.SegmentContainer;
import org.vitrivr.cineast.core.db.PersistencyWriterSupplier;
import org.vitrivr.cineast.core.features.extractor.Extractor;
import org.vitrivr.cineast.core.db.setup.EntityCreator;
import org.vitrivr.cineast.core.util.LogHelper;

public class ShotDescriptorExporter implements Extractor {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final String PROPERTY_NAME_DESTINATION = "destination";

	private final File folder;

	/**
	 * Default constructor - no parameters.
	 */
	public ShotDescriptorExporter() {
		this(new HashMap<>());
	}

	/**
	 * Constructor with property HashMap that allows for passing of parameters.
	 *
	 * Supported parameters:
	 *
	 * <ol>
	 *      <li>destination: Path where descriptors should be stored.</li>
	 *      <li>format: The image format to use (PNG, JPEG).</li>
	 * </ol>
	 *
	 * @param properties HashMap containing named properties
	 */
	public ShotDescriptorExporter(HashMap<String, String> properties) {
		this.folder =  new File(properties.getOrDefault(PROPERTY_NAME_DESTINATION, "./descriptors"));
	}

	@Override
	public void init(PersistencyWriterSupplier supply) {
		if(!folder.exists()){
			folder.mkdirs();
		}
	}

	@Override
	public void processSegment(SegmentContainer shot) {
		String id = String.format("%06d",shot.getId());
		
		BufferedImage img = shot.getAvgImg().getBufferedImage();
		
		try {
			ImageIO.write(img, "PNG", new File(folder, id + "_avg.png"));
		} catch (IOException e) {
			LOGGER.error(LogHelper.getStackTrace(e));
		}
		
		img = shot.getMedianImg().getBufferedImage();
		
		try {
			ImageIO.write(img, "PNG", new File(folder, id + "_med.png"));
		} catch (IOException e) {
			LOGGER.error(LogHelper.getStackTrace(e));
		}
		
		img = shot.getMostRepresentativeFrame().getImage().getBufferedImage();
		
		try {
			ImageIO.write(img, "PNG", new File(folder, id + "_rep.png"));
		} catch (IOException e) {
			LOGGER.error(LogHelper.getStackTrace(e));
		}
	}

	@Override
	public void finish() {}
	
	@Override
	public void initalizePersistentLayer(Supplier<EntityCreator> supply) {}

	@Override
	public void dropPersistentLayer(Supplier<EntityCreator> supply) {}
}
