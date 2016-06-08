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

import ch.unibas.cs.dbis.cineast.core.config.QueryConfig;
import ch.unibas.cs.dbis.cineast.core.data.SegmentContainer;
import ch.unibas.cs.dbis.cineast.core.data.StringDoublePair;
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
	public void finish() {
	}

	@Override
	public List<StringDoublePair> getSimilar(SegmentContainer sc, QueryConfig qc) {
		BufferedImage bimg = sc.getMostRepresentativeFrame().getImage().getBufferedImage();
		try {
			ImageIO.write(bimg, "PNG", new File(folder, this.df.format(Calendar.getInstance().getTime()) + ".png"));
		} catch (IOException e) {
			LOGGER.error(LogHelper.getStackTrace(e));
		}
		return new LinkedList<StringDoublePair>();
	}

	@Override
	public List<StringDoublePair> getSimilar(String shotId, QueryConfig qc) {
		return new LinkedList<StringDoublePair>();
	}


}
