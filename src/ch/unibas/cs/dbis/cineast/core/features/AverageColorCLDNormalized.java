package ch.unibas.cs.dbis.cineast.core.features;

import java.sql.ResultSet;
import java.util.List;

import ch.unibas.cs.dbis.cineast.core.config.Config;
import ch.unibas.cs.dbis.cineast.core.data.FloatVector;
import ch.unibas.cs.dbis.cineast.core.data.FrameContainer;
import ch.unibas.cs.dbis.cineast.core.data.LongDoublePair;
import ch.unibas.cs.dbis.cineast.core.features.abstracts.AbstractFeatureModule;
import ch.unibas.cs.dbis.cineast.core.util.ColorLayoutDescriptor;
import ch.unibas.cs.dbis.cineast.core.util.ImageHistogramEqualizer;

public class AverageColorCLDNormalized extends AbstractFeatureModule {

	public AverageColorCLDNormalized(){
		super("features.AverageColorCLDNormalized", "cld", 1960f / 4f);
	}
	
	@Override
	public void processShot(FrameContainer shot) {
		if(!phandler.check("SELECT * FROM features.AverageColorCLDNormalized WHERE shotid = " + shot.getId())){
			FloatVector fv = ColorLayoutDescriptor.calculateCLD(ImageHistogramEqualizer.getEqualized(shot.getAvgImg()));
			addToDB(shot.getId(), fv);
		}
	}

	@Override
	public List<LongDoublePair> getSimilar(FrameContainer qc) {
		FloatVector query = ColorLayoutDescriptor.calculateCLD(ImageHistogramEqualizer.getEqualized(qc.getAvgImg()));
		int limit = Config.resultsPerModule();
		
		ResultSet rset = this.selector.select("SELECT * FROM features.AverageColorCLDNormalized USING DISTANCE MINKOWSKI(1)(\'" + query.toFeatureString() + "\', cld) ORDER USING DISTANCE LIMIT " + limit);
		return manageResultSet(rset);
	}

	@Override
	public List<LongDoublePair> getSimilar(FrameContainer qc, String resultCacheName) {
		FloatVector query = ColorLayoutDescriptor.calculateCLD(ImageHistogramEqualizer.getEqualized(qc.getAvgImg()));
		int limit = Config.resultsPerModule();
		
		ResultSet rset = this.selector.select(getResultCacheLimitSQL(resultCacheName) + " SELECT * FROM features.AverageColorCLDNormalized, c WHERE shotid = c.filter USING DISTANCE MINKOWSKI(1)(\'" + query.toFeatureString() + "\', cld) ORDER USING DISTANCE LIMIT " + limit);
		return manageResultSet(rset);
	}

}
