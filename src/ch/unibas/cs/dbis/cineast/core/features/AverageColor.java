package ch.unibas.cs.dbis.cineast.core.features;

import java.sql.ResultSet;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ch.unibas.cs.dbis.cineast.core.color.ColorConverter;
import ch.unibas.cs.dbis.cineast.core.color.ReadableLabContainer;
import ch.unibas.cs.dbis.cineast.core.config.Config;
import ch.unibas.cs.dbis.cineast.core.data.FrameContainer;
import ch.unibas.cs.dbis.cineast.core.data.LongDoublePair;
import ch.unibas.cs.dbis.cineast.core.data.MultiImage;
import ch.unibas.cs.dbis.cineast.core.features.abstracts.AbstractFeatureModule;
import ch.unibas.cs.dbis.cineast.core.util.ColorUtils;
import ch.unibas.cs.dbis.cineast.core.util.TimeHelper;

public class AverageColor extends AbstractFeatureModule {

	public AverageColor() {
		super("features.AverageColor", "color", 196f / 4f);
	}

	private static final Logger LOGGER = LogManager.getLogger();
	
	public static ReadableLabContainer getAvg(MultiImage img){
		int avg = ColorUtils.getAvg(img.getColors());
		return ColorConverter.cachedRGBtoLab(avg);
	}
	
	@Override
	public void processShot(FrameContainer shot) {
		TimeHelper.tic();
		LOGGER.entry();
		if (!phandler.check("SELECT * FROM features.AverageColor WHERE shotid = " + shot.getId())) {
			ReadableLabContainer avg = getAvg(shot.getAvgImg());
			long shotId = shot.getId();
			addToDB(shotId, avg);
			LOGGER.debug("AverageColor.processShot() done in {}",
					TimeHelper.toc());
		}
		LOGGER.exit();
	}

	@Override
	public List<LongDoublePair> getSimilar(FrameContainer qc) {
		ReadableLabContainer query = getAvg(qc.getAvgImg());
		int limit = Config.getRetrieverConfig().getMaxResultsPerModule();
		
		ResultSet rset = this.selector.select("SELECT * FROM features.AverageColor USING DISTANCE MINKOWSKI(1)(\'" + query.toFeatureString() + "\', color) ORDER USING DISTANCE LIMIT " + limit);
		return manageResultSet(rset);
	}

	@Override
	public List<LongDoublePair> getSimilar(FrameContainer qc, String resultCacheName) {
		ReadableLabContainer query = getAvg(qc.getAvgImg());
		int limit = Config.getRetrieverConfig().getMaxResultsPerModule();
		
		ResultSet rset = this.selector.select(getResultCacheLimitSQL(resultCacheName) + " SELECT * FROM features.AverageColor, c WHERE shotid = c.filter USING DISTANCE MINKOWSKI(1)(\'" + query.toFeatureString() + "\', color) ORDER USING DISTANCE LIMIT " + limit);
		return manageResultSet(rset);
	}
	
}
