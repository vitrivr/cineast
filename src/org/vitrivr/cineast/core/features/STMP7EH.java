package ch.unibas.cs.dbis.cineast.core.features;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ch.unibas.cs.dbis.cineast.core.config.QueryConfig;
import ch.unibas.cs.dbis.cineast.core.data.FloatVectorImpl;
import ch.unibas.cs.dbis.cineast.core.data.Frame;
import ch.unibas.cs.dbis.cineast.core.data.MultiImage;
import ch.unibas.cs.dbis.cineast.core.data.SegmentContainer;
import ch.unibas.cs.dbis.cineast.core.data.StatElement;
import ch.unibas.cs.dbis.cineast.core.data.StringDoublePair;
import ch.unibas.cs.dbis.cineast.core.db.PersistencyWriter;

public class STMP7EH extends EHD {

	private static final Logger LOGGER = LogManager.getLogger();

	@Override
	public void init(PersistencyWriter<?> phandler) {
		this.phandler = phandler;
		this.phandler.open("features_STMP7EH");
	}
	
	@Override
	public void processShot(SegmentContainer shot) {
		LOGGER.entry();
		if (!phandler.idExists(shot.getId())) {
			List<Frame> frames = shot.getFrames();
			float[] hist = new float[80];
			StatElement[] stats = new StatElement[80];
			for(int i = 0; i < 80; ++i){
				stats[i] = new StatElement();
			}
			for(Frame f : frames){
				MultiImage img = f.getImage();
				
				hist = process(img, hist);
				for(int i = 0; i < 80; ++i){
					stats[i].add(hist[i]);
					hist[i] = 0f;
				}
			}
			float[] vec = new float[160];
			for(int i = 0; i < 80; ++i){
				vec[i] = stats[i].getAvg();
				vec[i + 80] = (float) Math.sqrt(stats[i].getVariance());
			}
			persist(shot.getId(), new FloatVectorImpl(vec));
		}
		LOGGER.exit();
	}

	@Override
	public List<StringDoublePair> getSimilar(SegmentContainer sc, QueryConfig qc) {
		// TODO Auto-generated method stub
		return super.getSimilar(sc, qc);
	}

	@Override
	public List<StringDoublePair> getSimilar(String shotId, QueryConfig qc) {
		// TODO Auto-generated method stub
		return super.getSimilar(shotId, qc);
	}
	

}
