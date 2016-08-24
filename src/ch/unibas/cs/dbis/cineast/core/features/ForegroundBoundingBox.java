package ch.unibas.cs.dbis.cineast.core.features;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import boofcv.struct.ImageRectangle;
import ch.unibas.cs.dbis.cineast.core.color.ColorConverter;
import ch.unibas.cs.dbis.cineast.core.color.ReadableLabContainer;
import ch.unibas.cs.dbis.cineast.core.config.Config;
import ch.unibas.cs.dbis.cineast.core.data.FeatureString;
import ch.unibas.cs.dbis.cineast.core.data.FloatVectorImpl;
import ch.unibas.cs.dbis.cineast.core.data.FrameContainer;
import ch.unibas.cs.dbis.cineast.core.data.LongDoublePair;
import ch.unibas.cs.dbis.cineast.core.data.MultiImage;
import ch.unibas.cs.dbis.cineast.core.data.Pair;
import ch.unibas.cs.dbis.cineast.core.db.PersistentTuple;
import ch.unibas.cs.dbis.cineast.core.descriptor.PathList;
import ch.unibas.cs.dbis.cineast.core.features.abstracts.AbstractFeatureModule;
import ch.unibas.cs.dbis.cineast.core.util.ColorUtils;
import ch.unibas.cs.dbis.cineast.core.util.MaskGenerator;
import ch.unibas.cs.dbis.cineast.core.util.TimeHelper;

public class ForegroundBoundingBox  extends AbstractFeatureModule {

	public ForegroundBoundingBox() {
		super("features.ForegroundBoundingBox", "rect", 0.5f);
	}

	private static final Logger LOGGER = LogManager.getLogger();
	
	@Override
	public void processShot(FrameContainer shot) {
		TimeHelper.tic();
		LOGGER.entry();
		if (!phandler.check("SELECT * FROM features.ForegroundBoundingBox WHERE shotid = " + shot.getId())) {
			ArrayList<Pair<Long,ArrayList<Float>>> bboxs = MaskGenerator.getNormalizedBbox(shot.getFrames(), shot.getPaths(), shot.getBgPaths());
			long shotId = shot.getId();
			for(Pair<Long,ArrayList<Float>> bbox : bboxs){
				FloatVectorImpl fv = new FloatVectorImpl(bbox.second);
				addToDB(shotId, bbox.first, fv);
			}
			LOGGER.debug("ForegroundBoundingBox.processShot() done in {}",
					TimeHelper.toc());
		}
		LOGGER.exit();
	}
	
	protected void addToDB(long shotId, long frameIdx, FeatureString fs) {
		PersistentTuple tuple = this.phandler.makeTuple(shotId, frameIdx, fs);
		this.phandler.write(tuple);
	}

	@Override
	public List<LongDoublePair> getSimilar(FrameContainer qc) {
		ArrayList<Pair<Long,ArrayList<Float>>> bboxs = MaskGenerator.getNormalizedBbox(qc.getFrames(), qc.getPaths(), qc.getBgPaths());
		FloatVectorImpl fv = new FloatVectorImpl(bboxs.get(0).second);

		int limit = Config.getRetrieverConfig().getMaxResultsPerModule();
		
		ResultSet rset = this.selector.select("SELECT * FROM features.ForegroundBoundingBox USING DISTANCE MINKOWSKI(2)(\'" + fv.toFeatureString() + "\', bbox) ORDER USING DISTANCE LIMIT " + limit);
		return manageResultSet(rset);
	}

	@Override
	public List<LongDoublePair> getSimilar(FrameContainer qc, String resultCacheName) {
		ArrayList<Pair<Long,ArrayList<Float>>> bboxs = MaskGenerator.getNormalizedBbox(qc.getFrames(), qc.getPaths(), qc.getBgPaths());
		FloatVectorImpl fv = new FloatVectorImpl(bboxs.get(0).second);
		
		int limit = Config.getRetrieverConfig().getMaxResultsPerModule();
		
		ResultSet rset = this.selector.select(getResultCacheLimitSQL(resultCacheName) + " SELECT * FROM features.ForegroundBoundingBox, c WHERE shotid = c.filter USING DISTANCE MINKOWSKI(2)(\'" + fv.toFeatureString() + "\', bbox) ORDER USING DISTANCE LIMIT " + limit);
		return manageResultSet(rset);
	}
	
}
