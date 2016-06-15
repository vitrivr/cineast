package ch.unibas.cs.dbis.cineast.core.test;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import boofcv.gui.feature.VisualizeFeatures;
import boofcv.gui.image.ImagePanel;
import boofcv.gui.image.ShowImages;
import boofcv.struct.geo.AssociatedPair;
import ch.unibas.cs.dbis.cineast.core.data.Frame;
import ch.unibas.cs.dbis.cineast.core.data.Pair;
import ch.unibas.cs.dbis.cineast.core.data.Shot;
import ch.unibas.cs.dbis.cineast.core.decode.video.JLibAVVideoDecoder;
import ch.unibas.cs.dbis.cineast.core.decode.video.VideoDecoder;
import ch.unibas.cs.dbis.cineast.core.descriptor.PathList;
import georegression.struct.point.Point2D_F32;


public class TestMotion {

	private static final Logger LOGGER = LogManager.getLogger();
	
	public static ImagePanel gui = new ImagePanel();
	
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
		
		ArrayList<Pair<Integer, LinkedList<Point2D_F32>>> forgroundPaths = new ArrayList<Pair<Integer, LinkedList<Point2D_F32>>>();
		ArrayList<Pair<Integer, LinkedList<Point2D_F32>>> backgroundPaths = new ArrayList<Pair<Integer, LinkedList<Point2D_F32>>>();
		
		PathList.separateFgBgPaths(allPaths, forgroundPaths, backgroundPaths);
		long separateTime = System.currentTimeMillis();
		LOGGER.info("finished separate bg and fg in {}", formatTime(separateTime - denseTrackingTime));
		
		Pair<List<Double>, ArrayList<ArrayList<Float>>> foregroundPair = getSubDivHist(1, forgroundPaths);
		Pair<List<Double>, ArrayList<ArrayList<Float>>> backgroundPair = getSubDivHist(1, backgroundPaths);
		System.out.println(foregroundPair.second);
		System.out.println(backgroundPair.second);
		long getHistTime = System.currentTimeMillis();
		LOGGER.info("finished getHist bg and fg in {}", formatTime(getHistTime - separateTime));
		
		visualize(shot.getFrames(),forgroundPaths,backgroundPaths);
		
	}
	
	private static Pair<List<Double>, ArrayList<ArrayList<Float>>> getSubDivHist(
			int subdiv, List<Pair<Integer, LinkedList<Point2D_F32>>> list) {

		double[] sums = new double[subdiv * subdiv];
		float[][] hists = new float[subdiv * subdiv][8];

		for (Pair<Integer, LinkedList<Point2D_F32>> pair : list) {
			LinkedList<Point2D_F32> path = pair.second;
			if (path.size() > 1) {
				Iterator<Point2D_F32> iter = path.iterator();
				Point2D_F32 last = iter.next();
				while (iter.hasNext()) {
					Point2D_F32 current = iter.next();
					double dx = current.x - last.x, dy = current.y - last.y;
					int idx = ((int) Math.floor(4 * Math.atan2(dy, dx)
							/ Math.PI) + 4) % 8;
					double len = Math.sqrt(dx * dx + dy * dy);
					hists[getidx(subdiv, last.x, last.y)][idx] += len;
					last = current;
				}
			}
		}

		for (int i = 0; i < sums.length; ++i) {
			float[] hist = hists[i];
			double sum = 0;
			for (int j = 0; j < hist.length; ++j) {
				sum += hist[j];
			}
			if (sum > 0) {
				for (int j = 0; j < hist.length; ++j) {
					hist[j] /= sum;
				}
				hists[i] = hist;
			}
			sums[i] = sum;
		}

		ArrayList<Double> sumList = new ArrayList<Double>(sums.length);
		for (double d : sums) {
			sumList.add(d);
		}

		ArrayList<ArrayList<Float>> histList = new ArrayList<ArrayList<Float>>(
				hists.length);
		for (float[] hist : hists) {
			ArrayList<Float> h = new ArrayList<Float>(8);
			for (float f : hist) {
				h.add(f);
			}
			histList.add(h);
		}

		return new Pair<List<Double>, ArrayList<ArrayList<Float>>>(sumList,
				histList);
	}
	
	private static int getidx(int subdiv, float x, float y) {
		int ix = (int) Math.floor(subdiv * x), iy = (int) Math.floor(subdiv * y);
		ix = Math.max(Math.min(ix, subdiv - 1), 0);
		iy = Math.max(Math.min(iy, subdiv - 1), 0);

		return ix * subdiv + iy;
	}
	
	private static void visualize(List<Frame> frames,
									ArrayList<Pair<Integer, LinkedList<Point2D_F32>>> forgroundPaths,
									ArrayList<Pair<Integer, LinkedList<Point2D_F32>>> backgroundPaths){
		BufferedImage  bufferedImage = null;
		bufferedImage = frames.get(0).getImage().getBufferedImage();
		gui.setPreferredSize(new Dimension(bufferedImage.getWidth(),bufferedImage.getHeight()));
		ShowImages.showWindow(gui,"visualize", true);
		
		ListIterator<Pair<Integer, LinkedList<Point2D_F32>>> fgPathItor = forgroundPaths.listIterator();
		ListIterator<Pair<Integer, LinkedList<Point2D_F32>>> bgPathItor = backgroundPaths.listIterator();
		
		for (int frameIdx = 0; frameIdx < frames.size(); ++frameIdx){
			Frame frame = frames.get(frameIdx);
			bufferedImage = frame.getImage().getBufferedImage();
			Graphics2D g2 = bufferedImage.createGraphics();
			
			while(fgPathItor.hasNext()){
				Pair<Integer, LinkedList<Point2D_F32>> pair = fgPathItor.next();
				if(pair.first > frameIdx)
					break;
				Point2D_F32 p = pair.second.getFirst();
				VisualizeFeatures.drawPoint(g2, (int)p.x, (int)p.y, 2, Color.red);
			}
			while(bgPathItor.hasNext()){
				Pair<Integer, LinkedList<Point2D_F32>> pair = bgPathItor.next();
				if(pair.first > frameIdx)
					break;
				Point2D_F32 p = pair.second.getFirst();
				VisualizeFeatures.drawPoint(g2, (int)p.x, (int)p.y, 2, Color.green);
			}
			
			gui.setBufferedImage(bufferedImage);
			gui.repaint();
		}
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