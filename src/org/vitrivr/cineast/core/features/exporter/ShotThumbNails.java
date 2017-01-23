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

public class ShotThumbNails implements Extractor {

	private File folder;
	private static final Logger LOGGER = LogManager.getLogger();
	
	@Override
	public void init(PersistencyWriterSupplier supply) {
		this.folder = new File(Config.getExtractorConfig().getOutputLocation(), "thumbnails");
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
		
		File img = new File(imageFolder, shot.getId() + ".png");
		if(img.exists()){
			return;
		}
		BufferedImage thumb = shot.getMostRepresentativeFrame().getImage().getThumbnailImage();
		try {
			ImageIO.write(thumb, "PNG", img);
		} catch (IOException e) {
			LOGGER.error("Could not write thumbnail image ", LogHelper.getStackTrace(e));
		}
		
	}

	@Override
	public void finish() {}
	
	@Override
	public void initalizePersistentLayer(Supplier<EntityCreator> supply) {}

}
