package org.vitrivr.cineast.core.features;

import java.util.List;

import org.vitrivr.cineast.core.config.QueryConfig;
import org.vitrivr.cineast.core.config.ReadableQueryConfig;
import org.vitrivr.cineast.core.config.ReadableQueryConfig.Distance;
import org.vitrivr.cineast.core.data.StringDoublePair;
import org.vitrivr.cineast.core.data.segments.SegmentContainer;
import org.vitrivr.cineast.core.features.abstracts.AbstractFeatureModule;
import org.vitrivr.cineast.core.segmenter.FuzzyColorHistogram;
import org.vitrivr.cineast.core.segmenter.FuzzyColorHistogramCalculator;
import org.vitrivr.cineast.core.util.ImageHistogramEqualizer;

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
	public List<StringDoublePair> getSimilar(SegmentContainer sc, ReadableQueryConfig qc) {
		FuzzyColorHistogram query = FuzzyColorHistogramCalculator.getHistogramNormalized((ImageHistogramEqualizer.getEqualized(sc.getAvgImg())).getBufferedImage());
		return getSimilar(query.toArray(null), qc);
	}

  @Override
  protected ReadableQueryConfig setQueryConfig(ReadableQueryConfig qc) {
    return QueryConfig.clone(qc).setDistanceIfEmpty(Distance.chisquared);
  }

}
