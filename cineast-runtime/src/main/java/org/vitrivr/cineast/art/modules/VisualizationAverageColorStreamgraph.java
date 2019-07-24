package org.vitrivr.cineast.art.modules;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.vitrivr.cineast.art.modules.visualization.AbstractVisualizationModule;
import org.vitrivr.cineast.art.modules.visualization.VisualizationResult;
import org.vitrivr.cineast.art.modules.visualization.VisualizationType;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;
import org.vitrivr.cineast.standalone.util.ArtUtil;

import com.eclipsesource.json.JsonObject;

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
    return "VisualizationAverageColorStreamgraph";
  }

  @Override
  public String visualizeMultipleSegments(List<String> segmentIds){
    return visualizeMulti(ArtUtil.getFeatureData(selectors.get("AverageColor"), segmentIds));
  }

  @Override
  public String visualizeMultimediaobject(String multimediaobjectId) {
    return visualizeMulti(ArtUtil.getFeatureData(selectors.get("AverageColor"), multimediaobjectId));
  }

  @Override
  protected String visualizeMulti(List<Map<String, PrimitiveTypeProvider>> featureData){
    int[][] colors = ArtUtil.createColorDistribution2();

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
    graph.add("name", "VisualizationAverageColorStreamgraph");

    graph = ArtUtil.createStreamGraphData(data, colors, graph);

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
    List<VisualizationType> types = new ArrayList<>();
    types.add(VisualizationType.VISUALIZATION_MULTIMEDIAOBJECT);
    return types;
  }

  @Override
  public VisualizationResult getResultType() {
    return VisualizationResult.GRAPH_STREAMGRAPH;
  }
}
