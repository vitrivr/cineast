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
import ch.unibas.cs.dbis.cineast.core.data.MultiImage;
import ch.unibas.cs.dbis.cineast.core.data.Pair;
import ch.unibas.cs.dbis.cineast.core.data.SegmentContainer;
import ch.unibas.cs.dbis.cineast.core.data.StatElement;
import ch.unibas.cs.dbis.cineast.core.data.StringDoublePair;
import ch.unibas.cs.dbis.cineast.core.features.abstracts.AbstractFeatureModule;
import ch.unibas.cs.dbis.cineast.core.util.GridPartitioner;

public class ChromaGrid8 extends AbstractFeatureModule {

	private static final Logger LOGGER = LogManager.getLogger();

	public ChromaGrid8(){
		super("features.ChromaGrid8", "grid", 86609f / 4f);
	}
	
	private Pair<FloatVector, float[]> buildChromaGrid(SegmentContainer qc){
		ArrayList<StatElement> stats = new ArrayList<StatElement>(64);
		for(int i = 0; i < 64; ++i){
			stats.add(new StatElement());
		}
		MultiImage img = qc.getMostRepresentativeFrame().getImage();
		int[] colors = img.getColors();
		ArrayList<Float> chromas = new ArrayList<Float>(img.getWidth() * img.getHeight());
		ArrayList<Float> alphas = new ArrayList<Float>(img.getWidth() * img.getHeight());
		
		for(int c : colors){
			chromas.add(ColorConverter.cachedRGBtoLab(c).getChroma());
			alphas.add(ReadableRGBContainer.getAlpha(c) / 255f);
		}
		
		ArrayList<LinkedList<Float>> partitions = GridPartitioner.partition(chromas, img.getWidth(), img.getHeight(), 8, 8);
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
		
		FloatVector query = new FloatVectorImpl(f);
		
		return new Pair<>(query, weights);
	}
	
//	@Override
//	public List<LongDoublePair> getSimilar(SegmentContainer qc) {
//		Pair<FloatVector, float[]> p = buildChromaGrid(qc);
//		ResultSet rset = this.selector.select("SELECT * FROM features.ChromaGrid8 USING DISTANCE MINKOWSKI(1, " + formatQueryWeights(p.second) + ")(\'" + p.first.toFeatureString() + "\', grid) ORDER USING DISTANCE LIMIT " + Config.getRetrieverConfig().getMaxResultsPerModule());
//		return manageResultSet(rset);
//	}
//
//	@Override
//	public List<LongDoublePair> getSimilar(SegmentContainer qc, String resultCacheName) {
//		Pair<FloatVector, float[]> p = buildChromaGrid(qc);
//		ResultSet rset = this.selector.select(getResultCacheLimitSQL(resultCacheName) + " SELECT * FROM features.ChromaGrid8, c WHERE shotid = c.filter USING DISTANCE MINKOWSKI(1, " + formatQueryWeights(p.second) + ")(\'" + p.first.toFeatureString() + "\', grid) ORDER USING DISTANCE LIMIT " + Config.getRetrieverConfig().getMaxResultsPerModule());
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
			ArrayList<Float> chromas = null;
			int width = 0, height = 0;
			for(Frame f : frames){
				if(chromas == null){
					width = f.getImage().getWidth();
					height = f.getImage().getHeight();
					chromas = new ArrayList<Float>(width * height);
				}else{
					chromas.clear();
				}
				
				int[] colors = f.getImage().getColors();
				for(int c : colors){
					chromas.add(ColorConverter.cachedRGBtoLab(c).getChroma());
				}
				
				ArrayList<LinkedList<Float>> partitions = GridPartitioner.partition(chromas, width, height, 8, 8);
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
	public List<StringDoublePair> getSimilar(SegmentContainer sc, QueryConfig qc) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<StringDoublePair> getSimilar(long shotId, QueryConfig qc) {
		// TODO Auto-generated method stub
		return null;
	}
}
