package org.vitrivr.cineast.core.util.web;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.apache.commons.codec.binary.Base64;
import org.vitrivr.cineast.core.util.LogHelper;

/**
 * @author rgasser
 * @version 1.0
 * @created 10.03.17
 */
public class ImageParser extends DataURLParser {
    /**
     * Converts a Base64 data URL to a BufferedImage.
     *
     * @param dataUrl String containing the data url.
     * @return BufferedImage or null, if conversion failed.
     */
    public static BufferedImage dataURLtoBufferedImage(String dataUrl) {

		 /* Convert Base64 string into byte array. */
        byte[] bytes = dataURLtoByteArray(dataUrl, "image/");
        if (bytes == null) {
          return null;
        }

        ByteArrayInputStream binstream = new ByteArrayInputStream(bytes);

        BufferedImage bimg;
        try {
            bimg = ImageIO.read(binstream);
        } catch (IOException e) {
            LOGGER.error("could not make image, {}", LogHelper.getStackTrace(e));
            return null;
        }

        return bimg;
    }

    /**
     *
     * @param img
     * @param format
     * @return
     */
    public static String BufferedImageToDataURL(BufferedImage img, String format){
        ByteArrayOutputStream bouts = new ByteArrayOutputStream();
        try {
            ImageIO.write(img, format, bouts);
        } catch (IOException e) {
            LOGGER.error("could not make image, {}", LogHelper.getStackTrace(e));
            return null;
        }
        String base64 = new String(Base64.encodeBase64(bouts.toByteArray()));
        return "data:image/" + format + ";base64," + base64;
    }

    /**
     * Checks, if provided data URL is a valid image. Returns true if so and  false otherwise.
     * No structural analysis is performed! Only the raw, data URL is being checked.
     *
     * @param dataUrl Data URL that should be checked.
     * @return True, if data URL is a valid Three v4 JSON geometry.
     */
    public static boolean isValidImage(String dataUrl) {
        return isValidDataUrl(dataUrl, "image/");
    }
}
