package org.vitrivr.cineast.core.features.exporter;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;

import javax.imageio.ImageIO;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.config.Config;
import org.vitrivr.cineast.core.config.ReadableQueryConfig;
import org.vitrivr.cineast.core.data.StringDoublePair;
import org.vitrivr.cineast.core.data.segments.SegmentContainer;
import org.vitrivr.cineast.core.db.DBSelectorSupplier;
import org.vitrivr.cineast.core.features.retriever.Retriever;
import org.vitrivr.cineast.core.setup.EntityCreator;
import org.vitrivr.cineast.core.util.LogHelper;

public class QueryImageExporter implements Retriever {

	private File folder = new File(Config.sharedConfig().getExtractor().getOutputLocation(), "queryImages");
	private DateFormat df = new SimpleDateFormat("dd-MM-yyyy_HH-mm-ss-SSS");
	private static final Logger LOGGER = LogManager.getLogger();
	
	@Override
	public void init(DBSelectorSupplier supply) {
		if(!this.folder.exists() || !this.folder.isDirectory()) {
			this.folder.mkdirs();
		}
	}

	@Override
	public void finish() {
	}

	@Override
	public List<StringDoublePair> getSimilar(SegmentContainer sc, ReadableQueryConfig qc) {
		BufferedImage bimg = sc.getMostRepresentativeFrame().getImage().getBufferedImage();
		try {
			ImageIO.write(bimg, "PNG", new File(folder, this.df.format(Calendar.getInstance().getTime()) + ".png"));
		} catch (IOException e) {
			LOGGER.error(LogHelper.getStackTrace(e));
		}
		return new LinkedList<StringDoublePair>();
	}

	@Override
	public List<StringDoublePair> getSimilar(String shotId, ReadableQueryConfig qc) {
		return new LinkedList<StringDoublePair>();
	}

	@Override
	public void initalizePersistentLayer(Supplier<EntityCreator> supply) {}

	@Override
	public void dropPersistentLayer(Supplier<EntityCreator> supply) {}
}
