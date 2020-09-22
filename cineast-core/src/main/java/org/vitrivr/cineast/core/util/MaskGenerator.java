package org.vitrivr.cineast.core.util;

import boofcv.abst.distort.FDistort;
import boofcv.alg.filter.binary.BinaryImageOps;
import boofcv.alg.filter.binary.Contour;
import boofcv.alg.filter.binary.ThresholdImageOps;
import boofcv.alg.filter.blur.GBlurImageOps;
import boofcv.alg.misc.PixelMath;
import boofcv.factory.filter.kernel.FactoryKernelGaussian;
import boofcv.gui.image.ShowImages;
import boofcv.io.image.ConvertBufferedImage;
import boofcv.struct.ConnectRule;
import boofcv.struct.ImageRectangle;
import boofcv.struct.convolve.Kernel1D_F32;
import boofcv.struct.image.GrayU8;
import georegression.struct.point.Point2D_F32;
import georegression.struct.point.Point2D_I32;
import org.ddogleg.nn.FactoryNearestNeighbor;
import org.ddogleg.nn.NearestNeighbor;
import org.ddogleg.nn.NnData;
import org.ddogleg.struct.FastQueue;
import org.vitrivr.cineast.core.data.Pair;
import org.vitrivr.cineast.core.data.frames.VideoFrame;
import org.vitrivr.cineast.core.descriptor.PathList;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

public class MaskGenerator {

	private MaskGenerator(){}

	public static ArrayList<Pair<Long,ArrayList<Float>>> getNormalizedBbox(List<VideoFrame> videoFrames,
			List<Pair<Integer, LinkedList<Point2D_F32>>> foregroundPaths,
			List<Pair<Integer, LinkedList<Point2D_F32>>> backgroundPaths){
		
		if (videoFrames == null || videoFrames.isEmpty() || foregroundPaths == null) {
			return null;
		}
		
		ArrayList<Pair<Long,ArrayList<Float>>> bboxWithIdx = new ArrayList<Pair<Long,ArrayList<Float>>>();
		
		ArrayList<ImageRectangle> rects = MaskGenerator.getFgBoundingBox(videoFrames, foregroundPaths, backgroundPaths);
		
		int width = videoFrames.get(0).getImage().getWidth();
		int height = videoFrames.get(0).getImage().getHeight();
		
		long frameIdx = 0;
		
		for(ImageRectangle rect : rects){
			ArrayList<Float> bbox = normalize(rect, width, height);
			bboxWithIdx.add(new Pair<Long,ArrayList<Float>>(frameIdx, bbox));
			frameIdx += PathList.frameInterval;
		}
		
		return bboxWithIdx;
	}
	
	public static ArrayList<Float> normalize(ImageRectangle rect, int width, int height){
		ArrayList<Float> norm = new ArrayList<Float>();
		norm.add((float)rect.getX0() / (float)width);
		norm.add((float)rect.getY0() / (float)height);
		norm.add((float)rect.getWidth() / (float)width);
		norm.add((float)rect.getHeight() / (float)height);
		return norm;
	}
	
	public static ArrayList<ImageRectangle> getFgBoundingBox(List<VideoFrame> videoFrames,
			List<Pair<Integer, LinkedList<Point2D_F32>>> foregroundPaths,
			List<Pair<Integer, LinkedList<Point2D_F32>>> backgroundPaths){
		
		if (videoFrames == null || videoFrames.isEmpty() || foregroundPaths == null) {
			return null;
		}
		
		ArrayList<ImageRectangle> rects = new ArrayList<ImageRectangle>();
		ArrayList<GrayU8> masks = getFgMasksByNN(videoFrames, foregroundPaths, backgroundPaths);
		
		for(GrayU8 mask : masks){
			ImageRectangle rect = getLargestBoundingBox(mask);
			rects.add(rect);			
		}
		
		return rects;
	}
	
