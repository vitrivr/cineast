package org.vitrivr.cineast.core.features;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.config.QueryConfig;
import org.vitrivr.cineast.core.data.FloatVector;
import org.vitrivr.cineast.core.data.FloatVectorImpl;
import org.vitrivr.cineast.core.data.Frame;
import org.vitrivr.cineast.core.data.MultiImage;
import org.vitrivr.cineast.core.data.SegmentContainer;
import org.vitrivr.cineast.core.data.StatElement;
import org.vitrivr.cineast.core.data.StringDoublePair;
import org.vitrivr.cineast.core.descriptor.EdgeImg;
import org.vitrivr.cineast.core.features.abstracts.AbstractFeatureModule;
import org.vitrivr.cineast.core.util.GridPartitioner;

public class EdgeGrid16Full extends AbstractFeatureModule {

	private static final Logger LOGGER = LogManager.getLogger();

	public EdgeGrid16Full(){
		super("features_EdgeGrid16Full", 124f / 4f);
	}
	
	
	@Override
	public void processShot(SegmentContainer shot) {
		LOGGER.entry();
		if (!phandler.idExists(shot.getId())) {
			StatElement[] stats = new StatElement[256];
			for(int i = 0; i < 256; ++i){
				stats[i] = new StatElement();
			}
			List<Frame> frames = shot.getFrames();
			List<Boolean> edgePixels = new ArrayList<>();
			for(Frame f : frames){
				MultiImage img = f.getImage();
				edgePixels = EdgeImg.getEdgePixels(img, edgePixels);
				ArrayList<LinkedList<Boolean>> partition = GridPartitioner.partition(edgePixels, img.getWidth(), img.getHeight(), 16, 16);
				for(int i = 0; i < partition.size(); ++i){
					LinkedList<Boolean> edge = partition.get(i);
					StatElement stat = stats[i];
					for(boolean b : edge){
						stat.add(b ? 1 : 0);
					}
				}
			}
			float[] result = new float[64];
			for(int i = 0; i < 64; ++i){
				result[i] = stats[i].getAvg();
			}
			persist(shot.getId(), new FloatVectorImpl(result));
		}
		LOGGER.exit();
	}



	private static FloatVector getEdges(MultiImage img){
		StatElement[] stats = new StatElement[256];
		for(int i = 0; i < 256; ++i){
			stats[i] = new StatElement();
		}
		List<Boolean> edgePixels = EdgeImg.getEdgePixels(img, new ArrayList<Boolean>(img.getWidth() * img.getHeight()));
		ArrayList<LinkedList<Boolean>> partition = GridPartitioner.partition(edgePixels, img.getWidth(), img.getHeight(), 16, 16);
		for(int i = 0; i < partition.size(); ++i){
			LinkedList<Boolean> edge = partition.get(i);
			StatElement stat = stats[i];
			for(boolean b : edge){
				stat.add(b ? 1 : 0);
			}
		}
		float[] f = new float[256];
		for(int i = 0; i < 256; ++i){
			f[i] = stats[i].getAvg();
		}
		
		return new FloatVectorImpl(f);
	}


	@Override
	public List<StringDoublePair> getSimilar(SegmentContainer sc, QueryConfig qc) {
		FloatVector query = getEdges(sc.getMostRepresentativeFrame().getImage());
		return getSimilar(query.toArray(null), qc);
	}

}
