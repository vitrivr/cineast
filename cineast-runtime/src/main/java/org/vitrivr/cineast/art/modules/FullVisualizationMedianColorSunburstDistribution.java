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
public class FullVisualizationMedianColorSunburstDistribution extends AbstractVisualizationModule {
  public FullVisualizationMedianColorSunburstDistribution() {
    super();
    tableNames.put("MedianColor", "features_MedianColor");
  }

  @Override
  public String getDisplayName() {
    return "FullVisualizationMedianColorSunburstDistribution";
  }

  @Override
  protected String visualizeMulti(List<Map<String, PrimitiveTypeProvider>> featureData){
    int[][][] colors = ArtUtil.createColorDistribution3();

    int[][] data = new int[3][216];
    for(int x=0;x<3;x++){
      for(int y=0;y<216;y++){
        data[x][y] = 1;
      }
    }

    JsonObject graph = new JsonObject();
    graph.add("name", "FullVisualizationMedianColorSunburst");
    graph.add("children", ArtUtil.getSunburstChildren(data, colors, 0, 0));

    return graph.toString();
  }

  @Override
  public String visualizeMultipleSegments(List<String> segmentIds){
    return visualizeMulti(ArtUtil.getFeatureData(selectors.get("MedianColor"), segmentIds));
  }

  @Override
  public String visualizeMultimediaobject(String multimediaobjectId) {
    return visualizeMulti(null);
  }

  @Override
  public List<VisualizationType> getVisualizations() {
    List<VisualizationType> types = new ArrayList<>();
    types.add(VisualizationType.VISUALIZATION_MULTIMEDIAOBJECT);
    return types;
  }

  @Override
  public VisualizationResult getResultType() {
    return VisualizationResult.GRAPH_SUNBURST;
  }
}
