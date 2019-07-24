package org.vitrivr.cineast.art.modules;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.art.modules.visualization.AbstractVisualizationModule;
import org.vitrivr.cineast.art.modules.visualization.VisualizationResult;
import org.vitrivr.cineast.art.modules.visualization.VisualizationType;
import org.vitrivr.cineast.core.color.ColorConverter;
import org.vitrivr.cineast.core.color.RGBContainer;
import org.vitrivr.cineast.core.color.ReadableLabContainer;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;
import org.vitrivr.cineast.core.db.DBSelector;
import org.vitrivr.cineast.core.util.web.ImageParser;

/**
 * Created by sein on 02.09.16.
 */
public class VisualizationDominantEdgeAverageColorGrid8 extends AbstractVisualizationModule {
  public VisualizationDominantEdgeAverageColorGrid8() {
    super();
    tableNames.put("DominantEdgeGrid8", "features_DominantEdgeGrid8");
    tableNames.put("AverageColorGrid8", "features_AverageColorGrid8");
  }

  private static final Logger LOGGER = LogManager.getLogger();

  @Override
  public String getDisplayName() {
    return "VisualizationDominantEdgeAverageColorGrid8";
  }

  @Override
  public String visualizeSegment(String segmentId) {
    DBSelector selector = selectors.get("DominantEdgeGrid8");
    List<Map<String, PrimitiveTypeProvider>> edgeResult = selector.getRows("id", segmentId);

    selector = selectors.get("AverageColorGrid8");
    List<Map<String, PrimitiveTypeProvider>> colorResult = selector.getRows("id", segmentId);
    int[][] colors = new int[8][8];
    for (Map<String, PrimitiveTypeProvider> row : colorResult) {
      int count = 0;
      float[] arr = row.get("feature").getFloatArray();
      for (int i = 0; i < arr.length; i += 3) {
        RGBContainer rgbContainer = ColorConverter.LabtoRGB(new ReadableLabContainer(arr[i], arr[i + 1], arr[i + 2]));
        colors[count % 8][count / 8] = rgbContainer.toIntColor();
        count++;
      }
    }

    BufferedImage image = new BufferedImage(256, 256, BufferedImage.TYPE_INT_RGB);

    Graphics2D graph = image.createGraphics();

    GradientPaint gradient;

    for (Map<String, PrimitiveTypeProvider> row : edgeResult) {
      float[] arr = row.get("feature").getFloatArray();
      for (int x = 0; x < 8; x++) {
        for (int y = 0; y < 8; y++) {
          switch ((int) arr[x * 8 + y]) {
            case -10:
              graph.setColor(new Color(colors[x][y]));
              graph.fillRect(x * 32, y * 32, 32, 32);
              break;
            case 2:
              gradient = new GradientPaint(x * 32, y * 32 + 16, new Color(colors[x][y]), x * 32 + 16, y * 32 + 16, Color.white, true);
              graph.setPaint(gradient);
              graph.fillRect(x * 32, y * 32, 32, 32);
              break;
            case 3:
              gradient = new GradientPaint(x * 32, y * 32, new Color(colors[x][y]), x * 32 + 16, y * 32 + 16, Color.white, true);
              graph.setPaint(gradient);
              graph.fillRect(x * 32, y * 32, 32, 32);
              break;
            case 0:
              gradient = new GradientPaint(x * 32 + 16, y * 32, new Color(colors[x][y]), x * 32 + 16, y * 32 + 16, Color.white, true);
              graph.setPaint(gradient);
              graph.fillRect(x * 32, y * 32, 32, 32);
              break;
            case 1:
              gradient = new GradientPaint(x * 32, y * 32 + 32, new Color(colors[x][y]), x * 32 + 16, y * 32 + 16, Color.white, true);
              graph.setPaint(gradient);
              graph.fillRect(x * 32, y * 32, 32, 32);
              break;
          }
        }
      }
    }
    graph.dispose();

    return ImageParser.BufferedImageToDataURL(image, "png");
  }

  @Override
  public List<VisualizationType> getVisualizations() {
    List<VisualizationType> types = new ArrayList<>();
    types.add(VisualizationType.VISUALIZATION_SEGMENT);
    return types;
  }

  @Override
  public VisualizationResult getResultType() {
    return VisualizationResult.IMAGE;
  }
}
