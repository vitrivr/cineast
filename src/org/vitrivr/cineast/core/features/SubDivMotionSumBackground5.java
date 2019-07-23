package org.vitrivr.cineast.core.features;

import java.util.ArrayList;
import java.util.List;

import org.vitrivr.cineast.core.config.ReadableQueryConfig;
import org.vitrivr.cineast.core.data.FloatVectorImpl;
import org.vitrivr.cineast.core.data.Pair;
import org.vitrivr.cineast.core.data.ReadableFloatVector;
import org.vitrivr.cineast.core.data.score.ScoreElement;
import org.vitrivr.cineast.core.data.segments.SegmentContainer;
import org.vitrivr.cineast.core.features.abstracts.MotionHistogramCalculator;

public class SubDivMotionSumBackground5 extends MotionHistogramCalculator {

	
	public SubDivMotionSumBackground5() {
		super("features.SubDivMotionHistogramBackground5", "sums", 2500, 5);
	}

	@Override
	public List<ScoreElement> getSimilar(SegmentContainer sc, ReadableQueryConfig qc) {
		Pair<List<Double>, ArrayList<ArrayList<Float>>> pair = getSubDivHist(5, sc.getBgPaths());
		FloatVectorImpl fv = new FloatVectorImpl(pair.first);
    return getSimilar(ReadableFloatVector.toArray(fv), qc);
	}

}
