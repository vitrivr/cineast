package org.vitrivr.cineast.art.modules;

import org.vitrivr.cineast.art.modules.abstracts.AbstractVisualizationModule;
import org.vitrivr.cineast.art.modules.visualization.VisualizationResult;
import org.vitrivr.cineast.art.modules.visualization.VisualizationType;
import org.vitrivr.cineast.core.data.providers.primitive.PrimitiveTypeProvider;
import org.vitrivr.cineast.core.db.ADAMproSelector;
import org.vitrivr.cineast.core.db.DBSelector;
import org.vitrivr.cineast.core.db.DBSelectorSupplier;
import org.vitrivr.cineast.core.util.ArtUtil;

import java.util.*;
import java.util.List;

/**
 * Created by sein on 26.08.16.
 */
public class VisualizationAverageColorGrid8 extends AbstractVisualizationModule{
  public VisualizationAverageColorGrid8() {
    super("features_AverageColorGrid8");
  }

  //Test main function
  public static void main(String[] args) {
    VisualizationAverageColorGrid8 module = new VisualizationAverageColorGrid8();
    module.init(new DBSelectorSupplier() {
      @Override
      public DBSelector get() {
        return new ADAMproSelector();
      }
    });
    //System.out.println(module.visualizeSegment("655390", 40));
    System.out.println(module.visualizeMultimediaobject("11", 60));
    module.finish();
  }

  @Override
  public String getDisplayName() {
    return "VisualizationAverageColorGrid8";
  }

  @Override
  public String visualizeMultimediaobject(String multimediaobjectId){
    return visualizeMultimediaobject(multimediaobjectId, 1);
  }

  public String visualizeMultimediaobject(String multimediaobjectId, int scale){
    DBSelector selector = selectors.get(tableName);
    DBSelector shotSelector = selectors.get(shotsTable);
    List<Map<String, PrimitiveTypeProvider>> shots = shotSelector.getRows("multimediaobject", multimediaobjectId);

    LOGGER.info("Need to calculate AverageColorGrid8 of " + shots.size() + " shots...");

    int[] pixels = new int[8*8*3];
    for (Map<String, PrimitiveTypeProvider> shot : shots) {
      int[] shotPixels = ArtUtil.shotToRGB(shot.get("id").getString(), selector, 8, 8);
      for(int i=0;i<shotPixels.length;i++){
        pixels[i] += shotPixels[i];
      }
    }

    for(int i=0;i<pixels.length && shots.size() > 0;i++){
      pixels[i] = pixels[i]/shots.size();
    }

    pixels = ArtUtil.scalePixels(pixels, scale, 8, 8, false);

    return ArtUtil.pixelsToImage(pixels, 8*scale, 8*scale, false);
  }

  @Override
  public String visualizeSegment(String segmentId) {
    return visualizeSegment(segmentId, 1);
  }

  public String visualizeSegment(String segmentId, int scale){
    DBSelector selector = selectors.get(tableName);
    return ArtUtil.pixelsToImage(ArtUtil.scalePixels(ArtUtil.shotToRGB(segmentId, selector, 8, 8), scale, 8, 8, false), 8*scale, 8*scale, false);
  }

  @Override
  public List<VisualizationType> getVisualizations(){
    List<VisualizationType> types = new ArrayList();
    types.add(VisualizationType.VISUALIZATION_SHOT);
    types.add(VisualizationType.VISUALIZATION_VIDEO);
    return types;
  }

  @Override
  public VisualizationResult getResultType() {
    return VisualizationResult.IMAGE;
  }
}
