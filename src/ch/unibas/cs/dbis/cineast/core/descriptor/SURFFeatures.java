package ch.unibas.cs.dbis.cineast.core.descriptor;

import java.util.ArrayList;
import java.util.List;

import boofcv.abst.feature.detect.extract.ConfigExtract;
import boofcv.abst.feature.detect.extract.NonMaxSuppression;
import boofcv.abst.feature.orientation.OrientationIntegral;
import boofcv.alg.feature.describe.DescribePointSurf;
import boofcv.alg.feature.detect.interest.FastHessianFeatureDetector;
import boofcv.alg.transform.ii.GIntegralImageOps;
import boofcv.core.image.GeneralizedImageOps;
import boofcv.factory.feature.describe.FactoryDescribePointAlgs;
import boofcv.factory.feature.detect.extract.FactoryFeatureExtractor;
import boofcv.factory.feature.orientation.FactoryOrientationAlgs;
import boofcv.io.image.ConvertBufferedImage;
import boofcv.struct.BoofDefaults;
import boofcv.struct.feature.BrightFeature;
import boofcv.struct.feature.ScalePoint;
import boofcv.struct.image.ImageSInt32;
import boofcv.struct.image.ImageUInt8;
import ch.unibas.cs.dbis.cineast.core.data.MultiImage;

public class SURFFeatures {

	private SURFFeatures() {
	}

	private static final int maxFeaturesPerScale = 10;
	
	public static List<BrightFeature> getSURF(MultiImage img){
		return getSURF(img, maxFeaturesPerScale);
	}
	
	/*
	 * based on example code by Peter Abeles
	 */
	public static List<BrightFeature> getSURF(MultiImage img, int maxFeaturesPerScale) {
		ImageUInt8 image = ConvertBufferedImage.convertFromSingle(img.getBufferedImage(), null, ImageUInt8.class);

		NonMaxSuppression extractor = FactoryFeatureExtractor.nonmax(new ConfigExtract(2, 0, 5, true));
		FastHessianFeatureDetector<ImageSInt32> detector = new FastHessianFeatureDetector<ImageSInt32>(
				extractor,	maxFeaturesPerScale, 2, 9, 4, 4, 6);

		OrientationIntegral<ImageSInt32> orientation = FactoryOrientationAlgs.sliding_ii(null, ImageSInt32.class);
		DescribePointSurf<ImageSInt32> descriptor = FactoryDescribePointAlgs.<ImageSInt32> surfStability(null, ImageSInt32.class);

		ImageSInt32 integral = GeneralizedImageOps.createSingleBand(ImageSInt32.class, image.width, image.height);
		GIntegralImageOps.transform(image, integral);

		detector.detect(integral);

		orientation.setImage(integral);
		descriptor.setImage(integral);

		List<ScalePoint> points = detector.getFoundPoints();

		List<BrightFeature> descriptions = new ArrayList<BrightFeature>(4 * maxFeaturesPerScale);

		for (ScalePoint p : points) {
			// estimate orientation
			orientation.setObjectRadius( p.scale*BoofDefaults.SURF_SCALE_TO_RADIUS);
			double angle = orientation.compute(p.x,p.y);
 
			// extract the SURF description for this region
			BrightFeature desc = descriptor.createDescription();
			descriptor.describe(p.x,p.y,angle,p.scale,desc);
 
			// save everything for processing later on
			descriptions.add(desc);
		}
		
		return descriptions;
	}

}
