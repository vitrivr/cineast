package ch.unibas.cs.dbis.cineast.core.features;

import java.sql.ResultSet;
import java.util.List;

import ch.unibas.cs.dbis.cineast.core.config.Config;
import ch.unibas.cs.dbis.cineast.core.data.FloatVector;
import ch.unibas.cs.dbis.cineast.core.data.FrameContainer;
import ch.unibas.cs.dbis.cineast.core.data.LongDoublePair;
import ch.unibas.cs.dbis.cineast.core.features.abstracts.AbstractFeatureModule;
import ch.unibas.cs.dbis.cineast.core.util.ColorLayoutDescriptor;

public class CLD extends AbstractFeatureModule {

	public CLD(){
		super("features.CLD", "cld", 1960f / 4f);
	}
	
	@Override
	public List<LongDoublePair> getSimilar(FrameContainer qc) {
		FloatVector query = ColorLayoutDescriptor.calculateCLD(qc.getMostRepresentativeFrame().getImage());
		int limit = Config.getRetrieverConfig().getMaxResultsPerModule();
		
		ResultSet rset = this.selector.select("SELECT * FROM features.CLD USING DISTANCE MINKOWSKI(1)(\'" + query.toFeatureString() + "\', cld) ORDER USING DISTANCE LIMIT " + limit);
		return manageResultSet(rset);
	}

	
	@Override
	public List<LongDoublePair> getSimilar(FrameContainer qc, String resultCacheName) {
		FloatVector query = ColorLayoutDescriptor.calculateCLD(qc.getMostRepresentativeFrame().getImage());
		int limit = Config.getRetrieverConfig().getMaxResultsPerModule();
		
		ResultSet rset = this.selector.select(getResultCacheLimitSQL(resultCacheName) + " SELECT * FROM features.CLD, c WHERE shotid = c.filter USING DISTANCE MINKOWSKI(1)(\'" + query.toFeatureString() + "\', cld) ORDER USING DISTANCE LIMIT " + limit);
		return manageResultSet(rset);
	}

	@Override
	public void processShot(FrameContainer shot) {
		if(!phandler.check("SELECT * FROM features.CLD WHERE shotid = " + shot.getId())){
			FloatVector fv = ColorLayoutDescriptor.calculateCLD(shot.getMostRepresentativeFrame().getImage());
			addToDB(shot.getId(), fv);
		}
	}

}
