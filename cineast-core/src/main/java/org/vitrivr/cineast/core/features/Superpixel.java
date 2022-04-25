package org.vitrivr.cineast.core.features;

import boofcv.abst.segmentation.ImageSuperpixels;
import boofcv.alg.filter.blur.GBlurImageOps;
import boofcv.alg.segmentation.ComputeRegionMeanColor;
import boofcv.alg.segmentation.ImageSegmentationOps;
import boofcv.factory.segmentation.ConfigFh04;
import boofcv.factory.segmentation.ConfigSlic;
import boofcv.factory.segmentation.FactoryImageSegmentation;
import boofcv.factory.segmentation.FactorySegmentationAlg;
import boofcv.gui.feature.VisualizeRegions;
import boofcv.io.image.ConvertBufferedImage;
import boofcv.struct.feature.ColorQueue_F32;
import boofcv.struct.image.GrayF32;
import boofcv.struct.image.GrayS32;
import boofcv.struct.image.ImageBase;
import boofcv.struct.image.ImageType;
import boofcv.struct.image.Planar;
import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import org.ddogleg.struct.FastQueue;
import org.ddogleg.struct.GrowQueue_I32;
import org.vitrivr.cineast.core.color.ColorConverter;
import org.vitrivr.cineast.core.color.LabContainer;
import org.vitrivr.cineast.core.color.RGBContainer;
import org.vitrivr.cineast.core.data.segments.SegmentContainer;

public class Superpixel {

  // definition of superpixel algorithms
  public static final int ALG_FH04 = 1; // Felzenszwalb-Huttenlocher
  public static final int ALG_MS = 2; // MeanShift
  public static final int ALG_SLIC = 3; // SLIC
  public static final int ALG_WATERSHED = 4; // Watershed

  // definition of image transformation
  public static final int IMG_AVG = 1; // average frame
  public static final int IMG_MED = 2; // median frame
  public static final int IMG_REP = 3; // most representative frame

  /**
   * Code Inspiration Source: https://boofcv.org/index.php?title=Example_Superpixels Segments an
   * image and calls visualization method
   */
  public static <T extends ImageBase<T>>
  BufferedImage performSegmentation(ImageSuperpixels<T> alg, T color) {
    GBlurImageOps.gaussian(color, color, 0.5, -1, null);
    GrayS32 pixelToSegment = new GrayS32(color.width, color.height);
    alg.segment(color, pixelToSegment);
    return computation(pixelToSegment, color, alg.getTotalSuperpixels());
  }

  /**
   * Code source: https://boofcv.org/index.php?title=Example_Superpixels
   * <p>
   * Don't need visualization, just computation of Buffered Image!
   * <p>
   */
  public static <T extends ImageBase<T>>
  BufferedImage computation(GrayS32 pixelToRegion, T color, int numSegments) {
    // Computes the mean color inside each region
    ImageType<T> type = color.getImageType();
    ComputeRegionMeanColor<T> colorize = FactorySegmentationAlg.regionMeanColor(type);

    FastQueue<float[]> segmentColor = new ColorQueue_F32(type.getNumBands());
    segmentColor.resize(numSegments);

    GrowQueue_I32 regionMemberCount = new GrowQueue_I32();
    regionMemberCount.resize(numSegments);

    ImageSegmentationOps.countRegionPixels(pixelToRegion, numSegments, regionMemberCount.data);
    colorize.process(color, pixelToRegion, regionMemberCount, segmentColor);

    // ATTENTION:
    // When uncommenting the following line, the superpixels get colored according to the colors specified in ColorSuperpixel.java
    // segmentColor = usePredefinedColorSet(segmentColor);

    return VisualizeRegions.regionsColor(pixelToRegion, segmentColor, null);
  }

  /**
   * Maps segment colors to predefined colors
   *
   * @param segmentColor mean segmentColor of different fields
   * @return segmentColor according to the closest color in {@link org.vitrivr.cineast.core.features.ColorSuperpixel}
   */
  private static FastQueue<float[]> usePredefinedColorSet(FastQueue<float[]> segmentColor) {
    ColorSuperpixel colorSuperpixel = new ColorSuperpixel();
    for (int i = 0; i < segmentColor.size; i++) {
      double minDeltaE = Double.MAX_VALUE;
      RGBContainer minColor = null;
      LabContainer c1 = ColorConverter.RGBtoLab((int) segmentColor.data[i][0],
          (int) segmentColor.data[i][1], (int) segmentColor.data[i][2]);
      for (int j = 0; j < colorSuperpixel.colors.size(); j++) {
        LabContainer c2 = ColorConverter.RGBtoLab(colorSuperpixel.colors.get(j).getR(),
            colorSuperpixel.colors.get(j).getG(), colorSuperpixel.colors.get(j).getB());
        double deltaE = calculateDeltaE(c1, c2);
        if (deltaE < minDeltaE) {
          minDeltaE = deltaE;
          minColor = colorSuperpixel.colors.get(j);
        }

      }
      assert minColor != null;
      segmentColor.data[i][0] = minColor.getR();
      segmentColor.data[i][1] = minColor.getG();
      segmentColor.data[i][2] = minColor.getB();
    }
    return segmentColor;
  }


