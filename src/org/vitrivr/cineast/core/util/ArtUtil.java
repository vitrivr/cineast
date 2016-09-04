package org.vitrivr.cineast.core.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.api.WebUtils;
import org.vitrivr.cineast.core.color.ColorConverter;
import org.vitrivr.cineast.core.color.RGBContainer;
import org.vitrivr.cineast.core.color.ReadableLabContainer;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;
import org.vitrivr.cineast.core.db.DBSelector;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;

/**
 * Created by sein on 30.08.16.
 */
public class ArtUtil {
  private static final Logger LOGGER = LogManager.getLogger();

  public static List<Map<String, PrimitiveTypeProvider>> sortBySequenceNumber(List<Map<String, PrimitiveTypeProvider>> list){
    Collections.sort(list, (a, b) -> a.get("number").getInt() < b.get("number").getInt() ? -1 : a.get("number").getInt() == b.get("number").getInt() ? 0 : 1);
    return list;
  }

  public static String pixelsToImage(int[] pixels, int sizeX, int sizeY, boolean isRgb){
    int pixelSize = 3;
    if(isRgb){
      pixelSize = 1;
    }
    if(sizeX * sizeY != pixels.length/pixelSize){
      LOGGER.error("Not matching number of available pixels and images size!");
      return null;
    }
    BufferedImage image = new BufferedImage(sizeX, sizeY, BufferedImage.TYPE_INT_RGB);

    for(int y=0;y<sizeY;y++){
      for(int x=0;x<sizeX;x++){
        int pos = (y*sizeX+x)*pixelSize;
        if(isRgb){
          image.setRGB(x, y, pixels[pos]);
        }
        else {
          image.setRGB(x, y, new Color(pixels[pos], pixels[pos + 1], pixels[pos + 2]).getRGB());
        }
      }
    }

    try {
      ImageIO.write(image, "png", new File("src/resources/imageArtUtil.png"));
    } catch (IOException e) {
      e.printStackTrace();
    }

    return WebUtils.BufferedImageToDataURL(image, "png");
  }

  /**
   * Scales a given Image in integer array format with the given multiplier.
   *
   * @param pixels containing the pixels, either as single integer in rgb format, or 3 integers in row for every pixel
   * @param multiplier scale multiplier, array size will increase cubic
   * @param sizeX x size of the given image
   * @param sizeY y size of the given image
   * @param isRgb true if the rgb is encoded in one single integer, false if 3 integers are used
   * @return scaled integer array representing the exact same image, just larger
   */
  public static int[] scalePixels(int[] pixels, int multiplier, int sizeX, int sizeY, boolean isRgb){
    int pixelSize = 3;
    if(isRgb){
      pixelSize = 1;
    }
    if(multiplier < 1){
      return pixels;
    }
    int[] newpixels = new int[pixels.length*multiplier*multiplier];
    for(int x=0;x<sizeX;x++){
      for(int y=0;y<sizeY;y++){
        for(int a=x*multiplier;a<(x+1)*multiplier;a++){
          for(int b=y*multiplier;b<(y+1)*multiplier;b++){
            for(int i=0;i<pixelSize;i++) {
              newpixels[(b * sizeX * multiplier + a) * pixelSize + i] = pixels[(y * sizeX + x) * pixelSize + i];
            }
          }
        }
      }
    }
    return newpixels;
  }

  /**
   * This reads a given shot into an integer array representing an image. This can be done on shots which have a given
   * size of values which represent a given position on a feature shot.
   *
   * @param shotId shot id to sue
   * @param selector DBSelector for the corresponding table
   * @param sizeX size x of the resulted image
   * @param sizeY size y of the resulted image
   * @return integer array representing an image in 3 integer rgb format
   */
  public static int[][][] shotToRGB(String shotId, DBSelector selector, int sizeX, int sizeY){
    java.util.List<Map<String, PrimitiveTypeProvider>> result = selector.getRows("id", shotId);

    int[][][] pixels = new int[sizeX][sizeY][3];

    for (Map<String, PrimitiveTypeProvider> row : result) {
      float[] arr = row.get("feature").getFloatArray();
      int count = 0;
      for(int x=0;x<sizeX;x++){
        for(int y=0;y<sizeY;y++) {
          RGBContainer rgbContainer = ColorConverter.LabtoRGB(new ReadableLabContainer(arr[count], arr[count + 1], arr[count + 2]));
          int color = rgbContainer.toIntColor();
          pixels[x][y][0] = rgbContainer.getRed(color);
          pixels[x][y][1] = rgbContainer.getGreen(color);
          pixels[x][y][2] = rgbContainer.getBlue(color);
          count+=3;
        }
      }
    }
    return pixels;
  }

  public static int[][] shotToInt(String shotId, DBSelector selector, int sizeX, int sizeY){
    java.util.List<Map<String, PrimitiveTypeProvider>> result = selector.getRows("id", shotId);

    int[][] pixels = new int[sizeX][sizeY];

    for (Map<String, PrimitiveTypeProvider> row : result) {
      float[] arr = row.get("feature").getFloatArray();
      int count = 0;
      for(int x=0;x<sizeX;x++){
        for(int y=0;y<sizeY;y++) {
          RGBContainer rgbContainer = ColorConverter.LabtoRGB(new ReadableLabContainer(arr[count], arr[count + 1], arr[count + 2]));
          pixels[x][y] = rgbContainer.toIntColor();
          count+=3;
        }
      }
    }
    return pixels;
  }
}
