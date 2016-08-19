package org.vitrivr.cineast.core.features.exporter;

import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.config.Config;
import org.vitrivr.cineast.core.data.Frame;
import org.vitrivr.cineast.core.data.SegmentContainer;
import org.vitrivr.cineast.core.db.PersistencyWriter;
import org.vitrivr.cineast.core.features.extractor.Extractor;

public class RepresentativeFrameExporter implements Extractor {

	private PersistencyWriter phandler;
	private File folder;
	private static final Logger LOGGER = LogManager.getLogger();

	@Override
	public void init(PersistencyWriter<?> phandler) {
		this.phandler = phandler;
		this.phandler.open("cineast_representativeframes");
		this.phandler.setFieldNames("id", "frame");
		this.folder = new File(Config.getExtractorConfig().getOutputLocation(), "representative_frames");
		this.folder.mkdirs();
	}

	@Override
	public void processShot(SegmentContainer shot) {
		File outFolder = new File(this.folder, shot.getSuperId());
		outFolder.mkdirs();
		File outFile = new File(outFolder, shot.getId() + ".jpg");
		Frame f = shot.getMostRepresentativeFrame();
		try {
			ImageIO.write(f.getImage().getBufferedImage(), "JPG", outFile);
		} catch (IOException e) {
			LOGGER.error("Could not write representative frame: {}", e.getMessage());
		}
		persist(shot.getId(), f.getId());
	}

	protected void persist(String shotId, int frameId) {
		this.phandler.persist(this.phandler.generateTuple(shotId, frameId));
	}
	
	@Override
	public void finish() {
		this.phandler.close();
	}

}
