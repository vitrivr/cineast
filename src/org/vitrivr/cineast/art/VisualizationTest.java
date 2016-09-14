package org.vitrivr.cineast.art;

import org.vitrivr.cineast.api.WebUtils;
import org.vitrivr.cineast.art.modules.VisualizationMedianColorGrid8;
import org.vitrivr.cineast.art.modules.visualization.Visualization;
import org.vitrivr.cineast.core.config.Config;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

/**
 * Created by sein on 02.09.16.
 */
public class VisualizationTest {
  public static void main(String[] args) {
    Visualization vis = new VisualizationMedianColorGrid8();
    vis.init(Config.getDatabaseConfig().getSelectorSupplier());
    try {
      //ImageIO.write(WebUtils.dataURLtoBufferedImage(vis.visualizeMultimediaobject("11")), "png", new File("src/resources/test.png"));
      ImageIO.write(WebUtils.dataURLtoBufferedImage(vis.visualizeSegment("720919")), "png", new File("src/resources/test.png"));
    } catch (IOException e) {
      e.printStackTrace();
    }
    vis.finish();
    /*Visualization vis = new VisualizationDominantEdgeAverageColorGrid16();
    vis.init(Config.getDatabaseConfig().getSelectorSupplier());
    for (int i = 1; i <= 200; i++) {
      File directory = new File("/Users/sein/Desktop/thumbnails/" + i + "/");
      String[] directoryContents = directory.list();
      List<String> segments = new ArrayList<String>();

      for (String fileName : directoryContents) {
        File temp = new File(String.valueOf(directory), fileName);
        segments.add(temp.getName());
      }

      for(String segment: segments){
        try {
          //ImageIO.write(WebUtils.dataURLtoBufferedImage(vis.visualizeMultimediaobject("11")), "png", new File("src/resources/test.png"));
          segment = segment.replace(".jpg", "");
          System.out.println(segment);
          ImageIO.write(WebUtils.dataURLtoBufferedImage(vis.visualizeSegment(segment)), "png", new File("src/resources/segment_" + segment + ".png"));
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
    vis.finish();*/
  }
}
