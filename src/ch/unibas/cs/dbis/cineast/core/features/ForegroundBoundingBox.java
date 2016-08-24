package ch.unibas.cs.dbis.cineast.core.features;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.config.QueryConfig;
import org.vitrivr.cineast.core.data.FloatVector;
import org.vitrivr.cineast.core.data.FloatVectorImpl;
import org.vitrivr.cineast.core.data.Pair;
import org.vitrivr.cineast.core.data.SegmentContainer;
import org.vitrivr.cineast.core.data.StringDoublePair;
import org.vitrivr.cineast.core.db.PersistentTuple;
import org.vitrivr.cineast.core.features.abstracts.AbstractFeatureModule;
import org.vitrivr.cineast.core.util.TimeHelper;

import ch.unibas.cs.dbis.cineast.core.util.MaskGenerator;

public class ForegroundBoundingBox  extends AbstractFeatureModule {

	public ForegroundBoundingBox() {
		super("features_ForegroundBoundingBox", 0.5f);
	}

	private static final Logger LOGGER = LogManager.getLogger();
	
	@Override
	public void processShot(SegmentContainer shot) {
		TimeHelper.tic();
		LOGGER.entry();
		if(!phandler.idExists(shot.getId())){
			ArrayList<Pair<Long,ArrayList<Float>>> bboxs = MaskGenerator.getNormalizedBbox(shot.getFrames(), shot.getPaths(), shot.getBgPaths());
			for(Pair<Long,ArrayList<Float>> bbox : bboxs){
				FloatVectorImpl fv = new FloatVectorImpl(bbox.second);
				persist(shot.getId(), bbox.first, fv);
			}
			LOGGER.debug("ForegroundBoundingBox.processShot() done in {}",
					TimeHelper.toc());
		}
		LOGGER.exit();
	}

	@Override
	public List<StringDoublePair> getSimilar(SegmentContainer sc, QueryConfig qc) {
		ArrayList<Pair<Long,ArrayList<Float>>> bboxs = MaskGenerator.getNormalizedBbox(sc.getFrames(), sc.getPaths(), sc.getBgPaths());
		FloatVectorImpl fv = new FloatVectorImpl(bboxs.get(0).second);
		
		return getSimilar(fv.toArray(null), qc);
	}
	
	private float[] arrayCache = null;
	protected void persist(String shotId, long frameIdx, FloatVector fs) {
		PersistentTuple tuple = this.phandler.generateTuple(shotId, frameIdx, arrayCache = fs.toArray(arrayCache));
		this.phandler.persist(tuple);
	}
}
