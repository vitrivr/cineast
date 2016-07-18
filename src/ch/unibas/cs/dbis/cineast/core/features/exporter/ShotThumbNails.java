package ch.unibas.cs.dbis.cineast.core.features.exporter;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ch.unibas.cs.dbis.cineast.core.config.Config;
import ch.unibas.cs.dbis.cineast.core.data.SegmentContainer;
import ch.unibas.cs.dbis.cineast.core.db.PersistencyWriter;
import ch.unibas.cs.dbis.cineast.core.features.extractor.Extractor;
import ch.unibas.cs.dbis.cineast.core.util.LogHelper;

public class ShotThumbNails implements Extractor {

	private File folder;
	private static final Logger LOGGER = LogManager.getLogger();
	
	@Override
	public void init(PersistencyWriter<?> phandler) {
		phandler.close(); //not needed
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
		
		File img = new File(imageFolder, shot.getId() + ".jpg");
		if(img.exists()){
			return;
		}
		BufferedImage thumb = shot.getMostRepresentativeFrame().getImage().getThumbnailImage();
		try {
			ImageIO.write(thumb, "JPG", img);
		} catch (IOException e) {
			LOGGER.error("Could not write thumbnail image ", LogHelper.getStackTrace(e));
		}
		
	}

	@Override
	public void finish() {}

}
