package org.vitrivr.cineast.art.modules;

import org.vitrivr.cineast.art.modules.abstracts.AbstractVisualizationModule;
import org.vitrivr.cineast.art.modules.visualization.VisualizationResult;
import org.vitrivr.cineast.art.modules.visualization.VisualizationType;
import org.vitrivr.cineast.core.db.DBSelector;
import org.vitrivr.cineast.core.util.ArtUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sein on 26.08.16.
 */
public class VisualizationMedianColorGrid8 extends AbstractVisualizationModule{
  public VisualizationMedianColorGrid8() {
    super();
    tableNames.put("MedianColorGrid8", "features_MedianColorGrid8");
  }

  @Override
  public String getDisplayName() {
    return "VisualizationMedianColorGrid8";
  }

  @Override
  public String visualizeSegment(String segmentId) {
    return visualizeSegment(segmentId, 1);
  }

  public String visualizeSegment(String segmentId, int scale){
    DBSelector selector = selectors.get("MedianColorGrid8");
    return ArtUtil.pixelsToImage(ArtUtil.scalePixels(ArtUtil.shotToRGB(segmentId, selector, 8, 8), scale, 8, 8, false), 8*scale, 8*scale, false);
  }

  @Override
  public List<VisualizationType> getVisualizations(){
    List<VisualizationType> types = new ArrayList();
    types.add(VisualizationType.VISUALIZATION_SHOT);
    return types;
  }

  @Override
  public VisualizationResult getResultType() {
    return VisualizationResult.IMAGE;
  }
}
