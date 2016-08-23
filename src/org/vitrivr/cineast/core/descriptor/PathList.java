package org.vitrivr.cineast.core.descriptor;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.vitrivr.cineast.core.data.Frame;
import org.vitrivr.cineast.core.data.MultiImage;
import org.vitrivr.cineast.core.data.Pair;

import boofcv.abst.feature.detect.interest.ConfigGeneralDetector;
import boofcv.abst.feature.tracker.PointTrack;
import boofcv.abst.feature.tracker.PointTracker;
import boofcv.alg.filter.derivative.GImageDerivativeOps;
import boofcv.alg.tracker.klt.PkltConfig;
import boofcv.factory.feature.tracker.FactoryPointTracker;
import boofcv.io.image.ConvertBufferedImage;
import boofcv.struct.image.GrayU8;
import georegression.struct.point.Point2D_F32;
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
		
		TLongObjectHashMap<LinkedList<Point2D_F32>> paths = new TLongObjectHashMap<LinkedList<Point2D_F32>>();
		TLongIntHashMap trackStartFrames = new TLongIntHashMap();
		ArrayList<PointTrack> tracks = new ArrayList<PointTrack>(numberOfPointsToTrack);
		GrayU8 gray = null;
		for(Frame f : frames){
			gray = ConvertBufferedImage.convertFrom(f.getImage().getBufferedImage(), gray);
			
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
				path.add(new Point2D_F32((float)(p.x / gray.width), (float)(p.y / gray.height)));
			}
		}
		
		long[] keys = paths.keys();
		ArrayList<Pair<Integer, LinkedList<Point2D_F32>>> pathList = new ArrayList<Pair<Integer, LinkedList<Point2D_F32>>>(keys.length);
		for(long key : keys){
			pathList.add(new Pair<>(trackStartFrames.get(key), paths.get(key)));
		}
		
		return pathList;
	}
	
}
