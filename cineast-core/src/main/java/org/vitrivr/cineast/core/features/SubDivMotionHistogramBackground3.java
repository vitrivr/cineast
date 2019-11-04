package org.vitrivr.cineast.core.features;

import org.vitrivr.cineast.core.config.ReadableQueryConfig;
import org.vitrivr.cineast.core.data.FloatVector;
import org.vitrivr.cineast.core.data.FloatVectorImpl;
import org.vitrivr.cineast.core.data.Pair;
import org.vitrivr.cineast.core.data.ReadableFloatVector;
import org.vitrivr.cineast.core.data.score.ScoreElement;
import org.vitrivr.cineast.core.data.segments.SegmentContainer;
import org.vitrivr.cineast.core.features.abstracts.SubDivMotionHistogram;
import org.vitrivr.cineast.core.util.MathHelper;

import java.util.ArrayList;
import java.util.List;

public class SubDivMotionHistogramBackground3 extends SubDivMotionHistogram {

	public SubDivMotionHistogramBackground3() {
		super("features_SubDivMotionHistogramBackground3", "hists", MathHelper.SQRT2 * 9, 3);
	}

	@Override
	public void processSegment(SegmentContainer shot) {
		if(!phandler.idExists(shot.getId())){
		
			Pair<List<Double>, ArrayList<ArrayList<Float>>> pair = getSubDivHist(3, shot.getBgPaths());
			
			FloatVector sum = new FloatVectorImpl(pair.first);
			ArrayList<Float> tmp = new ArrayList<Float>(3 * 3 * 8);
			for(List<Float> l : pair.second){
				for(float f : l){
					tmp.add(f);
				}
			}
			FloatVectorImpl fv = new FloatVectorImpl(tmp);

			persist(shot.getId(), sum, fv);
		}
	}

	@Override
	public List<ScoreElement> getSimilar(SegmentContainer sc, ReadableQueryConfig qc) {
		Pair<List<Double>, ArrayList<ArrayList<Float>>> pair = getSubDivHist(3, sc.getBgPaths());

		ArrayList<Float> tmp = new ArrayList<Float>(3 * 3 * 8);
		for (List<Float> l : pair.second) {
			for (float f : l) {
				tmp.add(f);
			}
		}
		FloatVectorImpl fv = new FloatVectorImpl(tmp);
    return getSimilar(ReadableFloatVector.toArray(fv), qc);
	}
}
