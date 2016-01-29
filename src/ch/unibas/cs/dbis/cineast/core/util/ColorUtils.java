package ch.unibas.cs.dbis.cineast.core.util;

import java.util.List;

import ch.unibas.cs.dbis.cineast.core.color.LabContainer;
import ch.unibas.cs.dbis.cineast.core.color.RGBContainer;
import ch.unibas.cs.dbis.cineast.core.color.ReadableLabContainer;
import ch.unibas.cs.dbis.cineast.core.color.ReadableRGBContainer;
import ch.unibas.cs.dbis.cineast.core.data.FloatVector;
import ch.unibas.cs.dbis.cineast.core.data.ReadableFloatVector;

public class ColorUtils {
	private ColorUtils(){}
	
	public static final ReadableLabContainer getAvg(List<ReadableLabContainer> containers){
		int size = containers.size();
		if(size == 0){
			return new LabContainer(0, 0, 0);
		}
		double L = 0, a = 0, b = 0, alpha = 0;
		for(ReadableLabContainer container : containers){
			L += container.getL() * container.getAlpha();
			a += container.getA() * container.getAlpha();
			b += container.getB() * container.getAlpha();
			alpha += container.getAlpha();
		}
		if(alpha < 1f){
			return new LabContainer(0, 0, 0);
		}
		return new LabContainer(L / alpha, a / alpha, b / alpha);
	}
	
	public static final int getAvg(int[] colors){
		
		if(colors.length == 0){
			return ReadableRGBContainer.WHITE_INT;
		}
		
		float r = 0, g = 0, b = 0, a = 0, len = 0;
		for(int color : colors){
			a = RGBContainer.getAlpha(color) / 255f;
			r += RGBContainer.getRed(color) * a;
			g += RGBContainer.getGreen(color) * a;
			b += RGBContainer.getBlue(color) * a;
			len += a;
		}
		
		if(len < 1f){
			return ReadableRGBContainer.WHITE_INT;
		}
		
		return RGBContainer.toIntColor(Math.round(r / len), Math.round(g / len), Math.round(b / len));
	}
	
	public static final int median(Iterable<Integer> colors){
		int[] histR = new int[256], histG = new int[256], histB = new int[256];
		for(int c : colors){
			if(RGBContainer.getAlpha(c) < 127){
				continue;
			}
			histR[RGBContainer.getRed(c)]++;
			histG[RGBContainer.getGreen(c)]++;
			histB[RGBContainer.getBlue(c)]++;
		}
		return RGBContainer.toIntColor(medianFromHistogram(histR), medianFromHistogram(histG), medianFromHistogram(histB));
	}
	
	private static int medianFromHistogram(int[] hist){
		int pos_l = 0, pos_r = hist.length - 1;
		int sum_l = hist[pos_l], sum_r = hist[pos_r];
		
		while(pos_l < pos_r){
			if(sum_l < sum_r){
				sum_l += hist[++pos_l];
			}else{
				sum_r += hist[--pos_r];
			}
		}
		return pos_l;
	}
	
	public static final int getAvg(Iterable<Integer> colors){
				
		float r = 0, g = 0, b = 0, a = 0, len = 0;
		for(int color : colors){
			a = RGBContainer.getAlpha(color) / 255f;
			r += RGBContainer.getRed(color) * a;
			g += RGBContainer.getGreen(color) * a;
			b += RGBContainer.getBlue(color) * a;
			len += a;
		}
		
		if(len < 1){
			return ReadableRGBContainer.WHITE_INT;
		}
		
		return RGBContainer.toIntColor(Math.round(r / len), Math.round(g / len), Math.round(b / len));
	}
	
	/**
	 * 
	 * @param colors
	 * @return value between 0 and 1
	 */
	public static final float getAvgAlpha(Iterable<Integer> colors){
		float a = 0f;
		int count = 0;
		for(int color : colors){
			a += RGBContainer.getAlpha(color) / 255f;
			++count;
		}
		return a / count;
	}
	
	public static final FloatVector getAvg(List<? extends ReadableFloatVector> vectors, FloatVector result){
		int size = vectors.size();
		if(size == 0){
			for(int i = 0; i < result.getElementCount(); ++i){
				result.setElement(i, 0f);
			}
			return result;
		}
		double[] sum = new double[result.getElementCount()];
		for(ReadableFloatVector vector : vectors){
			for(int i = 0; i < sum.length; ++i){
				sum[i] += vector.getElement(i);
			}
		}
		
		for(int i = 0; i < sum.length; ++i){
			result.setElement(i, (float) (sum[i] / size));
		}
		
		return result;
	}
}
