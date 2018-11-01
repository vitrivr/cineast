package org.vitrivr.cineast.art.modules;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.vitrivr.cineast.art.modules.abstracts.AbstractVisualizationModule;
import org.vitrivr.cineast.art.modules.visualization.VisualizationResult;
import org.vitrivr.cineast.art.modules.visualization.VisualizationType;
import org.vitrivr.cineast.core.data.entities.MediaObjectDescriptor;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;
import org.vitrivr.cineast.core.db.dao.reader.MediaObjectReader;
import org.vitrivr.cineast.core.util.ArtUtil;

import com.eclipsesource.json.JsonObject;

/**
 * Created by sein on 26.08.16.
 */
public class FullVisualizationMedianColorSunburstReal extends AbstractVisualizationModule {
  public FullVisualizationMedianColorSunburstReal() {
    super();
    tableNames.put("MedianColor", "features_MedianColor");
  }

  @Override
  public String getDisplayName() {
    return "VisualizationFullMedianColorSunburstReal";
  }

  @Override
  protected String visualizeMulti(List<Map<String, PrimitiveTypeProvider>> featureData){
    int[][][] basic = ArtUtil.createColorDistribution3();
    int[][][] colors = new int[3][216][3];

    int[][] data = new int[3][216];
    for (Map<String, PrimitiveTypeProvider> feature : featureData) {
      int[] pixel = ArtUtil.shotToRGB(feature.get("feature").getFloatArray(), 1, 1)[0][0];
      int min = ArtUtil.minDistance(basic[2], 216, pixel);
      data[0][min/36]++;
      data[1][min/6]++;
      data[2][min]++;
      colors[0][min/36][0] += pixel[0];
      colors[0][min/36][1] += pixel[1];
      colors[0][min/36][2] += pixel[2];
      colors[1][min/6][0] += pixel[0];
      colors[1][min/6][1] += pixel[1];
      colors[1][min/6][2] += pixel[2];
      colors[2][min][0] += pixel[0];
      colors[2][min][1] += pixel[1];
      colors[2][min][2] += pixel[2];
    }

    for(int x=0;x<3;x++){
      for(int y=0;y<216;y++){
        if(data[x][y] > 0){
          colors[x][y][0] = colors[x][y][0]/data[x][y];
          colors[x][y][1] = colors[x][y][1]/data[x][y];
          colors[x][y][2] = colors[x][y][2]/data[x][y];
        }
      }
    }

    JsonObject graph = new JsonObject();
    graph.add("name", "VisualizationFullMedianColorSunburstReal");
    graph.add("children", ArtUtil.getSunburstChildren(data, colors, 0, 0));

    return graph.toString();
  }

  @Override
  public String visualizeMultipleSegments(List<String> segmentIds){
    return visualizeMulti(ArtUtil.getFeatureData(selectors.get("MedianColor"), segmentIds));
  }

  @Override
  public String visualizeMultimediaobject(String multimediaobjectId) {
    //don't care about given id here...
    MediaObjectReader lookup = new MediaObjectReader();
    List<MediaObjectDescriptor> all = lookup.getAllObjects();
    List<Map<String, PrimitiveTypeProvider>> allData = new ArrayList<>();
    for(MediaObjectDescriptor multimedia: all){
      allData.addAll(ArtUtil.getFeatureData(selectors.get("MedianColor"), multimedia.getObjectId()));
      System.out.println(multimedia.getObjectId());
    }
    lookup.close();
    return visualizeMulti(allData);
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