  /**
   * Calculates delta E of two colours
   *
   * @param l1 colour 1
   * @param l2 colour 2
   * @return colour difference of l1 and l2
   */
  public static double calculateDeltaE(LabContainer l1, LabContainer l2) {

    float dL = l1.getL() - l2.getL();
    float dA = l1.getA() - l2.getA();
    float dB = l1.getB() - l2.getB();

    // calculate deltaE
    return Math.sqrt(Math.pow(dL, 2) + Math.pow(dA, 2) + Math.pow(dB, 2));
  }

  /**
   * This method applies the choice of the frame and calls computation of superpixel algorithms to a
   * BufferedImage
   *
   * @param image original image to apply superpixel algorithms
   * @param algID integer indicating which algorithm should be applied
   * @return BufferedImage containing superpixels
   */
  public static BufferedImage applySuperpixelBI(BufferedImage image, int algID) {
    BufferedImage mask = image;
    image = ConvertBufferedImage.stripAlphaChannel(image);
    ImageType<Planar<GrayF32>> imageType = ImageType.pl(3, GrayF32.class);

    ImageSuperpixels alg;
    switch (algID) {
      case 1:
        alg = FactoryImageSegmentation.fh04(new ConfigFh04(100, 30), imageType);
        break;
      case 2:
        alg = FactoryImageSegmentation.meanShift(null, imageType);
        break;
      case 3:
        alg = FactoryImageSegmentation.slic(new ConfigSlic(400), imageType);
        break;
      case 4:
        alg = FactoryImageSegmentation.watershed(null, imageType);
        break;
      default:
        throw new IllegalStateException("Unexpected value: " + algID);
    }
    ImageBase color = imageType.createImage(image.getWidth(), image.getHeight());
    ConvertBufferedImage.convertFrom(image, color, true);
    BufferedImage superpixel = Superpixel.performSegmentation(alg, color);

    return Superpixel.applyTransparency(superpixel, mask);
  }

  /**
   * This method applies the choice of the frame and calls computation of superpixel algorithms to a
   * BufferedImage
   *
   * @param segmentContainer original segmentContainer to apply superpixel algorithms
   * @param algID            integer indicating which algorithm should be applied
   * @return BufferedImage containing superpixels
   */
  public static BufferedImage applySuperpixelSC(SegmentContainer segmentContainer, int imgID,
      int algID) {
    BufferedImage image, mask;
    switch (imgID) {
      case 1:
        image = segmentContainer.getAvgImg().getBufferedImage();
        mask = segmentContainer.getAvgImg().getBufferedImage();
        break;
      case 2:
        image = segmentContainer.getMedianImg().getBufferedImage();
        mask = segmentContainer.getMedianImg().getBufferedImage();
        break;
      case 3:
        image = segmentContainer.getMostRepresentativeFrame().getImage().getBufferedImage();
        mask = segmentContainer.getMostRepresentativeFrame().getImage().getBufferedImage();
        break;
      default:
        throw new IllegalStateException("Unexpected value: " + imgID);
    }
    return applySuperpixel(algID, image, mask);
  }

  /**
   * This method applies the choice of the superpixel algorithm and computed superpixel algorithms
   *
   * @param algID integer indicating which algorithm should be applied
   * @param image BufferedImage to apply superpixels to
   * @param mask  BufferedImage containing original image to provide transparency mask
   * @return BufferedImage containing superpixels
   */
  public static BufferedImage applySuperpixel(int algID, BufferedImage image, BufferedImage mask) {
    image = ConvertBufferedImage.stripAlphaChannel(image);
    ImageType<Planar<GrayF32>> imageType = ImageType.pl(3, GrayF32.class);

    ImageSuperpixels alg;
    switch (algID) {
      case 1:
        alg = FactoryImageSegmentation.fh04(new ConfigFh04(100, 30), imageType);
        break;
      case 2:
        alg = FactoryImageSegmentation.meanShift(null, imageType);
        break;
      case 3:
        alg = FactoryImageSegmentation.slic(new ConfigSlic(400), imageType);
        break;
      case 4:
        alg = FactoryImageSegmentation.watershed(null, imageType);
        break;
      default:
        throw new IllegalStateException("Unexpected value: " + algID);
    }
    ImageBase color = imageType.createImage(image.getWidth(), image.getHeight());
    ConvertBufferedImage.convertFrom(image, color, true);
    BufferedImage superpixel = Superpixel.performSegmentation(alg, color);

    return Superpixel.applyTransparency(superpixel, mask);
  }


  /**
   * Delete parts of superpixel images where sketch was transparent
   *
   * @param image superpixel sketch, transparent parts are coloured black
   * @param mask  original sketch providing alpha (transparency) values
   * @return cleaned BufferedImage of superpixels
   */
  public static BufferedImage applyTransparency(BufferedImage image, BufferedImage mask) {
    BufferedImage combined = new BufferedImage(image.getWidth(), image.getHeight(),
        BufferedImage.TYPE_INT_ARGB);
    Graphics2D g = combined.createGraphics();
    g.setComposite(AlphaComposite.Src);
    g.drawImage(image, 0, 0, null);
    g.setComposite(AlphaComposite.DstIn);
    g.drawImage(mask, 0, 0, null);

    g.dispose();

    return combined;
  }

}
