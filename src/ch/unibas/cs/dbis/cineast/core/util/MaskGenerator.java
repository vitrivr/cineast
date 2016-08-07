package ch.unibas.cs.dbis.cineast.core.util;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import boofcv.abst.distort.FDistort;
import boofcv.alg.filter.binary.ThresholdImageOps;
import boofcv.alg.filter.blur.GBlurImageOps;
import boofcv.alg.misc.PixelMath;
import boofcv.factory.filter.kernel.FactoryKernelGaussian;
import boofcv.gui.image.ShowImages;
import boofcv.io.image.ConvertBufferedImage;
import boofcv.struct.convolve.Kernel1D_I32;
import boofcv.struct.image.GrayU8;
import ch.unibas.cs.dbis.cineast.core.data.Frame;
import ch.unibas.cs.dbis.cineast.core.data.Pair;
import ch.unibas.cs.dbis.cineast.core.descriptor.PathList;
import georegression.struct.point.Point2D_F32;

public class MaskGenerator {

	private MaskGenerator(){}
	
	public static ArrayList<GrayU8> getFgMasks(List<Frame> frames,
			List<Pair<Integer, LinkedList<Point2D_F32>>> foregroundPaths) {
		
		if (frames == null || frames.isEmpty() || foregroundPaths == null) {
			return null;
		}

		ArrayList<GrayU8> masksScaled = generateScaledMasksFromPath(frames, foregroundPaths);

		ArrayList<GrayU8> masksScaledSmoothed1 = createNewMasks(masksScaled);
		smoothMasks(masksScaled, masksScaledSmoothed1, 4, 2, 64, 26);
		
		ArrayList<GrayU8> masks = scaleUpMasks(masksScaledSmoothed1, frames.get(0).getImage().getBufferedImage().getWidth(), frames.get(0).getImage().getBufferedImage().getHeight());
		
		ArrayList<GrayU8> masksSmoothed1 = createNewMasks(masks);
		smoothMasks(masks, masksSmoothed1, 5, 2, 64, 10);
		ArrayList<GrayU8> masksSmoothed2 = createNewMasks(masks);
		smoothMasks(masksSmoothed1, masksSmoothed2, 5, 2, 64, 10);
		
		multiply3D(masksSmoothed2,masksSmoothed2,255);
		
		return masksSmoothed2;
	}
	
	public static ArrayList<GrayU8> generateScaledMasksFromPath(List<Frame> frames,
			List<Pair<Integer, LinkedList<Point2D_F32>>> foregroundPaths) {
		
		if (frames == null || frames.isEmpty() || foregroundPaths == null) {
			return null;
		}

		ArrayList<GrayU8> masks = new ArrayList<GrayU8>();

		int width = frames.get(0).getImage().getBufferedImage().getWidth();
		int height = frames.get(0).getImage().getBufferedImage().getHeight();

		ListIterator<Pair<Integer, LinkedList<Point2D_F32>>> fgPathItor = foregroundPaths.listIterator();

		int cnt = 0;
		for (int frameIdx = 0; frameIdx < frames.size(); ++frameIdx) {
			if (cnt >= PathList.frameInterval) {
				cnt = 0;
				continue;
			}
			cnt += 1;

			GrayU8 mask = new GrayU8(width / PathList.samplingInterval, height / PathList.samplingInterval);
			
			while (fgPathItor.hasNext()) {
				Pair<Integer, LinkedList<Point2D_F32>> pair = fgPathItor.next();
				if (pair.first > frameIdx)
					break;
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
		
		Kernel1D_I32 kernel = FactoryKernelGaussian.gaussian(Kernel1D_I32.class,-1,spatialRadius);
		int divisor = kernel.computeSum();
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
					output.get(i).set(x, y, total/divisor);
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
