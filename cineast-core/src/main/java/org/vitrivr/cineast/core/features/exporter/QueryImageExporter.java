package org.vitrivr.cineast.core.features.exporter;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.function.Supplier;

import javax.imageio.ImageIO;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.vitrivr.cineast.core.config.ReadableQueryConfig;
import org.vitrivr.cineast.core.data.score.ScoreElement;
import org.vitrivr.cineast.core.data.segments.SegmentContainer;
import org.vitrivr.cineast.core.db.DBSelectorSupplier;
import org.vitrivr.cineast.core.features.retriever.Retriever;
import org.vitrivr.cineast.core.db.setup.EntityCreator;
import org.vitrivr.cineast.core.util.LogHelper;

public class QueryImageExporter implements Retriever {

	private static final Logger LOGGER = LogManager.getLogger();
	private static final String PROPERTY_NAME_DESTINATION = "destination";

	private final File folder;
	private final DateFormat df = new SimpleDateFormat("dd-MM-yyyy_HH-mm-ss-SSS");


	/**
	 * Default constructor - no parameters.
	 */
	public QueryImageExporter() {
		this(new HashMap<>());
	}

	/**
	 * Constructor with property HashMap that allows for passing of parameters.
	 *
	 * Supported parameters:
	 *
	 * <ol>
	 *      <li>destination: Path where query images should be stored.</li>
	 * </ol>
	 *
	 * @param properties HashMap containing named properties
	 */
	public QueryImageExporter(HashMap<String, String> properties) {
		this.folder =  new File(properties.getOrDefault(PROPERTY_NAME_DESTINATION, "./query_images"));
	}

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
	public List<ScoreElement> getSimilar(SegmentContainer sc, ReadableQueryConfig qc) {
		BufferedImage bimg = sc.getMostRepresentativeFrame().getImage().getBufferedImage();
		try {
		  String filename = (qc != null && qc.getQueryId() != null) ? qc.getQueryId().toString() : this.df.format(Calendar.getInstance().getTime());
			ImageIO.write(bimg, "PNG", new File(folder, filename + ".png"));
		} catch (IOException e) {
			LOGGER.error(LogHelper.getStackTrace(e));
		}
		return Collections.emptyList();
	}

	@Override
	public List<ScoreElement> getSimilar(String shotId, ReadableQueryConfig qc) {
		return Collections.emptyList();
	}

	@Override
	public void initalizePersistentLayer(Supplier<EntityCreator> supply) {}

	@Override
	public void dropPersistentLayer(Supplier<EntityCreator> supply) {}
}
