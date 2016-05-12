package ch.unibas.cs.dbis.cineast.core.descriptor;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import boofcv.abst.feature.detect.interest.ConfigGeneralDetector;
import boofcv.abst.feature.tracker.PointTrack;
import boofcv.abst.feature.tracker.PointTracker;
import boofcv.abst.sfm.d2.ImageMotion2D;
import boofcv.alg.filter.derivative.GImageDerivativeOps;
import boofcv.alg.tracker.klt.PkltConfig;
import boofcv.factory.feature.tracker.FactoryPointTracker;
import boofcv.factory.sfm.FactoryMotion2D;
import boofcv.io.image.ConvertBufferedImage;
import boofcv.struct.image.GrayU8;
import ch.unibas.cs.dbis.cineast.core.data.Frame;
import ch.unibas.cs.dbis.cineast.core.data.MultiImage;
import ch.unibas.cs.dbis.cineast.core.data.Pair;
import georegression.struct.homography.Homography2D_F64;
import georegression.struct.point.Point2D_F32;
import georegression.transform.homography.HomographyPointOps_F64;
import gnu.trove.map.hash.TLongIntHashMap;
import gnu.trove.map.hash.TLongObjectHashMap;

public class PathList {

private PathList(){}
	
	public static ArrayList<Pair<Integer, LinkedList<Point2D_F32>>> getPaths(List<Frame> frames){
		if(frames.size() < 2){
			return new ArrayList<Pair<Integer, LinkedList<Point2D_F32>>>(1);
		}
		
		MultiImage img = frames.get(0).getImage();
		int numberOfPointsToTrack = img.getWidth() * img.getHeight() / 10000;
		
		PkltConfig config = new PkltConfig();
		config.templateRadius = 3;
		config.pyramidScaling = new int[] { 1, 2, 4, 8 };
		PointTracker<GrayU8> tracker = FactoryPointTracker.klt(config, new ConfigGeneralDetector(numberOfPointsToTrack, 3, 1), GrayU8.class, GImageDerivativeOps.getDerivativeType(GrayU8.class));
		
		PointTracker<GrayU8> trackerForMotion2D = FactoryPointTracker.klt(config, new ConfigGeneralDetector(numberOfPointsToTrack, 3, 1), GrayU8.class, GImageDerivativeOps.getDerivativeType(GrayU8.class));
		ImageMotion2D<GrayU8,Homography2D_F64> motion2D = FactoryMotion2D.createMotion2D(50,3,2,0,0.6,0.5,false,trackerForMotion2D,new Homography2D_F64());
		Homography2D_F64 curToFirst = new Homography2D_F64(1.0,0.0,0.0,
															0.0,1.0,0.0,
															0.0,0.0,1.0);
		Homography2D_F64 curToPrev = new Homography2D_F64();
		PointTrack transP = new PointTrack();
		
		TLongObjectHashMap<LinkedList<Point2D_F32>> paths = new TLongObjectHashMap<LinkedList<Point2D_F32>>();
		TLongIntHashMap trackStartFrames = new TLongIntHashMap();
		ArrayList<PointTrack> tracks = new ArrayList<PointTrack>(numberOfPointsToTrack);
		GrayU8 gray = null;
		for(Frame f : frames){
			gray = ConvertBufferedImage.convertFrom(f.getImage().getBufferedImage(), gray);
			
			motion2D.process(gray);
			motion2D.getFirstToCurrent().invert(curToPrev);
			Homography2D_F64 curToFirstOld = curToFirst.copy();
			curToFirstOld.concat(curToPrev, curToFirst);
			motion2D.setToFirst();
			
			tracker.process(gray);
			tracks.clear();
			tracker.spawnTracks();
			if (tracker.getActiveTracks(tracks).size() < (numberOfPointsToTrack * 0.9f)){
				tracker.spawnTracks();
			}
			
			for(PointTrack p : tracks){
				if(!trackStartFrames.containsKey(p.featureId)){
					trackStartFrames.put(p.featureId, f.getId());
				}
				LinkedList<Point2D_F32> path = paths.get(p.featureId);
				if(path == null){
					path = new LinkedList<Point2D_F32>();
					paths.put(p.featureId, path);
				}
				HomographyPointOps_F64.transform(curToFirst, p, transP);
				path.add(new Point2D_F32((float)(transP.x / gray.width), (float)(transP.y / gray.height)));
			}
		}
		
		long[] keys = paths.keys();
		ArrayList<Pair<Integer, LinkedList<Point2D_F32>>> pathList = new ArrayList<Pair<Integer, LinkedList<Point2D_F32>>>(keys.length);
		for(long key : keys){
			if(paths.get(key).size() > 1){
				pathList.add(new Pair<>(trackStartFrames.get(key), paths.get(key)));
			}
		}
		
		return pathList;
	}
	
}
