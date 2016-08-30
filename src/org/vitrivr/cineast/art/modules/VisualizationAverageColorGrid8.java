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

import java.awt.image.BufferedImage;
import java.util.*;

/**
 * Created by sein on 26.08.16.
 */
public class VisualizationAverageColorGrid8 extends AbstractVisualizationModule implements Visualization{
  protected VisualizationAverageColorGrid8() {
    super("features_AverageColorGrid8");
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
    System.out.println(module.visualizeShot("655378"));
    module.finish();
  }

  @Override
  public String getName() {
    return "VisualizationAverageColorGrid8";
  }

  @Override
  public String visualizeShot(String shotId) {
    DBSelector selector = selectors.get(tableName);
    List<Map<String, PrimitiveTypeProvider>> result = selector.getRows("id", shotId);

    BufferedImage image = new BufferedImage(8, 8, BufferedImage.TYPE_INT_RGB);

    for (Map<String, PrimitiveTypeProvider> row : result) {
      float[] arr = row.get("feature").getFloatArray();
      for (int i = 0; i < arr.length; i+=3) {
        RGBContainer rgbContainer = ColorConverter.LabtoRGB(new ReadableLabContainer(arr[i], arr[i + 1], arr[i + 2]));
        image.setRGB((i/3)%8, (int)(i/24), rgbContainer.toIntColor());
      }
    }

    return WebUtils.BufferedImageToDataURL(image, "png");
  }

  @Override
  public List<VisualizationType> getVisualizations(){
    List<VisualizationType> types = new ArrayList();
    types.add(VisualizationType.VISUALIZATION_SHOT);
    return types;
  }
}
