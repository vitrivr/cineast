package org.vitrivr.cineast.core.descriptor;

import org.vitrivr.cineast.core.color.ReadableRGBContainer;
import org.vitrivr.cineast.core.data.raw.images.MultiImage;

public class ImageDistance {

	private ImageDistance(){}
	
	public static double colorDistance(int col1, int col2){
		int r1 = ReadableRGBContainer.getRed(col1), r2 = ReadableRGBContainer.getRed(col2),
				g1 = ReadableRGBContainer.getGreen(col1), g2 = ReadableRGBContainer.getGreen(col2),
				b1 = ReadableRGBContainer.getBlue(col1), b2 = ReadableRGBContainer.getBlue(col2);
		
		return Math.sqrt((r1 - r2) * (r1 - r2) + (g1 - g2) * (g1 - g2) + (b1 - b2) * (b1 - b2));
	}
	
	public static double colorDistance(int[] img1, int[] img2){
		if(img1.length != img2.length){
			return Double.POSITIVE_INFINITY;
		}
		double distance = 0;
		for(int i = 0; i < img1.length; ++i){
			distance += colorDistance(img1[i], img2[i]);
		}
		distance /= img1.length;
		return distance;
	}
	
	public static double colorDistanceFull(MultiImage img1, MultiImage img2){
		if(img1.getWidth() != img2.getWidth() || img1.getHeight() != img2.getHeight()){
			return Double.POSITIVE_INFINITY;
		}
		return colorDistance(img1.getColors(), img2.getColors());
	}
	
	public static double colorDistance(MultiImage img1, MultiImage img2){
		if(img1.getWidth() != img2.getWidth() || img1.getHeight() != img2.getHeight()){
			return Double.POSITIVE_INFINITY;
		}
		return colorDistance(img1.getThumbnailColors(), img2.getThumbnailColors());
	}
	
}
