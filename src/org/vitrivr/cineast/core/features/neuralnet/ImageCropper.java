package org.vitrivr.cineast.core.features.neuralnet;

import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.geometry.Positions;

import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * Hides the current implementation of resizing & cropping an image
 * <p>
 * Created by silvan on 23.08.16.
 */
public class ImageCropper {

    /**
     * Scale an Image to specified x*y parameters
     * First scales the image to whichever is smaller, x or y
     * then crops the image by cutting off from the center
     */
    public static BufferedImage scaleAndCropImage(BufferedImage img, int x, int y) {
        try {
            return Thumbnails.of(img).size(x, y).crop(Positions.CENTER).asBufferedImage();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}