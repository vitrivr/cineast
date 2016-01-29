package ch.unibas.cs.dbis.cineast.core.features;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ch.unibas.cs.dbis.cineast.core.color.ColorConverter;
import ch.unibas.cs.dbis.cineast.core.color.LabContainer;
import ch.unibas.cs.dbis.cineast.core.color.ReadableLabContainer;
import ch.unibas.cs.dbis.cineast.core.config.Config;
import ch.unibas.cs.dbis.cineast.core.data.FrameContainer;
import ch.unibas.cs.dbis.cineast.core.data.LongDoublePair;
import ch.unibas.cs.dbis.cineast.core.db.PersistentTuple;
import ch.unibas.cs.dbis.cineast.core.features.abstracts.AbstractFeatureModule;

@Deprecated
public class SaturationAndChroma extends AbstractFeatureModule {

	private static final Logger LOGGER = LogManager.getLogger();

	public SaturationAndChroma(){
		super("features.SaturationAndChroma", "", LabContainer.MAX_CHROMA);
	}
	

	@Override
	public void processShot(FrameContainer shot) {
		if(!phandler.check("SELECT * FROM features.SaturationAndChroma WHERE shotid = " + shot.getId())){
			int[] colors = shot.getMostRepresentativeFrame().getImage().getColors();
			double saturation = 0, chroma = 0;
			for(int color : colors){
				ReadableLabContainer lab = ColorConverter.cachedRGBtoLab(color);
				saturation += lab.getSaturation();
				chroma += lab.getChroma();
			}
			saturation /= colors.length;
			chroma /= colors.length;
			addToDB(shot.getId(), chroma, saturation);
		}
	}

	private void addToDB(long shotId, double chroma, double saturation) {
		PersistentTuple tuple = phandler.makeTuple(shotId, chroma, saturation);
		this.phandler.write(tuple);
		LOGGER.debug("{} : {}, {}", shotId, chroma, saturation);
	}
	
	@Override
	public List<LongDoublePair> getSimilar(FrameContainer qc) {
		int[] colors = qc.getMostRepresentativeFrame().getImage().getColors();
		double saturation = 0, chroma = 0;
		for(int color : colors){
			ReadableLabContainer lab = ColorConverter.cachedRGBtoLab(color);
			saturation += lab.getSaturation();
			chroma += lab.getChroma();
		}
		saturation /= colors.length;
		chroma /= colors.length;
		
		int limit = Config.resultsPerModule();
		
		ResultSet rset = this.selector.select("SELECT ABS(chroma - " + chroma + ") + ABS(saturation - " + saturation + ") * 100 as dist, shotId FROM features.SaturationAndChroma ORDER BY dist ASC LIMIT " + limit);
		return manageResultSet(rset);
	}

	@Override
	public List<LongDoublePair> getSimilar(long shotId) {
		int limit = Config.resultsPerModule();
		
		ResultSet rset = this.selector.select("WITH q AS (SELECT saturation, chroma FROM features.SaturationAndChroma WHERE shotid = " + shotId + ") SELECT ABS(SaturationAndChroma.chroma - q.chroma) + ABS(SaturationAndChroma.saturation - q.saturation) * 100 as dist, shotId FROM features.SaturationAndChroma, q ORDER BY dist ASC LIMIT " + limit);
		return manageResultSet(rset);
	}


	@Override
	public List<LongDoublePair> getSimilar(FrameContainer qc, String resultCacheName) {
		return new ArrayList<>(1);
	}

}
