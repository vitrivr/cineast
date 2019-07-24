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
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;
import org.vitrivr.cineast.core.db.DBSelector;
import org.vitrivr.cineast.core.util.web.ImageParser;

/**
 * Created by sein on 02.09.16.
 */
public class VisualizationDominantEdgeGrid16 extends AbstractVisualizationModule {
  public VisualizationDominantEdgeGrid16() {
    super();
    tableNames.put("DominantEdgeGrid16", "features_DominantEdgeGrid16");
  }

  private static final Logger LOGGER = LogManager.getLogger();

  @Override
  public String getDisplayName() {
    return "VisualizationDominantEdgeGrid16";
  }

  @Override
  public String visualizeSegment(String segmentId) {
    DBSelector selector = selectors.get("DominantEdgeGrid16");
    List<Map<String, PrimitiveTypeProvider>> result = selector.getRows("id", segmentId);

    BufferedImage image = new BufferedImage(512, 512, BufferedImage.TYPE_INT_RGB);
    Graphics2D graph = image.createGraphics();
    GradientPaint gradient;

    for (Map<String, PrimitiveTypeProvider> row : result) {
      float[] arr = row.get("feature").getFloatArray();
      for (int x = 0; x < 16; x++) {
        for (int y = 0; y < 16; y++) {
          switch ((int) arr[x * 16 + y]) {
            case -10:
              graph.setColor(Color.lightGray);
              break;
            case 2:
              gradient = new GradientPaint(x * 32, y * 32 + 16, Color.white, x * 32 + 32, y * 32 + 16, Color.black);
              graph.setPaint(gradient);
              break;
            case 3:
              gradient = new GradientPaint(x * 32, y * 32, Color.white, x * 32 + 32, y * 32 + 32, Color.black);
              graph.setPaint(gradient);
              break;
            case 0:
              gradient = new GradientPaint(x * 32 + 16, y * 32, Color.white, x * 32 + 16, y * 32 + 32, Color.black);
              graph.setPaint(gradient);
              break;
            case 1:
              gradient = new GradientPaint(x * 32, y * 32 + 32, Color.white, x * 32 + 32, y * 32, Color.black);
              graph.setPaint(gradient);
              break;
          }
          graph.fillRect(x * 32, y * 32, 32, 32);
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
