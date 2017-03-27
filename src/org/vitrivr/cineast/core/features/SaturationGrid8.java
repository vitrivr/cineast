package org.vitrivr.cineast.core.features;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.color.ColorConverter;
import org.vitrivr.cineast.core.color.ReadableRGBContainer;
import org.vitrivr.cineast.core.config.QueryConfig;
import org.vitrivr.cineast.core.config.ReadableQueryConfig;
import org.vitrivr.cineast.core.data.FloatVector;
import org.vitrivr.cineast.core.data.FloatVectorImpl;
import org.vitrivr.cineast.core.data.MultiImage;
import org.vitrivr.cineast.core.data.Pair;
import org.vitrivr.cineast.core.data.StatElement;
import org.vitrivr.cineast.core.data.StringDoublePair;
import org.vitrivr.cineast.core.data.frames.VideoFrame;
import org.vitrivr.cineast.core.data.segments.SegmentContainer;
import org.vitrivr.cineast.core.features.abstracts.AbstractFeatureModule;
import org.vitrivr.cineast.core.util.GridPartitioner;

public class SaturationGrid8 extends AbstractFeatureModule {

	private static final Logger LOGGER = LogManager.getLogger();

	public SaturationGrid8(){
		super("features_SaturationGrid8", 45f / 4f);
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

	@Override
	public void processShot(SegmentContainer shot) {
		LOGGER.entry();
		if (!phandler.idExists(shot.getId())) {
			ArrayList<StatElement> stats = new ArrayList<StatElement>(64);
			for(int i = 0; i < 64; ++i){
				stats.add(new StatElement());
			}
			List<VideoFrame> videoFrames = shot.getVideoFrames();
			ArrayList<Float> saturations = null;
			int width = 0, height = 0;
			for(VideoFrame f : videoFrames){
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
	public List<StringDoublePair> getSimilar(SegmentContainer sc, ReadableQueryConfig qc) {
		Pair<FloatVector, float[]> p = computeGrid(sc);
		return getSimilar(p.first.toArray(null), new QueryConfig(qc).setDistanceWeights(p.second));
	}

}
