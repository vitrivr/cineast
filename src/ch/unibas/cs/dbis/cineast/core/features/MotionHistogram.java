package ch.unibas.cs.dbis.cineast.core.features;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ch.unibas.cs.dbis.cineast.core.config.Config;
import ch.unibas.cs.dbis.cineast.core.data.FeatureString;
import ch.unibas.cs.dbis.cineast.core.data.FloatVectorImpl;
import ch.unibas.cs.dbis.cineast.core.data.FrameContainer;
import ch.unibas.cs.dbis.cineast.core.data.LongDoublePair;
import ch.unibas.cs.dbis.cineast.core.data.Pair;
import ch.unibas.cs.dbis.cineast.core.db.PersistencyWriter;
import ch.unibas.cs.dbis.cineast.core.db.PersistentTuple;
import ch.unibas.cs.dbis.cineast.core.features.abstracts.SubDivMotionHistogram;
import ch.unibas.cs.dbis.cineast.core.util.MathHelper;
import georegression.struct.point.Point2D_F32;

public class MotionHistogram extends SubDivMotionHistogram {

	public MotionHistogram() {
		super("features.motionhistogram", "hist", MathHelper.SQRT2);
	}
	
	@Override
	public void init(PersistencyWriter<?> phandler) {
		this.phandler = phandler;
		this.phandler.open("features.MotionHistogram");
	}

	@Override
	public void processShot(FrameContainer shot) {
		if(!phandler.check("SELECT * FROM features.MotionHistogram WHERE shotid = " + shot.getId())){
			List<LinkedList<Point2D_F32>> paths = shot.getPaths();
			
			Pair<List<Double>, ArrayList<ArrayList<Float>>> pair = getSubDivHist(1, paths);
			
			double sum = pair.first.get(0);
			FloatVectorImpl fv = new FloatVectorImpl(pair.second.get(0));

			addToDB(shot.getId(), sum, fv);
		}
	}

	protected void addToDB(long shotId, double sum, FeatureString fs) {
		PersistentTuple tuple = this.phandler.makeTuple(shotId, sum, fs);
		this.phandler.write(tuple);
	}
	
	@Override
	public List<LongDoublePair> getSimilar(FrameContainer qc) {		
		int limit = Config.resultsPerModule();
		
		Pair<List<Double>, ArrayList<ArrayList<Float>>> pair = getSubDivHist(1, qc.getPaths());

		FloatVectorImpl fv = new FloatVectorImpl(pair.second.get(0));
		
		ResultSet rset = this.selector.select("SELECT * FROM features.MotionHistogram USING DISTANCE MINKOWSKI(2)(\'" + fv.toFeatureString() + "\', ) ORDER USING DISTANCE LIMIT " + limit);
		return manageResultSet(rset);
	}

	@Override
	public List<LongDoublePair> getSimilar(FrameContainer qc, String resultCacheName) {
		int limit = Config.resultsPerModule();
		
		Pair<List<Double>, ArrayList<ArrayList<Float>>> pair = getSubDivHist(1, qc.getPaths());

		FloatVectorImpl fv = new FloatVectorImpl(pair.second.get(0));
		
		ResultSet rset = this.selector.select(getResultCacheLimitSQL(resultCacheName) + " SELECT * FROM features.MotionHistogram, c WHERE shotid = c.filter USING DISTANCE MINKOWSKI(2)(\'" + fv.toFeatureString() + "\', ) ORDER USING DISTANCE LIMIT " + limit);
		return manageResultSet(rset);
	}

	

}
