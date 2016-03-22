package ch.unibas.cs.dbis.cineast.core.features;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ch.unibas.cs.dbis.cineast.core.color.RGBContainer;
import ch.unibas.cs.dbis.cineast.core.color.ReadableRGBContainer;
import ch.unibas.cs.dbis.cineast.core.config.Config;
import ch.unibas.cs.dbis.cineast.core.data.Frame;
import ch.unibas.cs.dbis.cineast.core.data.FrameContainer;
import ch.unibas.cs.dbis.cineast.core.data.LongDoublePair;
import ch.unibas.cs.dbis.cineast.core.db.PersistentTuple;
import ch.unibas.cs.dbis.cineast.core.features.abstracts.AbstractFeatureModule;

@Deprecated
public class Contrast extends AbstractFeatureModule {
	
	private static final Logger LOGGER = LogManager.getLogger();
	
	public Contrast(){
		super("features.Contrast", "", 255 + 255 + 512);
	}

	@Override
	public void processShot(FrameContainer shot) {
		if(!phandler.check("SELECT * FROM features.Contrast WHERE shotid = " + shot.getId())){
			float lmin = Float.MAX_VALUE, lmax = Float.MIN_VALUE;
			for (Frame f : shot.getFrames()) {
				int[] colors = f.getImage().getColors();
				for (int color : colors) {
					if(ReadableRGBContainer.getAlpha(color) < 127){
						continue;
					}
					float l = RGBContainer.getLuminance(color);
					lmin = Math.min(lmin, l);
					lmax = Math.max(lmax, l);
				}
			}
			addToDB(shot.getId(), lmin, lmax);
		}
	}

	private void addToDB(long shotId, float lmin, float lmax) {
		PersistentTuple tuple = this.phandler.makeTuple(shotId, lmin, lmax);
		this.phandler.write(tuple);
		LOGGER.debug("{} : {} , {}", shotId, lmin, lmax);
	}

	@Override
	public List<LongDoublePair> getSimilar(FrameContainer qc) {
		float lmin = Float.MAX_VALUE, lmax = Float.MIN_VALUE;
		int[] colors = qc.getMostRepresentativeFrame().getImage().getColors();
		for (int color : colors) {
			if(ReadableRGBContainer.getAlpha(color) < 127){
				continue;
			}
			float l = RGBContainer.getLuminance(color);
			lmin = Math.min(lmin, l);
			lmax = Math.max(lmax, l);
		}
		
		float contrast = (lmax - lmin) / (lmax + lmin);
		if(Float.isInfinite(contrast) || Float.isNaN(contrast)){
			contrast = 0;
		}
		int limit = Config.getRetrieverConfig().getMaxResultsPerModule();
		
		ResultSet rset = this.selector.select("SELECT ABS(lmin - " + lmin + ") + ABS(lmax - " + lmax + ") + ABS(((lmax - lmin) / (lmax + lmin)) - " + contrast + ") * 512 AS dist, shotId FROM features.Contrast ORDER BY dist ASC LIMIT " + limit);
		return manageResultSet(rset);
	}

	@Override
	public List<LongDoublePair> getSimilar(long shotId) {
		int limit = Config.getRetrieverConfig().getMaxResultsPerModule();
		
		ResultSet rset = this.selector.select("WITH q AS (SELECT lmin, lmax, ((lmax - lmin) / (lmax + lmin)) AS contrast FROM features.Contrast WHERE shotid = 12345) SELECT ABS(Contrast.lmin - q.lmin) + ABS(Contrast.lmax - q.lmax) + ABS(((Contrast.lmax - Contrast.lmin) / (Contrast.lmax + Contrast.lmin)) - q.contrast) * 512 AS dist, shotId FROM features.Contrast, q ORDER BY dist ASC LIMIT " + limit);
		return manageResultSet(rset);
	}

	@Override
	public List<LongDoublePair> getSimilar(FrameContainer qc, String resultCacheName) {
		return new ArrayList<>(1);
	}
	
	public List<LongDoublePair> getSimilar(long shotId, String resultCacheName){
		return new ArrayList<>(1);
	}

}
