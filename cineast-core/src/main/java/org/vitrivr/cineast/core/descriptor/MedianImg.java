package org.vitrivr.cineast.core.descriptor;

import java.util.Arrays;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.color.RGBContainer;
import org.vitrivr.cineast.core.color.ReadableRGBContainer;
import org.vitrivr.cineast.core.data.frames.VideoFrame;
import org.vitrivr.cineast.core.data.raw.images.MultiImage;
import org.vitrivr.cineast.core.util.DecodingError;

/**
 * WARNING: EXTREMELY SLOW
 */
public class MedianImg {

  private static final Logger LOGGER = LogManager.getLogger();

  private MedianImg() {
  }

  public static MultiImage getMedian(List<VideoFrame> videoFrames) {

    LOGGER.traceEntry();

    MultiImage first = videoFrames.get(0).getImage();
    int width = first.getWidth(), height = first.getHeight();

    short[] buffer = new short[width * height * 128];

    int[] result = new int[width * height];

    //red pass
    try {
      for (VideoFrame f : videoFrames) {
        int[] colors = f.getImage().getColors();
        for (int i = 0; i < colors.length; ++i) {
          buffer[128 * i + (RGBContainer.getRed(colors[i])) / 2]++;
        }
        colors = null;
      }
    } catch (Exception e) {
      throw new DecodingError();
    }
    short[] hist = new short[128];

    for (int i = 0; i < result.length; ++i) {
      System.arraycopy(buffer, i * 128, hist, 0, 128);
      int r = medianFromHistogram(hist) * 2;
      result[i] = r;
    }

    //green pass

    Arrays.fill(buffer, (short) 0);
    try {
      for (VideoFrame f : videoFrames) {
        int[] colors = f.getImage().getColors();
        for (int i = 0; i < colors.length; ++i) {
          buffer[128 * i + (RGBContainer.getGreen(colors[i])) / 2]++;
        }
        colors = null;
      }
    } catch (Exception e) {
      throw new DecodingError();
    }

    for (int i = 0; i < result.length; ++i) {
      System.arraycopy(buffer, i * 128, hist, 0, 128);
      int r = result[i];
      int g = medianFromHistogram(hist) * 2;
      result[i] = ReadableRGBContainer.toIntColor(r, g, 0);
    }

    //blue pass
    Arrays.fill(buffer, (short) 0);
    try {
      for (VideoFrame f : videoFrames) {
        int[] colors = f.getImage().getColors();
        for (int i = 0; i < colors.length; ++i) {
          buffer[128 * i + (RGBContainer.getBlue(colors[i])) / 2]++;
        }
        colors = null;
      }
    } catch (Exception e) {
      throw new DecodingError();
    }

    for (int i = 0; i < result.length; ++i) {
      System.arraycopy(buffer, i * 128, hist, 0, 128);
      int r = ReadableRGBContainer.getRed(result[i]);
      int g = ReadableRGBContainer.getGreen(result[i]);
      int b = medianFromHistogram(hist) * 2;
      result[i] = ReadableRGBContainer.toIntColor(r, g, b);
    }

    buffer = null;

    System.gc();
    LOGGER.traceExit();
    return first.factory().newMultiImage(width, height, result);
  }

  private static int medianFromHistogram(short[] hist) {
    int pos_l = 0, pos_r = hist.length - 1;
    int sum_l = uShortToInt(hist[pos_l]), sum_r = uShortToInt(hist[pos_r]);

    while (pos_l < pos_r) {
      if (sum_l < sum_r) {
        sum_l += uShortToInt(hist[++pos_l]);
      } else {
        sum_r += uShortToInt(hist[--pos_r]);
      }
    }
    return pos_l;
  }

  private static int uShortToInt(short s) {
    return (s % 0xFFFF);
  }

}
