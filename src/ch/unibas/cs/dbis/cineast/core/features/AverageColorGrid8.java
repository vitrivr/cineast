package ch.unibas.cs.dbis.cineast.core.features;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ch.unibas.cs.dbis.cineast.core.color.ColorConverter;
import ch.unibas.cs.dbis.cineast.core.color.ReadableLabContainer;
import ch.unibas.cs.dbis.cineast.core.color.ReadableRGBContainer;
import ch.unibas.cs.dbis.cineast.core.config.Config;
import ch.unibas.cs.dbis.cineast.core.data.FloatVector;
import ch.unibas.cs.dbis.cineast.core.data.FloatVectorImpl;
import ch.unibas.cs.dbis.cineast.core.data.FrameContainer;
import ch.unibas.cs.dbis.cineast.core.data.LongDoublePair;
import ch.unibas.cs.dbis.cineast.core.data.MultiImage;
import ch.unibas.cs.dbis.cineast.core.data.Pair;
import ch.unibas.cs.dbis.cineast.core.features.abstracts.AbstractFeatureModule;
import ch.unibas.cs.dbis.cineast.core.util.ColorUtils;
import ch.unibas.cs.dbis.cineast.core.util.GridPartitioner;

public class AverageColorGrid8 extends AbstractFeatureModule {

	public AverageColorGrid8(){
		super("features.AverageColorGrid8", "grid", 12595f / 4f);
	}
	
	protected AverageColorGrid8(String tableName, String colName, float maxDist){
		super(tableName, colName, maxDist);
	}
	
	private static final Logger LOGGER = LogManager.getLogger();

	@Override
	public List<LongDoublePair> getSimilar(FrameContainer qc) {
		Pair<FloatVector, float[]> p = partition(qc.getAvgImg());
		FloatVector query = p.first;
		int limit = Config.getRetrieverConfig().getMaxResultsPerModule();
		
		ResultSet rset = this.selector.select("SELECT * FROM features.AverageColorGrid8 USING DISTANCE MINKOWSKI(1, " + formatQueryWeights(p.second) + ")(\'" + query.toFeatureString() + "\', grid) ORDER USING DISTANCE LIMIT " + limit);
		
		return manageResultSet(rset);
	}


	@Override
	public List<LongDoublePair> getSimilar(FrameContainer qc, String resultCacheName) {
		Pair<FloatVector, float[]> p = partition(qc.getAvgImg());
		FloatVector query = p.first;
		int limit = Config.getRetrieverConfig().getMaxResultsPerModule();
		
		ResultSet rset = this.selector.select(getResultCacheLimitSQL(resultCacheName) + "SELECT * FROM features.AverageColorGrid8, c WHERE shotid = c.filter USING DISTANCE MINKOWSKI(1, " + formatQueryWeights(p.second) + ")(\'" + query.toFeatureString() + "\', grid) ORDER USING DISTANCE LIMIT " + limit);
		
		return manageResultSet(rset);
	}

	@Override
	public void processShot(FrameContainer shot) {
		LOGGER.entry();
		if (!phandler.check("SELECT * FROM features.AverageColorGrid8 WHERE shotid = " + shot.getId())) {
			MultiImage avgimg = shot.getAvgImg();
			
			addToDB(shot.getId(), partition(avgimg).first);
		}
		LOGGER.exit();
	}

	protected static Pair<FloatVector, float[]> partition(MultiImage img){
		ArrayList<ReadableLabContainer> labs = new ArrayList<ReadableLabContainer>(img.getWidth() * img.getHeight());
		ArrayList<Float> alphas = new ArrayList<Float>(img.getWidth() * img.getHeight());
		int[] colors = img.getColors();
		for(int c : colors){
			labs.add(ColorConverter.cachedRGBtoLab(c));
			alphas.add(ReadableRGBContainer.getAlpha(c) / 255f);
		}
		
		ArrayList<LinkedList<ReadableLabContainer>> partitions = GridPartitioner.partition(labs, img.getWidth(), img.getHeight(), 8, 8);
		
		float[] result = new float[8 * 8 * 3];
		int i = 0;
		for(LinkedList<ReadableLabContainer> list : partitions){
			ReadableLabContainer avg = ColorUtils.getAvg(list);
			result[i++] = avg.getL();
			result[i++] = avg.getA();
			result[i++] = avg.getB();
		}
		
		
		ArrayList<LinkedList<Float>> alphaPartitions = GridPartitioner.partition(alphas, img.getWidth(), img.getHeight(), 8, 8);
		float[] weights = new float[8 * 8 * 3];
		i = 0;
		for(LinkedList<Float> list : alphaPartitions){
			float a = 0;
			int c = 0;
			for(float f : list){
				a += f;
				++c;
			}
			weights[i++] = a / c;
			weights[i++] = a / c;
			weights[i++] = a / c;
		}
		
		return new Pair<FloatVector, float[]>(new FloatVectorImpl(result), weights);
	}

}
