package ch.unibas.cs.dbis.cineast.core.test;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import boofcv.struct.ImageRectangle;
import boofcv.struct.geo.AssociatedPair;
import boofcv.struct.image.GrayU8;
import ch.unibas.cs.dbis.cineast.core.data.Frame;
import ch.unibas.cs.dbis.cineast.core.data.Pair;
import ch.unibas.cs.dbis.cineast.core.data.Shot;
import ch.unibas.cs.dbis.cineast.core.decode.video.JLibAVVideoDecoder;
import ch.unibas.cs.dbis.cineast.core.decode.video.VideoDecoder;
import ch.unibas.cs.dbis.cineast.core.descriptor.PathList;
import ch.unibas.cs.dbis.cineast.core.util.MotionHistogramUtil;
import ch.unibas.cs.dbis.cineast.core.util.VisualizationUtil;
import ch.unibas.cs.dbis.cineast.core.util.MaskGenerator;
import georegression.struct.point.Point2D_F32;


public class TestMotion {

	private static final Logger LOGGER = LogManager.getLogger();
	
	public static void main(String[] args) {
		if(args.length < 1){
			LOGGER.error("no parameter");
		}
		
		long startTime = System.currentTimeMillis();
		
		File videoFile = new File(args[0]);
		VideoDecoder videoDecoder = new JLibAVVideoDecoder(videoFile);
		
		Shot shot = decodeVideo(videoDecoder);
		long decodeTime = System.currentTimeMillis();
		LOGGER.info("finished decode in {}", formatTime(decodeTime - startTime));
		
		LinkedList<Pair<Integer,ArrayList<AssociatedPair>>> allPaths = PathList.getDensePaths(shot.getFrames());
		long denseTrackingTime = System.currentTimeMillis();
		LOGGER.info("finished denseTracking in {}", formatTime(denseTrackingTime - decodeTime));
		
		ArrayList<Pair<Integer, LinkedList<Point2D_F32>>> foregroundPaths = new ArrayList<Pair<Integer, LinkedList<Point2D_F32>>>();
		ArrayList<Pair<Integer, LinkedList<Point2D_F32>>> backgroundPaths = new ArrayList<Pair<Integer, LinkedList<Point2D_F32>>>();
		
		PathList.separateFgBgPaths(shot.getFrames(), allPaths, foregroundPaths, backgroundPaths);
		long separateTime = System.currentTimeMillis();
		LOGGER.info("finished separate bg and fg in {}", formatTime(separateTime - denseTrackingTime));
		
		Pair<List<Double>, ArrayList<ArrayList<Float>>> foregroundPair = MotionHistogramUtil.getSubDivHist(1, foregroundPaths);
		Pair<List<Double>, ArrayList<ArrayList<Float>>> backgroundPair = MotionHistogramUtil.getSubDivHist(1, backgroundPaths);
		System.out.println(foregroundPair.first.get(0));
		System.out.println(foregroundPair.first.get(0)/shot.getFrames().size());
		System.out.println(foregroundPair.second);
		System.out.println(backgroundPair.first.get(0));
		System.out.println(backgroundPair.first.get(0)/shot.getFrames().size());
		System.out.println(backgroundPair.second);
		long getHistTime = System.currentTimeMillis();
		LOGGER.info("finished getHist bg and fg in {}", formatTime(getHistTime - separateTime));
		
		ArrayList<GrayU8> masks = MaskGenerator.getFgMasksByNN(shot.getFrames(), foregroundPaths, backgroundPaths);
		long getFgMasksTime = System.currentTimeMillis();
		LOGGER.info("finished getFgMasksByNN in {}", formatTime(getFgMasksTime - getHistTime));
		
		ArrayList<GrayU8> masks2 = MaskGenerator.getFgMasksByFilter(shot.getFrames(), foregroundPaths, backgroundPaths);
		long getFgMasksTime2 = System.currentTimeMillis();
		LOGGER.info("finished getFgMasksByFilter in {}", formatTime(getFgMasksTime2 - getFgMasksTime));
		
		ArrayList<ImageRectangle> rects = MaskGenerator.getFgBoundingBox(shot.getFrames(), foregroundPaths, backgroundPaths);
		long getFgBoundingBoxTime = System.currentTimeMillis();
		LOGGER.info("finished getFgBoundingBox in {}", formatTime(getFgBoundingBoxTime - getFgMasksTime2));
		
		VisualizationUtil.visualize(shot.getFrames(),foregroundPaths,backgroundPaths,masks,masks2,rects);
		System.exit(0);
	}
	
	private static Shot decodeVideo(VideoDecoder videoDecoder){
		Shot shot = new Shot(1, videoDecoder.getTotalFrameCount());
		Frame f = null;
		while(true){
			f = videoDecoder.getFrame();
			if (f == null){
				break;
			}
			shot.addFrame(f);
		}
		return shot;
	}
	
	private static String formatTime(long ms){
		return String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(ms),
	            TimeUnit.MILLISECONDS.toMinutes(ms) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(ms)),
	            TimeUnit.MILLISECONDS.toSeconds(ms) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(ms)));
	}
}