	public static ImageRectangle getLargestBoundingBox(GrayU8 mask){		
		
		List<ImageRectangle> rects =  getBoundingBox(mask);
		ImageRectangle largest = new ImageRectangle(0,0,0,0);
		for(ImageRectangle rect : rects){
			if(rect.getWidth() * rect.getHeight() > largest.getWidth() * largest.getHeight()){
				largest = rect;
			}
		}
		return largest;
	}
	
	public static List<ImageRectangle> getBoundingBox(GrayU8 mask){
		
		List<Contour> contours =  BinaryImageOps.contour(mask,ConnectRule.FOUR,null);
		List<ImageRectangle> rects = new ArrayList<ImageRectangle>();
		
		for(Contour contour : contours){
			ImageRectangle rect = new ImageRectangle(mask.width,mask.height,0,0);
			for (Point2D_I32 p : contour.external){
				if (p.x < rect.x0) {
          rect.x0 = p.x;
        }
				if (p.y < rect.y0) {
          rect.y0 = p.y;
        }
				if (p.x > rect.x1) {
          rect.x1 = p.x;
        }
				if (p.y > rect.y1) {
          rect.y1 = p.y;
        }
			}
			rects.add(rect);
		}
		
		return rects;
	}
	
	public static ArrayList<GrayU8> getFgMasksByFilter(List<VideoFrame> videoFrames,
			List<Pair<Integer, LinkedList<Point2D_F32>>> foregroundPaths,
			List<Pair<Integer, LinkedList<Point2D_F32>>> backgroundPaths) {

		if (videoFrames == null || videoFrames.isEmpty() || foregroundPaths == null) {
			return null;
		}
		
		ArrayList<GrayU8> masksScaled = generateScaledMasksFromPath(videoFrames, foregroundPaths);

		ArrayList<GrayU8> masksScaledSmoothed1 = createNewMasks(masksScaled);
		smoothMasks(masksScaled, masksScaledSmoothed1, 4, 2, 64, 26);
		
		ArrayList<GrayU8> masks = scaleUpMasks(masksScaledSmoothed1, videoFrames.get(0).getImage().getBufferedImage().getWidth(), videoFrames.get(0).getImage().getBufferedImage().getHeight());
		
		ArrayList<GrayU8> masksSmoothed1 = createNewMasks(masks);
		smoothMasks(masks, masksSmoothed1, 5, 2, 64, 10);
		ArrayList<GrayU8> masksSmoothed2 = createNewMasks(masks);
		smoothMasks(masksSmoothed1, masksSmoothed2, 5, 2, 64, 10);
		
		//multiply3D(masksSmoothed2,masksSmoothed2,255);
		
		return masksSmoothed2;
	}
	
	public static ArrayList<GrayU8> getFgMasksByNN(List<VideoFrame> videoFrames,
			List<Pair<Integer, LinkedList<Point2D_F32>>> foregroundPaths,
			List<Pair<Integer, LinkedList<Point2D_F32>>> backgroundPaths) {

		if (videoFrames == null || videoFrames.isEmpty() || foregroundPaths == null) {
			return null;
		}

		ArrayList<GrayU8> masks = generateMasksFromPath(videoFrames, foregroundPaths, backgroundPaths);
				
		ArrayList<GrayU8> masksSmoothed1 = createNewMasks(masks);
		smoothMasks(masks, masksSmoothed1, 21, 2, 64, 26);
		ArrayList<GrayU8> masksSmoothed2 = createNewMasks(masks);
		smoothMasks(masksSmoothed1, masksSmoothed2, 11, 2, 64, 26);
		
		//multiply3D(masksSmoothed2,masksSmoothed2,255);
		
		return masksSmoothed2;
	}
	
