package org.vitrivr.cineast.core.features.exporter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.color.ReadableRGBContainer;
import org.vitrivr.cineast.core.data.segments.SegmentContainer;
import org.vitrivr.cineast.core.db.PersistencyWriterSupplier;
import org.vitrivr.cineast.core.db.setup.EntityCreator;
import org.vitrivr.cineast.core.features.extractor.Extractor;
import org.vitrivr.cineast.core.util.LogHelper;
import org.vitrivr.cineast.core.util.MotionHistoryImage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.function.Supplier;

public class MotionHistoryImageExporter implements Extractor {

	private static final String PROPERTY_NAME_DESTINATION = "destination";
	private static final String PROPERTY_NAME_FORMAT = "format";
	private static final Logger LOGGER = LogManager.getLogger();

	private final File folder;
	private final String format;

	/**
	 * Default constructor - no parameters.
	 */
	public MotionHistoryImageExporter() {
		this(new HashMap<>());
	}

	/**
	 * Constructor with property HashMap that allows for passing of parameters.
	 *
	 * Supported parameters:
	 *
	 * <ol>
	 *      <li>destination: Path where motion history image images should be stored.</li>
	 * </ol>
	 *
	 * @param properties HashMap containing named properties
	 */
	public MotionHistoryImageExporter(HashMap<String, String> properties) {
		this.folder =  new File(properties.getOrDefault(PROPERTY_NAME_DESTINATION, "./motion_history"));
		this.format = properties.getOrDefault(PROPERTY_NAME_FORMAT, "PNG");
	}

	@Override
	public void init(PersistencyWriterSupplier phandlerSupply) {
		if(!this.folder.exists()){
			this.folder.mkdirs();
		}
	}

	@Override
	public void processSegment(SegmentContainer shot) {
		MotionHistoryImage mhi = MotionHistoryImage.motionHistoryImage(shot, 30, 30);
		if(mhi == null){
			return;
		}
		BufferedImage img = new BufferedImage(mhi.getWidth(), mhi.getHeight(), BufferedImage.TYPE_INT_RGB);
		int[] colors = new int[mhi.getIntensities().length];
		for(int i = 0; i < colors.length; ++i){
			int c = mhi.getIntensities()[i] * 2;
			colors[i] = ReadableRGBContainer.toIntColor(c, c, c);
		}
		img.setRGB(0, 0, img.getWidth(), img.getHeight(), colors, 0, img.getWidth());
		try {
			ImageIO.write(img, format, new File(folder, String.format("%s.%s", shot.getId(), this.format.toLowerCase())));
		} catch (IOException e) {
			LOGGER.error("Error while exporting motion history image: {}", LogHelper.getStackTrace(e));
		}
	}

	@Override
	public void finish() {}
	
	@Override
	public void initalizePersistentLayer(Supplier<EntityCreator> supply) {}

	@Override
	public void dropPersistentLayer(Supplier<EntityCreator> supply) {}
}
