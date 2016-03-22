package ch.unibas.cs.dbis.cineast.core.features;

import java.sql.ResultSet;
import java.util.List;

import ch.unibas.cs.dbis.cineast.core.config.Config;
import ch.unibas.cs.dbis.cineast.core.data.FrameContainer;
import ch.unibas.cs.dbis.cineast.core.data.LongDoublePair;
import ch.unibas.cs.dbis.cineast.core.features.abstracts.AbstractFeatureModule;
import ch.unibas.cs.dbis.cineast.core.segmenter.FuzzyColorHistogram;
import ch.unibas.cs.dbis.cineast.core.segmenter.FuzzyColorHistogramCalculator;
import ch.unibas.cs.dbis.cineast.core.util.ImageHistogramEqualizer;

public class MedianFuzzyHistNormalized extends AbstractFeatureModule {

	public MedianFuzzyHistNormalized(){
		super("features.MedianFuzzyHistNormalized", "hist", 2f / 4f);
	}
	
	@Override
	public void processShot(FrameContainer shot) {
		if (!phandler.check("SELECT * FROM features.MedianFuzzyHistNormalized WHERE shotid = " + shot.getId())) {
			FuzzyColorHistogram fch = FuzzyColorHistogramCalculator.getHistogramNormalized(ImageHistogramEqualizer.getEqualized(shot.getMedianImg()).getBufferedImage());
			addToDB(shot.getId(), fch);
		}
	}

	@Override
	public List<LongDoublePair> getSimilar(FrameContainer qc) {
		FuzzyColorHistogram query = FuzzyColorHistogramCalculator.getHistogramNormalized(ImageHistogramEqualizer.getEqualized(qc.getMedianImg()).getBufferedImage());
		int limit = Config.getRetrieverConfig().getMaxResultsPerModule();
		
		ResultSet rset = this.selector.select("SELECT * FROM features.MedianFuzzyHistNormalized USING DISTANCE MINKOWSKI(1)(\'" + query.toFeatureString() + "\', hist) ORDER USING DISTANCE LIMIT " + limit);
		return manageResultSet(rset);
	}

	@Override
	public List<LongDoublePair> getSimilar(FrameContainer qc, String resultCacheName) {
		FuzzyColorHistogram query = FuzzyColorHistogramCalculator.getHistogramNormalized(ImageHistogramEqualizer.getEqualized(qc.getMedianImg()).getBufferedImage());
		int limit = Config.getRetrieverConfig().getMaxResultsPerModule();
		
		ResultSet rset = this.selector.select(getResultCacheLimitSQL(resultCacheName) + " SELECT * FROM features.MedianFuzzyHistNormalized, c WHERE shotid = c.filter USING DISTANCE MINKOWSKI(1)(\'" + query.toFeatureString() + "\', hist) ORDER USING DISTANCE LIMIT " + limit);
		return manageResultSet(rset);
	}

}
