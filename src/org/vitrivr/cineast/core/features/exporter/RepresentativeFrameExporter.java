package org.vitrivr.cineast.core.features.exporter;

import java.io.File;
import java.io.IOException;
import java.util.function.Supplier;

import javax.imageio.ImageIO;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.adampro.grpc.AdamGrpc.AttributeType;
import org.vitrivr.cineast.core.config.Config;
import org.vitrivr.cineast.core.data.Frame;
import org.vitrivr.cineast.core.data.SegmentContainer;
import org.vitrivr.cineast.core.db.PersistencyWriter;
import org.vitrivr.cineast.core.db.PersistencyWriterSupplier;
import org.vitrivr.cineast.core.features.extractor.Extractor;
import org.vitrivr.cineast.core.setup.EntityCreator;
import org.vitrivr.cineast.core.setup.EntityCreator.AttributeDefinition;

public class RepresentativeFrameExporter implements Extractor {

	@SuppressWarnings("rawtypes")
	private PersistencyWriter phandler;
	private File folder;
	private static final Logger LOGGER = LogManager.getLogger();

	@Override
	public void init(PersistencyWriterSupplier supply) {
		this.phandler = supply.get();
		this.phandler.open("cineast_representativeframes");
		this.phandler.setFieldNames("id", "frame");
		this.folder = new File(Config.getExtractorConfig().getOutputLocation(), "representative_frames");
		this.folder.mkdirs();
	}

	@Override
	public void processShot(SegmentContainer segment) {
		File outFolder = new File(this.folder, segment.getSuperId());
		outFolder.mkdirs();
		File outFile = new File(outFolder, segment.getId() + ".png");
		Frame f = segment.getMostRepresentativeFrame();
		try {
			ImageIO.write(f.getImage().getBufferedImage(), "PNG", outFile);
		} catch (IOException e) {
			LOGGER.error("Could not write representative frame: {}", e.getMessage());
		}
		persist(segment.getId(), f.getId());
	}

	@SuppressWarnings("unchecked")
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

}
