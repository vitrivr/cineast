package org.vitrivr.cineast.api;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.apache.commons.codec.binary.Base64;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.data.frames.AudioFrame;
import org.vitrivr.cineast.core.util.LogHelper;

public class WebUtils {

	private WebUtils() {
	}

	private static final Logger LOGGER = LogManager.getLogger();

	/**
	 * Converts a Base64 data URL to a BufferedImage.
	 *
	 * @param dataUrl String containing the data url.
	 * @return BufferedImage or null, if conversion failed.
	 */
	public static BufferedImage dataURLtoBufferedImage(String dataUrl) {
		
		 /* Convert Base64 string into byte array. */
		byte[] bytes = dataURLtoByteArray(dataUrl);
		if (bytes == null) return null;


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
     * Converts a Base64 data URL into a List auf AudioFrames.
	 *
     * @param dataUrl String containing the data url.
     * @return List of AudioFrames or empty list, if conversion failed.
     */
	public static List<AudioFrame> dataURLtoAudioFrames(String dataUrl) {

	    ArrayList<AudioFrame> list = new ArrayList<>();

	    /* Convert Base64 string into byte array. */
	    byte[] bytes = dataURLtoByteArray(dataUrl);
		if (bytes == null) return list;


        try {
            /* Read data as AudioInputStream and re-sample it. */
            ByteArrayInputStream rawByteStream = new ByteArrayInputStream(bytes);
            AudioInputStream inputAudio = AudioSystem.getAudioInputStream(rawByteStream);
            AudioFormat targetFormat = new AudioFormat(22050.0f, 16, 1, true, false);
            AudioInputStream convertedAudio = AudioSystem.getAudioInputStream(targetFormat, inputAudio);

            /* Constants:
             * - Length of a single AudioFrame (in bytes)
             * - Total length of the frames data in the AudioInputStream.
             */

			final int framesize = 2048;
			final int bytesPerSample = targetFormat.getSampleSizeInBits()/8;
			final int bytesPerFrame = framesize * bytesPerSample;
            final long length = convertedAudio.getFrameLength() * bytesPerSample * targetFormat.getChannels();

            /*
             * Read the data into constant length AudioFrames.
             */
            int read = 0;
            int idx = 0;
            long timestamp = 0;
			boolean done = false;
            while (!done) {
                /* Allocate a byte-array for the frames-data. */
                byte[] data = null;
                if (read + bytesPerFrame < length) {
                    data = new byte[bytesPerFrame];
                } else {
                    data = new byte[(int)(length-read)];
                    done = true;
                }
                /* Read frames-data and create AudioFrame. */
                int len = convertedAudio.read(data, 0, data.length);
                list.add(new AudioFrame(idx, timestamp, targetFormat.getSampleRate(), targetFormat.getChannels(), data));
				timestamp += (len*1000.0f)/(bytesPerSample * targetFormat.getChannels() * targetFormat.getSampleRate());
                idx += 1;
                read += len;
            }
        } catch (UnsupportedAudioFileException e) {
            LOGGER.error("Could not create frames frames from Base64 input because the file-format is not supported. {}", LogHelper.getStackTrace(e));
        } catch (IOException e) {
            LOGGER.error("Could not create frames frames from Base64 input due to a serious IO error: {}", LogHelper.getStackTrace(e));
        }

        return list;
    }

	/**
	 * Converts a base 64 data URL to a byte array and returns it. Only supported types of
     * data URLs are currently converted (i.e. images and frames).
	 *
	 * @param dataUrl String containing the data url.
	 * @return Byte array of the data.
	 */
	public static byte[] dataURLtoByteArray(String dataUrl) {
		dataUrl = dataUrl.replace(' ', '+');

		/* Check if string is actually a valid data URL. */
		if (!dataUrl.startsWith("data:")) {
			LOGGER.warn("This is not a valid data URL.");
			return null;
		}

		/* Check if data URL is of supported type. */
		if (dataUrl.substring(5, 11).equals("image/")) {
			LOGGER.info("Data URL has been identified as image.");
		} else if (dataUrl.substring(5, 11).equals("frames/")) {
			LOGGER.info("Data URL has been identified as frames.");
		} else {
			LOGGER.warn("Type of data URL is neither image nor frames and therefore not supported.");
			return null;
		}

		/* Convert and return byte array. */
		int headerLength = dataUrl.indexOf(',');
		String base46data = dataUrl.substring(headerLength + 1);
		return Base64.decodeBase64(base46data);
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

}