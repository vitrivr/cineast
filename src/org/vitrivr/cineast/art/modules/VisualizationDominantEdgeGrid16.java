package org.vitrivr.cineast.art.modules;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.api.WebUtils;
import org.vitrivr.cineast.art.modules.abstracts.AbstractVisualizationModule;
import org.vitrivr.cineast.art.modules.visualization.VisualizationResult;
import org.vitrivr.cineast.art.modules.visualization.VisualizationType;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;
import org.vitrivr.cineast.core.db.ADAMproSelector;
import org.vitrivr.cineast.core.db.DBSelector;
import org.vitrivr.cineast.core.db.DBSelectorSupplier;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by sein on 02.09.16.
 */
public class VisualizationDominantEdgeGrid16 extends AbstractVisualizationModule {
  protected VisualizationDominantEdgeGrid16() {
    super();
    tableNames.put("DominantEdgeGrid16", "features_DominantEdgeGrid16");
  }

  private static final Logger LOGGER = LogManager.getLogger();

  @Override
  public String getDisplayName() {
    return "VisualizationDominantEdgeGrid16";
  }

  public static void main(String[] args) {
    VisualizationDominantEdgeGrid16 module = new VisualizationDominantEdgeGrid16();
    module.init(new DBSelectorSupplier() {
      @Override
      public DBSelector get() {
        return new ADAMproSelector();
      }
    });
    System.out.println(module.visualizeSegment("65563"));
    //System.out.println(module.visualizeMultimediaobject("11", 60));
    module.finish();
  }

  @Override
  public String visualizeSegment(String segmentId) {
    DBSelector selector = selectors.get("DominantEdgeGrid16");
    List<Map<String, PrimitiveTypeProvider>> result = selector.getRows("id", segmentId);

    BufferedImage image = new BufferedImage(512, 512, BufferedImage.TYPE_INT_RGB);

    Graphics2D graph = image.createGraphics();

    GradientPaint gradient;

    for (Map<String, PrimitiveTypeProvider> row : result) {
      float[] arr = row.get("feature").getFloatArray();
      for (int x = 0; x < 16; x++) {
        for (int y = 0; y < 16; y++) {
          switch ((int) arr[x * 16 + y]) {
            case -10:
              graph.setColor(Color.lightGray);
              break;
            case 2:
              gradient = new GradientPaint(x * 32, y * 32 + 16, Color.white, x * 32 + 32, y * 32 + 16, Color.black);
              graph.setPaint(gradient);
              break;
            case 3:
              gradient = new GradientPaint(x * 32, y * 32, Color.white, x * 32 + 32, y * 32 + 32, Color.black);
              graph.setPaint(gradient);
              break;
            case 0:
              gradient = new GradientPaint(x * 32 + 16, y * 32, Color.white, x * 32 + 16, y * 32 + 32, Color.black);
              graph.setPaint(gradient);
              break;
            case 1:
              gradient = new GradientPaint(x * 32, y * 32 + 32, Color.white, x * 32 + 32, y * 32, Color.black);
              graph.setPaint(gradient);
              break;
          }
          graph.fillRect(x * 32, y * 32, 32, 32);
        }
      }
    }
    graph.dispose();

    try {
      ImageIO.write(image, "png", new File("src/resources/imageDominantEdgeGrid16.png"));
    } catch (IOException e) {
      e.printStackTrace();
    }

    return WebUtils.BufferedImageToDataURL(image, "png");
  }


  @Override
  public List<VisualizationType> getVisualizations() {
    List<VisualizationType> types = new ArrayList();
    types.add(VisualizationType.VISUALIZATION_SHOT);
    return types;
  }

  @Override
  public VisualizationResult getResultType() {
    return VisualizationResult.IMAGE;
  }
}
