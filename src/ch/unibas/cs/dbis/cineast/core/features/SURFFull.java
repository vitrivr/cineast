package ch.unibas.cs.dbis.cineast.core.features;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import boofcv.struct.feature.BrightFeature;
import ch.unibas.cs.dbis.cineast.core.config.Config;
import ch.unibas.cs.dbis.cineast.core.data.FloatVector;
import ch.unibas.cs.dbis.cineast.core.data.FloatVectorImpl;
import ch.unibas.cs.dbis.cineast.core.data.Frame;
import ch.unibas.cs.dbis.cineast.core.data.FrameContainer;
import ch.unibas.cs.dbis.cineast.core.data.LongDoublePair;
import ch.unibas.cs.dbis.cineast.core.db.PersistencyWriter;
import ch.unibas.cs.dbis.cineast.core.descriptor.SURFFeatures;
import ch.unibas.cs.dbis.cineast.core.util.KMeansPP;
import ch.unibas.cs.dbis.cineast.core.util.KMeansPP.KMenasResult;
import ch.unibas.cs.dbis.cineast.core.util.LogHelper;
import ch.unibas.cs.dbis.cineast.core.util.MathHelper;
import gnu.trove.map.hash.TLongDoubleHashMap;

public class SURFFull extends SURF {

	private static final Logger LOGGER = LogManager.getLogger();

	
	@Override
	public void init(PersistencyWriter<?> phandler) {
		this.phandler = phandler;
		this.phandler.open("features.SURFFull");
	}
	
	@Override
	public void processShot(FrameContainer shot) {
		LOGGER.entry();
		if (!phandler.check("SELECT * FROM features.SURFFull WHERE shotid = " + shot.getId() + " LIMIT 1")) {
			List<FloatVector> allsurfs = new LinkedList<FloatVector>();
			for(Frame f : shot.getFrames()){
				List<BrightFeature> surfs = SURFFeatures.getSURF(f.getImage());
				for(BrightFeature sf : surfs){
					allsurfs.add(new FloatVectorImpl(sf.value));
				}
			}
			if(allsurfs.size() > 40){
				ArrayList<FloatVector> tmp = new ArrayList<FloatVector>(allsurfs.size());
				tmp.addAll(allsurfs);
				
				KMenasResult<FloatVector> result = KMeansPP.kMeansPP(tmp, new FloatVectorImpl(new float[64]), 40, 0.0001);
				ArrayList<FloatVector> k = result.getCenters();
				allsurfs.clear();
				allsurfs.addAll(k);
			}
			
			for(FloatVector sf : allsurfs){
				addToDB(shot.getId(), sf);
			}
		}
		LOGGER.exit();
	}
	protected List<LongDoublePair> getSimilar(String string){
		int limit = Config.resultsPerModule();
		ResultSet rset = this.selector.select("SELECT * FROM features.SURFFull USING DISTANCE MINKOWSKI(2)(\'" + string + "\', surf) ORDER USING DISTANCE LIMIT " + limit);
		ArrayList<LongDoublePair> result = new ArrayList<>(limit);
		if(rset != null){
			try {
				while(rset.next()){
					double dist = rset.getDouble(1);
					long shotId = rset.getLong(2);

					result.add(new LongDoublePair(shotId, MathHelper.getScore(dist, this.maxDist)));
				}
			} catch (SQLException e) {
				LOGGER.fatal(LogHelper.SQL_MARKER, LogHelper.getStackTrace(e));
			}
		}
		
		return result;
	}
	
	@Override
	public List<LongDoublePair> getSimilar(long shotId) {
		TLongDoubleHashMap map = new TLongDoubleHashMap();
		int limit = Config.resultsPerModule();
		ResultSet rset1 = this.selector.select("SELECT surf FROM features.SURFFull WHERE shotid = " + shotId + " LIMIT " + (limit / 2));
		int counter = 0;
		if(rset1 != null){
			try {
				while(rset1.next()){
					String featureString = rset1.getString(1);
					List<LongDoublePair> list = getSimilar(featureString);
					++counter;
					for(LongDoublePair pair : list){
						if(map.contains(pair.key)){
							map.put(pair.key, pair.value + map.get(pair.key));
						}else{
							map.put(pair.key, pair.value);
						}
					}
				}
			} catch (SQLException e) {
				LOGGER.fatal(LogHelper.SQL_MARKER, LogHelper.getStackTrace(e));
			}
		}
		return mapToList(map, counter);
	}
	
}