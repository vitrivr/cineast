package org.vitrivr.cineast.art.modules;

import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.resizers.configurations.ScalingMode;
import org.vitrivr.cineast.api.WebUtils;
import org.vitrivr.cineast.art.modules.abstracts.AbstractVisualizationModule;
import org.vitrivr.cineast.art.modules.visualization.VisualizationResult;
import org.vitrivr.cineast.art.modules.visualization.VisualizationType;
import org.vitrivr.cineast.core.config.Config;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;
import org.vitrivr.cineast.core.db.DBSelector;
import org.vitrivr.cineast.core.util.ArtUtil;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by sein on 30.08.16.
 */
public class VisualizationAverageColorGradient extends AbstractVisualizationModule {
  public VisualizationAverageColorGradient() {
    super("features_AverageColor");
  }

  @Override
  public String getDisplayName() {
    return "VisualizationAverageColorGradient";
  }

  public static void main(String[] args){
    VisualizationAverageColorGradient vis = new VisualizationAverageColorGradient();
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

    BufferedImage image = new BufferedImage(shots.size(), 1, BufferedImage.TYPE_INT_RGB);
    int count = 0;
    for (Map<String, PrimitiveTypeProvider> shot : shots) {
      int[] avg = ArtUtil.shotToInt(shot.get("id").getString(), selector, 1, 1);
      image.setRGB(count, 0, avg[0]);
      count++;
    }

    try {
      image = Thumbnails.of(image).scalingMode(ScalingMode.BILINEAR).scale(10, 100).asBufferedImage();
      ImageIO.write(image, "png", new File("src/resources/imageAverageColorGradient.png"));
    } catch (IOException e) {
      e.printStackTrace();
    }

    return WebUtils.BufferedImageToDataURL(image, "png");
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
