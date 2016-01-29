package ch.unibas.cs.dbis.cineast.core.features.exporter;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ch.unibas.cs.dbis.cineast.core.color.ReadableRGBContainer;
import ch.unibas.cs.dbis.cineast.core.data.FrameContainer;
import ch.unibas.cs.dbis.cineast.core.db.PersistencyWriter;
import ch.unibas.cs.dbis.cineast.core.features.extractor.Extractor;
import ch.unibas.cs.dbis.cineast.core.util.LogHelper;
import ch.unibas.cs.dbis.cineast.core.util.MotionHistoryImage;

public class MotionHistoryImageExporter implements Extractor {

	private File folder = new File("MotionHistoryImages");
	private String format = "png";
	
	private static final Logger LOGGER = LogManager.getLogger();
	
	@Override
	public void init(PersistencyWriter<?> phandler) {
		phandler.close();
		if(!this.folder.exists()){
			this.folder.mkdirs();
		}
	}

	@Override
	public void processShot(FrameContainer shot) {
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

}
