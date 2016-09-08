package org.vitrivr.cineast.art.modules;

import org.vitrivr.cineast.api.WebUtils;
import org.vitrivr.cineast.art.modules.abstracts.AbstractVisualizationModule;
import org.vitrivr.cineast.art.modules.visualization.SegmentDescriptorComparator;
import org.vitrivr.cineast.art.modules.visualization.VisualizationResult;
import org.vitrivr.cineast.art.modules.visualization.VisualizationType;
import org.vitrivr.cineast.core.color.ColorConverter;
import org.vitrivr.cineast.core.color.RGBContainer;
import org.vitrivr.cineast.core.color.ReadableLabContainer;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;
import org.vitrivr.cineast.core.db.DBSelector;
import org.vitrivr.cineast.core.db.SegmentLookup;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by sein on 30.08.16.
 */
public class VisualizationDominantColorStripe extends AbstractVisualizationModule {
  public VisualizationDominantColorStripe() {
    super();
    tableNames.put("DominantColor", "features_DominantColors");
  }

  @Override
  public String getDisplayName() {
    return "VisualizationDominantColorStripe";
  }

  @Override
  public String visualizeMultimediaobject(String multimediaobjectId) {
    DBSelector selector = selectors.get("DominantColor");
    SegmentLookup segmentLookup = new SegmentLookup();
    List<SegmentLookup.SegmentDescriptor> segments = segmentLookup.lookUpAllSegments(multimediaobjectId);
    Collections.sort(segments, new SegmentDescriptorComparator());

    BufferedImage image = new BufferedImage(segments.size() * 10, 100, BufferedImage.TYPE_INT_RGB);
    Graphics2D graph = image.createGraphics();
    int count = 0;
    for (SegmentLookup.SegmentDescriptor segment : segments) {
      List<Map<String, PrimitiveTypeProvider>> result = selector.getRows("id", segment.getShotId());
      for (Map<String, PrimitiveTypeProvider> row : result) {
        float[] arr = row.get("feature").getFloatArray();
        RGBContainer rgbContainer = ColorConverter.LabtoRGB(new ReadableLabContainer(arr[0], arr[1], arr[2]));
        graph.setColor(new Color(rgbContainer.toIntColor()));
        graph.fillRect(count * 10, 0, 10, 100);
      }
      count++;
    }
    graph.dispose();

    return WebUtils.BufferedImageToDataURL(image, "png");
  }

  @Override
  public List<VisualizationType> getVisualizations() {
    List<VisualizationType> types = new ArrayList();
    types.add(VisualizationType.VISUALIZATION_MULTIMEDIAOBJECT);
    return types;
  }

  @Override
  public VisualizationResult getResultType() {
    return VisualizationResult.IMAGE;
  }
}
