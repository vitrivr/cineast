package ch.unibas.cs.dbis.cineast.core.features.exporter;

import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ch.unibas.cs.dbis.cineast.core.data.Frame;
import ch.unibas.cs.dbis.cineast.core.data.SegmentContainer;
import ch.unibas.cs.dbis.cineast.core.db.PersistencyWriter;
import ch.unibas.cs.dbis.cineast.core.features.extractor.Extractor;

public class RepresentativeFrameExporter implements Extractor {

	private PersistencyWriter phandler;
	private final File folder = new File("representative_frames");
	private static final Logger LOGGER = LogManager.getLogger();

	@Override
	public void init(PersistencyWriter<?> phandler) {
		this.phandler = phandler;
		this.phandler.open("cineast.representativeframes");
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
