package org.vitrivr.cineast.art.modules;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.api.WebUtils;
import org.vitrivr.cineast.art.modules.abstracts.AbstractVisualizationModule;
import org.vitrivr.cineast.art.modules.visualization.VisualizationResult;
import org.vitrivr.cineast.art.modules.visualization.VisualizationType;
import org.vitrivr.cineast.core.color.ColorConverter;
import org.vitrivr.cineast.core.color.RGBContainer;
import org.vitrivr.cineast.core.color.ReadableLabContainer;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;
import org.vitrivr.cineast.core.db.ADAMproSelector;
import org.vitrivr.cineast.core.db.DBSelector;
import org.vitrivr.cineast.core.db.DBSelectorSupplier;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by sein on 02.09.16.
 */
public class VisualizationDominantEdgeAverageColorGrid16 extends AbstractVisualizationModule {
  protected VisualizationDominantEdgeAverageColorGrid16() {
    super();
    tableNames.put("DominantEdgeGrid16", "features_DominantEdgeGrid16");
    tableNames.put("AverageColorGrid8", "features_AverageColorGrid8");
  }

  private static final Logger LOGGER = LogManager.getLogger();

  @Override
  public String getDisplayName() {
    return "VisualizationDominantEdgeAverageColorGrid16";
  }

  public static void main(String[] args) {
    VisualizationDominantEdgeAverageColorGrid16 module = new VisualizationDominantEdgeAverageColorGrid16();
    module.init(new DBSelectorSupplier() {
      @Override
      public DBSelector get() {
        return new ADAMproSelector();
      }
    });
    System.out.println(module.visualizeSegment("1900546"));
    //System.out.println(module.visualizeMultimediaobject("11", 60));
    module.finish();
  }

  @Override
  public String visualizeSegment(String segmentId) {
    DBSelector selector = selectors.get("DominantEdgeGrid16");
    List<Map<String, PrimitiveTypeProvider>> edgeResult = selector.getRows("id", segmentId);

    selector = selectors.get("AverageColorGrid8");
    List<Map<String, PrimitiveTypeProvider>> colorResult = selector.getRows("id", segmentId);
    int[][] colors = new int[8][8];
    for (Map<String, PrimitiveTypeProvider> row : colorResult) {
      int count = 0;
      float[] arr = row.get("feature").getFloatArray();
      for (int i = 0; i < arr.length; i += 3) {
        RGBContainer rgbContainer = ColorConverter.LabtoRGB(new ReadableLabContainer(arr[i], arr[i + 1], arr[i + 2]));
        colors[count / 8][count % 8] = rgbContainer.toIntColor();
        count++;
      }
    }

    BufferedImage image = new BufferedImage(512, 512, BufferedImage.TYPE_INT_RGB);

    Graphics2D graph = image.createGraphics();

    GradientPaint gradient;

    for (Map<String, PrimitiveTypeProvider> row : edgeResult) {
      float[] arr = row.get("feature").getFloatArray();
      for (int x = 0; x < 16; x++) {
        for (int y = 0; y < 16; y++) {
          switch ((int) arr[x * 16 + y]) {
            case -10:
              graph.setColor(new Color(colors[x/2][y/2]));
              graph.fillRect(x * 32, y * 32, 32, 32);
              break;
            case 2:
              gradient = new GradientPaint(x * 32, y * 32 + 16, new Color(colors[x/2][y/2]), x * 32 + 16, y * 32 + 16, Color.white, true);
              graph.setPaint(gradient);
              graph.fillRect(x * 32, y * 32, 32, 32);
              break;
            case 3:
              gradient = new GradientPaint(x * 32, y * 32, new Color(colors[x/2][y/2]), x * 32 + 16, y * 32 + 16, Color.white, true);
              graph.setPaint(gradient);
              graph.fillRect(x * 32, y * 32, 32, 32);
              break;
            case 0:
              gradient = new GradientPaint(x * 32 + 16, y * 32, new Color(colors[x/2][y/2]), x * 32 + 16, y * 32 + 16, Color.white, true);
              graph.setPaint(gradient);
              graph.fillRect(x * 32, y * 32, 32, 32);
              break;
            case 1:
              gradient = new GradientPaint(x * 32, y * 32 + 32, new Color(colors[x/2][y/2]), x * 32 + 16, y * 32 + 16, Color.white, true);
              graph.setPaint(gradient);
              graph.fillRect(x * 32, y * 32, 32, 32);
              break;
          }
        }
      }
    }
    graph.dispose();

    try {
      ImageIO.write(image, "png", new File("src/resources/imageDominantEdgeAverageColorGrid16.png"));
    } catch (IOException e) {
      e.printStackTrace();
    }

    return WebUtils.BufferedImageToDataURL(image, "png");
  }

  @Override
  public List<VisualizationType> getVisualizations() {
    List<VisualizationType> types = new ArrayList();
    types.add(VisualizationType.VISUALIZATION_SHOT);
    return types;
  }

  @Override
  public VisualizationResult getResultType() {
    return VisualizationResult.IMAGE;
  }
}
