package ch.unibas.cs.dbis.cineast.core.descriptor;

import ch.unibas.cs.dbis.cineast.core.color.RGBContainer;
import ch.unibas.cs.dbis.cineast.core.data.MultiImage;

public class ImageDistance {

	private ImageDistance(){}
	
	public static double colorDistance(int col1, int col2){
		int r1 = RGBContainer.getRed(col1), r2 = RGBContainer.getRed(col2),
				g1 = RGBContainer.getGreen(col1), g2 = RGBContainer.getGreen(col2),
				b1 = RGBContainer.getBlue(col1), b2 = RGBContainer.getBlue(col2);
		
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
