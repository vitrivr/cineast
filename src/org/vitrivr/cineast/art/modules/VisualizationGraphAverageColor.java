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
public class VisualizationGraphAverageColor extends AbstractVisualizationModule {
  public VisualizationGraphAverageColor() {
    super();
    tableNames.put("AverageColor", "features_AverageColor");
  }

  @Override
  public String getDisplayName() {
    return "VisualizationGraphAverageColor";
  }

  @Override
  public String visualizeMultimediaobject(String multimediaobjectId) {
    List<Map<String, PrimitiveTypeProvider>> featureData = ArtUtil.getFeatureData(selectors.get("AverageColor"), multimediaobjectId);

    int[][][] colors = new int[3][216][3];
    for(int x=0;x<6;x++){
      if (x == 0 || x == 1 || x == 5) {
        colors[0][x][0] = 255;
      }
      if (x >= 1 && x <= 3) {
        colors[0][x][1] = 255;
      }
      if (x >= 3 && x <= 5) {
        colors[0][x][2] = 255;
      }
      for(int y=0;y<6;y++) {
        switch(x){
          case 0:
            colors[1][x*6+y][0] = 255;
            colors[1][x*6+y][1] = 255*y/6;
            for(int z=0;z<6;z++){
              colors[2][x*36+y*6+z][0] = 255;
              colors[2][x*36+y*6+z][1] = 255*y/6 + 255*z/36;
            }
            break;
          case 1:
            colors[1][x*6+y][1] = 255;
            colors[1][x*6+y][0] = 255 - 255*y/6;
            for(int z=0;z<6;z++){
              colors[2][x*36+y*6+z][1] = 255;
              colors[2][x*36+y*6+z][0] = 255 - 255*y/6 - 255*z/36;
            }
            break;
          case 2:
            colors[1][x*6+y][1] = 255;
            colors[1][x*6+y][2] = 255*y/6;
            for(int z=0;z<6;z++){
              colors[2][x*36+y*6+z][1] = 255;
              colors[2][x*36+y*6+z][2] = 255*y/6 + 255*z/36;
            }
            break;
          case 3:
            colors[1][x*6+y][2] = 255;
            colors[1][x*6+y][1] = 255 - 255*y/6;
            for(int z=0;z<6;z++){
              colors[2][x*36+y*6+z][2] = 255;
              colors[2][x*36+y*6+z][1] = 255 - 255*y/6 - 255*z/36;
            }
            break;
          case 4:
            colors[1][x*6+y][2] = 255;
            colors[1][x*6+y][0] = 255*y/6;
            for(int z=0;z<6;z++){
              colors[2][x*36+y*6+z][2] = 255;
              colors[2][x*36+y*6+z][0] = 255*y/6 + 255*z/36;
            }
            break;
          case 5:
            colors[1][x*6+y][0] = 255;
            colors[1][x*6+y][2] = 255 - 255*y/6;
            for(int z=0;z<6;z++){
              colors[2][x*36+y*6+z][0] = 255;
              colors[2][x*36+y*6+z][2] = 255 - 255*y/6 - 255*z/36;
            }
            break;
        }
      }
    }

    int[][] data = new int[3][216];
    for (Map<String, PrimitiveTypeProvider> feature : featureData) {
      int[] pixel = ArtUtil.shotToRGB(feature.get("feature").getFloatArray(), 1, 1)[0][0];
      //data[0][minDistance(colors[0], 6, pixel)]++;
      //data[1][minDistance(colors[1], 36, pixel)]++;
      int min = minDistance(colors[2], 216, pixel);
      data[0][min/36]++;
      data[1][min/6]++;
      data[2][min]++;
    }

    JsonObject graph = new JsonObject();
    graph.add("name", "VisualizationGraphAverageColor");
    graph.add("multimediaobject", multimediaobjectId);
    graph.add("children", getGraphChildren(data, colors, 0, 0));

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

  private JsonArray getGraphChildren(int[][] data, int[][][] colors, int step, int offset){
    JsonArray sub = new JsonArray();

    if(step == 1){
      offset -= 3;
    }

    for(int x=offset;x<6+offset;x++){
      int c = x;
      if(c < 0){
        c += 36;
      }
      JsonObject sub1 = new JsonObject();
      sub1.add("name", "range" + step + "-" + c);
      sub1.add("size", data[step][c]);
      int[] color = colors[step][c];
      sub1.add("color", color[0] + "," + color[1] + "," + color[2]);
      if(step + 1 < data.length){
        //children
        sub1.add("children", getGraphChildren(data, colors, step + 1, c*6));
      }
      else{
        //no children
      }
      if(data[step][c] > 0) {
        sub.add(sub1);
      }
    }

    return sub;
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
