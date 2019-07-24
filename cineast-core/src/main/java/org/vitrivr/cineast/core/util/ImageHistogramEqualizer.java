package org.vitrivr.cineast.core.util;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.vitrivr.cineast.core.color.ReadableRGBContainer;
import org.vitrivr.cineast.core.data.MultiImage;
import org.vitrivr.cineast.core.data.MultiImageFactory;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class ImageHistogramEqualizer {

	private ImageHistogramEqualizer(){}
	
	private static LoadingCache<MultiImage, MultiImage> cache = CacheBuilder.newBuilder().expireAfterAccess(1, TimeUnit.MINUTES).build(new CacheLoader<MultiImage, MultiImage>(){

		@Override
		public MultiImage load(MultiImage in) throws Exception {
			return equalizeHistogram(in);
		}
	});
	
	public static MultiImage getEqualized(MultiImage in){
		try {
			synchronized(cache){
				return cache.get(in);
			}
		} catch (ExecutionException e) {
			return equalizeHistogram(in); //should never happen
		}
	}
	
	/**
	 * 
	 * Equalizes the color histogram of the input image.
	 * @param in
	 * @return
	 */
	private static MultiImage equalizeHistogram(MultiImage in){
		int[] inColors = in.getColors();
		int[] returnColors = new int[inColors.length];
		
		float[] red = new float[256], green = new float[256], blue = new float[256];
		
		// build histogram
		for(int color : inColors){
			float alpha = ReadableRGBContainer.getAlpha(color) / 255f;
			red[ReadableRGBContainer.getRed(color)] += alpha;
			green[ReadableRGBContainer.getGreen(color)] += alpha;
			blue[ReadableRGBContainer.getBlue(color)] += alpha;
		}
		
		int[] redMap = buildMapFromHist(red);
		int[] greenMap = buildMapFromHist(green);
		int[] blueMap = buildMapFromHist(blue);
		
		//apply mapping
		for(int i = 0; i < inColors.length; ++i){
			int color = inColors[i];
			returnColors[i] = ReadableRGBContainer.toIntColor(
					redMap[ReadableRGBContainer.getRed(color)],
					greenMap[ReadableRGBContainer.getGreen(color)],
					blueMap[ReadableRGBContainer.getBlue(color)],
					ReadableRGBContainer.getAlpha(color));
		}
		
		return MultiImageFactory.newMultiImage(in.getWidth(), in.getHeight(), returnColors);		
		
	}
	
	private static int[] buildMapFromHist(float[] hist){
		int[] _return = new int[hist.length];
		_return[0] = Math.round(hist[0]);
		
		for(int i = 1; i < hist.length; ++i){
			_return[i] = Math.round(_return[i-1] + hist[i]);
		}
		
		if(_return[0] == _return[_return.length - 1]){//all zeros
			for(int i = 0; i < _return.length; ++i){
				_return[i] = 0;
			}
			return _return;
		}
		
		for(int i = 0; i < hist.length; ++i){
			_return[i] = Math.round((_return[i] * 255.0f) / _return[_return.length - 1]);
		}
		
		return _return;
	}
	
}
