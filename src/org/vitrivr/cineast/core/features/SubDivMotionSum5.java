package org.vitrivr.cineast.core.features;

import java.util.ArrayList;
import java.util.List;

import org.vitrivr.cineast.core.config.QueryConfig;
import org.vitrivr.cineast.core.data.FloatVectorImpl;
import org.vitrivr.cineast.core.data.Pair;
import org.vitrivr.cineast.core.data.segments.SegmentContainer;
import org.vitrivr.cineast.core.data.StringDoublePair;
import org.vitrivr.cineast.core.features.abstracts.MotionHistogramCalculator;

public class SubDivMotionSum5 extends MotionHistogramCalculator {

	
	public SubDivMotionSum5() {
		super("features_SubDivMotionHistogram5", "sums", 2500);
	}

	@Override
	public List<StringDoublePair> getSimilar(SegmentContainer sc, QueryConfig qc) {		
		Pair<List<Double>, ArrayList<ArrayList<Float>>> pair = getSubDivHist(5, sc.getBgPaths());
		FloatVectorImpl fv = new FloatVectorImpl(pair.first);
		return getSimilar(fv.toArray(null), qc);
	}

}
