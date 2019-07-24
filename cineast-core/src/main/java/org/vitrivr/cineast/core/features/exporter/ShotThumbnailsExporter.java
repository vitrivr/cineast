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

public class ShotThumbnailsExporter implements Extractor {
	private static final Logger LOGGER = LogManager.getLogger();

	private static final String PROPERTY_NAME_DESTINATION = "destination";
	private static final String PROPERTY_NAME_FORMAT = "format";

	/** Destination folder. */
	private final File folder;

	/** Output format for thumbnails. Defaults to PNG. */
	private final String format;

	/**
	 * Default constructor - no parameters.
	 */
	public ShotThumbnailsExporter() {
		this(new HashMap<>());
	}

	/**
	 * Constructor with property HashMap that allows for passing of parameters.
	 *
	 * Supported parameters:
	 *
	 * <ol>
	 *      <li>destination: Path where shot thumbnails should be stored.</li>
	 *      <li>format: The image format to use (PNG, JPEG).</li>
	 * </ol>
	 *
	 * @param properties HashMap containing named properties
	 */
	public ShotThumbnailsExporter(HashMap<String, String> properties) {
		this.folder =  new File(properties.getOrDefault(PROPERTY_NAME_DESTINATION, "./thumbnails"));
		this.format = properties.getOrDefault(PROPERTY_NAME_FORMAT, "PNG");
	}

    /**
     *
     * @param supply
     */
	@Override
	public void init(PersistencyWriterSupplier supply) {
		if(!this.folder.exists()){
			this.folder.mkdirs();
		}
	}

	@Override
	public void processSegment(SegmentContainer shot) {
		
		File imageFolder = new File(this.folder, shot.getSuperId());
		if(!imageFolder.exists()){
			imageFolder.mkdirs();
		}
		
		File img = new File(imageFolder, shot.getId() + "." + this.format.toLowerCase());
		if(img.exists()){
			return;
		}
		BufferedImage thumb = shot.getMostRepresentativeFrame().getImage().getThumbnailImage();
		try {
			ImageIO.write(thumb, format, img);
		} catch (IOException e) {
			LOGGER.error("Could not write thumbnail image: {}", LogHelper.getStackTrace(e));
		}
	}

	@Override
	public void finish() {}
	
	@Override
	public void initalizePersistentLayer(Supplier<EntityCreator> supply) {}

	@Override
	public void dropPersistentLayer(Supplier<EntityCreator> supply) {}
}
