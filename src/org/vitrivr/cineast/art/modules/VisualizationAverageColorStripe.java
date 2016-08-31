package org.vitrivr.cineast.art.modules;

import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.resizers.configurations.ScalingMode;
import org.vitrivr.cineast.api.WebUtils;
import org.vitrivr.cineast.art.modules.abstracts.AbstractVisualizationModule;
import org.vitrivr.cineast.art.modules.visualization.VisualizationResult;
import org.vitrivr.cineast.art.modules.visualization.VisualizationType;
import org.vitrivr.cineast.core.color.ColorConverter;
import org.vitrivr.cineast.core.color.RGBContainer;
import org.vitrivr.cineast.core.color.ReadableLabContainer;
import org.vitrivr.cineast.core.config.Config;
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
 * Created by sein on 30.08.16.
 */
public class VisualizationAverageColorStripe extends AbstractVisualizationModule {
  public VisualizationAverageColorStripe() {
    super("features_AverageColor");
  }

  @Override
  public String getDisplayName() {
    return "VisualizationAverageColorStripe";
  }

  public static void main(String[] args){
    VisualizationAverageColorStripe vis = new VisualizationAverageColorStripe();
    vis.init(Config.getDatabaseConfig().getSelectorSupplier());
    System.out.println(vis.visualizeMultimediaobject("11", 10));
    vis.finish();
  }

  @Override
  public String visualizeMultimediaobject(String multimediaobjectId) {
    return visualizeMultimediaobject(multimediaobjectId, 1);
  }

  public String visualizeMultimediaobject(String multimediaobjectId, int scale) {
    DBSelector selector = selectors.get(tableName);
    DBSelector shotSelector = selectors.get(shotsTable);
    List<Map<String, PrimitiveTypeProvider>> shots = shotSelector.getRows("multimediaobject", multimediaobjectId);

    int[] pixels = new int[shots.size()*8];
    int count = 0;
    for (Map<String, PrimitiveTypeProvider> shot : shots) {
      List<Map<String, PrimitiveTypeProvider>> result = selector.getRows("id", shot.get("id").getString());
      for (Map<String, PrimitiveTypeProvider> row : result) {
        float[] arr = row.get("feature").getFloatArray();
        for (int i = 0; i < 8; i++) {
          RGBContainer rgbContainer = ColorConverter.LabtoRGB(new ReadableLabContainer(arr[0], arr[1], arr[2]));
          pixels[shots.size()*i + count] = rgbContainer.toIntColor();
        }
      }
      count++;
    }

    pixels = ArtUtil.scalePixels(pixels, scale, shots.size(), 8, true);

    return ArtUtil.pixelsToImage(pixels, shots.size()*scale, 8*scale, true);
  }

  @Override
  public List<VisualizationType> getVisualizations() {
    List<VisualizationType> types = new ArrayList();
    types.add(VisualizationType.VISUALIZATION_VIDEO);
    return types;
  }

  @Override
  public VisualizationResult getResultType() {
    return VisualizationResult.IMAGE;
  }
}
