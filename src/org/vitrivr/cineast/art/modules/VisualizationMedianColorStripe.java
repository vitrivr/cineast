package org.vitrivr.cineast.art.modules;

import org.vitrivr.cineast.api.WebUtils;
import org.vitrivr.cineast.art.modules.abstracts.AbstractVisualizationModule;
import org.vitrivr.cineast.art.modules.visualization.VisualizationResult;
import org.vitrivr.cineast.art.modules.visualization.VisualizationType;
import org.vitrivr.cineast.core.color.ColorConverter;
import org.vitrivr.cineast.core.color.RGBContainer;
import org.vitrivr.cineast.core.color.ReadableLabContainer;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;
import org.vitrivr.cineast.core.util.ArtUtil;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by sein on 30.08.16.
 */
public class VisualizationMedianColorStripe extends AbstractVisualizationModule {
  public VisualizationMedianColorStripe() {
    super();
    tableNames.put("MedianColor", "features_MedianColor");
  }

  @Override
  public String getDisplayName() {
    return "VisualizationMedianColorStripe";
  }

  @Override
  public String visualizeMultimediaobject(String multimediaobjectId) {
    String cacheData = visualizationCache.getFromCache(getDisplayName(), VisualizationType.VISUALIZATION_MULTIMEDIAOBJECT, multimediaobjectId);
    if(cacheData != null){
      return cacheData;
    }
    List<Map<String, PrimitiveTypeProvider>> featureData = ArtUtil.getFeatureData(selectors.get("MedianColor"), multimediaobjectId);

    BufferedImage image = new BufferedImage(featureData.size() * 10, 100, BufferedImage.TYPE_INT_RGB);
    Graphics2D graph = image.createGraphics();
    int count = 0;
    for (Map<String, PrimitiveTypeProvider> feature : featureData) {
      float[] arr = feature.get("feature").getFloatArray();
      RGBContainer rgbContainer = ColorConverter.LabtoRGB(new ReadableLabContainer(arr[0], arr[1], arr[2]));
      graph.setColor(new Color(rgbContainer.toIntColor()));
      graph.fillRect(count * 10, 0, 10, 100);
      count++;
    }
    graph.dispose();
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
