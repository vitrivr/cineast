package ch.unibas.cs.dbis.cineast.core.features;

import java.util.List;

import ch.unibas.cs.dbis.cineast.core.config.QueryConfig;
import ch.unibas.cs.dbis.cineast.core.data.FloatVector;
import ch.unibas.cs.dbis.cineast.core.data.SegmentContainer;
import ch.unibas.cs.dbis.cineast.core.data.StringDoublePair;
import ch.unibas.cs.dbis.cineast.core.features.abstracts.AbstractFeatureModule;
import ch.unibas.cs.dbis.cineast.core.util.ColorLayoutDescriptor;

public class CLD extends AbstractFeatureModule {

	public CLD(){
		super("features.CLD", "cld", 1960f / 4f);
	}
	
//	@Override
//	public List<LongDoublePair> getSimilar(SegmentContainer qc) {
//		FloatVector query = ColorLayoutDescriptor.calculateCLD(qc.getMostRepresentativeFrame().getImage());
//		int limit = Config.getRetrieverConfig().getMaxResultsPerModule();
//		
//		ResultSet rset = this.selector.select("SELECT * FROM features.CLD USING DISTANCE MINKOWSKI(1)(\'" + query.toFeatureString() + "\', cld) ORDER USING DISTANCE LIMIT " + limit);
//		return manageResultSet(rset);
//	}
//
//	
//	@Override
//	public List<LongDoublePair> getSimilar(SegmentContainer qc, String resultCacheName) {
//		FloatVector query = ColorLayoutDescriptor.calculateCLD(qc.getMostRepresentativeFrame().getImage());
//		int limit = Config.getRetrieverConfig().getMaxResultsPerModule();
//		
//		ResultSet rset = this.selector.select(getResultCacheLimitSQL(resultCacheName) + " SELECT * FROM features.CLD, c WHERE shotid = c.filter USING DISTANCE MINKOWSKI(1)(\'" + query.toFeatureString() + "\', cld) ORDER USING DISTANCE LIMIT " + limit);
//		return manageResultSet(rset);
//	}

	@Override
	public void processShot(SegmentContainer shot) {
		if(!phandler.idExists(shot.getId())){
			FloatVector fv = ColorLayoutDescriptor.calculateCLD(shot.getMostRepresentativeFrame().getImage());
			persist(shot.getId(), fv);
		}
	}

	@Override
	public List<StringDoublePair> getSimilar(SegmentContainer sc, QueryConfig qc) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<StringDoublePair> getSimilar(long shotId, QueryConfig qc) {
		// TODO Auto-generated method stub
		return null;
	}

}
