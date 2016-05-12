package ch.unibas.cs.dbis.cineast.core.features;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ch.unibas.cs.dbis.cineast.core.config.QueryConfig;
import ch.unibas.cs.dbis.cineast.core.data.FloatVector;
import ch.unibas.cs.dbis.cineast.core.data.MultiImage;
import ch.unibas.cs.dbis.cineast.core.data.Pair;
import ch.unibas.cs.dbis.cineast.core.data.SegmentContainer;
import ch.unibas.cs.dbis.cineast.core.data.StringDoublePair;
import ch.unibas.cs.dbis.cineast.core.features.abstracts.AbstractFeatureModule;
import ch.unibas.cs.dbis.cineast.core.util.ARPartioner;

public class MedianColorARP44 extends AbstractFeatureModule {

	
	private static final Logger LOGGER = LogManager.getLogger();

	public MedianColorARP44(){
		super("features.MedianColorARP44", 115854f / 4f);
	}


	@Override
	public void processShot(SegmentContainer shot) {
		LOGGER.entry();
		if(!phandler.idExists(shot.getId())){
			MultiImage median = shot.getMedianImg();
			FloatVector vec = ARPartioner.partitionImage(median, 4, 4).first;
			persist(shot.getId(), vec);
		}
		LOGGER.exit();
	}

	@Override
	public List<StringDoublePair> getSimilar(SegmentContainer sc, QueryConfig qc) {
		Pair<FloatVector, float[]> p = ARPartioner.partitionImage(sc.getMedianImg(), 4, 4);
		return getSimilar(p.first.toArray(null), qc);
	}

	@Override
	public List<StringDoublePair> getSimilar(long shotId, QueryConfig qc) {
		// TODO Auto-generated method stub
		return null;
	}

}
