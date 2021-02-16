package org.vitrivr.cineast.core.features.exporter;

import static org.vitrivr.cineast.core.util.CineastConstants.GENERIC_ID_COLUMN_QUALIFIER;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.data.frames.VideoFrame;
import org.vitrivr.cineast.core.data.segments.SegmentContainer;
import org.vitrivr.cineast.core.db.PersistencyWriter;
import org.vitrivr.cineast.core.db.PersistencyWriterSupplier;
import org.vitrivr.cineast.core.db.setup.AttributeDefinition;
import org.vitrivr.cineast.core.db.setup.AttributeDefinition.AttributeType;
import org.vitrivr.cineast.core.db.setup.EntityCreator;
import org.vitrivr.cineast.core.features.extractor.Extractor;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.function.Supplier;

public class RepresentativeFrameExporter implements Extractor {

	private static final String PROPERTY_NAME_DESTINATION = "destination";
	private static final Logger LOGGER = LogManager.getLogger();

	@SuppressWarnings("rawtypes")
	private PersistencyWriter phandler;
	private final File folder;


	/**
	 * Default constructor - no parameters.
	 */
	public RepresentativeFrameExporter() {
		this(new HashMap<>());
	}

	/**
	 * Constructor with property HashMap that allows for passing of parameters.
	 *
	 * Supported parameters:
	 *
	 * <ol>
	 *      <li>destination: Path where representative frames should be stored.</li>
	 * </ol>
	 *
	 * @param properties HashMap containing named properties
	 */
	public RepresentativeFrameExporter(HashMap<String, String> properties) {
		this.folder =  new File(properties.getOrDefault(PROPERTY_NAME_DESTINATION, "./representative_frames"));
	}

	@Override
	public void init(PersistencyWriterSupplier supply, int batchSize) {
		this.phandler = supply.get();
		this.phandler.open("cineast_representativeframes");
		this.phandler.setFieldNames(GENERIC_ID_COLUMN_QUALIFIER, "frame");
		this.folder.mkdirs();
	}

	@Override
	public void processSegment(SegmentContainer segment) {
		File outFolder = new File(this.folder, segment.getSuperId());
		outFolder.mkdirs();
		File outFile = new File(outFolder, segment.getId() + ".png");
		VideoFrame f = segment.getMostRepresentativeFrame();
		try {
			ImageIO.write(f.getImage().getBufferedImage(), "PNG", outFile);
		} catch (IOException e) {
			LOGGER.error("Could not write representative frame: {}", e.getMessage());
		}
		persist(segment.getId(), f.getId());
	}

	protected void persist(String shotId, int frameId) {
		this.phandler.persist(this.phandler.generateTuple(shotId, frameId));
	}
	
	@Override
	public void finish() {
		this.phandler.close();
	}

	@Override
	public void initalizePersistentLayer(Supplier<EntityCreator> supply) {
		supply.get().createIdEntity("cineast_representativeframes", new AttributeDefinition("frame", AttributeType.INT));
	}

	@Override
	public void dropPersistentLayer(Supplier<EntityCreator> supply) {
		supply.get().dropEntity("cineast_representativeframes");
	}
}
