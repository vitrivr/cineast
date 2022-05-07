package org.vitrivr.cineast.core.util.images;

import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.geometry.Positions;
import net.coobird.thumbnailator.resizers.Resizers;
import net.coobird.thumbnailator.resizers.configurations.Antialiasing;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.image.BufferedImage;
import java.io.IOException;

public class ImagePreprocessingHelper {

    private ImagePreprocessingHelper() {
    }

    private static final Logger LOGGER = LogManager.getLogger();

    /**
     * Scales an input image to the specified dimensions, not preserving the original aspect ratio
     */
    public static BufferedImage forceScale(BufferedImage img, int width, int height) {
        if (img == null) {
            return null;
        }
        if (img.getWidth() == width && img.getHeight() == height) {
            return img;
        }
        try {
            return Thumbnails.of(img).forceSize(width, height).asBufferedImage();
        } catch (IOException e) {
            LOGGER.error("Could not resize image", e);
            return null;
        }
    }

    /**
     * Scales image to fit and crops center square
     */
    public static BufferedImage squaredScaleCenterCrop(BufferedImage img, int size) {

        if (img == null) {
            return null;
        }

        try {
            BufferedImage tmp;

            if (img.getWidth() > img.getHeight()) {
                tmp = Thumbnails.of(img).height(size).resizer(Resizers.BICUBIC).antialiasing(Antialiasing.OFF).asBufferedImage();
            } else {
                tmp = Thumbnails.of(img).width(size).resizer(Resizers.BICUBIC).antialiasing(Antialiasing.OFF).asBufferedImage();
            }

            return Thumbnails.of(tmp).crop(Positions.CENTER).size(size, size).asBufferedImage();
        } catch (IOException e) {
            LOGGER.error("Error while preparing image", e);
            return null;
        }

    }

    /**
     * Transforms provided image to a linearized color tensor of shape Height x Width x Color
     */
    public static float[] imageToHWCArray(BufferedImage img, float[] mean, float[] std) {

        if (img == null) {
            return new float[0];
        }

        if (mean == null || mean.length < 3) {
            mean = new float[]{0f, 0f, 0f};
        }

        if (std == null || std.length < 3) {
            std = new float[]{1f, 1f, 1f};
        }

        int[] colors = img.getRGB(0, 0, img.getWidth(), img.getHeight(), null, 0, img.getWidth());

        float[] rgb = new float[img.getWidth() * img.getHeight() * 3];

        for (int i = 0; i < colors.length; i++) {
            int j = i * 3;
            rgb[j] = ((((colors[i] >> 16) & 0xFF) / 255f) - mean[0]) / std[0]; // r
            rgb[j + 1] = ((((colors[i] >> 8) & 0xFF) / 255f) - mean[1]) / std[1]; // g
            rgb[j + 2] = (((colors[i] & 0xFF) / 255f) - mean[2]) / std[2]; // b
        }

        return rgb;

    }

    /**
     * Transforms provided image to a linearized color tensor of shape Color x Height x Width
     */
    public static float[] imageToCHWArray(BufferedImage img, float[] mean, float[] std) {

        if (img == null) {
            return new float[0];
        }

        if (mean == null || mean.length < 3) {
            mean = new float[]{0f, 0f, 0f};
        }

        if (std == null || std.length < 3) {
            std = new float[]{1f, 1f, 1f};
        }

        int[] colors = img.getRGB(0, 0, img.getWidth(), img.getHeight(), null, 0, img.getWidth());

        final int gOffset = colors.length;
        final int bOffset = 2 * gOffset;

        float[] rgb = new float[img.getWidth() * img.getHeight() * 3];

        for (int i = 0; i < colors.length; i++) {

            rgb[i] = ((((colors[i] >> 16) & 0xFF) / 255f) - mean[0]) / std[0]; // r
            rgb[i + gOffset] = ((((colors[i] >> 8) & 0xFF) / 255f) - mean[1]) / std[1]; // g
            rgb[i + bOffset] = (((colors[i] & 0xFF) / 255f) - mean[2]) / std[2]; // b
        }

        return rgb;

    }

}
