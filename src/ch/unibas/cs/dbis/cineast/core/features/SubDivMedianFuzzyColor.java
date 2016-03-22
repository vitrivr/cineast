package ch.unibas.cs.dbis.cineast.core.features;

import java.sql.ResultSet;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ch.unibas.cs.dbis.cineast.core.config.Config;
import ch.unibas.cs.dbis.cineast.core.data.FrameContainer;
import ch.unibas.cs.dbis.cineast.core.data.LongDoublePair;
import ch.unibas.cs.dbis.cineast.core.features.abstracts.AbstractFeatureModule;
import ch.unibas.cs.dbis.cineast.core.segmenter.FuzzyColorHistogramCalculator;
import ch.unibas.cs.dbis.cineast.core.segmenter.SubdividedFuzzyColorHistogram;

public class SubDivMedianFuzzyColor extends AbstractFeatureModule {

	private static final Logger LOGGER = LogManager.getLogger();

	public SubDivMedianFuzzyColor(){
		super("features.SubDivMedianFuzzyColor", "hist", 2f / 4f);
	}
	
	@Override
	public List<LongDoublePair> getSimilar(FrameContainer qc) {
		SubdividedFuzzyColorHistogram query = FuzzyColorHistogramCalculator.getSubdividedHistogramNormalized(qc.getMedianImg().getBufferedImage(), 2);
		int limit = Config.getRetrieverConfig().getMaxResultsPerModule();
		
		ResultSet rset = this.selector.select("SELECT * FROM features.SubDivMedianFuzzyColor USING DISTANCE MINKOWSKI(1)(\'" + query.toFeatureString() + "\', hist) ORDER USING DISTANCE LIMIT " + limit);
		return manageResultSet(rset);
	}

	@Override
	public List<LongDoublePair> getSimilar(FrameContainer qc, String resultCacheName) {
		SubdividedFuzzyColorHistogram query = FuzzyColorHistogramCalculator.getSubdividedHistogramNormalized(qc.getMedianImg().getBufferedImage(), 2);
		int limit = Config.getRetrieverConfig().getMaxResultsPerModule();
		
		ResultSet rset = this.selector.select(getResultCacheLimitSQL(resultCacheName) + " SELECT * FROM features.SubDivMedianFuzzyColor, c WHERE shotid = c.filter USING DISTANCE MINKOWSKI(1)(\'" + query.toFeatureString() + "\', hist) ORDER USING DISTANCE LIMIT " + limit);
		return manageResultSet(rset);
	}

	@Override
	public void processShot(FrameContainer shot) {
		LOGGER.entry();
		if (!phandler.check("SELECT * FROM features.SubDivMedianFuzzyColor WHERE shotid = " + shot.getId())) {
			SubdividedFuzzyColorHistogram fch = FuzzyColorHistogramCalculator.getSubdividedHistogramNormalized(shot.getMedianImg().getBufferedImage(), 2);
			addToDB(shot.getId(), fch);
		}
		LOGGER.exit();
	}

}
