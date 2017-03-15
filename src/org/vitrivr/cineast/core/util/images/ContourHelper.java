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

import java.awt.image.BufferedImage;
import java.util.List;

/**
 * @author rgasser
 * @version 1.0
 * @created 22.01.17
 */
public class ContourHelper {

    /**
     *
     */
    private ContourHelper() {

    }

    /**
     *
     * @param image
     * @return
     */
    public static List<Contour> getContours(BufferedImage image) {
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

        // Detect blobs inside the image using an 8-connect rule
        return BinaryImageOps.contour(filtered, ConnectRule.EIGHT, label);
    }

    /**
     *
     * @param contour
     * @return
     */
    public static Point2D_I32 centroid(List<Point2D_I32> contour) {
        Point2D_I32 centroid = new Point2D_I32();

        for (Point2D_I32 point : contour) {
            centroid.x += point.x;
            centroid.y += point.y;
        }

        centroid.x /= contour.size();
        centroid.y /= contour.size();
        return centroid;
    }


    /**
     *
     */
    public static double[] centroidDistance(List<Point2D_I32> contour, boolean pad) {
        Point2D_I32 centroid = centroid(contour);

        int size = 0;
        if (pad) {
            double base2log = Math.log(contour.size())/Math.log(2);
            if (Math.round(base2log) >= base2log) {
                size = (int) Math.pow(2,(Math.round(base2log)));
            } else {
                size = (int) Math.pow(2,(Math.round(base2log) + 1));
            }
        } else {
            size = contour.size();
        }

        double[] distance = new double[size];
        for (int i = 0;i<size;i++) {
            if (i < contour.size()) {
                Point2D_I32 point = contour.get(i);
                distance[i] = Math.sqrt(Math.pow(point.x - centroid.x, 2) + Math.pow(point.y - centroid.y, 2));
            } else {
                distance[i] = 0;
            }
        }

        return distance;
    }
}
