package ch.unibas.cs.dbis.cineast.core.features;

import gnu.trove.map.hash.TLongDoubleHashMap;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import boofcv.struct.feature.SurfFeature;
import ch.unibas.cs.dbis.cineast.core.config.Config;
import ch.unibas.cs.dbis.cineast.core.data.FloatVector;
import ch.unibas.cs.dbis.cineast.core.data.FloatVectorImpl;
import ch.unibas.cs.dbis.cineast.core.data.FrameContainer;
import ch.unibas.cs.dbis.cineast.core.data.LongDoublePair;
import ch.unibas.cs.dbis.cineast.core.descriptor.SURFFeatures;
import ch.unibas.cs.dbis.cineast.core.features.abstracts.AbstractFeatureModule;
import ch.unibas.cs.dbis.cineast.core.util.LogHelper;

@Deprecated
public class SURF extends AbstractFeatureModule {

	private static final Logger LOGGER = LogManager.getLogger();
	
	
	public SURF(){
		super("features.SURF", "", 2);
	}
	

	@Override
	public void processShot(FrameContainer shot) {
		LOGGER.entry();
		if (!phandler.check("SELECT * FROM features.SURF WHERE shotid = " + shot.getId() + " LIMIT 1")) {
			List<SurfFeature> surfs = SURFFeatures.getSURF(shot.getMostRepresentativeFrame().getImage());
			for(SurfFeature sf : surfs){
				FloatVector fv = new FloatVectorImpl(sf.value);
				addToDB(shot.getId(), fv);
			}
		}
		LOGGER.exit();

	}

	protected List<LongDoublePair> getSimilar(FloatVector fv){
		return getSimilar(fv.toFeatureString());
	}
	
	protected List<LongDoublePair> getSimilar(String string){
		int limit = Config.resultsPerModule();
		ResultSet rset = this.selector.select("SELECT * FROM features.SURF USING DISTANCE MINKOWSKI(1)(\'" + string + "\', surf) ORDER USING DISTANCE LIMIT " + limit);
		return manageResultSet(rset);
	}
	
	@Override
	public List<LongDoublePair> getSimilar(FrameContainer qc) {
		List<SurfFeature> surfs = SURFFeatures.getSURF(qc.getMostRepresentativeFrame().getImage(), 5);
		TLongDoubleHashMap map = new TLongDoubleHashMap();
		for(SurfFeature sf : surfs){
			FloatVector fv = new FloatVectorImpl(sf.value);
			List<LongDoublePair> list = getSimilar(fv);
			for(LongDoublePair pair : list){
				if(map.contains(pair.key)){
					map.put(pair.key, pair.value + map.get(pair.key));
				}else{
					map.put(pair.key, pair.value);
				}
			}
		}
		return mapToList(map, surfs.size());
	}

	@Override
	public List<LongDoublePair> getSimilar(long shotId) {
		TLongDoubleHashMap map = new TLongDoubleHashMap();
		int limit = Config.resultsPerModule();
		ResultSet rset1 = this.selector.select("SELECT surf FROM features.surf WHERE shotid = " + shotId + " LIMIT " + (limit / 2));
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
	
	protected static List<LongDoublePair> mapToList(TLongDoubleHashMap map, double div){
		List<LongDoublePair> _return = new ArrayList<>(Math.min(1, map.size()));
		if(div <= 0){
			return _return;
		}
		long[] keys = map.keys();
		for (long key : keys) {
			double val = map.get(key);
			if (val > 0) {
				_return.add(new LongDoublePair(key, val));
			}
		}

		Collections.sort(_return, new Comparator<LongDoublePair>() {

			@Override
			public int compare(LongDoublePair o1, LongDoublePair o2) {
				return Double.compare(o2.value, o1.value);
			}

		});

		int limit = Config.resultsPerModule();

		if (_return.size() > limit) {
			_return = _return.subList(0, limit - 1);
		}
		
		for (LongDoublePair p : _return) {
			p.value /= div;
		}
		
		return _return;
	}


	@Override
	public List<LongDoublePair> getSimilar(FrameContainer qc, String resultCacheName) {
		return new ArrayList<>(1);
	}

}
