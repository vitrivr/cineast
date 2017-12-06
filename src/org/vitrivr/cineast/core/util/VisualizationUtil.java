package org.vitrivr.cineast.core.util;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import org.vitrivr.cineast.core.data.Pair;
import org.vitrivr.cineast.core.data.frames.VideoFrame;
import org.vitrivr.cineast.core.descriptor.PathList;

import boofcv.alg.misc.PixelMath;
import boofcv.gui.feature.VisualizeFeatures;
import boofcv.gui.image.ImagePanel;
import boofcv.gui.image.ShowImages;
import boofcv.io.image.ConvertBufferedImage;
import boofcv.struct.ImageRectangle;
import boofcv.struct.image.GrayU8;
import georegression.struct.point.Point2D_F32;

public class VisualizationUtil {
	
	private VisualizationUtil(){}
	
	public static ImagePanel gui = new ImagePanel();
	
	public static void visualize(List<VideoFrame> videoFrames,
			ArrayList<Pair<Integer, LinkedList<Point2D_F32>>> foregroundPaths,
			ArrayList<Pair<Integer, LinkedList<Point2D_F32>>> backgroundPaths,
			ArrayList<GrayU8> masks,ArrayList<GrayU8> masks2,
			ArrayList<ImageRectangle> rects) {
		BufferedImage bufferedImage = null;
		BufferedImage track = null;
		BufferedImage mask = null;

		track = videoFrames.get(0).getImage().getBufferedImage();
		int width = track.getWidth();
		int height = track.getHeight();
		int imageType = track.getType();
		bufferedImage = new BufferedImage(width * 2, height * 2, imageType);
		gui.setPreferredSize(new Dimension(width * 2, height * 2));
		ShowImages.showWindow(gui, "visualize", true);

		ListIterator<Pair<Integer, LinkedList<Point2D_F32>>> fgPathItor = foregroundPaths.listIterator();
		ListIterator<Pair<Integer, LinkedList<Point2D_F32>>> bgPathItor = backgroundPaths.listIterator();

		ListIterator<GrayU8> maskIter = masks.listIterator();
		ListIterator<GrayU8> maskIter2 = masks2.listIterator();
		
		ListIterator<ImageRectangle> rectIter = rects.listIterator();

		int cnt = 0;
		for (int frameIdx = 0; frameIdx < videoFrames.size(); ++frameIdx) {
			if (cnt >= PathList.frameInterval) {
				cnt = 0;
				continue;
			}
			cnt += 1;

			VideoFrame videoFrame = videoFrames.get(frameIdx);
			track = videoFrame.getImage().getBufferedImage();
			Graphics2D g2 = bufferedImage.createGraphics();
			g2.drawImage(track, null, 0, 0);
			while (fgPathItor.hasNext()) {
				Pair<Integer, LinkedList<Point2D_F32>> pair = fgPathItor.next();
				if (pair.first > frameIdx) {
          break;
        }
				Point2D_F32 p1 = pair.second.getFirst();
				Point2D_F32 p2 = pair.second.getLast();
				VisualizeFeatures.drawPoint(g2, (int) (p1.x * width), (int) (p1.y * height), 2, Color.red);
				g2.drawLine((int) (p1.x * width), (int) (p1.y * height), (int) (p2.x * width), (int) (p2.y * height));
			}
			while (bgPathItor.hasNext()) {
				Pair<Integer, LinkedList<Point2D_F32>> pair = bgPathItor.next();
				if (pair.first > frameIdx) {
          break;
        }
				Point2D_F32 p1 = pair.second.getFirst();
				Point2D_F32 p2 = pair.second.getLast();
				VisualizeFeatures.drawPoint(g2, (int) (p1.x * width), (int) (p1.y * height), 2, Color.green);
				g2.drawLine((int) (p1.x * width), (int) (p1.y * height), (int) (p2.x * width), (int) (p2.y * height));
			}

			if (maskIter.hasNext()) {
				GrayU8 m = maskIter.next();
				PixelMath.multiply(m,255,m);
				mask = ConvertBufferedImage.convertTo(m, null);
				g2.drawImage(mask, null, 0, height);
			}
			
			if (maskIter2.hasNext()) {
				GrayU8 m = maskIter2.next();
				PixelMath.multiply(m,255,m);
				mask = ConvertBufferedImage.convertTo(m, null);
				g2.drawImage(mask, null, width, height);
			}

			if (rectIter.hasNext()) {
				g2.drawImage(track, null, width, 0);
				ImageRectangle rect = rectIter.next();
				g2.drawRect(rect.getX0() + width, rect.getY0(), rect.getWidth(), rect.getHeight());
			}
			
			gui.setImage(bufferedImage);
			gui.repaint();

			try {
				Thread.sleep(250);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
	}
	
}
