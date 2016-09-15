package org.vitrivr.cineast.core.features.exporter;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.function.Supplier;

import javax.imageio.ImageIO;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.color.ReadableRGBContainer;
import org.vitrivr.cineast.core.config.Config;
import org.vitrivr.cineast.core.data.SegmentContainer;
import org.vitrivr.cineast.core.db.PersistencyWriterSupplier;
import org.vitrivr.cineast.core.features.extractor.Extractor;
import org.vitrivr.cineast.core.setup.EntityCreator;
import org.vitrivr.cineast.core.util.LogHelper;
import org.vitrivr.cineast.core.util.MotionHistoryImage;

public class MotionHistoryImageExporter implements Extractor {

	private File folder = new File(Config.getExtractorConfig().getOutputLocation(), "MotionHistoryImages");
	private String format = "png";
	
	private static final Logger LOGGER = LogManager.getLogger();
	
	@Override
	public void init(PersistencyWriterSupplier phandlerSupply) {
		if(!this.folder.exists()){
			this.folder.mkdirs();
		}
	}

	@Override
	public void processShot(SegmentContainer shot) {
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
			ImageIO.write(img, format, new File(folder, String.format("%06d", shot.getId()) + "." + this.format));
		} catch (IOException e) {
			LOGGER.error("Error while exporting motion history image: {}", LogHelper.getStackTrace(e));
		}
	}

	@Override
	public void finish() {}
	
	@Override
	public void initalizePersistentLayer(Supplier<EntityCreator> supply) {}

}
