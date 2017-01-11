package org.vitrivr.cineast.art.modules;

import org.vitrivr.cineast.api.WebUtils;
import org.vitrivr.cineast.art.modules.abstracts.AbstractVisualizationModule;
import org.vitrivr.cineast.art.modules.visualization.SegmentDescriptorComparator;
import org.vitrivr.cineast.art.modules.visualization.VisualizationResult;
import org.vitrivr.cineast.art.modules.visualization.VisualizationType;
import org.vitrivr.cineast.core.color.ColorConverter;
import org.vitrivr.cineast.core.color.RGBContainer;
import org.vitrivr.cineast.core.color.ReadableLabContainer;
import org.vitrivr.cineast.core.data.entities.SegmentDescriptor;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;
import org.vitrivr.cineast.core.db.SegmentLookup;
import org.vitrivr.cineast.core.util.ArtUtil;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by sein on 30.08.16.
 */
public class VisualizationMedianColorStripeVariable extends AbstractVisualizationModule {
  public VisualizationMedianColorStripeVariable() {
    super();
    tableNames.put("MedianColor", "features_MedianColor");
  }

  @Override
  public String getDisplayName() {
    return "VisualizationMedianColorStripeVariable";
  }

  protected String visualizeMulti(List<Map<String, PrimitiveTypeProvider>> featureData, List<SegmentDescriptor> segments){
    Collections.sort(segments, new SegmentDescriptorComparator());

    int count = 0;
    int totalWidth = 0;
    int[] colors = new int[segments.size()];
    int[] widths = new int[segments.size()];
    for (SegmentDescriptor segment : segments) {
      widths[count] = (segment.getEndFrame() - segment.getStartFrame()) / 10 + 1;
      totalWidth += widths[count];
      float[] arr = featureData.get(count).get("feature").getFloatArray();
      for (int i = 0; i < 8; i++) {
        RGBContainer rgbContainer = ColorConverter.LabtoRGB(new ReadableLabContainer(arr[0], arr[1], arr[2]));
        colors[count] = rgbContainer.toIntColor();
      }
      count++;
    }

    BufferedImage image = new BufferedImage(totalWidth, 100, BufferedImage.TYPE_INT_RGB);
    Graphics2D graph = image.createGraphics();
    for (int i = 0, pos = 0; i < widths.length; i++) {
      graph.setColor(new Color(colors[i]));
      graph.fillRect(pos, 0, widths[i], 100);
      pos += widths[i];
    }
    graph.dispose();

    return WebUtils.BufferedImageToDataURL(image, "png");
  }

  @Override
  protected String visualizeMulti(List<Map<String, PrimitiveTypeProvider>> featureData){
    return visualizeMulti(featureData, new ArrayList<SegmentDescriptor>());
  }

  @Override
  public String visualizeMultipleSegments(List<String> segmentIds){
    SegmentLookup segmentLookup = new SegmentLookup();
    Map<String, SegmentDescriptor> segmentMap = segmentLookup.lookUpShots(segmentIds.toArray(new String[segmentIds.size()]));
    List<SegmentDescriptor> segments = new ArrayList<>();
    for (Map.Entry<String, SegmentDescriptor> entry : segmentMap.entrySet()) {
      segments.add(entry.getValue());
    }
    return visualizeMulti(ArtUtil.getFeatureData(selectors.get("MedianColor"), segmentIds), segments);
  }

  @Override
  public String visualizeMultimediaobject(String multimediaobjectId) {
    SegmentLookup segmentLookup = new SegmentLookup();
    List<SegmentDescriptor> segments = segmentLookup.lookUpAllSegments(multimediaobjectId);
    return visualizeMulti(ArtUtil.getFeatureData(selectors.get("MedianColor"), multimediaobjectId), segments);
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
