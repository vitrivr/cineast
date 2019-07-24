package org.vitrivr.cineast.art.modules;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.vitrivr.cineast.art.modules.visualization.AbstractVisualizationModule;
import org.vitrivr.cineast.art.modules.visualization.VisualizationResult;
import org.vitrivr.cineast.art.modules.visualization.VisualizationType;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;
import org.vitrivr.cineast.core.db.DBSelector;
import org.vitrivr.cineast.standalone.util.ArtUtil;
import org.vitrivr.cineast.core.util.web.ImageParser;

/**
 * Created by sein on 26.08.16.
 */
public class VisualizationMedianColorGrid8 extends AbstractVisualizationModule {
  public VisualizationMedianColorGrid8() {
    super();
    tableNames.put("MedianColorGrid8", "features_MedianColorGrid8");
  }

  @Override
  public String getDisplayName() {
    return "VisualizationMedianColorGrid8";
  }

  @Override
  public String visualizeSegment(String segmentId) {
    DBSelector selector = selectors.get("MedianColorGrid8");
    int[][][] pixels = ArtUtil.shotToRGB(segmentId, selector, 8, 8);

    BufferedImage image = new BufferedImage(8, 8, BufferedImage.TYPE_INT_RGB);
    Graphics2D graph = image.createGraphics();
    for (int x = 0; x < pixels.length; x++) {
      for (int y = 0; y < pixels[0].length; y++) {
        graph.setColor(new Color(pixels[x][y][0], pixels[x][y][1], pixels[x][y][2]));
        graph.fillRect(x, y, 1, 1);
      }
    }
    graph.dispose();

    return ImageParser.BufferedImageToDataURL(image, "png");
  }

  @Override
  protected String visualizeMulti(List<Map<String, PrimitiveTypeProvider>> featureData){
    int[][][] pixels = new int[8][8][3];
    for (Map<String, PrimitiveTypeProvider> feature : featureData) {
      int[][][] shotPixels = ArtUtil.shotToRGB(feature.get("feature").getFloatArray(), 8, 8);
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
          pixels[x][y][i] = pixels[x][y][i] / featureData.size();
        }
      }
    }

    BufferedImage image = new BufferedImage(8, 8, BufferedImage.TYPE_INT_RGB);
    Graphics2D graph = image.createGraphics();
    for (int x = 0; x < pixels.length; x++) {
      for (int y = 0; y < pixels[0].length; y++) {
        graph.setColor(new Color(pixels[x][y][0], pixels[x][y][1], pixels[x][y][2]));
        graph.fillRect(x, y, 1, 1);
      }
    }
    graph.dispose();

    return ImageParser.BufferedImageToDataURL(image, "png");
  }

  @Override
  public String visualizeMultipleSegments(List<String> segmentIds){
    return visualizeMulti(ArtUtil.getFeatureData(selectors.get("MedianColorGrid8"), segmentIds));
  }

  @Override
  public String visualizeMultimediaobject(String multimediaobjectId) {
    return visualizeMulti(ArtUtil.getFeatureData(selectors.get("MedianColorGrid8"), multimediaobjectId));
  }

  @Override
  public List<VisualizationType> getVisualizations() {
    List<VisualizationType> types = new ArrayList<>();
    types.add(VisualizationType.VISUALIZATION_SEGMENT);
    types.add(VisualizationType.VISUALIZATION_MULTIMEDIAOBJECT);
    return types;
  }

  @Override
  public VisualizationResult getResultType() {
    return VisualizationResult.IMAGE;
  }
}
