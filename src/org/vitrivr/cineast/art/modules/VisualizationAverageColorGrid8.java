package org.vitrivr.cineast.art.modules;

import org.vitrivr.cineast.api.WebUtils;
import org.vitrivr.cineast.art.modules.abstracts.AbstractVisualizationModule;
import org.vitrivr.cineast.art.modules.visualization.VisualizationResult;
import org.vitrivr.cineast.art.modules.visualization.VisualizationType;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;
import org.vitrivr.cineast.core.db.DBSelector;
import org.vitrivr.cineast.core.util.ArtUtil;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by sein on 26.08.16.
 */
public class VisualizationAverageColorGrid8 extends AbstractVisualizationModule {
  public VisualizationAverageColorGrid8() {
    super();
    tableNames.put("AverageColorGrid8", "features_AverageColorGrid8");
  }

  @Override
  public String getDisplayName() {
    return "VisualizationAverageColorGrid8";
  }

  @Override
  public String visualizeMultimediaobject(String multimediaobjectId) {
    DBSelector selector = selectors.get("AverageColorGrid8");
    DBSelector shotSelector = selectors.get(segmentTable);
    List<Map<String, PrimitiveTypeProvider>> shots = shotSelector.getRows("multimediaobject", multimediaobjectId);

    int[][][] pixels = new int[8][8][3];
    for (Map<String, PrimitiveTypeProvider> shot : shots) {
      int[][][] shotPixels = ArtUtil.shotToRGB(shot.get("id").getString(), selector, 8, 8);
      for (int x = 0; x < pixels.length; x++) {
        for (int y = 0; y < pixels[0].length; y++) {
          for (int i = 0; i < 3; i++) {
            pixels[x][y][i] += shotPixels[x][y][i];
          }
        }
      }
    }

    for (int x = 0; x < pixels.length; x++) {
      for (int y = 0; y < pixels[0].length; y++) {
        for (int i = 0; i < 3; i++) {
          pixels[x][y][i] = pixels[x][y][i] / shots.size();
        }
      }
    }

    BufferedImage image = new BufferedImage(256, 256, BufferedImage.TYPE_INT_RGB);
    Graphics2D graph = image.createGraphics();
    for (int x = 0; x < pixels.length; x++) {
      for (int y = 0; y < pixels[0].length; y++) {
        graph.setColor(new Color(pixels[x][y][0], pixels[x][y][1], pixels[x][y][2]));
        graph.fillRect(x * 32, y * 32, 32, 32);
      }
    }
    graph.dispose();

    return WebUtils.BufferedImageToDataURL(image, "png");
  }

  @Override
  public String visualizeSegment(String segmentId) {
    DBSelector selector = selectors.get("AverageColorGrid8");
    int[][][] pixels = ArtUtil.shotToRGB(segmentId, selector, 8, 8);

    BufferedImage image = new BufferedImage(256, 256, BufferedImage.TYPE_INT_RGB);
    Graphics2D graph = image.createGraphics();
    for (int x = 0; x < pixels.length; x++) {
      for (int y = 0; y < pixels[0].length; y++) {
        graph.setColor(new Color(pixels[x][y][0], pixels[x][y][1], pixels[x][y][2]));
        graph.fillRect(x * 32, y * 32, 32, 32);
      }
    }
    graph.dispose();

    try {
      ImageIO.write(image, "png", new File("src/resources/imageAverageColorGrid8.png"));
    } catch (IOException e) {
      e.printStackTrace();
    }

    return WebUtils.BufferedImageToDataURL(image, "png");
  }

  @Override
  public List<VisualizationType> getVisualizations() {
    List<VisualizationType> types = new ArrayList();
    types.add(VisualizationType.VISUALIZATION_SHOT);
    types.add(VisualizationType.VISUALIZATION_VIDEO);
    return types;
  }

  @Override
  public VisualizationResult getResultType() {
    return VisualizationResult.IMAGE;
  }
}
