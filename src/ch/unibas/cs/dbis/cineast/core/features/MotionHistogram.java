package ch.unibas.cs.dbis.cineast.core.features;

import java.util.ArrayList;
import java.util.List;

import ch.unibas.cs.dbis.cineast.core.config.QueryConfig;
import ch.unibas.cs.dbis.cineast.core.data.FloatVectorImpl;
import ch.unibas.cs.dbis.cineast.core.data.Pair;
import ch.unibas.cs.dbis.cineast.core.data.ReadableFloatVector;
import ch.unibas.cs.dbis.cineast.core.data.SegmentContainer;
import ch.unibas.cs.dbis.cineast.core.data.StringDoublePair;
import ch.unibas.cs.dbis.cineast.core.db.PersistentTuple;
import ch.unibas.cs.dbis.cineast.core.features.abstracts.SubDivMotionHistogram;
import ch.unibas.cs.dbis.cineast.core.util.MathHelper;

public class MotionHistogram extends SubDivMotionHistogram {

	public MotionHistogram() {
		super("features_motionhistogram", MathHelper.SQRT2);
	}
	

	@Override
	public void processShot(SegmentContainer shot) {
		if(!phandler.idExists(shot.getId())){
			
			Pair<List<Double>, ArrayList<ArrayList<Float>>> pair = getSubDivHist(1, shot.getPaths());
			
			double sum = pair.first.get(0);
			FloatVectorImpl fv = new FloatVectorImpl(pair.second.get(0));

			persist(shot.getId(), sum, fv);
		}
	}

	protected void persist(String shotId, double sum, ReadableFloatVector fs) {
		PersistentTuple tuple = this.phandler.generateTuple(shotId, sum, fs);
		this.phandler.persist(tuple);
	}

	@Override
	public List<StringDoublePair> getSimilar(SegmentContainer sc, QueryConfig qc) {
		Pair<List<Double>, ArrayList<ArrayList<Float>>> pair = getSubDivHist(1, sc.getPaths());
		
		FloatVectorImpl fv = new FloatVectorImpl(pair.second.get(0));
		return getSimilar(fv.toArray(null), qc);
	}

	@Override
	public List<StringDoublePair> getSimilar(String shotId, QueryConfig qc) {
		// TODO Auto-generated method stub
		return null;
	}
	
}
