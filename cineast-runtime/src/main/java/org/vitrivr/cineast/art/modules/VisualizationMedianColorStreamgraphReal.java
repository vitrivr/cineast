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
public class VisualizationMedianColorStreamgraphReal extends AbstractVisualizationModule {
  public VisualizationMedianColorStreamgraphReal() {
    super();
    tableNames.put("MedianColor", "features_MedianColorGrid8");
  }

  @Override
  public String getDisplayName() {
    return "VisualizationMedianColorStreamgraphReal";
  }

  @Override
  public String visualizeMultipleSegments(List<String> segmentIds){
    return visualizeMulti(ArtUtil.getFeatureData(selectors.get("MedianColor"), segmentIds));
  }

  @Override
  public String visualizeMultimediaobject(String multimediaobjectId) {
    return visualizeMulti(ArtUtil.getFeatureData(selectors.get("MedianColor"), multimediaobjectId));
  }

  @Override
  protected String visualizeMulti(List<Map<String, PrimitiveTypeProvider>> featureData){
    int[][] base = ArtUtil.createColorDistribution2();
    int[][] colors = new int[36][4];

    int[][] data = new int[featureData.size()][36];
    int counter = 0;
    for (Map<String, PrimitiveTypeProvider> feature : featureData) {
      int[][][] pixel = ArtUtil.shotToRGB(feature.get("feature").getFloatArray(), 8, 8);
      for(int x=0;x<pixel.length;x++) {
        for(int y=0;y<pixel[0].length;y++){
          int min = ArtUtil.minDistance(base, 36, pixel[x][y]);
          data[counter][min]++;
          colors[min][0] += pixel[x][y][0];
          colors[min][1] += pixel[x][y][1];
          colors[min][2] += pixel[x][y][2];
          colors[min][3]++;
        }
      }
      counter++;
    }

    for(int x=0;x<colors.length;x++){
      if(colors[x][3] > 0) {
        colors[x][0] = colors[x][0] / colors[x][3];
        colors[x][1] = colors[x][1] / colors[x][3];
        colors[x][2] = colors[x][2] / colors[x][3];
      }
    }

    JsonObject graph = new JsonObject();
    graph.add("name", "VisualizationMedianColorStreamgraphReal");

    graph = ArtUtil.createStreamGraphData(data, colors, graph);

    return graph.toString();
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
