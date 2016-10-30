package org.vitrivr.cineast.art;

import org.vitrivr.cineast.art.modules.VisualizationGraphAverageColor;
import org.vitrivr.cineast.art.modules.visualization.Visualization;
import org.vitrivr.cineast.core.config.Config;

/**
 * Created by sein on 02.09.16.
 */
public class VisualizationTest {
  public static void main(String[] args) {
    Visualization vis = new VisualizationGraphAverageColor();
    vis.init(Config.getDatabaseConfig().getSelectorSupplier());
    for(int x=11;x<12;x++) {
      //ImageIO.write(WebUtils.dataURLtoBufferedImage(vis.visualizeMultimediaobject("" + x)), "png", new File("src/resources/test.png"));
    }
    //ImageIO.write(WebUtils.dataURLtoBufferedImage(vis.visualizeSegment("1900546")), "png", new File("src/resources/test.png"));
    System.out.println(vis.visualizeMultimediaobject("11"));
    vis.finish();
    //Visualization vis = new VisualizationDominantEdgeAverageColorGrid16();
    /*vis.init(Config.getDatabaseConfig().getSelectorSupplier());
    for (int i = 10000; i <= 200; i++) {
      /*System.out.println(i);
      try {
        ImageIO.write(WebUtils.dataURLtoBufferedImage(vis.visualizeMultimediaobject(i + "")), "png", new File("src/resources/edgeStripe_" + i + ".png"));
        //ImageIO.write(WebUtils.dataURLtoBufferedImage(vis.visualizeSegment("720919")), "png", new File("src/resources/test.png"));
      } catch (IOException e) {
        e.printStackTrace();
      }
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
          ImageIO.write(WebUtils.dataURLtoBufferedImage(vis.visualizeSegment(segment)), "png", new File("src/resources/edge16_" + segment + ".png"));
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
    vis.finish();*/
  }
}
