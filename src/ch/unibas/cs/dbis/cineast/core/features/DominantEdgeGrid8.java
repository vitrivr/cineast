package ch.unibas.cs.dbis.cineast.core.features;

import java.util.Iterator;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import boofcv.alg.feature.detect.edge.EdgeContour;
import boofcv.alg.feature.detect.edge.EdgeSegment;
import ch.unibas.cs.dbis.cineast.core.config.QueryConfig;
import ch.unibas.cs.dbis.cineast.core.data.FloatVector;
import ch.unibas.cs.dbis.cineast.core.data.FloatVectorImpl;
import ch.unibas.cs.dbis.cineast.core.data.MultiImage;
import ch.unibas.cs.dbis.cineast.core.data.SegmentContainer;
import ch.unibas.cs.dbis.cineast.core.data.StringDoublePair;
import ch.unibas.cs.dbis.cineast.core.descriptor.EdgeList;
import ch.unibas.cs.dbis.cineast.core.features.abstracts.AbstractFeatureModule;
import georegression.struct.point.Point2D_I32;

public class DominantEdgeGrid8 extends AbstractFeatureModule {

	private static final Logger LOGGER = LogManager.getLogger();
	public DominantEdgeGrid8() {
		super("features.DominantEdgeGrid8", 437f / 4f);
	}

	@Override
	public void processShot(SegmentContainer shot) {
		LOGGER.entry();
		if (!phandler.idExists(shot.getId())) {
			short[][][] edgeHist = new short[16][16][4];
			buildEdgeHist(edgeHist, shot.getMostRepresentativeFrame().getImage());
			short[] dominant = getDominants(edgeHist);
			FloatVector fv = new FloatVectorImpl(dominant);
			persist(shot.getId(), fv);
		}
		LOGGER.exit();
	}


	static void buildEdgeHist(short[][][] edgeHist, MultiImage img){
		List<EdgeContour> contourList = EdgeList.getEdgeList(img);
		for(EdgeContour contour : contourList){
			for(EdgeSegment segment : contour.segments){
				List<Point2D_I32> points = segment.points;
				if(points.size() >= 2){
					Iterator<Point2D_I32> iter = points.iterator();
					Point2D_I32 last = iter.next();
					while (iter.hasNext()) {
						Point2D_I32 current = iter.next();
						int dX = current.x - last.x, dY = current.y - last.y;
						if(dX != 0 || dY != 0){
							int cX = (current.x + last.x) / 2, cY = (current.y + last.y) / 2;
							edgeHist[(int)Math.floor(cX / (img.getWidth() / 7.9999f))][(int)Math.floor(cY / (img.getHeight() / 7.9999f))][(int)(((Math.atan2(dY, dX) + Math.PI) / Math.PI) * 4 % 4)]++;
							
						}
						last = current;
					}
				}
			}
		}
	}
	
	static short[] getDominants(short[][][] edgeHist){
		short[] dominant = new short[8 * 8];
		for(int y = 0; y < 8; ++y){
			for(int x = 0; x < 8; ++x){
				short idx = -10; 
				int max = 0;
				for(short i = 0; i < 4; ++i){
					if(edgeHist[y][x][i] > max){
						idx = i;
						max = edgeHist[y][x][i];
					}
				}
				dominant[8 * y + x] = idx;
			}
		}
		return dominant;
	}

	@Override
	public List<StringDoublePair> getSimilar(SegmentContainer sc, QueryConfig qc) {
		short[][][] edgeHist = new short[8][8][4];
		buildEdgeHist(edgeHist, sc.getMostRepresentativeFrame().getImage());
		short[] dominant = getDominants(edgeHist);
		FloatVector fv = new FloatVectorImpl(dominant);
		return getSimilar(fv.toArray(null), qc);
	}

	@Override
	public List<StringDoublePair> getSimilar(long shotId, QueryConfig qc) {
		// TODO Auto-generated method stub
		return null;
	}
}