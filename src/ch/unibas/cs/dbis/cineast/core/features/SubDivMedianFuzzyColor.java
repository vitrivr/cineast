package ch.unibas.cs.dbis.cineast.core.features;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ch.unibas.cs.dbis.cineast.core.config.QueryConfig;
import ch.unibas.cs.dbis.cineast.core.data.SegmentContainer;
import ch.unibas.cs.dbis.cineast.core.data.StringDoublePair;
import ch.unibas.cs.dbis.cineast.core.features.abstracts.AbstractFeatureModule;
import ch.unibas.cs.dbis.cineast.core.segmenter.FuzzyColorHistogramCalculator;
import ch.unibas.cs.dbis.cineast.core.segmenter.SubdividedFuzzyColorHistogram;

public class SubDivMedianFuzzyColor extends AbstractFeatureModule {

	private static final Logger LOGGER = LogManager.getLogger();

	public SubDivMedianFuzzyColor(){
		super("features_SubDivMedianFuzzyColor", 2f / 4f);
	}
	
	@Override
	public void processShot(SegmentContainer shot) {
		LOGGER.entry();
		if (!phandler.idExists(shot.getId())) {
			SubdividedFuzzyColorHistogram fch = FuzzyColorHistogramCalculator.getSubdividedHistogramNormalized(shot.getMedianImg().getBufferedImage(), 2);
			persist(shot.getId(), fch);
		}
		LOGGER.exit();
	}

	@Override
	public List<StringDoublePair> getSimilar(SegmentContainer sc, QueryConfig qc) {
		SubdividedFuzzyColorHistogram query = FuzzyColorHistogramCalculator.getSubdividedHistogramNormalized(sc.getMedianImg().getBufferedImage(), 2);
		return getSimilar(query.toArray(null), qc);
	}


}
