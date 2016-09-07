package org.vitrivr.cineast.core.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.color.ColorConverter;
import org.vitrivr.cineast.core.color.RGBContainer;
import org.vitrivr.cineast.core.color.ReadableLabContainer;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;
import org.vitrivr.cineast.core.db.DBSelector;

import java.util.Map;

/**
 * Created by sein on 30.08.16.
 */
public class ArtUtil {
  private static final Logger LOGGER = LogManager.getLogger();

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
