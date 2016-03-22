package ch.unibas.cs.dbis.cineast.core.features;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ch.unibas.cs.dbis.cineast.core.color.ColorConverter;
import ch.unibas.cs.dbis.cineast.core.color.LabContainer;
import ch.unibas.cs.dbis.cineast.core.color.ReadableLabContainer;
import ch.unibas.cs.dbis.cineast.core.color.ReadableRGBContainer;
import ch.unibas.cs.dbis.cineast.core.config.Config;
import ch.unibas.cs.dbis.cineast.core.data.FloatVector;
import ch.unibas.cs.dbis.cineast.core.data.FloatVectorImpl;
import ch.unibas.cs.dbis.cineast.core.data.FrameContainer;
import ch.unibas.cs.dbis.cineast.core.data.LongDoublePair;
import ch.unibas.cs.dbis.cineast.core.data.MultiImage;
import ch.unibas.cs.dbis.cineast.core.db.PersistentTuple;
import ch.unibas.cs.dbis.cineast.core.features.abstracts.AbstractFeatureModule;
import ch.unibas.cs.dbis.cineast.core.util.KMeansPP;
import ch.unibas.cs.dbis.cineast.core.util.TimeHelper;

public class DominantColors extends AbstractFeatureModule {

	private static final Logger LOGGER = LogManager.getLogger();
	
	public DominantColors(){
		super("features.DominantColors", "colors", 488f / 4f);
	}
	
	public static LabContainer[] getDominantColor(MultiImage img){
		int[] colors = img.getThumbnailColors();
		ArrayList<ReadableLabContainer> colorList = new ArrayList<ReadableLabContainer>(colors.length);
		for(int col : colors){
			if(ReadableRGBContainer.getAlpha(col) < 127){
				continue;
			}
			colorList.add(ColorConverter.cachedRGBtoLab(col));
		}
		
		if(colorList.size() < 3){
			return new LabContainer[]{new LabContainer(), new LabContainer(), new LabContainer()};
		}
		
		KMeansPP.KMenasResult<ReadableLabContainer> result = KMeansPP.bestOfkMeansPP(colorList, new LabContainer(0, 0, 0), 3, 0.001, 10);
		
		FloatVector[] vectors = result.getCenters().toArray(new FloatVectorImpl[3]);
		LabContainer[] _return = new LabContainer[]{new LabContainer(0, 0, 0),new LabContainer(0, 0, 0),new LabContainer(0, 0, 0)};
		for(int i = 0; i < Math.min(3, vectors.length); ++i){
			if(vectors[i] == null){
				break;
			}
			_return[i] = new LabContainer(vectors[i].getElement(0), vectors[i].getElement(1), vectors[i].getElement(2));
		}
		return _return;
	}
	
	@Override
	public void processShot(FrameContainer shot) {
		if(!phandler.check("SELECT * FROM features.DominantColors WHERE shotid = " + shot.getId())){
			TimeHelper.tic();
			LOGGER.entry();
			LabContainer[] dominant = getDominantColor(shot.getMostRepresentativeFrame().getImage());
			long shotId = shot.getId();
	
			addToDB(shotId, dominant);
			LOGGER.debug("DominantColor.processShot() done in {}", TimeHelper.toc());
			LOGGER.exit();
		}
	}

	private void addToDB(long shotId, LabContainer[] dominant) {
		PersistentTuple tuple = this.phandler.makeTuple(shotId);
		FloatVectorImpl fvi = new FloatVectorImpl();
		for(LabContainer lab : dominant){
			fvi.add(lab.getL());
			fvi.add(lab.getA());
			fvi.add(lab.getB());
		}
		tuple.addElement(fvi);
		this.phandler.write(tuple);
		LOGGER.debug("{} : {}", shotId, Arrays.toString(dominant));
	}

	@Override
	public List<LongDoublePair> getSimilar(FrameContainer qc) {
		LabContainer[] query = getDominantColor(qc.getMostRepresentativeFrame().getImage());

		int limit = Config.getRetrieverConfig().getMaxResultsPerModule();
		
		FloatVectorImpl fvi = new FloatVectorImpl();
		for(LabContainer lab : query){
			fvi.add(lab.getL());
			fvi.add(lab.getA());
			fvi.add(lab.getB());
		}
		
		ResultSet rset = this.selector.select("SELECT * FROM features.DominantColors USING DISTANCE MINKOWSKI(1)(\'" + fvi.toFeatureString() + "\', colors) ORDER USING DISTANCE LIMIT " + limit);
		return manageResultSet(rset);
	}

	@Override
	public List<LongDoublePair> getSimilar(FrameContainer qc, String resultCacheName) {
		LabContainer[] query = getDominantColor(qc.getMostRepresentativeFrame().getImage());

		int limit = Config.getRetrieverConfig().getMaxResultsPerModule();
		
		FloatVectorImpl fvi = new FloatVectorImpl();
		for(LabContainer lab : query){
			fvi.add(lab.getL());
			fvi.add(lab.getA());
			fvi.add(lab.getB());
		}
		
		ResultSet rset = this.selector.select(getResultCacheLimitSQL(resultCacheName) + " SELECT * FROM features.DominantColors, c WHERE shotid = c.filter USING DISTANCE MINKOWSKI(1)(\'" + fvi.toFeatureString() + "\', colors) ORDER USING DISTANCE LIMIT " + limit);
		return manageResultSet(rset);
	}
	
}
