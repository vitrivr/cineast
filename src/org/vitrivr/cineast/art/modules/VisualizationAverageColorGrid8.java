package org.vitrivr.cineast.art.modules;

import org.vitrivr.cineast.api.WebUtils;
import org.vitrivr.cineast.art.modules.abstracts.AbstractVisualizationModule;
import org.vitrivr.cineast.art.modules.visualization.SegmentDescriptorComparator;
import org.vitrivr.cineast.art.modules.visualization.VisualizationResult;
import org.vitrivr.cineast.art.modules.visualization.VisualizationType;
import org.vitrivr.cineast.core.db.DBSelector;
import org.vitrivr.cineast.core.db.SegmentLookup;
import org.vitrivr.cineast.core.util.ArtUtil;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
    SegmentLookup segmentLookup = new SegmentLookup();
    List<SegmentLookup.SegmentDescriptor> segments = segmentLookup.lookUpAllSegments(multimediaobjectId);
    Collections.sort(segments, new SegmentDescriptorComparator());

    int[][][] pixels = new int[8][8][3];
    for (SegmentLookup.SegmentDescriptor segment : segments) {
      int[][][] shotPixels = ArtUtil.shotToRGB(segment.getShotId(), selector, 8, 8);
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
          pixels[x][y][i] = pixels[x][y][i] / segments.size();
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

    return WebUtils.BufferedImageToDataURL(image, "png");
  }

  @Override
  public List<VisualizationType> getVisualizations() {
    List<VisualizationType> types = new ArrayList();
    types.add(VisualizationType.VISUALIZATION_SEGMENT);
    types.add(VisualizationType.VISUALIZATION_MULTIMEDIAOBJECT);
    return types;
  }

  @Override
  public VisualizationResult getResultType() {
    return VisualizationResult.IMAGE;
  }
}
