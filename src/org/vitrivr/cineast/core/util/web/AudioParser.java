package org.vitrivr.cineast.core.util.web;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.vitrivr.cineast.core.data.frames.AudioDescriptor;
import org.vitrivr.cineast.core.data.frames.AudioFrame;
import org.vitrivr.cineast.core.util.LogHelper;

/**
 * @author rgasser
 * @version 1.0
 * @created 10.03.17
 */
public class AudioParser extends DataURLParser {
    /** Mimetype of supported data URL's. Only WAV audio is currently supported. */
    private static final String MIME_TYPE = "audio/wav";

    /**
     * Converts a Base64 data URL into a List auf AudioFrames.
     *
     * @param dataUrl String containing the data url.
     * @return List of AudioFrames or empty list, if conversion failed.
     */
    public static List<AudioFrame> parseWaveAudio(String dataUrl, float samplerate, int channels) {

        ArrayList<AudioFrame> list = new ArrayList<>();

	    /* Convert Base64 string into byte array. */
        byte[] bytes = dataURLtoByteArray(dataUrl, MIME_TYPE);
        if (bytes == null) {
          return list;
        }


        try {
            /* Read data as AudioInputStream and re-sample it. */
            ByteArrayInputStream rawByteStream = new ByteArrayInputStream(bytes);
            AudioInputStream inputAudio = AudioSystem.getAudioInputStream(rawByteStream);
            AudioFormat targetFormat = new AudioFormat(samplerate, 16, channels, true, false);
            AudioInputStream convertedAudio = AudioSystem.getAudioInputStream(targetFormat, inputAudio);

            /* Estimate duration of the segment. */
            long duration = (long) Math.floor((convertedAudio.getFrameLength()/samplerate));

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
                list.add(new AudioFrame(idx, timestamp, data, new AudioDescriptor(targetFormat.getSampleRate(), targetFormat.getChannels(), duration)));
                timestamp += (len*1000.0f)/(bytesPerSample * targetFormat.getChannels() * targetFormat.getSampleRate());
                idx += 1;
                read += len;
            }
        } catch (UnsupportedAudioFileException e) {
            LOGGER.error("Could not create audio frames from Base64 input because the file-format is not supported. {}", LogHelper.getStackTrace(e));
        } catch (IOException e) {
            LOGGER.error("Could not create audio frames from Base64 input due to a serious IO error: {}", LogHelper.getStackTrace(e));
        }

        return list;
    }
}
