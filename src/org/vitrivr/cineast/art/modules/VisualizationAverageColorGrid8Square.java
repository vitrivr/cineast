package org.vitrivr.cineast.art.modules;

import org.vitrivr.cineast.api.WebUtils;
import org.vitrivr.cineast.art.modules.abstracts.AbstractVisualizationModule;
import org.vitrivr.cineast.art.modules.visualization.SegmentDescriptorComparator;
import org.vitrivr.cineast.art.modules.visualization.VisualizationResult;
import org.vitrivr.cineast.art.modules.visualization.VisualizationType;
import org.vitrivr.cineast.core.db.DBSelector;
import org.vitrivr.cineast.core.db.SegmentLookup;
import org.vitrivr.cineast.core.util.ArtUtil;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by sein on 26.08.16.
 */
public class VisualizationAverageColorGrid8Square extends AbstractVisualizationModule {
  public VisualizationAverageColorGrid8Square() {
    super();
    tableNames.put("AverageColorGrid8", "features_AverageColorGrid8");
  }

  @Override
  public String getDisplayName() {
    return "VisualizationAverageColorGrid8Square";
  }

  @Override
  public String visualizeMultimediaobject(String multimediaobjectId) {
    String cacheData = visualizationCache.getFromCache(getDisplayName(), VisualizationType.VISUALIZATION_MULTIMEDIAOBJECT, multimediaobjectId);
    if(cacheData != null){
      return cacheData;
    }
    DBSelector selector = selectors.get("AverageColorGrid8");
    SegmentLookup segmentLookup = new SegmentLookup();
    List<SegmentLookup.SegmentDescriptor> segments = segmentLookup.lookUpAllSegments(multimediaobjectId);
    Collections.sort(segments, new SegmentDescriptorComparator());

    int size[] = {(int) Math.ceil(Math.sqrt(segments.size())), (int) Math.ceil(Math.sqrt(segments.size()))};
    if (size[0] * size[1] - size[0] >= segments.size()) {
      size[1]--;
    }

    BufferedImage image = new BufferedImage(8 * size[0], 8 * size[1], BufferedImage.TYPE_INT_RGB);

    int count = 0;
    for (SegmentLookup.SegmentDescriptor segment : segments) {
      int[][] pixels = ArtUtil.shotToInt(segment.getSegmentId(), selector, 8, 8);
      int baseY = (count / size[0]) * 8;
      int baseX = (count % size[0]) * 8;
      for (int x = 0; x < pixels.length; x++) {
        for (int y = 0; y < pixels[0].length; y++) {
          image.setRGB(baseX + x, baseY + y, pixels[x][y]);
        }
      }
      count++;
    }

    return visualizationCache.cacheResult(getDisplayName(), VisualizationType.VISUALIZATION_MULTIMEDIAOBJECT, multimediaobjectId, WebUtils.BufferedImageToDataURL(image, "png"));
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
