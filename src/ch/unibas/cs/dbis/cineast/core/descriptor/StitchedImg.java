package ch.unibas.cs.dbis.cineast.core.descriptor;

import georegression.struct.homo.Homography2D_F64;
import georegression.struct.point.Point2D_F64;

import java.awt.image.BufferedImage;
import java.util.List;

import boofcv.abst.feature.detect.interest.ConfigGeneralDetector;
import boofcv.abst.feature.tracker.PointTracker;
import boofcv.abst.sfm.d2.ImageMotion2D;
import boofcv.abst.sfm.d2.MsToGrayMotion2D;
import boofcv.alg.sfm.d2.StitchingFromMotion2D;
import boofcv.core.image.ConvertBufferedImage;
import boofcv.factory.feature.tracker.FactoryPointTracker;
import boofcv.factory.sfm.FactoryMotion2D;
import boofcv.struct.image.ImageFloat32;
import boofcv.struct.image.MultiSpectral;
import ch.unibas.cs.dbis.cineast.core.data.Frame;
import ch.unibas.cs.dbis.cineast.core.data.MultiImage;
import ch.unibas.cs.dbis.cineast.core.data.MultiImageFactory;

public class StitchedImg {

	private StitchedImg(){}
	
	public static MultiImage getStitched(List<Frame> frames){
		
		MultiImage first = frames.get(0).getImage();
		int width = first.getWidth(), height = first.getHeight();
		
		/* CODE BLOCK TAKEN FROM BOOFCV VIDEO MOSAIC EXAMPLE */
		
		// Configure the feature detector
		ConfigGeneralDetector confDetector = new ConfigGeneralDetector();
		confDetector.threshold = 10;
		confDetector.maxFeatures = 300;
		confDetector.radius = 2;

		// Use a KLT tracker
		PointTracker<ImageFloat32> tracker = FactoryPointTracker.klt(new int[]{1,2,4,8},confDetector,3,
				ImageFloat32.class,ImageFloat32.class);

		// This estimates the 2D image motion
		// An Affine2D_F64 model also works quite well.
		ImageMotion2D<ImageFloat32,Homography2D_F64> motion2D =
				FactoryMotion2D.createMotion2D(200,3,2,30,0.6,0.5,false,tracker,new Homography2D_F64());

		// wrap it so it output color images while estimating motion from gray
		ImageMotion2D<MultiSpectral<ImageFloat32>,Homography2D_F64> motion2DColor =
				new MsToGrayMotion2D<ImageFloat32,Homography2D_F64>(motion2D,ImageFloat32.class);

		// This fuses the images together
		StitchingFromMotion2D<MultiSpectral<ImageFloat32>,Homography2D_F64>
		stitch = FactoryMotion2D.createVideoStitchMS(0.5, motion2DColor, ImageFloat32.class);
		
		// shrink the input image and center it
		Homography2D_F64 shrink = new Homography2D_F64(1,0,width,0,1,height,0,0,1);
		shrink = shrink.invert(null);

		stitch.configure(width*3,3*height,shrink);
		
		/* END OF EXAMPLE CODE BLOCK */
		
		MultiSpectral<ImageFloat32> multiSpectralImage = new MultiSpectral<>(ImageFloat32.class, width, height, 3);
		
		boolean doOutput = true;
		
		for(Frame f : frames){
			BufferedImage img = f.getImage().getBufferedImage();
			ConvertBufferedImage.convertFromMulti(img, multiSpectralImage, true, ImageFloat32.class);
			//ConvertBufferedImage.orderBandsIntoRGB(multiSpectralImage, img);
			doOutput &= stitch.process(multiSpectralImage);
			
			if(!doOutput){
				break;
			}
			
			StitchingFromMotion2D.Corners corners = stitch.getImageCorners(width, height, null);
			
			
			if( nearBorder(corners.p0,stitch) || nearBorder(corners.p1,stitch) ||
					nearBorder(corners.p2,stitch) || nearBorder(corners.p3,stitch) ) {
				stitch.setOriginToCurrent();
			}
			
		}
		BufferedImage out;
//		if(doOutput){
			multiSpectralImage = stitch.getStitchedImage();
			out = ConvertBufferedImage.convertTo_F32(multiSpectralImage, null, true);
			//ConvertBufferedImage.orderBandsIntoRGB(multiSpectralImage, out);
//		}else{
//			out = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
//		}
		
		return MultiImageFactory.newMultiImage(out);
		
		
	}
	
	/**
	 * Checks to see if the point is near the image border
	 * TAKEN FROM BOOFCV VIDEO MOSAIC EXAMPLE 
	 * @author Peter Abeles
	 */
	private static boolean nearBorder( Point2D_F64 p , StitchingFromMotion2D<?,?> stitch ) {
		int r = 10;
		if( p.x < r || p.y < r ) {
			return true;
		}
		if( p.x >= stitch.getStitchedImage().width-r ) {
			return true;
		}
		if( p.y >= stitch.getStitchedImage().height-r ) {
			return true;
		}

		return false;
	}
	
}
