package ch.unibas.cs.dbis.cineast.core.features.exporter;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ch.unibas.cs.dbis.cineast.core.data.FrameContainer;
import ch.unibas.cs.dbis.cineast.core.data.LongDoublePair;
import ch.unibas.cs.dbis.cineast.core.db.DBSelector;
import ch.unibas.cs.dbis.cineast.core.features.retriever.Retriever;
import ch.unibas.cs.dbis.cineast.core.util.LogHelper;

public class QueryImageExporter implements Retriever {

	private File folder = new File("queryImages");
	private DateFormat df = new SimpleDateFormat("dd-MM-yyyy_HH-mm-ss-SSS");
	private static final Logger LOGGER = LogManager.getLogger();
	
	@Override
	public void init(DBSelector selector) {
		selector.close();
		if(!this.folder.exists() || !this.folder.isDirectory()) {
			this.folder.mkdirs();
		}
	}

	@Override
	public List<LongDoublePair> getSimilar(FrameContainer qc) {
		BufferedImage bimg = qc.getMostRepresentativeFrame().getImage().getBufferedImage();
		try {
			ImageIO.write(bimg, "PNG", new File(folder, this.df.format(Calendar.getInstance().getTime()) + ".png"));
		} catch (IOException e) {
			LOGGER.error(LogHelper.getStackTrace(e));
		}
		return new LinkedList<LongDoublePair>();
	}

	@Override
	public List<LongDoublePair> getSimilar(long shotId) {
		return new LinkedList<LongDoublePair>();
	}

	@Override
	public void finish() {
	}

	@Override
	public float getConfidenceWeight() {
		return 0;
	}

	@Override
	public List<LongDoublePair> getSimilar(FrameContainer qc, String resultCacheName) {
		return this.getSimilar(qc);
	}

	@Override
	public List<LongDoublePair> getSimilar(long shotId, String resultCacheName) {
		return new LinkedList<LongDoublePair>();
	}

}
