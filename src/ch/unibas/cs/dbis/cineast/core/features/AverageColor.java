package ch.unibas.cs.dbis.cineast.core.features;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ch.unibas.cs.dbis.cineast.core.color.ColorConverter;
import ch.unibas.cs.dbis.cineast.core.color.ReadableLabContainer;
import ch.unibas.cs.dbis.cineast.core.config.QueryConfig;
import ch.unibas.cs.dbis.cineast.core.data.MultiImage;
import ch.unibas.cs.dbis.cineast.core.data.SegmentContainer;
import ch.unibas.cs.dbis.cineast.core.data.StringDoublePair;
import ch.unibas.cs.dbis.cineast.core.features.abstracts.AbstractFeatureModule;
import ch.unibas.cs.dbis.cineast.core.util.ColorUtils;
import ch.unibas.cs.dbis.cineast.core.util.TimeHelper;

public class AverageColor extends AbstractFeatureModule {

	public AverageColor() {
		super("features.AverageColor", 196f / 4f);
	}

	private static final Logger LOGGER = LogManager.getLogger();
	
	public static ReadableLabContainer getAvg(MultiImage img){
		int avg = ColorUtils.getAvg(img.getColors());
		return ColorConverter.cachedRGBtoLab(avg);
	}
	
	@Override
	public void processShot(SegmentContainer shot) {
		TimeHelper.tic();
		LOGGER.entry();
		if (!phandler.idExists(shot.getId())) {
			ReadableLabContainer avg = getAvg(shot.getAvgImg());
			persist(shot.getId(), avg);
			LOGGER.debug("AverageColor.processShot() done in {}",
					TimeHelper.toc());
		}
		LOGGER.exit();
	}

	@Override
	public List<StringDoublePair> getSimilar(SegmentContainer sc, QueryConfig qc) {
		ReadableLabContainer query = getAvg(sc.getAvgImg());
		return getSimilar(query.toArray(null), qc);
	}

	@Override
	public List<StringDoublePair> getSimilar(long shotId, QueryConfig qc) {
		// TODO Auto-generated method stub
		return null;
	}

}
