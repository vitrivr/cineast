package ch.unibas.cs.dbis.cineast.core.descriptor;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import georegression.struct.homography.Homography2D_F64;
import georegression.struct.point.Point2D_F32;
import georegression.struct.point.Point2D_F64;
import georegression.transform.homography.HomographyPointOps_F64;

import boofcv.abst.filter.derivative.ImageGradient;
import boofcv.alg.tracker.klt.KltConfig;
import boofcv.alg.tracker.klt.KltTrackFault;
import boofcv.alg.tracker.klt.PkltConfig;
import boofcv.alg.tracker.klt.PyramidKltFeature;
import boofcv.alg.tracker.klt.PyramidKltTracker;
import boofcv.alg.transform.pyramid.PyramidOps;
import boofcv.factory.filter.derivative.FactoryDerivative;
import boofcv.factory.geo.ConfigRansac;
import boofcv.factory.geo.FactoryMultiViewRobust;
import boofcv.factory.tracker.FactoryTrackerAlg;
import boofcv.factory.transform.pyramid.FactoryPyramid;
import boofcv.io.image.ConvertBufferedImage;
import boofcv.struct.geo.AssociatedPair;
import boofcv.struct.image.GrayS16;
import boofcv.struct.image.GrayU8;
import boofcv.struct.pyramid.PyramidDiscrete;

import org.ddogleg.fitting.modelset.ModelMatcher;

import ch.unibas.cs.dbis.cineast.core.data.Frame;
import ch.unibas.cs.dbis.cineast.core.data.Pair;

public class PathList {

	private PathList(){}
	
	public static void separateFgBgPaths(List<Frame> frames,
										 LinkedList<Pair<Integer,ArrayList<AssociatedPair>>> allPaths,
										 List<Pair<Integer, LinkedList<Point2D_F32>>> forgroundPaths,
										 List<Pair<Integer, LinkedList<Point2D_F32>>> backgroundPaths){	
		ModelMatcher<Homography2D_F64,AssociatedPair> robustF = FactoryMultiViewRobust.homographyRansac(null, new ConfigRansac(200,1));
		if (allPaths == null || frames == null || frames.isEmpty()){
			return;
		}
		
		int width = frames.get(0).getImage().getWidth();
		int height = frames.get(0).getImage().getHeight();
		
		for(Pair<Integer,ArrayList<AssociatedPair>> pair : allPaths){
			List<AssociatedPair> inliers = new ArrayList<AssociatedPair>();
			List<AssociatedPair> outliers = new ArrayList<AssociatedPair>();
			
			int frameIdx = pair.first;
			ArrayList<AssociatedPair> matches = pair.second;
			
			Homography2D_F64 curToPrev = new Homography2D_F64(1.0,0.0,0.0, 0.0,1.0,0.0, 0.0,0.0,1.0);
			if(robustF.process(matches)){
				curToPrev = robustF.getModelParameters();
				inliers.addAll(robustF.getMatchSet());
				for (int i = 0,j = 0; i < matches.size(); ++i){
					if (i == robustF.getInputIndex(j)){
						if ( j < inliers.size()-1){
							++j;	
						}
					}
					else{
						outliers.add(matches.get(i));
					}
				}
			}
			else{
				curToPrev = new Homography2D_F64(1.0,0.0,0.0, 0.0,1.0,0.0, 0.0,0.0,1.0);
				inliers.addAll(matches);
			}
			
			for (AssociatedPair p : inliers){
				LinkedList<Point2D_F32> path = new LinkedList<Point2D_F32>();
				path.add(new Point2D_F32((float)p.p1.x/(float)width,(float)p.p1.y/(float)height));
				path.add(new Point2D_F32((float)p.p2.x/(float)width,(float)p.p2.y/(float)height));
				backgroundPaths.add(new Pair<Integer, LinkedList<Point2D_F32>>(frameIdx,path));
			}
			
			for (AssociatedPair p : outliers){
				p.p2 = HomographyPointOps_F64.transform(curToPrev, p.p2, p.p2);
				LinkedList<Point2D_F32> path = new LinkedList<Point2D_F32>();
				path.add(new Point2D_F32((float)p.p1.x/(float)width,(float)p.p1.y/(float)height));
				path.add(new Point2D_F32((float)p.p2.x/(float)width,(float)p.p2.y/(float)height));
				forgroundPaths.add(new Pair<Integer, LinkedList<Point2D_F32>>(frameIdx,path));
			}
		}
	}
	
