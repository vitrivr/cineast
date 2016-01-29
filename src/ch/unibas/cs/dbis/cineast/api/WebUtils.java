package ch.unibas.cs.dbis.cineast.api;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.apache.commons.codec.binary.Base64;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ch.unibas.cs.dbis.cineast.core.util.LogHelper;

public class WebUtils {

	private WebUtils() {
	}

	private static final Logger LOGGER = LogManager.getLogger();

	public static BufferedImage dataURLtoBufferedImage(String dataUrl) {

		dataUrl = dataUrl.replaceAll("\\s+", "");

		if (!dataUrl.startsWith("data:")) {
			LOGGER.warn("no valid data url");
			return null;
		}

		if (!dataUrl.substring(5, 11).equals("image/")) {
			LOGGER.warn("no image in data url");
			return null;
		}

		int headerLength = dataUrl.indexOf(',');

		String base46data = dataUrl.substring(headerLength + 1);

		byte[] bytes = Base64.decodeBase64(base46data);

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

}
