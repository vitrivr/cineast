package ch.unibas.cs.dbis.cineast.core.features;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ch.unibas.cs.dbis.cineast.core.config.Config;
import ch.unibas.cs.dbis.cineast.core.data.FloatVectorImpl;
import ch.unibas.cs.dbis.cineast.core.data.Frame;
import ch.unibas.cs.dbis.cineast.core.data.FrameContainer;
import ch.unibas.cs.dbis.cineast.core.data.LongDoublePair;
import ch.unibas.cs.dbis.cineast.core.data.MultiImage;
import ch.unibas.cs.dbis.cineast.core.data.StatElement;
import ch.unibas.cs.dbis.cineast.core.db.PersistencyWriter;
import ch.unibas.cs.dbis.cineast.core.util.LogHelper;
import ch.unibas.cs.dbis.cineast.core.util.MathHelper;

public class STMP7EH extends EHD {

	private static final Logger LOGGER = LogManager.getLogger();
	private static final float MAX_DIST = 1f;
	
	@Override
	public void init(PersistencyWriter<?> phandler) {
		this.phandler = phandler;
		this.phandler.open("features.STMP7EH");
	}
	
	@Override
	public void processShot(FrameContainer shot) {
		LOGGER.entry();
		if (!phandler.check("SELECT * FROM features.STMP7EH WHERE shotid = " + shot.getId())) {
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
			addToDB(shot.getId(), new FloatVectorImpl(vec));
		}
		LOGGER.exit();
	}
	
	
	@Override
	public List<LongDoublePair> getSimilar(FrameContainer qc) {
		ArrayList<LongDoublePair> result = new ArrayList<>(1);
		//TODO ??
		return result;
	}
	
	@Override
	public List<LongDoublePair> getSimilar(long shotId) { //TODO limiting call goes to EHD
		int limit = Config.getRetrieverConfig().getMaxResultsPerModule();
		
		ResultSet rset = this.selector.select("WITH q AS (SELECT hist FROM features.STMP7EH WHERE shotid = " + shotId + ") SELECT shotid FROM features.STMP7EH, q USING DISTANCE MINKOWSKI(2)(q.hist, STMP7EH.hist) ORDER USING DISTANCE LIMIT " + limit);
		ArrayList<LongDoublePair> result = new ArrayList<>(limit);
		if(rset != null){
			try {
				while(rset.next()){
					double dist = rset.getDouble(1);
					long id = rset.getLong(2);
					result.add(new LongDoublePair(id, MathHelper.getScore(dist, MAX_DIST)));
				}
			} catch (SQLException e) {
				LOGGER.fatal(LogHelper.SQL_MARKER, LogHelper.getStackTrace(e));
			}
		}
		
		return result;
	}
}
