package org.vitrivr.cineast.core.util.images;

import boofcv.alg.color.ColorHsv;
import boofcv.alg.filter.binary.BinaryImageOps;
import boofcv.alg.filter.binary.Contour;
import boofcv.alg.filter.binary.GThresholdImageOps;
import boofcv.alg.filter.binary.ThresholdImageOps;
import boofcv.io.image.ConvertBufferedImage;
import boofcv.struct.ConnectRule;
import boofcv.struct.image.GrayF32;
import boofcv.struct.image.GrayS32;
import boofcv.struct.image.GrayU8;
import boofcv.struct.image.Planar;
import georegression.metric.UtilAngle;
import georegression.struct.point.Point2D_I32;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;


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
     * This method provides the best results if the image is a black & white, i.e. factually binary, image!
     * See {@link ContourHelper#segmentImageByColour(BufferedImage,float[])} to convert a coloured image to a binary image.
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
     * Segments a colored image by identifying the average color in a given square region and turning all pixels that are
     * close to that color to white.
     *
     * @param image The image that should be converted
     * @param startX The x-coordinate of the top-left corner of the square region.
     * @param startY The y-coordinate of the top-left corner of the square region.
     * @param size The size of the square region.
     * @return Converted image where pixels close to the provided color are white and the others are black
     */
    public static BufferedImage segmentImageByColor(BufferedImage image, int startX, int startY, int size) {
        final float[] avgColor = new float[]{0.0f,0.0f,0.0f};
        for (int x = startX; x<startX + size; x++) {
            for (int y = startY; y<startY + size; y++) {
                int rgb = image.getRGB(x,y);
                avgColor[0] += (rgb >> 16) & 0xFF;
                avgColor[1] += (rgb >> 8) & 0xFF;
                avgColor[2] += rgb & 0xFF;
            }
        }
        avgColor[0] /= size*size;
        avgColor[1] /= size*size;
        avgColor[2] /= size*size;
        return segmentImageByColour(image, avgColor);
    }

    /**
     * Segments a colored image by turning all pixels that are close to the provided color to white.
     *
     * @param image The image that should be converted.
     * @param colorRgb The colour that should be turned to white.
     * @return Converted image where pixels close to the provided color are white and the others are black
     */
    public static BufferedImage segmentImageByColour(BufferedImage image, float[] colorRgb) {
        /* Phase 1): Convert average RGB color to HSV. */
        final float[] avgHsvColor = new float[]{0.0f,0.0f,0.0f};
        ColorHsv.rgbToHsv(colorRgb[0], colorRgb[1], colorRgb[2], avgHsvColor);

        /* Phase 2a): Convert the input BufferedImage to a HSV image and extract hue and saturation bands, which are independent of intensity. */
        final Planar<GrayF32> input = ConvertBufferedImage.convertFromPlanar(image,null, true, GrayF32.class);
        final Planar<GrayF32> hsv = input.createSameShape();
        ColorHsv.rgbToHsv_F32(input,hsv);

        final GrayF32 H = hsv.getBand(0);
        final GrayF32 S = hsv.getBand(1);

        /* Phase 2b): Determine thresholds. */
        float maxDist2 = 0.4f*0.4f;
        float adjustUnits = (float)(Math.PI/2.0);

        /* Phase 3): For each pixel in the image, determine distance to average color. If color is closed, turn pixel white. */
        final BufferedImage output = new BufferedImage(input.width,input.height,BufferedImage.TYPE_INT_RGB);
        for(int y = 0; y < hsv.height; y++) {
            for(int x = 0; x < hsv.width; x++) {
                // Hue is an angle in radians, so simple subtraction doesn't work
                float dh = UtilAngle.dist(H.unsafe_get(x,y),avgHsvColor[0]);
                float ds = (S.unsafe_get(x,y)-avgHsvColor[1])*adjustUnits;

                // this distance measure is a bit naive, but good enough for to demonstrate the concept
                float dist2 = dh*dh + ds*ds;
                if( dist2 <= maxDist2 ) {
                    output.setRGB(x,y,Color.WHITE.getRGB());
                }
            }
        }
        return output;
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
     * Determines whether or not a given image is a binary image. This method first checks the type of the image. If by
     * type, the image is non-binary, the individual pixels are checked.
     *
     * @param image The image that should be checked.
     * @return True if image is a binary image. False otherwise.
     */
    public static boolean isBinary(BufferedImage image) {
        if (image.getType() == BufferedImage.TYPE_BYTE_BINARY) return true;
        final int black = Color.black.getRGB();
        final int white = Color.white.getRGB();
        for (int x = 0; x<image.getWidth(); x++) {
            for (int y = 0; x<image.getHeight(); y++) {
                if (image.getRGB(x,y) != black && image.getRGB(x,y) != white) {
                    return false;
                }
            }
        }
        return true;
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
