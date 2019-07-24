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
import org.vitrivr.cineast.core.color.ColorConverter;
import org.vitrivr.cineast.core.color.RGBContainer;
import org.vitrivr.cineast.core.color.ReadableLabContainer;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;
import org.vitrivr.cineast.standalone.util.ArtUtil;
import org.vitrivr.cineast.core.util.web.ImageParser;

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
  protected String visualizeMulti(List<Map<String, PrimitiveTypeProvider>> featureData){
    BufferedImage image = new BufferedImage(featureData.size(), 1, BufferedImage.TYPE_INT_RGB);
    Graphics2D graph = image.createGraphics();
    int count = 0;
    for (Map<String, PrimitiveTypeProvider> feature : featureData) {
      float[] arr = feature.get("feature").getFloatArray();
      RGBContainer rgbContainer = ColorConverter.LabtoRGB(new ReadableLabContainer(arr[0], arr[1], arr[2]));
      graph.setColor(new Color(rgbContainer.toIntColor()));
      graph.fillRect(count, 0, 1, 1);
      count++;
    }
    graph.dispose();

    return ImageParser.BufferedImageToDataURL(image, "png");
  }

  @Override
  public String visualizeMultipleSegments(List<String> segmentIds){
    return visualizeMulti(ArtUtil.getFeatureData(selectors.get("DominantColor"), segmentIds));
  }

  @Override
  public String visualizeMultimediaobject(String multimediaobjectId) {
    return visualizeMulti(ArtUtil.getFeatureData(selectors.get("DominantColor"), multimediaobjectId));
  }

  @Override
  public List<VisualizationType> getVisualizations() {
    List<VisualizationType> types = new ArrayList<>();
    types.add(VisualizationType.VISUALIZATION_MULTIMEDIAOBJECT);
    return types;
  }

  @Override
  public VisualizationResult getResultType() {
    return VisualizationResult.IMAGE;
  }
}
