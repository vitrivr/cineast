package org.vitrivr.cineast.core.features.exporter;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
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

public class ShotDescriptorExporter implements Extractor {

	private static File folder = new File(Config.getExtractorConfig().getOutputLocation(), "descriptors");
	private static final Logger LOGGER = LogManager.getLogger();
	
	@Override
	public void init(PersistencyWriterSupplier supply) {
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
	
	@Override
	public void initalizePersistentLayer(Supplier<EntityCreator> supply) {}

	@Override
	public void dropPersistentLayer(Supplier<EntityCreator> supply) {}
}
