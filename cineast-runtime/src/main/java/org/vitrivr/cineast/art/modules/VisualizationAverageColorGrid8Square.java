package org.vitrivr.cineast.art.modules;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.vitrivr.cineast.art.modules.visualization.AbstractVisualizationModule;
import org.vitrivr.cineast.art.modules.visualization.VisualizationResult;
import org.vitrivr.cineast.art.modules.visualization.VisualizationType;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;
import org.vitrivr.cineast.standalone.util.ArtUtil;
import org.vitrivr.cineast.core.util.web.ImageParser;

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
    return visualizeMulti(ArtUtil.getFeatureData(selectors.get("AverageColorGrid8"), multimediaobjectId));
  }

  @Override
  public String visualizeMultipleSegments(List<String> segmentIds){
    return visualizeMulti(ArtUtil.getFeatureData(selectors.get("AverageColorGrid8"), segmentIds));
  }

  @Override
  protected String visualizeMulti(List<Map<String, PrimitiveTypeProvider>> featureData){
    int size[] = {(int) Math.ceil(Math.sqrt(featureData.size())), (int) Math.ceil(Math.sqrt(featureData.size()))};
    if (size[0] * size[1] - size[0] >= featureData.size()) {
      size[1]--;
    }

    BufferedImage image = new BufferedImage(8 * size[0], 8 * size[1], BufferedImage.TYPE_INT_RGB);

    int count = 0;
    for (Map<String, PrimitiveTypeProvider> feature : featureData) {
      int[][] pixels = ArtUtil.shotToInt(feature.get("feature").getFloatArray(), 8, 8);
      int baseY = (count / size[0]) * 8;
      int baseX = (count % size[0]) * 8;
      for (int x = 0; x < pixels.length; x++) {
        for (int y = 0; y < pixels[0].length; y++) {
          image.setRGB(baseX + x, baseY + y, pixels[x][y]);
        }
      }
      count++;
    }

    return ImageParser.BufferedImageToDataURL(image, "png");
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
