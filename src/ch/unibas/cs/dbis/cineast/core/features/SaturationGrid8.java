package ch.unibas.cs.dbis.cineast.core.features;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ch.unibas.cs.dbis.cineast.core.color.ColorConverter;
import ch.unibas.cs.dbis.cineast.core.color.ReadableRGBContainer;
import ch.unibas.cs.dbis.cineast.core.config.QueryConfig;
import ch.unibas.cs.dbis.cineast.core.data.FloatVector;
import ch.unibas.cs.dbis.cineast.core.data.FloatVectorImpl;
import ch.unibas.cs.dbis.cineast.core.data.Frame;
import ch.unibas.cs.dbis.cineast.core.data.LongDoublePair;
import ch.unibas.cs.dbis.cineast.core.data.MultiImage;
import ch.unibas.cs.dbis.cineast.core.data.Pair;
import ch.unibas.cs.dbis.cineast.core.data.SegmentContainer;
import ch.unibas.cs.dbis.cineast.core.data.StatElement;
import ch.unibas.cs.dbis.cineast.core.features.abstracts.AbstractFeatureModule;
import ch.unibas.cs.dbis.cineast.core.util.GridPartitioner;

public class SaturationGrid8 extends AbstractFeatureModule {

	private static final Logger LOGGER = LogManager.getLogger();

	public SaturationGrid8(){
		super("features.SaturationGrid8", "grid", 45f / 4f);
	}
	
	private Pair<FloatVector, float[]> computeGrid(SegmentContainer qc){
		ArrayList<StatElement> stats = new ArrayList<StatElement>(64);
		for(int i = 0; i < 64; ++i){
			stats.add(new StatElement());
		}
		MultiImage img = qc.getMostRepresentativeFrame().getImage();
		int[] colors = img.getColors();
		ArrayList<Float> saturations = new ArrayList<Float>(img.getWidth() * img.getHeight());
		ArrayList<Float> alphas = new ArrayList<Float>(img.getWidth() * img.getHeight());
		for(int c : colors){
			saturations.add(ColorConverter.cachedRGBtoLab(c).getSaturation());
			alphas.add(ReadableRGBContainer.getAlpha(c) / 255f);
		}
		
		ArrayList<LinkedList<Float>> partitions = GridPartitioner.partition(saturations, img.getWidth(), img.getHeight(), 8, 8);
		ArrayList<LinkedList<Float>> alphaPartitions = GridPartitioner.partition(alphas, img.getWidth(), img.getHeight(), 8, 8);
		for(int i = 0; i < partitions.size(); ++i){
			StatElement stat = stats.get(i);
			Iterator<Float> iter = alphaPartitions.get(i).iterator();
			for(float c : partitions.get(i)){
				if(iter.next() < 0.5f){
					continue;
				}
				stat.add(c);
			}
		}
		float[] f = new float[128];
		for(int i = 0; i < 64; ++i){
			f[2 * i] = stats.get(i).getAvg();
			f[2 * i + 1] = stats.get(i).getVariance();
		}
		
		float[] weights = new float[128];
		
		for(int i = 0; i < alphaPartitions.size(); ++i){
			float w = 0;
			for(float v : alphaPartitions.get(i)){
				w += v;
			}
			w /= alphaPartitions.get(i).size();
			weights[2 * i] = w;
			weights[2 * i + 1] = w;
		}
		
		return new Pair<FloatVector, float[]>(new FloatVectorImpl(f), weights);
	}
	
//	@Override
//	public List<LongDoublePair> getSimilar(SegmentContainer qc) {
//		int limit = Config.getRetrieverConfig().getMaxResultsPerModule();
//		
//		Pair<FloatVector, float[]> p = computeGrid(qc);
//		
//		ResultSet rset = this.selector.select("SELECT * FROM features.SaturationGrid8 USING DISTANCE MINKOWSKI(1, " + formatQueryWeights(p.second) + ")(\'" + p.first.toFeatureString() + "\', grid) ORDER USING DISTANCE LIMIT " + limit);
//		return manageResultSet(rset);
//	}
//
//
//	@Override
//	public List<LongDoublePair> getSimilar(SegmentContainer qc, String resultCacheName) {
//		int limit = Config.getRetrieverConfig().getMaxResultsPerModule();
//		
//		Pair<FloatVector, float[]> p = computeGrid(qc);
//		
//		ResultSet rset = this.selector.select(getResultCacheLimitSQL(resultCacheName) + " SELECT * FROM features.SaturationGrid8, c WHERE shotid = c.filter USING DISTANCE MINKOWSKI(1, " + formatQueryWeights(p.second) + ")(\'" + p.first.toFeatureString() + "\', grid) ORDER USING DISTANCE LIMIT " + limit);
//		return manageResultSet(rset);
//	}

	@Override
	public void processShot(SegmentContainer shot) {
		LOGGER.entry();
		if (!phandler.idExists(shot.getId())) {
			ArrayList<StatElement> stats = new ArrayList<StatElement>(64);
			for(int i = 0; i < 64; ++i){
				stats.add(new StatElement());
			}
			List<Frame> frames = shot.getFrames();
			ArrayList<Float> saturations = null;
			int width = 0, height = 0;
			for(Frame f : frames){
				if(saturations == null){
					width = f.getImage().getWidth();
					height = f.getImage().getHeight();
					saturations = new ArrayList<Float>(width * height);
				}else{
					saturations.clear();
				}
				
				int[] colors = f.getImage().getColors();
				for(int c : colors){
					saturations.add(ColorConverter.cachedRGBtoLab(c).getSaturation());
				}
				
				ArrayList<LinkedList<Float>> partitions = GridPartitioner.partition(saturations, width, height, 8, 8);
				for(int i = 0; i < partitions.size(); ++i){
					StatElement stat = stats.get(i);
					for(float c : partitions.get(i)){
						stat.add(c);
					}
				}
			}
			
			float[] result = new float[128];
			for(int i = 0; i < 64; ++i){
				result[2 * i] = stats.get(i).getAvg();
				result[2 * i + 1] = stats.get(i).getVariance();
			}
			
			persist(shot.getId(), new FloatVectorImpl(result));
			
		}
		LOGGER.exit();
	}

	@Override
	public List<LongDoublePair> getSimilar(SegmentContainer sc, QueryConfig qc) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<LongDoublePair> getSimilar(long shotId, QueryConfig qc) {
		// TODO Auto-generated method stub
		return null;
	}
}
