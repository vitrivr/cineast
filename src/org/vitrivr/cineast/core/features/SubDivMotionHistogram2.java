package ch.unibas.cs.dbis.cineast.core.features;

import java.util.ArrayList;
import java.util.List;

import ch.unibas.cs.dbis.cineast.core.config.QueryConfig;
import ch.unibas.cs.dbis.cineast.core.data.FloatVector;
import ch.unibas.cs.dbis.cineast.core.data.FloatVectorImpl;
import ch.unibas.cs.dbis.cineast.core.data.Pair;
import ch.unibas.cs.dbis.cineast.core.data.SegmentContainer;
import ch.unibas.cs.dbis.cineast.core.data.StringDoublePair;
import ch.unibas.cs.dbis.cineast.core.features.abstracts.SubDivMotionHistogram;
import ch.unibas.cs.dbis.cineast.core.util.MathHelper;

public class SubDivMotionHistogram2 extends SubDivMotionHistogram {

	public SubDivMotionHistogram2() {
		super("features_SubDivMotionHistogram2", MathHelper.SQRT2 * 4);
	}

	@Override
	public void processShot(SegmentContainer shot) {
		if(!phandler.idExists(shot.getId())){
			
			Pair<List<Double>, ArrayList<ArrayList<Float>>> pair = getSubDivHist(2, shot.getPaths());
			
			FloatVector sum = new FloatVectorImpl(pair.first);
			ArrayList<Float> tmp = new ArrayList<Float>(2 * 2 * 8);
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
	public List<StringDoublePair> getSimilar(SegmentContainer sc, QueryConfig qc) {
		Pair<List<Double>, ArrayList<ArrayList<Float>>> pair = getSubDivHist(2, sc.getPaths());
		ArrayList<Float> tmp = new ArrayList<Float>(2 * 2 * 8);
		for(List<Float> l : pair.second){
			for(float f : l){
				tmp.add(f);
			}
		}
		FloatVectorImpl fv = new FloatVectorImpl(tmp);
		return getSimilar(fv.toArray(null), qc);
	}

	@Override
	public List<StringDoublePair> getSimilar(String shotId, QueryConfig qc) {
		// TODO Auto-generated method stub
		return null;
	}

}
