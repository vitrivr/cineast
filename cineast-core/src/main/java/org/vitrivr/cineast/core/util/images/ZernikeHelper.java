package org.vitrivr.cineast.core.util.images;

import boofcv.alg.filter.binary.BinaryImageOps;
import boofcv.alg.filter.binary.Contour;
import boofcv.alg.filter.binary.GThresholdImageOps;
import boofcv.alg.filter.binary.ThresholdImageOps;
import boofcv.io.image.ConvertBufferedImage;
import boofcv.struct.ConnectRule;
import boofcv.struct.image.GrayF32;
import boofcv.struct.image.GrayS32;
import boofcv.struct.image.GrayU8;
import georegression.struct.point.Point2D_I32;
import org.apache.commons.math3.complex.Complex;
import org.vitrivr.cineast.core.util.math.ZernikeMoments;
import org.vitrivr.cineast.core.util.math.functions.ZernikeBasisFunction;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import static java.awt.image.BufferedImage.TYPE_INT_RGB;

/**
 * @author rgasser
 * @version 1.0
 * @created 19.03.17
 */
public final class ZernikeHelper {

    /**
     * Private constructor; do not instantiate!
     */
    private ZernikeHelper() {
    }

    /**
     * Calculates and returns the Zernike Moments of order n for the provided image.
     *
     * @param image BufferedImage for which Zernike Moments should be extracted.
     * @param order Order up to which Zernike Moments should be calculated.
     * @return ZernikeMoments for image.
     */
    public static ZernikeMoments zernikeMoments(BufferedImage image, int order) {

        double[][] data = new double[image.getWidth()][image.getHeight()];

        for (int x=0;x<image.getWidth();x++) {
            for (int y=0;y<image.getHeight();y++) {
                int rgb = image.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = (rgb & 0xFF);

                /* Store gay level in terms of values between 0.0 and 1.0. */
                data[x][y] = (r + g + b) / (3.0 * 255.0);
            }
        }

        ZernikeMoments m = new ZernikeMoments(data);
        m.compute(order);
        return m;
    }

    /**
     * Calculates and returns a list of Zernike Moments for all shapes that have been detected in the provided
     * image. To do so, first the image is transformed into a binary image by means of thresholding. Afterwards,
     * contours are detected in the binary image and shapes are extracted for every detected contour. The so
     * extracted data is then handed to a class that obtains the Zernike Moments.
     *
     * @param image BufferedImage for which Zernike Moments should be extracted.
     * @param minimal Minimal size of the shape in terms of contour-length. Smaller shapes will be ignored.
     * @param order Order up to which Zernike Moments should be calculated.
     * @return List of Zernike Moments for the image.
     */
    public static List<ZernikeMoments> zernikeMomentsForShapes(BufferedImage image, int minimal, int order) {
        /* Extract the contours (shapes) from the buffered image. */
        GrayF32 input = ConvertBufferedImage.convertFromSingle(image, null, GrayF32.class);
        GrayU8 binary = new GrayU8(input.width,input.height);
        GrayS32 label = new GrayS32(input.width,input.height);

        /* Select a global threshold using Otsu's method and apply that threshold. */
        double threshold = GThresholdImageOps.computeOtsu(input, 0, 255);
        ThresholdImageOps.threshold(input, binary,(float)threshold,true);

        // remove small blobs through erosion and dilation
        // The null in the input indicates that it should internally declare the work image it needs
        // this is less efficient, but easier to code.
        GrayU8 filtered = BinaryImageOps.erode8(binary, 1, null);
        filtered = BinaryImageOps.dilate8(filtered, 1, null);

        /* Detect blobs inside the image using an 8-connect rule. */
        List<Contour> contours = BinaryImageOps.contour(filtered, ConnectRule.EIGHT, label);

        List<ZernikeMoments> moments = new ArrayList<>();
        for (Contour contour : contours) {
            for (List<Point2D_I32> shape : contour.internal) {
                if (shape.size() >= minimal) {
                    int[] bounds = ContourHelper.bounds(shape);
                    int w = bounds[1] - bounds[0];
                    int h = bounds[3] - bounds[2];
                    double[][] data = new double[w][h];

                    for (int x = 0; x < w; x++) {
                        for (int y = 0; y < h; y++) {
                            if (filtered.get(x + bounds[0],y + bounds[2]) == 1) {
                                data[x][y] = 0.0f;
                            } else {
                                data[x][y] = 1.0f;
                            }
                        }
                    }

                    ZernikeMoments m = new ZernikeMoments(data);
                    m.compute(order);
                    moments.add(m);
                }
            }
        }

        return moments;
    }

    /**
     * Attempts at reconstructing an image from a list of complete Zernike Moments. The list must contain
     * a complete set of complex Zernike Moments up to some arbitrary order. The moments must be ordered according
     * to Noll's sequential index (ascending).
     *
     * @param moments
     * @return
     */
    public static BufferedImage reconstructImage(ZernikeMoments moments) {
        return ZernikeHelper.reconstructImage(moments.getHeight(), moments.getHeight(), moments.getMoments());
    }

    /**
     * Attempts at reconstructing an image from a list of complex Zernike Moments. The list must contain
     * a complete set of Zernike Moments up to some arbitrary order n. The moments must be ordered
     * according to Noll's sequential indexing scheme (ascending).
     *
     * @param w the width of the desired image
     * @param h the height of the desired image
     * @param moments List of Zernike Moments is ascending order according to Noll's index.
     * @return The reconstructed image
     */
    public static BufferedImage reconstructImage(final int w, final int h, final List<Complex> moments) {
        /* Scale x and y dimension to unit-disk. */
        final double c = -1.0;
        final double d = 1.0;

        /* Prepare array for imaga data. */
        final double[][] imageData = new double[w][h];

        double maxValue = 0.0f;
        int indexFeature = 0;
        for (int n=0; indexFeature < moments.size(); ++n) {
            for (int m=0;m<=n;m++) {
                if((n-Math.abs(m))%2 == 0) {
                    final Complex moment = moments.get(indexFeature);
                    final ZernikeBasisFunction bf1 = new ZernikeBasisFunction(n, m);
                    for (int i = 0; i < w; i++) {
                        for (int j = 0; j < h; j++) {
                            Complex v = new Complex(c+(i*(d-c))/(w-1), d-(j*(d-c))/(h-1));
                            Complex res = moment.multiply(bf1.value(v));
                            imageData[i][j] += res.getReal();
                            maxValue = Math.max(maxValue, imageData[i][j]);
                        }
                    }
                    indexFeature++;
                }
            }
        }

        BufferedImage image = new BufferedImage(w, h, TYPE_INT_RGB);
        for (int x = 1; x < w; x++) {
            for (int y = 1; y < h; y++) {
                int i = (int)(255.0*(imageData[x-1][y-1] )/(maxValue));
                int rgb = ((255 & 0xFF) << 24) | ((i & 0xFF) << 16) | ((i & 0xFF) << 8)  | ((i & 0xFF) << 0);
                image.setRGB(x,y,rgb);
            }
        }

        return image;
    }
}
