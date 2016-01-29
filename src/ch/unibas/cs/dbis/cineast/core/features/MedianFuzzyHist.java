package ch.unibas.cs.dbis.cineast.core.features;

import java.sql.ResultSet;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ch.unibas.cs.dbis.cineast.core.config.Config;
import ch.unibas.cs.dbis.cineast.core.data.FrameContainer;
import ch.unibas.cs.dbis.cineast.core.data.LongDoublePair;
import ch.unibas.cs.dbis.cineast.core.features.abstracts.AbstractFeatureModule;
import ch.unibas.cs.dbis.cineast.core.segmenter.FuzzyColorHistogram;
import ch.unibas.cs.dbis.cineast.core.segmenter.FuzzyColorHistogramCalculator;

public class MedianFuzzyHist extends AbstractFeatureModule {

	private static final Logger LOGGER = LogManager.getLogger();

	public MedianFuzzyHist(){
		super("features.MedianFuzzyHist", "hist", 2f / 4f);
	}

	@Override
	public List<LongDoublePair> getSimilar(FrameContainer qc) {
		FuzzyColorHistogram query = FuzzyColorHistogramCalculator.getHistogramNormalized(qc.getMedianImg().getBufferedImage());
		int limit = Config.resultsPerModule();
		
		ResultSet rset = this.selector.select("SELECT * FROM features.MedianFuzzyHist USING DISTANCE MINKOWSKI(1)(\'" + query.toFeatureString() + "\', hist) ORDER USING DISTANCE LIMIT " + limit);
		return manageResultSet(rset);
	}


	@Override
	public List<LongDoublePair> getSimilar(FrameContainer qc, String resultCacheName) {
		FuzzyColorHistogram query = FuzzyColorHistogramCalculator.getHistogramNormalized(qc.getMedianImg().getBufferedImage());
		int limit = Config.resultsPerModule();
		
		ResultSet rset = this.selector.select(getResultCacheLimitSQL(resultCacheName) + " SELECT * FROM features.MedianFuzzyHist, c WHERE shotid = c.filter USING DISTANCE MINKOWSKI(1)(\'" + query.toFeatureString() + "\', hist) ORDER USING DISTANCE LIMIT " + limit);
		return manageResultSet(rset);
	}

	@Override
	public void processShot(FrameContainer shot) {
		LOGGER.entry();
		if (!phandler.check("SELECT * FROM features.MedianFuzzyHist WHERE shotid = " + shot.getId())) {
			FuzzyColorHistogram fch = FuzzyColorHistogramCalculator.getHistogramNormalized(shot.getMedianImg().getBufferedImage());
			addToDB(shot.getId(), fch);
		}
		LOGGER.exit();
	}

}
