package org.vitrivr.cineast.core.features.exporter;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.function.Supplier;

import javax.imageio.ImageIO;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.vitrivr.cineast.core.config.Config;
import org.vitrivr.cineast.core.data.SegmentContainer;
import org.vitrivr.cineast.core.db.PersistencyWriterSupplier;
import org.vitrivr.cineast.core.features.extractor.Extractor;
import org.vitrivr.cineast.core.setup.EntityCreator;
import org.vitrivr.cineast.core.util.LogHelper;

public class ShotThumbNails implements Extractor {
	private static final Logger LOGGER = LogManager.getLogger();

	private static final String PROPERTY_NAME_DESTINATION = "destination";
	private static final String PROPERTY_NAME_FORMAT = "format";

	/** Destination folder. */
	private File folder;

	/** Output format for thumbnails. Defaults to PNG. */
	private String format = "PNG";

	/**
	 * Default constructor - no parameters.
	 */
	public ShotThumbNails() {
		this.folder = Config.sharedConfig().getExtractor().getOutputLocation();
	}

	/**
	 * Constructor with property HashMap that allows for passing of parameters.
	 *
	 * 'destination' and 'format' are currently supported parameters.
	 *
	 * @param properties
	 */
	public ShotThumbNails(HashMap<String, String> properties) {
		if (properties.containsKey(PROPERTY_NAME_DESTINATION)) {
			this.folder = new File(properties.get(PROPERTY_NAME_DESTINATION));
		} else {
			this.folder = Config.sharedConfig().getExtractor().getOutputLocation();
		}

		if (properties.containsKey(PROPERTY_NAME_FORMAT)) {
			this.format = properties.get(PROPERTY_NAME_FORMAT);
		}
	}

    /**
     *
     * @param supply
     */
	@Override
	public void init(PersistencyWriterSupplier supply) {
		this.folder = new File( Config.sharedConfig().getExtractor().getOutputLocation(), "thumbnails");
		if(!this.folder.exists()){
			this.folder.mkdirs();
		}
	}

	@Override
	public void processShot(SegmentContainer shot) {
		
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
			LOGGER.error("Could not write thumbnail image ", LogHelper.getStackTrace(e));
		}
		
	}

	@Override
	public void finish() {}
	
	@Override
	public void initalizePersistentLayer(Supplier<EntityCreator> supply) {}

	@Override
	public void dropPersistentLayer(Supplier<EntityCreator> supply) {}
}
