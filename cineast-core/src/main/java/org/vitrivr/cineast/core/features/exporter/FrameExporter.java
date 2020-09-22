package org.vitrivr.cineast.core.features.exporter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.data.frames.VideoFrame;
import org.vitrivr.cineast.core.data.segments.SegmentContainer;
import org.vitrivr.cineast.core.db.PersistencyWriterSupplier;
import org.vitrivr.cineast.core.db.setup.EntityCreator;
import org.vitrivr.cineast.core.features.extractor.Extractor;
import org.vitrivr.cineast.core.util.LogHelper;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.function.Supplier;

public class FrameExporter implements Extractor {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final String PROPERTY_NAME_DESTINATION = "destination";
	private static final String PROPERTY_NAME_FORMAT = "format";
	private static final String PROPERTY_NAME_OFFSET = "offset";

	private final File folder;
	private final int offset;
	private final String format;
	

	/**
	 * Default constructor - no parameters.
	 */
	public FrameExporter() {
		this(new HashMap<>());
	}

	/**
	 * Constructor with property HashMap that allows for passing of parameters.
	 *
	 * Supported parameters:
	 *
	 * <ol>
	 *      <li>destination: Path where motion frameimages should be stored.</li>
	 * </ol>
	 *
	 * @param properties HashMap containing named properties
	 */
	public FrameExporter(HashMap<String, String> properties) {
		this.folder =  new File(properties.getOrDefault(PROPERTY_NAME_DESTINATION, "./thumbnails"));
		this.format = properties.getOrDefault(PROPERTY_NAME_FORMAT, "PNG");
		this.offset= Integer.valueOf(properties.getOrDefault(PROPERTY_NAME_OFFSET, "1"));
	}


	
	@Override
	public void init(PersistencyWriterSupplier phandlerSupply, int batchSize) {
		if(!this.folder.exists()){
			this.folder.mkdirs();
		}
	}

	@Override
	public void processSegment(SegmentContainer shot) {
		for(VideoFrame f : shot.getVideoFrames()){
			if(f.getId() % this.offset == 0){
				try {
					ImageIO.write(f.getImage().getBufferedImage(), this.format, new File(folder, String.format("%06d",(f.getId() / this.offset)) + "." + this.format));
				} catch (IOException e) {
					LOGGER.error("Error while exporting frame: {}", LogHelper.getStackTrace(e));
				}
			}
		}
	}

	@Override
	public void finish() {}

	@Override
	public void initalizePersistentLayer(Supplier<EntityCreator> supply) {}

	@Override
	public void dropPersistentLayer(Supplier<EntityCreator> supply) {}
}
