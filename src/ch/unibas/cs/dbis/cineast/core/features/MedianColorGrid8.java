package ch.unibas.cs.dbis.cineast.core.features;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ch.unibas.cs.dbis.cineast.core.color.ColorConverter;
import ch.unibas.cs.dbis.cineast.core.color.ReadableLabContainer;
import ch.unibas.cs.dbis.cineast.core.color.ReadableRGBContainer;
import ch.unibas.cs.dbis.cineast.core.config.QueryConfig;
import ch.unibas.cs.dbis.cineast.core.data.FloatVector;
import ch.unibas.cs.dbis.cineast.core.data.FloatVectorImpl;
import ch.unibas.cs.dbis.cineast.core.data.MultiImage;
import ch.unibas.cs.dbis.cineast.core.data.Pair;
import ch.unibas.cs.dbis.cineast.core.data.SegmentContainer;
import ch.unibas.cs.dbis.cineast.core.data.StringDoublePair;
import ch.unibas.cs.dbis.cineast.core.features.abstracts.AbstractFeatureModule;
import ch.unibas.cs.dbis.cineast.core.util.ColorUtils;
import ch.unibas.cs.dbis.cineast.core.util.GridPartitioner;

public class MedianColorGrid8 extends AbstractFeatureModule {

	private static final Logger LOGGER = LogManager.getLogger();

	public MedianColorGrid8(){
		super("features_MedianColorGrid8", 12595f / 4f);
	}
	
	protected MedianColorGrid8(String tableName, float maxDist){
		super(tableName, maxDist);
	}
	

	@Override
	public void processShot(SegmentContainer shot) {
		LOGGER.entry();
		if (!phandler.idExists(shot.getId())) {
			MultiImage medimg = shot.getMedianImg();
			
			persist(shot.getId(), partition(medimg).first);
		}
		LOGGER.exit();

	}

	protected static Pair<FloatVector, float[]> partition(MultiImage img){
		ArrayList<Integer> labs = new ArrayList<Integer>(img.getWidth() * img.getHeight());
		ArrayList<Float> alphas = new ArrayList<Float>(img.getWidth() * img.getHeight());
		int[] colors = img.getColors();
		for(int c : colors){
			labs.add(c);
			alphas.add(ReadableRGBContainer.getAlpha(c) / 255f);
		}
		
		ArrayList<LinkedList<Integer>> partitions = GridPartitioner.partition(labs, img.getWidth(), img.getHeight(), 8, 8);
		
		float[] result = new float[8 * 8 * 3];
		int i = 0;
		for(LinkedList<Integer> list : partitions){
			ReadableLabContainer lab = ColorConverter.cachedRGBtoLab(ColorUtils.median(list));
			result[i++] = lab.getL();
			result[i++] = lab.getA();
			result[i++] = lab.getB();
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

	@Override
	public List<StringDoublePair> getSimilar(SegmentContainer sc, QueryConfig qc) {
		Pair<FloatVector, float[]> p = partition(sc.getMedianImg());
		return getSimilar(p.first.toArray(null), qc);
	}

}
