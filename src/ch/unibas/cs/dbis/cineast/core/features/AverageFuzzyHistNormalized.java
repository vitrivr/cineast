package ch.unibas.cs.dbis.cineast.core.features;

import java.util.List;

import ch.unibas.cs.dbis.cineast.core.config.QueryConfig;
import ch.unibas.cs.dbis.cineast.core.data.SegmentContainer;
import ch.unibas.cs.dbis.cineast.core.data.StringDoublePair;
import ch.unibas.cs.dbis.cineast.core.features.abstracts.AbstractFeatureModule;
import ch.unibas.cs.dbis.cineast.core.segmenter.FuzzyColorHistogram;
import ch.unibas.cs.dbis.cineast.core.segmenter.FuzzyColorHistogramCalculator;
import ch.unibas.cs.dbis.cineast.core.util.ImageHistogramEqualizer;

public class AverageFuzzyHistNormalized extends AbstractFeatureModule {

	public AverageFuzzyHistNormalized(){
		super("features_AverageFuzzyHistNormalized", 2f / 4f);
	}
	
	@Override
	public void processShot(SegmentContainer shot) {
		if (!phandler.idExists(shot.getId())) {
			FuzzyColorHistogram fch = FuzzyColorHistogramCalculator.getHistogramNormalized(ImageHistogramEqualizer.getEqualized(shot.getAvgImg()).getBufferedImage());
			persist(shot.getId(), fch);
		}
	}

	@Override
	public List<StringDoublePair> getSimilar(SegmentContainer sc, QueryConfig qc) {
		FuzzyColorHistogram query = FuzzyColorHistogramCalculator.getHistogramNormalized((ImageHistogramEqualizer.getEqualized(sc.getAvgImg())).getBufferedImage());
		return getSimilar(query.toArray(null), qc);
	}


}
