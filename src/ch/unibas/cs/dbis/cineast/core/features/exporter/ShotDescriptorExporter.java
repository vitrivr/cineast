package ch.unibas.cs.dbis.cineast.core.features.exporter;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ch.unibas.cs.dbis.cineast.core.data.SegmentContainer;
import ch.unibas.cs.dbis.cineast.core.db.PersistencyWriter;
import ch.unibas.cs.dbis.cineast.core.features.extractor.Extractor;
import ch.unibas.cs.dbis.cineast.core.util.LogHelper;

public class ShotDescriptorExporter implements Extractor {

	private static File folder = new File("descriptors");
	private static final Logger LOGGER = LogManager.getLogger();
	
	@Override
	public void init(PersistencyWriter<?> phandler) {
		phandler.close();
		
		if(!folder.exists()){
			folder.mkdirs();
		}
	}

	@Override
	public void processShot(SegmentContainer shot) {
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

}
