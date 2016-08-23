package org.vitrivr.cineast.core.features;

import java.util.List;

import org.vitrivr.cineast.core.config.QueryConfig;
import org.vitrivr.cineast.core.data.SegmentContainer;
import org.vitrivr.cineast.core.data.StringDoublePair;
import org.vitrivr.cineast.core.features.abstracts.AbstractFeatureModule;
import org.vitrivr.cineast.core.segmenter.FuzzyColorHistogram;
import org.vitrivr.cineast.core.segmenter.FuzzyColorHistogramCalculator;
import org.vitrivr.cineast.core.util.ImageHistogramEqualizer;

public class MedianFuzzyHistNormalized extends AbstractFeatureModule {

	public MedianFuzzyHistNormalized(){
		super("features_MedianFuzzyHistNormalized", 2f / 4f);
	}
	
	@Override
	public void processShot(SegmentContainer shot) {
		if (!phandler.idExists(shot.getId())) {
			FuzzyColorHistogram fch = FuzzyColorHistogramCalculator.getHistogramNormalized(ImageHistogramEqualizer.getEqualized(shot.getMedianImg()).getBufferedImage());
			persist(shot.getId(), fch);
		}
	}

	@Override
	public List<StringDoublePair> getSimilar(SegmentContainer sc, QueryConfig qc) {
		FuzzyColorHistogram query = FuzzyColorHistogramCalculator.getHistogramNormalized(ImageHistogramEqualizer.getEqualized(sc.getMedianImg()).getBufferedImage());
		return getSimilar(query.toArray(null), qc);
	}

}
