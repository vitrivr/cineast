package org.vitrivr.cineast.art.modules;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.api.WebUtils;
import org.vitrivr.cineast.art.modules.abstracts.AbstractVisualizationModule;
import org.vitrivr.cineast.art.modules.visualization.Visualization;
import org.vitrivr.cineast.art.modules.visualization.VisualizationType;
import org.vitrivr.cineast.core.color.ColorConverter;
import org.vitrivr.cineast.core.color.RGBContainer;
import org.vitrivr.cineast.core.color.ReadableLabContainer;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;
import org.vitrivr.cineast.core.db.ADAMproSelector;
import org.vitrivr.cineast.core.db.DBSelector;
import org.vitrivr.cineast.core.db.DBSelectorSupplier;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;

/**
 * Created by sein on 26.08.16.
 */
public class VisualizationAverageColorGrid8 extends AbstractVisualizationModule{
  public VisualizationAverageColorGrid8() {
    super("features_AverageColorGrid8");
  }

  @Override
  public void init(DBSelectorSupplier supplier){
    selectors = new HashMap();
    DBSelector selector = supplier.get();
    selector.open(tableName);
    selectors.put(tableName, selector);
    selector = supplier.get();
    selector.open("cineast_segment");
    selectors.put("cineast_segment", selector);
  }

  private static final Logger LOGGER = LogManager.getLogger();

  //Test main function
  public static void main(String[] args) {
    VisualizationAverageColorGrid8 module = new VisualizationAverageColorGrid8();
    module.init(new DBSelectorSupplier() {
      @Override
      public DBSelector get() {
        return new ADAMproSelector();
      }
    });
    //System.out.println(module.visualizeShot("655386"));
    System.out.println(module.visualizeVideo("11"));
    module.finish();
  }

  @Override
  public String getDisplayName() {
    return "VisualizationAverageColorGrid8";
  }

  //here we need some refactoring, as visualizeVideo and visualizeShot are doing similar stuff

  @Override
  public String visualizeVideo(String movieId){
    DBSelector selector = selectors.get(tableName);
    DBSelector shotSelector = selectors.get("cineast_segment");
    List<Map<String, PrimitiveTypeProvider>> shots = shotSelector.getRows("multimediaobject", movieId);

    LOGGER.info("Need to calculate average of " + shots.size() + " shots...");

    int[] pixels = new int[8*8*3];
    for (Map<String, PrimitiveTypeProvider> shot : shots) {
      int[] shotPixels = shotToRGB(shot.get("id").getString());
      for(int i=0;i<shotPixels.length;i++){
        pixels[i] += shotPixels[i];
      }
    }

    for(int i=0;i<pixels.length;i++){
      pixels[i] = pixels[i]/shots.size();
    }

    return pixelsToImage(pixels);
  }

  private String pixelsToImage(int[] pixels){
    BufferedImage image = new BufferedImage(8, 8, BufferedImage.TYPE_INT_RGB);

    for(int y=0;y<8;y++){
      for(int x=0;x<8;x++){
        int pos = (y*8+x)*3;
        image.setRGB(x, y, new Color(pixels[pos], pixels[pos+1], pixels[pos+2]).getRGB());
      }
    }

    return WebUtils.BufferedImageToDataURL(image, "png");
  }

  private int[] shotToRGB(String shotId){
    DBSelector selector = selectors.get(tableName);
    List<Map<String, PrimitiveTypeProvider>> result = selector.getRows("id", shotId);

    int[] pixels = new int[8*8*3];

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

  @Override
  public String visualizeShot(String shotId) {
    return pixelsToImage(shotToRGB(shotId));
  }

  @Override
  public List<VisualizationType> getVisualizations(){
    List<VisualizationType> types = new ArrayList();
    types.add(VisualizationType.VISUALIZATION_SHOT);
    return types;
  }
}
