package ch.unibas.cs.dbis.cineast.core.descriptor;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import boofcv.abst.feature.associate.AssociateDescription;
import boofcv.abst.feature.associate.ScoreAssociation;
import boofcv.abst.feature.detdesc.DetectDescribePoint;
import boofcv.abst.feature.detect.interest.ConfigFastHessian;
import boofcv.abst.feature.detect.interest.ConfigGeneralDetector;
import boofcv.abst.feature.tracker.PointTrack;
import boofcv.abst.feature.tracker.PointTracker;
import boofcv.abst.sfm.d2.ImageMotion2D;
import boofcv.alg.descriptor.UtilFeature;
import boofcv.alg.filter.derivative.GImageDerivativeOps;
import boofcv.alg.tracker.klt.PkltConfig;
import boofcv.factory.feature.associate.FactoryAssociation;
import boofcv.factory.feature.detdesc.FactoryDetectDescribe;
import boofcv.factory.feature.tracker.FactoryPointTracker;
import boofcv.factory.geo.ConfigRansac;
import boofcv.factory.geo.FactoryMultiViewRobust;
import boofcv.factory.sfm.FactoryMotion2D;
import boofcv.io.image.ConvertBufferedImage;
import boofcv.struct.feature.AssociatedIndex;
import boofcv.struct.feature.BrightFeature;
import boofcv.struct.geo.AssociatedPair;
import boofcv.struct.image.GrayU8;

import org.ddogleg.fitting.modelset.ModelMatcher;
import org.ddogleg.struct.FastQueue;
import ch.unibas.cs.dbis.cineast.core.data.Frame;
import ch.unibas.cs.dbis.cineast.core.data.MultiImage;
import ch.unibas.cs.dbis.cineast.core.data.Pair;
import georegression.struct.homography.Homography2D_F64;
import georegression.struct.point.Point2D_F32;
import georegression.struct.point.Point2D_F64;
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
		int numberOfPointsToTrack = img.getWidth() * img.getHeight() / 100;
		
		PkltConfig config = new PkltConfig();
		config.templateRadius = 3;
		config.pyramidScaling = new int[] { 1, 2, 4, 8 };
		PointTracker<GrayU8> tracker = FactoryPointTracker.klt(config, new ConfigGeneralDetector(numberOfPointsToTrack, 3, 1), GrayU8.class, GImageDerivativeOps.getDerivativeType(GrayU8.class));
		//PointTracker<GrayU8> tracker = FactoryPointTracker.dda_FH_SURF_Stable(new ConfigFastHessian(1, 2, 200, 1, 9, 4, 4), null,null, GrayU8.class);
				
		DetectDescribePoint<GrayU8, BrightFeature> detDesc = FactoryDetectDescribe.surfStable(new ConfigFastHessian(1, 2, 200, 1, 9, 4, 4), null,null, GrayU8.class);
		ScoreAssociation<BrightFeature> scorer = FactoryAssociation.defaultScore(detDesc.getDescriptionType());
		AssociateDescription<BrightFeature> associate = FactoryAssociation.greedy( scorer, 2, true);
		ModelMatcher<Homography2D_F64,AssociatedPair> modelMatcher = FactoryMultiViewRobust.homographyRansac(null,new ConfigRansac(60,3));
		Homography2D_F64 currentToFirst = new Homography2D_F64(1.0,0.0,0.0, 0.0,1.0,0.0, 0.0,0.0,1.0);
		Homography2D_F64 currentToLast = null;
		
		TLongObjectHashMap<LinkedList<Point2D_F32>> paths = new TLongObjectHashMap<LinkedList<Point2D_F32>>();
		TLongIntHashMap trackStartFrames = new TLongIntHashMap();
		ArrayList<PointTrack> tracks = new ArrayList<PointTrack>(numberOfPointsToTrack);
		
		GrayU8 gray = null;
		GrayU8 grayLast = null;
		int frameIdx = 0;
		for(Frame f : frames){
			gray = ConvertBufferedImage.convertFrom(f.getImage().getBufferedImage(), gray);
			
			tracker.process(gray);
			tracks.clear();
			tracker.spawnTracks();
			if (tracker.getActiveTracks(tracks).size() < (numberOfPointsToTrack * 0.9f)){
				tracker.spawnTracks();
			}
			
			if (0 == frameIdx){
				currentToLast = new Homography2D_F64(1.0,0.0,0.0, 0.0,1.0,0.0, 0.0,0.0,1.0);
			}
			else{
				currentToLast = getHomography(gray,grayLast, detDesc, associate, modelMatcher);
			}
			currentToFirst = currentToLast.concat(currentToFirst,null);
			
			for(PointTrack p : tracks){
				if(!trackStartFrames.containsKey(p.featureId)){
					trackStartFrames.put(p.featureId, f.getId());
				}
				LinkedList<Point2D_F32> path = paths.get(p.featureId);
				if(path == null){
					path = new LinkedList<Point2D_F32>();
					paths.put(p.featureId, path);
				}
				Point2D_F64 transP = HomographyPointOps_F64.transform(currentToFirst, p, null);
				path.add(new Point2D_F32((float)(transP.x / gray.width), (float)(transP.y / gray.height)));
			}
			
			frameIdx += 1;
			grayLast = gray.clone();
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
	

	
	private static Homography2D_F64 getHomography(GrayU8 src, GrayU8 dst,
			DetectDescribePoint<GrayU8, BrightFeature> detDesc, 
			AssociateDescription<BrightFeature> associate,
			ModelMatcher<Homography2D_F64,AssociatedPair> modelMatcher){
		
		List<Point2D_F64> pointsSrc = new ArrayList<Point2D_F64>();
		List<Point2D_F64> pointsDst = new ArrayList<Point2D_F64>();
		
		FastQueue<BrightFeature> descsSrc = UtilFeature.createQueue(detDesc,100);
		FastQueue<BrightFeature> descsDst = UtilFeature.createQueue(detDesc,100);
		
		describeImage(src,pointsSrc,descsSrc, detDesc);
		describeImage(dst,pointsDst,descsDst, detDesc);
		
		associate.setSource(descsSrc);
		associate.setDestination(descsDst);
		associate.associate();
		
		FastQueue<AssociatedIndex> matches = associate.getMatches();
		List<AssociatedPair> pairs = new ArrayList<AssociatedPair>();
 
		for( int i = 0; i < matches.size(); i++ ) {
			AssociatedIndex match = matches.get(i);
 
			Point2D_F64 s = pointsSrc.get(match.src);
			Point2D_F64 d = pointsDst.get(match.dst);
 
			pairs.add( new AssociatedPair(s,d,false));
		}
 
		if( !modelMatcher.process(pairs) )
			return new Homography2D_F64(1.0,0.0,0.0, 0.0,1.0,0.0, 0.0,0.0,1.0);
 
		return modelMatcher.getModelParameters().copy();		
	}
	
	private static void describeImage(GrayU8 input, List<Point2D_F64> points, FastQueue<BrightFeature> descs, DetectDescribePoint<GrayU8, BrightFeature> detDesc)
	{
		detDesc.detect(input);
		for( int i = 0; i < detDesc.getNumberOfFeatures(); i++ ) {
			points.add( detDesc.getLocation(i).copy() );
			descs.grow().setTo(detDesc.getDescription(i));
		}
	}
	
}