	public static ArrayList<GrayU8> generateMasksFromPath(List<VideoFrame> videoFrames,
			List<Pair<Integer, LinkedList<Point2D_F32>>> foregroundPaths,
			List<Pair<Integer, LinkedList<Point2D_F32>>> backgroundPaths){
		
		if (videoFrames == null || videoFrames.isEmpty() || foregroundPaths == null) {
			return null;
		}

		ArrayList<GrayU8> masks = new ArrayList<GrayU8>();

		int width = videoFrames.get(0).getImage().getBufferedImage().getWidth();
		int height = videoFrames.get(0).getImage().getBufferedImage().getHeight();

		ListIterator<Pair<Integer, LinkedList<Point2D_F32>>> fgPathItor = foregroundPaths.listIterator();
		ListIterator<Pair<Integer, LinkedList<Point2D_F32>>> bgPathItor = backgroundPaths.listIterator();

		int cnt = 0;
		for (int frameIdx = 0; frameIdx < videoFrames.size(); ++frameIdx) {
			if (cnt >= PathList.frameInterval) {
				cnt = 0;
				continue;
			}
			cnt += 1;
			
			GrayU8 mask = new GrayU8(width, height);
			
			NearestNeighbor<Integer> nn = FactoryNearestNeighbor.kdtree();
			LinkedList<double[]> nnPoints = new LinkedList<double[]>();
			LinkedList<Integer> nnData = new LinkedList<Integer>();
			
			while (fgPathItor.hasNext()) {
				Pair<Integer, LinkedList<Point2D_F32>> pair = fgPathItor.next();
				if (pair.first > frameIdx) {
          break;
        }
				Point2D_F32 p = pair.second.getFirst();
				double[] point = {p.x * width, p.y * height};
				nnPoints.add(point);
				nnData.add(1);
			}
			
			while (bgPathItor.hasNext()) {
				Pair<Integer, LinkedList<Point2D_F32>> pair = bgPathItor.next();
				if (pair.first > frameIdx) {
          break;
        }
				Point2D_F32 p = pair.second.getFirst();
				double[] point = {p.x * width, p.y * height};
				nnPoints.add(point);
				nnData.add(0);
			}
			
			nn.init(2);
			nn.setPoints(nnPoints, nnData);
			
			for(int x = 0; x < width; ++x){
				for(int y = 0; y < height; ++y){
					double[] point = {x, y};
					@SuppressWarnings("unchecked")
					FastQueue<NnData<Integer>> results = new FastQueue(5,NnData.class,true);
					nn.findNearest(point, -1, 5, results);
					int sum = 0;
					for(NnData<Integer> r : results.toList()){
						sum += r.data.intValue();
					}
					int value = sum > results.size()/2 ? 1 : 0;
					mask.set(x, y, value);
				}
			}
			
			//showBineryImage(mask);
			masks.add(mask);
		}
		
		return masks;
		
	}
	
	public static ArrayList<GrayU8> generateScaledMasksFromPath(List<VideoFrame> videoFrames,
			List<Pair<Integer, LinkedList<Point2D_F32>>> foregroundPaths) {
		
		if (videoFrames == null || videoFrames.isEmpty() || foregroundPaths == null) {
			return null;
		}

		ArrayList<GrayU8> masks = new ArrayList<GrayU8>();

		int width = videoFrames.get(0).getImage().getBufferedImage().getWidth();
		int height = videoFrames.get(0).getImage().getBufferedImage().getHeight();

		ListIterator<Pair<Integer, LinkedList<Point2D_F32>>> fgPathItor = foregroundPaths.listIterator();

		int cnt = 0;
		for (int frameIdx = 0; frameIdx < videoFrames.size(); ++frameIdx) {
			if (cnt >= PathList.frameInterval) {
				cnt = 0;
				continue;
			}
			cnt += 1;

			GrayU8 mask = new GrayU8(width / PathList.samplingInterval, height / PathList.samplingInterval);
			
			while (fgPathItor.hasNext()) {
				Pair<Integer, LinkedList<Point2D_F32>> pair = fgPathItor.next();
				if (pair.first > frameIdx) {
          break;
        }
				Point2D_F32 p1 = pair.second.getFirst();
				int x = (int) (p1.x * width / PathList.samplingInterval);
				int y = (int) (p1.y * height / PathList.samplingInterval);
				if (mask.isInBounds(x, y)) {
					mask.set(x, y, 1);
				}
			}
			//showBineryImage(mask);
			masks.add(mask);
		}
		
		return masks;
	}
	
