package ch.unibas.cs.dbis.cineast.core.features;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import ch.unibas.cs.dbis.cineast.core.config.Config;
import ch.unibas.cs.dbis.cineast.core.data.FloatVector;
import ch.unibas.cs.dbis.cineast.core.data.FloatVectorImpl;
import ch.unibas.cs.dbis.cineast.core.data.FrameContainer;
import ch.unibas.cs.dbis.cineast.core.data.LongDoublePair;
import ch.unibas.cs.dbis.cineast.core.data.Pair;
import ch.unibas.cs.dbis.cineast.core.features.abstracts.SubDivMotionHistogram;
import ch.unibas.cs.dbis.cineast.core.util.MathHelper;

public class SubDivMotionHistogramBackground3 extends SubDivMotionHistogram {

	public SubDivMotionHistogramBackground3() {
		super("features.SubDivMotionHistogramBackground3", "hists", MathHelper.SQRT2 * 9);
	}

	@Override
	public void processShot(FrameContainer shot) {
		if(!phandler.check("SELECT * FROM features.SubDivMotionHistogramBackground3 WHERE shotid = " + shot.getId())){
		
			Pair<List<Double>, ArrayList<ArrayList<Float>>> pair = getSubDivHist(3, shot.getBgPaths());
			
			FloatVector sum = new FloatVectorImpl(pair.first);
			ArrayList<Float> tmp = new ArrayList<Float>(3 * 3 * 8);
			for(List<Float> l : pair.second){
				for(float f : l){
					tmp.add(f);
				}
			}
			FloatVectorImpl fv = new FloatVectorImpl(tmp);

			addToDB(shot.getId(), sum, fv);
		}
	}

	@Override
	public List<LongDoublePair> getSimilar(FrameContainer qc) {
		int limit = Config.getRetrieverConfig().getMaxResultsPerModule();
		
		Pair<List<Double>, ArrayList<ArrayList<Float>>> pair = getSubDivHist(3, qc.getBgPaths());

		ArrayList<Float> tmp = new ArrayList<Float>(3 * 3 * 8);
		for(List<Float> l : pair.second){
			for(float f : l){
				tmp.add(f);
			}
		}
		FloatVectorImpl fv = new FloatVectorImpl(tmp);
		
		ResultSet rset = this.selector.select("SELECT * FROM features.SubDivMotionHistogramBackground3 WHERE features.SubDivMotionHistogramBackground3.sums != \'<0,0,0,0,0,0,0,0,0>\' USING DISTANCE MINKOWSKI(2)(\'" + fv.toFeatureString() + "\', hists) ORDER USING DISTANCE LIMIT " + limit);
		return manageResultSet(rset);
	}

	@Override
	public List<LongDoublePair> getSimilar(FrameContainer qc, String resultCacheName) {
		int limit = Config.getRetrieverConfig().getMaxResultsPerModule();
		
		Pair<List<Double>, ArrayList<ArrayList<Float>>> pair = getSubDivHist(3, qc.getBgPaths());

		ArrayList<Float> tmp = new ArrayList<Float>(3 * 3 * 8);
		for(List<Float> l : pair.second){
			for(float f : l){
				tmp.add(f);
			}
		}
		FloatVectorImpl fv = new FloatVectorImpl(tmp);
		
		ResultSet rset = this.selector.select(getResultCacheLimitSQL(resultCacheName) + " SELECT * FROM features.SubDivMotionHistogramBackground3, c WHERE shotid = c.filter USING DISTANCE MINKOWSKI(2)(\'" + fv.toFeatureString() + "\', hists) ORDER USING DISTANCE LIMIT " + limit);
		return manageResultSet(rset);
	}
}
