package org.vitrivr.cineast.art.modules;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import org.vitrivr.cineast.art.modules.abstracts.AbstractVisualizationModule;
import org.vitrivr.cineast.art.modules.visualization.VisualizationResult;
import org.vitrivr.cineast.art.modules.visualization.VisualizationType;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;
import org.vitrivr.cineast.core.util.ArtUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by sein on 26.08.16.
 */
public class VisualizationAverageColorStreamgraph extends AbstractVisualizationModule {
  public VisualizationAverageColorStreamgraph() {
    super();
    tableNames.put("AverageColor", "features_AverageColorGrid8");
  }

  @Override
  public String getDisplayName() {
    return "VisualizationStreamgraphAverageColorGrid8";
  }

  @Override
  public String visualizeMultimediaobject(String multimediaobjectId) {
    List<Map<String, PrimitiveTypeProvider>> featureData = ArtUtil.getFeatureData(selectors.get("AverageColor"), multimediaobjectId);

    int[][] colors = new int[36][3];
    for(int x=0;x<6;x++){
      for(int y=0;y<6;y++) {
        switch(x){
          case 0:
            colors[x*6+y][0] = 255;
            colors[x*6+y][1] = 255*y/6;
            break;
          case 1:
            colors[x*6+y][1] = 255;
            colors[x*6+y][0] = 255 - 255*y/6;
            break;
          case 2:
            colors[x*6+y][1] = 255;
            colors[x*6+y][2] = 255*y/6;
            break;
          case 3:
            colors[x*6+y][2] = 255;
            colors[x*6+y][1] = 255 - 255*y/6;
            break;
          case 4:
            colors[x*6+y][2] = 255;
            colors[x*6+y][0] = 255*y/6;
            break;
          case 5:
            colors[x*6+y][0] = 255;
            colors[x*6+y][2] = 255 - 255*y/6;
            break;
        }
      }
    }

    int[][] data = new int[featureData.size()][36];
    int counter = 0;
    for (Map<String, PrimitiveTypeProvider> feature : featureData) {
      int[][][] pixel = ArtUtil.shotToRGB(feature.get("feature").getFloatArray(), 8, 8);
      for(int x=0;x<pixel.length;x++) {
        for(int y=0;y<pixel[0].length;y++){
          int min = minDistance(colors, 36, pixel[x][y]);
          data[counter][min]++;
        }
      }
      counter++;
    }

    JsonObject graph = new JsonObject();
    graph.add("name", "VisualizationStreamgraphAverageColorGrid8");
    graph.add("multimediaobject", multimediaobjectId);

    JsonArray graphColors = new JsonArray();
    JsonArray signals = new JsonArray();
    int num = 0;
    for(int x=0;x<data[0].length;x++){
      JsonArray signal = new JsonArray();
      int count = 0;
      for(int y=0;y<40;y++){
        signal.add(data[y][x]*500 + 1);
        count += data[y][x]*500 + 1;
      }
      if(count > 0) {
        signals.add(signal);
        graphColors.add("rgb(" + colors[x][0] + "," + colors[x][1] + "," + colors[x][2] + ")");
        num++;
      }
    }
    graph.add("colors", graphColors);
    graph.add("data", signals);

    return graph.toString();
  }

  private int minDistance(int[][] colors, int len, int[] color){
    int smallest = 0;
    int minDist = 3*256;
    for(int x=0;x<len;x++){
      int dist = Math.abs(color[0] - colors[x][0]) + Math.abs(color[1] - colors[x][1]) + Math.abs(color[2] - colors[x][2]);
      if(dist < minDist){
        minDist = dist;
        smallest = x;
      }
    }
    return smallest;
  }

  @Override
  public List<VisualizationType> getVisualizations() {
    List<VisualizationType> types = new ArrayList();
    types.add(VisualizationType.VISUALIZATION_MULTIMEDIAOBJECT);
    return types;
  }

  @Override
  public VisualizationResult getResultType() {
    return VisualizationResult.GRAPH_SUNBURST;
  }
}
