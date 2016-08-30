package org.vitrivr.cineast.core.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.api.WebUtils;
import org.vitrivr.cineast.core.color.ColorConverter;
import org.vitrivr.cineast.core.color.RGBContainer;
import org.vitrivr.cineast.core.color.ReadableLabContainer;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;
import org.vitrivr.cineast.core.db.DBSelector;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;

/**
 * Created by sein on 30.08.16.
 */
public class ArtUtil {
  private static final Logger LOGGER = LogManager.getLogger();

  public static String pixelsToImage(int[] pixels, int sizeX, int sizeY, boolean isRgb){
    int pixelSize = 3;
    if(isRgb){
      pixelSize = 1;
    }
    if(sizeX * sizeY != pixels.length/pixelSize){
      LOGGER.error("Not matching number of available pixels and images size!");
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

    return WebUtils.BufferedImageToDataURL(image, "png");
  }

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

  public static int[] shotToRGB(String shotId, DBSelector selector, int sizeX, int sizeY){
    java.util.List<Map<String, PrimitiveTypeProvider>> result = selector.getRows("id", shotId);

    int[] pixels = new int[sizeX*sizeY*3];

    for (Map<String, PrimitiveTypeProvider> row : result) {
      float[] arr = row.get("feature").getFloatArray();
      for (int i = 0; i < arr.length; i+=3) {
        RGBContainer rgbContainer = ColorConverter.LabtoRGB(new ReadableLabContainer(arr[i], arr[i + 1], arr[i + 2]));
        int color = rgbContainer.toIntColor();
        pixels[i] = rgbContainer.getRed(color);
        pixels[i+1] = rgbContainer.getGreen(color);
        pixels[i+2] = rgbContainer.getBlue(color);
      }
    }
    return pixels;
  }
}
