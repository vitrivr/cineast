package ch.unibas.cs.dbis.cineast.core.features.exporter;

import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ch.unibas.cs.dbis.cineast.core.config.Config;
import ch.unibas.cs.dbis.cineast.core.data.Frame;
import ch.unibas.cs.dbis.cineast.core.data.SegmentContainer;
import ch.unibas.cs.dbis.cineast.core.db.PersistencyWriter;
import ch.unibas.cs.dbis.cineast.core.features.extractor.Extractor;
import ch.unibas.cs.dbis.cineast.core.util.LogHelper;

public class FrameExporter implements Extractor {

	private File folder = new File(Config.getExtractorConfig().getOutputLocation(), "exportedFrames");
	private int offset;
	private String format = "png";
	
	private static final Logger LOGGER = LogManager.getLogger();
	
	public FrameExporter(int offsetBetweenFrames){
		this.offset = offsetBetweenFrames;
	}
	
	public FrameExporter(int offsetBetweenFrames, String format){
		this(offsetBetweenFrames);
		this.format = format;
	}
	
	@Override
	public void init(PersistencyWriter<?> phandler) {
		phandler.close();
		if(!this.folder.exists()){
			this.folder.mkdirs();
		}
	}

	@Override
	public void processShot(SegmentContainer shot) {
		for(Frame f : shot.getFrames()){
			if(f.getId() % this.offset == 0){
				try {
					ImageIO.write(f.getImage().getBufferedImage(), this.format, new File(folder, String.format("%06d",(f.getId() / this.offset)) + "." + this.format));
				} catch (IOException e) {
					LOGGER.error("Error while exporting frame: {}", LogHelper.getStackTrace(e));
				}
			}
		}
	}

	@Override
	public void finish() {}

}