	public static LinkedList<Pair<Integer,ArrayList<AssociatedPair>>> getDensePaths(List<Frame> frames){
		if(frames.size() < 2){
			return null;
		}
		int samplingInterval = 10;
		PkltConfig configKlt = new PkltConfig(3, new int[] { 1, 2, 4 });
		configKlt.config.minDeterminant = 0.001f;
		ImageGradient<GrayU8, GrayS16> gradient = FactoryDerivative.sobel(GrayU8.class, GrayS16.class);
		PyramidDiscrete<GrayU8> pyramid = FactoryPyramid.discreteGaussian(configKlt.pyramidScaling,-1,2,true,GrayU8.class);
		PyramidKltTracker<GrayU8, GrayS16> tracker = FactoryTrackerAlg.kltPyramid(configKlt.config, GrayU8.class, null);
		
		GrayS16[] derivX = null;
		GrayS16[] derivY = null;
		
		LinkedList<PyramidKltFeature> tracks = new LinkedList<PyramidKltFeature>();
		LinkedList<Pair<Integer,ArrayList<AssociatedPair>>> paths = new LinkedList<Pair<Integer,ArrayList<AssociatedPair>>>();
		
		GrayU8 gray = null;
		int frameIdx = 0;
		for (Frame frame : frames){
			gray = ConvertBufferedImage.convertFrom(frame.getImage().getBufferedImage(), gray);		
			ArrayList<AssociatedPair> tracksPairs = new ArrayList<AssociatedPair>();
			
			if (frameIdx == 0){
				tracks = denseSampling(gray, derivX, derivY, samplingInterval, configKlt, gradient, pyramid, tracker);
			}
			else{
				tracks = tracking(gray, derivX, derivY, tracks, tracksPairs, gradient, pyramid, tracker);
				tracks = denseSampling(gray, derivX, derivY, samplingInterval, configKlt, gradient, pyramid, tracker);
			}
			
			paths.add(new Pair<Integer,ArrayList<AssociatedPair>>(frameIdx,tracksPairs));
			
			++frameIdx;
		}
		return paths;
	}
	
	public static LinkedList<PyramidKltFeature> denseSampling( GrayU8 image, GrayS16[] derivX, GrayS16[] derivY,
															int samplingInterval,
															PkltConfig configKlt,
															ImageGradient<GrayU8, GrayS16> gradient,
															PyramidDiscrete<GrayU8> pyramid,
															PyramidKltTracker<GrayU8, GrayS16> tracker){
		LinkedList<PyramidKltFeature> tracks = new LinkedList<PyramidKltFeature>();
		
		pyramid.process(image);
		derivX = declareOutput(pyramid, derivX);
		derivY = declareOutput(pyramid, derivY);
		PyramidOps.gradient(pyramid, gradient, derivX, derivY);
		tracker.setImage(pyramid,derivX,derivY);
		
		for( int y = 0; y < image.height; y+= samplingInterval ) {
			for( int x = 0; x < image.width; x+= samplingInterval ) {
				PyramidKltFeature t = new PyramidKltFeature(configKlt.pyramidScaling.length,configKlt.templateRadius);
				t.setPosition(x,y);
				tracker.setDescription(t);
				tracks.add(t);
			}
		}
		return tracks;
	}
	
	public static LinkedList<PyramidKltFeature> tracking( GrayU8 image, GrayS16[] derivX, GrayS16[] derivY,
													LinkedList<PyramidKltFeature> tracks,
													ArrayList<AssociatedPair> tracksPairs,
													ImageGradient<GrayU8, GrayS16> gradient,
													PyramidDiscrete<GrayU8> pyramid,
													PyramidKltTracker<GrayU8, GrayS16> tracker
													){
		pyramid.process(image);
		derivX = declareOutput(pyramid, derivX);
		derivY = declareOutput(pyramid, derivY);
		PyramidOps.gradient(pyramid, gradient, derivX, derivY);
		tracker.setImage(pyramid,derivX,derivY);
		
		ListIterator<PyramidKltFeature> listIterator = tracks.listIterator();
		while( listIterator.hasNext() ) {
			PyramidKltFeature track = listIterator.next();
			Point2D_F64 prevP = new Point2D_F64(track.x,track.y);
			KltTrackFault ret = tracker.track(track);
			boolean success = false;
			if( ret == KltTrackFault.SUCCESS ) {
				if( image.isInBounds((int)track.x,(int)track.y) && tracker.setDescription(track) ) {
					tracksPairs.add(new AssociatedPair(prevP,new Point2D_F64(track.x,track.y)));
					success = true;
				}
			}
			if( !success ) {
				listIterator.remove();
			}
		}
		
		return tracks;
	}
	
	public static GrayS16[] declareOutput(PyramidDiscrete<GrayU8> pyramid,GrayS16[] deriv) {
		if( deriv == null ) {
			deriv = PyramidOps.declareOutput(pyramid, GrayS16.class);
		}
		else if( deriv[0].width != pyramid.getLayer(0).width ||
				deriv[0].height != pyramid.getLayer(0).height )
		{
			PyramidOps.reshapeOutput(pyramid,deriv);
		}
		return deriv;
	}	
}
