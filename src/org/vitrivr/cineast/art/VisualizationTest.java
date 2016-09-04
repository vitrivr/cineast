package org.vitrivr.cineast.art;

import org.vitrivr.cineast.api.WebUtils;
import org.vitrivr.cineast.art.modules.*;
import org.vitrivr.cineast.art.modules.visualization.Visualization;
import org.vitrivr.cineast.core.config.Config;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

/**
 * Created by sein on 02.09.16.
 */
public class VisualizationTest {
  public static void main(String[] args){
    Visualization vis = new VisualizationMedianColorGrid8Square();
    vis.init(Config.getDatabaseConfig().getSelectorSupplier());
    try {
      ImageIO.write(WebUtils.dataURLtoBufferedImage(vis.visualizeMultimediaobject("11")), "png", new File("src/resources/test.png"));
      //ImageIO.write(WebUtils.dataURLtoBufferedImage(vis.visualizeSegment("720900")), "png", new File("src/resources/test.png"));
    } catch (IOException e) {
      e.printStackTrace();
    }
    vis.finish();
  }
}
