package org.vitrivr.cineast.art.modules;

import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.resizers.configurations.ScalingMode;
import org.vitrivr.cineast.api.WebUtils;
import org.vitrivr.cineast.art.modules.abstracts.AbstractVisualizationModule;
import org.vitrivr.cineast.art.modules.visualization.VisualizationResult;
import org.vitrivr.cineast.art.modules.visualization.VisualizationType;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;
import org.vitrivr.cineast.core.util.ArtUtil;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by sein on 30.08.16.
 */
public class VisualizationDominantColorGradient extends AbstractVisualizationModule {
  public VisualizationDominantColorGradient() {
    super();
    tableNames.put("DominantColor", "features_DominantColors");
  }

  @Override
  public String getDisplayName() {
    return "VisualizationDominantColorGradient";
  }

  @Override
  public String visualizeMultimediaobject(String multimediaobjectId) {
    String cacheData = visualizationCache.getFromCache(getDisplayName(), VisualizationType.VISUALIZATION_MULTIMEDIAOBJECT, multimediaobjectId);
    if(cacheData != null){
      return cacheData;
    }
    List<Map<String, PrimitiveTypeProvider>> featureData = ArtUtil.getFeatureData(selectors.get("DominantColor"), multimediaobjectId);

    BufferedImage image = new BufferedImage(featureData.size(), 1, BufferedImage.TYPE_INT_RGB);
    int count = 0;
    for (Map<String, PrimitiveTypeProvider> feature : featureData) {
      int[][] avg = ArtUtil.shotToInt(feature.get("feature").getFloatArray(), 1, 1);
      image.setRGB(count, 0, avg[0][0]);
      count++;
    }

    try {
      image = Thumbnails.of(image).scalingMode(ScalingMode.BILINEAR).scale(10, 100).asBufferedImage();
    } catch (IOException e) {
      e.printStackTrace();
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
