package ch.unibas.cs.dbis.cineast.core.features;

import java.sql.ResultSet;
import java.util.List;

import ch.unibas.cs.dbis.cineast.core.config.Config;
import ch.unibas.cs.dbis.cineast.core.data.FloatVector;
import ch.unibas.cs.dbis.cineast.core.data.FrameContainer;
import ch.unibas.cs.dbis.cineast.core.data.LongDoublePair;
import ch.unibas.cs.dbis.cineast.core.features.abstracts.AbstractFeatureModule;
import ch.unibas.cs.dbis.cineast.core.util.ColorLayoutDescriptor;

public class AverageColorCLD extends AbstractFeatureModule {

	public AverageColorCLD(){
		super("features.AverageColorCLD", "cld", 1960f / 4f);
	}
	
	@Override
	public List<LongDoublePair> getSimilar(FrameContainer qc) {
		FloatVector query = ColorLayoutDescriptor.calculateCLD(qc.getAvgImg());
		int limit = Config.resultsPerModule();
		
		ResultSet rset = this.selector.select("SELECT * FROM features.AverageColorCLD USING DISTANCE MINKOWSKI(1)(\'" + query.toFeatureString() + "\', cld) ORDER USING DISTANCE LIMIT " + limit);
		return manageResultSet(rset);
	}

	@Override
	public void processShot(FrameContainer shot) {
		if(!phandler.check("SELECT * FROM features.AverageColorCLD WHERE shotid = " + shot.getId())){
			FloatVector fv = ColorLayoutDescriptor.calculateCLD(shot.getAvgImg());
			addToDB(shot.getId(), fv);
		}
	}

	@Override
	public List<LongDoublePair> getSimilar(FrameContainer qc, String resultCacheName) {
		FloatVector query = ColorLayoutDescriptor.calculateCLD(qc.getAvgImg());
		int limit = Config.resultsPerModule();
		
		ResultSet rset = this.selector.select(getResultCacheLimitSQL(resultCacheName) + "SELECT * FROM features.AverageColorCLD, c WHERE shotid = c.filter USING DISTANCE MINKOWSKI(1)(\'" + query.toFeatureString() + "\', cld) ORDER USING DISTANCE LIMIT " + limit);
		return manageResultSet(rset);
	}

}
