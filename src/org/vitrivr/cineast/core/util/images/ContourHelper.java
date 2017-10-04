package org.vitrivr.cineast.core.util.images;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.List;

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

/**
 * @author rgasser
 * @version 1.0
 * @created 22.01.17
 */
public final class ContourHelper {

    /**
     * Private constructor; do not instantiate!
     */
    private ContourHelper() {}

    /**
     * Applies a contour-detection algorithm on the provided image and returns a list of detected contours. First, the image
     * is converted to a BinaryImage using a threshold algorithm (Otsu). Afterwards, blobs in the image are detected using
     * an 8-connect rule.
     *
     * @param image BufferedImage in which contours should be detected.
     * @return List of contours.
     */
    public static List<Contour> getContours(BufferedImage image) {
        /* Draw a black frame around to image so as to make sure that all detected contours are internal contours. */
        BufferedImage resized = new BufferedImage(image.getWidth() + 4, image.getHeight() + 4, image.getType());
        Graphics g = resized.getGraphics();
        g.setColor(Color.BLACK);
        g.fillRect(0,0,resized.getWidth(),resized.getHeight());
        g.drawImage(image, 2,2, image.getWidth(), image.getHeight(), null);

        /* Convert to BufferedImage to Gray-scale image and prepare Binary image. */
        GrayF32 input = ConvertBufferedImage.convertFromSingle(resized, null, GrayF32.class);
        GrayU8 binary = new GrayU8(input.width,input.height);
        GrayS32 label = new GrayS32(input.width,input.height);

        /* Select a global threshold using Otsu's method and apply that threshold. */
        double threshold = GThresholdImageOps.computeOtsu(input, 0, 255);
        ThresholdImageOps.threshold(input, binary,(float)threshold,true);

        /* Remove small blobs through erosion and dilation;  The null in the input indicates that it should internally
         * declare the work image it needs this is less efficient, but easier to code. */
        GrayU8 filtered = BinaryImageOps.erode8(binary, 1, null);
        filtered = BinaryImageOps.dilate8(filtered, 1, null);

        /* Detect blobs inside the image using an 8-connect rule. */
        return BinaryImageOps.contour(filtered, ConnectRule.EIGHT, label);
    }

    /**
     * Calculates and returns the position of the centroid given a contour.
     *
     * @param contour List of points that make up the contour.
     * @return Coordinates of the centroid.
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
     * Calculates and returns the Centroid Distance Function for the provided contour. I.e. the
     * distance of each point from the centroid of the shape described by the contour.
     *
     * @param contour List of points that make up the contour.
     * @param pad True if the returned list should be padded so that its size is a power of 2 (for FFT).
     * @return List of centroid distance for each point.
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

    /**
     * Calculates and returns the bounds for the provided 2D shape.
     *
     * @param vertices List of vertices that make up the shape for which bounds should be calculated.
     * @return Float-array spanning the bounds: {max_x, min_x, max_y, min_y}
     */
    public static int[] bounds(List<Point2D_I32> vertices) {
        /* If no vertices are in the list, the box is zero. */
        if (vertices.isEmpty()) {
            return new int[4];
        }

        /* Initialize the bounding-box. */
        int[] bounds = {Integer.MAX_VALUE, -Integer.MAX_VALUE, Integer.MAX_VALUE, -Integer.MAX_VALUE};

        /* Find max and min y-values. */
        for(Point2D_I32 vertex : vertices) {
            if (vertex.x < bounds[0]) {
              bounds[0] = vertex.x;
            }
            if (vertex.x > bounds[1]) {
              bounds[1] = vertex.x;
            }
            if (vertex.y < bounds[2]) {
              bounds[2] = vertex.y;
            }
            if (vertex.y > bounds[3]) {
              bounds[3] = vertex.y;
            }
        }

        /* Return bounding-box. */
        return bounds;
    }
}
