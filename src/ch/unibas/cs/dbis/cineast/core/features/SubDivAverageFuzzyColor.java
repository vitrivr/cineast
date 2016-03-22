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

public class SubDivAverageFuzzyColor extends AbstractFeatureModule {

	private static final Logger LOGGER = LogManager.getLogger();

	public SubDivAverageFuzzyColor(){
		super("features.SubDivAverageFuzzyColor", "hist", 2f / 4f);
	}
	
	@Override
	public List<LongDoublePair> getSimilar(FrameContainer qc) {
		SubdividedFuzzyColorHistogram query = FuzzyColorHistogramCalculator.getSubdividedHistogramNormalized(qc.getAvgImg().getBufferedImage(), 2);
		int limit = Config.getRetrieverConfig().getMaxResultsPerModule();
		
		ResultSet rset = this.selector.select("SELECT * FROM features.SubDivAverageFuzzyColor USING DISTANCE MINKOWSKI(1)(\'" + query.toFeatureString() + "\', hist) ORDER USING DISTANCE LIMIT " + limit);
		return manageResultSet(rset);
	}

	@Override
	public List<LongDoublePair> getSimilar(FrameContainer qc, String resultCacheName) {
		SubdividedFuzzyColorHistogram query = FuzzyColorHistogramCalculator.getSubdividedHistogramNormalized(qc.getAvgImg().getBufferedImage(), 2);
		int limit = Config.getRetrieverConfig().getMaxResultsPerModule();
		
		ResultSet rset = this.selector.select(getResultCacheLimitSQL(resultCacheName) + " SELECT * FROM features.SubDivAverageFuzzyColor, c WHERE shotid = c.filter USING DISTANCE MINKOWSKI(1)(\'" + query.toFeatureString() + "\', hist) ORDER USING DISTANCE LIMIT " + limit);
		return manageResultSet(rset);
	}

	@Override
	public void processShot(FrameContainer shot) {
		LOGGER.entry();
		if (!phandler.check("SELECT * FROM features.SubDivAverageFuzzyColor WHERE shotid = " + shot.getId())) {
			SubdividedFuzzyColorHistogram fch = FuzzyColorHistogramCalculator.getSubdividedHistogramNormalized(shot.getAvgImg().getBufferedImage(), 2);
			addToDB(shot.getId(), fch);
		}
		LOGGER.exit();
	}

}
