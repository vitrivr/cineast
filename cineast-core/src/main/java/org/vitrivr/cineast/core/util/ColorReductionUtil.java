package org.vitrivr.cineast.core.util;

import org.vitrivr.cineast.core.color.ColorConverter;
import org.vitrivr.cineast.core.color.FuzzyColorHistogramQuantizer;
import org.vitrivr.cineast.core.color.ReadableHSVContainer;
import org.vitrivr.cineast.core.data.MultiImage;
import org.vitrivr.cineast.core.data.MultiImageFactory;

public class ColorReductionUtil {

  private ColorReductionUtil(){}
  
  private static enum Color11 {
    RED(0xFF0000), ORANGE(0xFFAA00), YELLOW(0xFFFF00), GREEN(0x00FF00), CYAN(0x00FFFF), BLUE(
        0x0000FF), VIOLET(0xAA00FF), PURPLE(0xFF00AA), WHITE(0xFFFFFF), GREY(0x808080), BLACK(0);

    private final int color;

    Color11(int color) {
      this.color = color;
    }

  }
  
  public static int quantize15(int rgb){
    return FuzzyColorHistogramQuantizer.quantize(ColorConverter.cachedRGBtoLab(rgb)).getRGB().toIntColor();
  }
  
  public static MultiImage quantize15(MultiImage img){
    int[] inColors = img.getColors();
    int[] outColors = new int[inColors.length];
    
    for(int i = 0; i< inColors.length; ++i){
      outColors[i] = quantize15(inColors[i]);
    }
    
    return MultiImageFactory.newMultiImage(img.getWidth(), img.getHeight(), outColors);
  }
  
  public static int quantize11(int rgb){
    return quantize11(ColorConverter.cachedRGBtoHSV(rgb)).color;
  }
  
  public static MultiImage quantize11(MultiImage img){
    int[] inColors = img.getColors();
    int[] outColors = new int[inColors.length];
    
    for(int i = 0; i< inColors.length; ++i){
      outColors[i] = quantize11(inColors[i]);
    }
    
    return MultiImageFactory.newMultiImage(img.getWidth(), img.getHeight(), outColors);
  }
  
  private static Color11 quantize11(ReadableHSVContainer hsv) {
    if (hsv.getV() < 0.25f) {
      return Color11.BLACK;
    }
    if (hsv.getS() < 0.20) {
      if (hsv.getV() > .8f) {
        return Color11.WHITE;
      } else {
        return Color11.GREY;
      }
    }

    if(hsv.getS() * hsv.getV() < 0.1f){
      return Color11.GREY;
    }
    
    float angle = hsv.getH() * 360f;

    if (angle > 25f && angle <= 50f) {
      return Color11.ORANGE;
    } else if (angle > 50f && angle <= 70f) {
      return Color11.YELLOW;
    } else if (angle > 70f && angle <= 160f) {
      return Color11.GREEN;
    } else if (angle > 160f && angle <= 200f) {
      return Color11.CYAN;
    } else if (angle > 200f && angle <= 260f) {
      return Color11.BLUE;
    } else if (angle > 260f && angle <= 300f) {
      return Color11.VIOLET;
    } else if (angle > 300f && angle <= 330f) {
      return Color11.PURPLE;
    }
    return Color11.RED;
  }
  
}
