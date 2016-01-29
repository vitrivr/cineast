package ch.unibas.cs.dbis.cineast.core.features;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ch.unibas.cs.dbis.cineast.core.color.ColorConverter;
import ch.unibas.cs.dbis.cineast.core.color.HSVContainer;
import ch.unibas.cs.dbis.cineast.core.color.RGBContainer;
import ch.unibas.cs.dbis.cineast.core.color.ReadableRGBContainer;
import ch.unibas.cs.dbis.cineast.core.config.Config;
import ch.unibas.cs.dbis.cineast.core.data.FloatVector;
import ch.unibas.cs.dbis.cineast.core.data.FloatVectorImpl;
import ch.unibas.cs.dbis.cineast.core.data.Frame;
import ch.unibas.cs.dbis.cineast.core.data.FrameContainer;
import ch.unibas.cs.dbis.cineast.core.data.LongDoublePair;
import ch.unibas.cs.dbis.cineast.core.data.MultiImage;
import ch.unibas.cs.dbis.cineast.core.data.Pair;
import ch.unibas.cs.dbis.cineast.core.data.StatElement;
import ch.unibas.cs.dbis.cineast.core.features.abstracts.AbstractFeatureModule;
import ch.unibas.cs.dbis.cineast.core.util.GridPartitioner;

public class HueValueVarianceGrid8 extends AbstractFeatureModule {

	private static final Logger LOGGER = LogManager.getLogger();
	
	public HueValueVarianceGrid8(){
		super("features.HueValueVarianceGrid8", "grid", 17f / 4f);
	}
	
	private Pair<FloatVector, float[]> computeGrid(FrameContainer qc){
		ArrayList<StatElement> stats = new ArrayList<StatElement>(64);
		for(int i = 0; i < 128; ++i){
			stats.add(new StatElement());
		}
		MultiImage img = qc.getMostRepresentativeFrame().getImage();
		int[] colors = img.getColors();
		ArrayList<HSVContainer> hsvs = new ArrayList<HSVContainer>(img.getWidth() * img.getHeight());
		ArrayList<Float> alphas = new ArrayList<Float>(img.getWidth() * img.getHeight());
		for(int c : colors){
			hsvs.add(ColorConverter.RGBtoHSV(new RGBContainer(c)));
			alphas.add(ReadableRGBContainer.getAlpha(c) / 255f);
		}
		
		ArrayList<LinkedList<HSVContainer>> partitions = GridPartitioner.partition(hsvs, img.getWidth(), img.getHeight(), 8, 8);
		ArrayList<LinkedList<Float>> alphaPartitions = GridPartitioner.partition(alphas, img.getWidth(), img.getHeight(), 8, 8);
		for(int i = 0; i < partitions.size(); ++i){
			StatElement hue = stats.get(2 * i);
			StatElement value = stats.get(2 * i + 1);
			Iterator<Float> iter = alphaPartitions.get(i).iterator();
			for(HSVContainer c : partitions.get(i)){
				if(iter.next() < 0.5f){
					continue;
				}
				hue.add(c.getH());
				value.add(c.getV());
			}
		}
		float[] f = new float[128];
		for(int i = 0; i < 128; ++i){
			f[i] = stats.get(i).getVariance();
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
	public List<LongDoublePair> getSimilar(FrameContainer qc) {
		int limit = Config.resultsPerModule();
		Pair<FloatVector, float[]> p = computeGrid(qc);

		ResultSet rset = this.selector.select("SELECT * FROM features.HueValueVarianceGrid8 USING DISTANCE MINKOWSKI(1, " + formatQueryWeights(p.second) + ")(\'" + p.first.toFeatureString() + "\', grid) ORDER USING DISTANCE LIMIT " + limit);
		return manageResultSet(rset);
	}

	@Override
	public List<LongDoublePair> getSimilar(FrameContainer qc, String resultCacheName) {
		int limit = Config.resultsPerModule();
		Pair<FloatVector, float[]> p = computeGrid(qc);

		ResultSet rset = this.selector.select(getResultCacheLimitSQL(resultCacheName) + " SELECT * FROM features.HueValueVarianceGrid8, c WHERE shotid = c.filter USING DISTANCE MINKOWSKI(1, " + formatQueryWeights(p.second) + ")(\'" + p.first.toFeatureString() + "\', grid) ORDER USING DISTANCE LIMIT " + limit);
		return manageResultSet(rset);
	}

	@Override
	public void processShot(FrameContainer shot) {
		LOGGER.entry();
		if (!phandler.check("SELECT * FROM features.HueValueVarianceGrid8 WHERE shotid = " + shot.getId())) {
			StatElement[] stats = new StatElement[128];
			for(int i = 0; i < 128; ++i){
				stats[i] = new StatElement();
			}
			ArrayList<HSVContainer> hsvs = null;
			List<Frame> frames = shot.getFrames();
			for(Frame f : frames){
				MultiImage img = f.getImage();
				int[] ints = img.getColors();
				
				if(hsvs == null){
					hsvs = new ArrayList<HSVContainer>(ints.length);
				}else{
					hsvs.clear();
				}
				
				for(int c : ints){
					hsvs.add(ColorConverter.RGBtoHSV(new RGBContainer(c)));
				}
				
				ArrayList<LinkedList<HSVContainer>> partition = GridPartitioner.partition(hsvs, img.getWidth(), img.getHeight(), 8, 8);
				for(int i = 0; i < 64; ++i){
					LinkedList<HSVContainer> list = partition.get(i);
					StatElement hue = stats[2 * i];
					StatElement value = stats[2 * i + 1];
					for(HSVContainer c : list){
						hue.add(c.getH());
						value.add(c.getV());
					}
				}
			}
			
			float[] fv = new float[128];
			int i = 0;
			for(StatElement s : stats){
				fv[i++] = s.getVariance();
			}
			
			addToDB(shot.getId(), new FloatVectorImpl(fv));
		}
	}

}