	public static ArrayList<GrayU8> smoothMasks(ArrayList<GrayU8> input, ArrayList<GrayU8> output,
			int spatialRadius, int temporalRadius, double multipyFactor, int threshold){
		
		if(input == null || input.isEmpty()){
			return input;
		}
		if(output == null){
			output = createNewMasks(input);
		}
		if(output.size() != input.size()){
			throw new IllegalArgumentException("size of input and output do not match. input: "+input.size()+" output: "+output.size());
		}
		
		multiply3D(input, input, multipyFactor);
		gaussianFilter3D(input, output, spatialRadius, temporalRadius);
		threshold3D(output,output,threshold);
		
		return output;
	}
	
	public static ArrayList<GrayU8> gaussianFilter3D(ArrayList<GrayU8> input, ArrayList<GrayU8> output,
			int spatialRadius, int temporalRadius) {
		
		ArrayList<GrayU8> spatialResult = createNewMasks(input);
		gaussianFilterSpatial(input, spatialResult, spatialRadius);
		gaussianFilterTemporal(spatialResult, output, temporalRadius);
		
		return output;
	}
	
	public static ArrayList<GrayU8> gaussianFilterSpatial(ArrayList<GrayU8> input, ArrayList<GrayU8> output, int spatialRadius){
		
		for (int i = 0; i < input.size(); ++i){
			GBlurImageOps.gaussian(input.get(i), output.get(i), -1, spatialRadius, null);
		}
		
		return output;
	}
	
	public static ArrayList<GrayU8> gaussianFilterTemporal(ArrayList<GrayU8> input, ArrayList<GrayU8> output, int spatialRadius){
		int width = input.get(0).getWidth();
		int height = input.get(0).getHeight();
		int len = input.size();
		
		Kernel1D_F32 kernel = FactoryKernelGaussian.gaussian(Kernel1D_F32.class,-1,spatialRadius);
		float divisor = kernel.computeSum();
		int data1D[] = new int[len + 2*kernel.offset];
		for (int x = 0; x < width; ++x){
			for (int y = 0; y < height; ++y){
				for(int i = 0; i < len; ++i){
					data1D[i + kernel.offset] = input.get(i).get(x, y);
				}
				for(int i = 0; i < len; ++i){
					int total = 0;
					for (int k = 0; k < kernel.width; ++k){
						total += (data1D[i+k] & 0xFF) * kernel.data[k];
					}
					output.get(i).set(x, y, Math.round(total/divisor));
				}
			} 
		}
		
		return output;
	}
	
	public static ArrayList<GrayU8> multiply3D(ArrayList<GrayU8> input, ArrayList<GrayU8> output, double value){
		
		for (int i = 0; i < input.size(); ++i){
			PixelMath.multiply(input.get(i), value, output.get(i));
		}
		
		return output;
	}
	
	public static ArrayList<GrayU8> threshold3D(ArrayList<GrayU8> input, ArrayList<GrayU8> output, int threshold){
		
		for (int i = 0; i < input.size(); ++i){
			ThresholdImageOps.threshold(input.get(i), output.get(i), threshold, false);
		}
		
		return output;
	}
	
	public static ArrayList<GrayU8> createNewMasks(ArrayList<GrayU8> input){
		ArrayList<GrayU8> output = new ArrayList<GrayU8>();
		for (int i = 0; i < input.size(); ++i){
			output.add(input.get(i).createSameShape());
		}
		return output;
	}
	
	public static ArrayList<GrayU8> scaleUpMasks(ArrayList<GrayU8> input, int width, int height){
		ArrayList<GrayU8> output = new ArrayList<GrayU8>();
		for (int i = 0; i < input.size(); ++i){
			GrayU8 in = input.get(i);
			GrayU8 out = new GrayU8(width, height);
			new FDistort(in, out).scaleExt().apply();
			output.add(out);
		}
		return output;
	}
	
	public static void showBineryImage(GrayU8 image){
		PixelMath.multiply(image,255,image);
		BufferedImage out = ConvertBufferedImage.convertTo(image,null);
		ShowImages.showWindow(out,"Output");
	}
	
}
