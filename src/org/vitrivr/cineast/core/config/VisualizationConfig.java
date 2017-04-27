package org.vitrivr.cineast.core.config;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.art.modules.*;
import org.vitrivr.cineast.art.modules.visualization.Visualization;
import org.vitrivr.cineast.core.data.DoublePair;
import org.vitrivr.cineast.core.util.ReflectionHelper;

import java.util.*;

public final class VisualizationConfig {


  private static final String DEFAULT_CACHE_PATH = "cache/art/";
  private static final HashMap<String, List<Class<? extends Visualization>>> DEFAULT_VISUALIZATION_CATEGORIES = new HashMap<>();
  private static final boolean DEFAULT_CACHE_ENABLED = true;

  private HashMap<String, List<Class<? extends Visualization>>> visualizationCategories;
  private String cachePath;
  private boolean cacheEnabled;
  

  
  private static Logger LOGGER = LogManager.getLogger();

  static {
    //add all categories with their containing visualizations
    List<Class<? extends Visualization>> list;

    list = new ArrayList<>();
    list.add(VisualizationAverageColorGrid8.class);
    list.add(VisualizationMedianColorGrid8.class);
    list.add(VisualizationDominantEdgeGrid8.class);
    list.add(VisualizationDominantEdgeGrid16.class);
    list.add(VisualizationDominantEdgeAverageColorGrid8.class);
    list.add(VisualizationDominantEdgeAverageColorGrid16.class);
    DEFAULT_VISUALIZATION_CATEGORIES.put("segments", list);

    list = new ArrayList<>();
    list.add(VisualizationAverageColorGrid8.class);
    list.add(VisualizationMedianColorGrid8.class);
    list.add(VisualizationAverageColorGradient.class);
    list.add(VisualizationMedianColorGradient.class);
    list.add(VisualizationAverageColorStripe.class);
    list.add(VisualizationMedianColorStripe.class);
    list.add(VisualizationAverageColorStripeVariable.class);
    list.add(VisualizationMedianColorStripeVariable.class);
    list.add(VisualizationMedianColorGrid8Square.class);
    list.add(VisualizationAverageColorGrid8Square.class);
    list.add(VisualizationDominantColorStripe.class);
    list.add(VisualizationDominantColorGradient.class);
    list.add(VisualizationDominantColorStripeVariable.class);
    list.add(VisualizationAverageColorSunburst.class);
    list.add(VisualizationMedianColorSunburst.class);
    list.add(VisualizationDominantColorSunburst.class);
    list.add(VisualizationAverageColorSunburstReal.class);
    list.add(VisualizationDominantColorSunburstReal.class);
    list.add(VisualizationMedianColorSunburstReal.class);
    list.add(VisualizationAverageColorStreamgraph.class);
    list.add(VisualizationMedianColorStreamgraph.class);
    list.add(VisualizationAverageColorStreamgraphReal.class);
    list.add(VisualizationMedianColorStreamgraphReal.class);
    DEFAULT_VISUALIZATION_CATEGORIES.put("multimediaobjects", list);

    list = new ArrayList<>();
    list.add(VisualizationAverageColorGrid8.class);
    list.add(VisualizationAverageColorGrid8.class);
    list.add(VisualizationAverageColorStripe.class);
    list.add(VisualizationMedianColorStripe.class);
    list.add(VisualizationAverageColorGradient.class);
    list.add(VisualizationMedianColorGradient.class);
    list.add(VisualizationDominantEdgeGrid8.class);
    list.add(VisualizationDominantEdgeGrid16.class);
    list.add(VisualizationAverageColorStripeVariable.class);
    list.add(VisualizationMedianColorStripeVariable.class);
    list.add(VisualizationDominantColorStripe.class);
    list.add(VisualizationDominantColorGradient.class);
    list.add(VisualizationDominantColorStripeVariable.class);
    DEFAULT_VISUALIZATION_CATEGORIES.put("featureRevert", list);
  }

  @JsonCreator
  public VisualizationConfig() {

  }

  public List<Class<? extends Visualization>> getVisualizations() {
    List<Class<? extends Visualization>> _return = new ArrayList<Class<? extends Visualization>>();
    for (Map.Entry<String, List<Class<? extends Visualization>>> entry : visualizationCategories.entrySet()) {
      _return.addAll(entry.getValue());
    }
    return _return;
  }
  @JsonProperty
  public String getCachePath() {
    return cachePath;
  }
  public void setCachePath(String cachePath) {
    this.cachePath = cachePath;
  }

  @JsonProperty
  public boolean isCacheEnabled() {
    return cacheEnabled;
  }
  public void setCacheEnabled(boolean cacheEnabled) {
    this.cacheEnabled = cacheEnabled;
  }

  public boolean isValidVisualization(Class<?> vizClass) {
    for (Map.Entry<String, List<Class<? extends Visualization>>> entry : visualizationCategories.entrySet()) {
      for (Class<? extends Visualization> visualization : entry.getValue()) {
        if(visualization.equals(vizClass)){
          return true;
        }
      }
    }
    return false;
  }

  public List<Class<? extends Visualization>> getVisualizationsByCategory(String category) {
    return this.visualizationCategories.get(category);
  }

  public List<String> getVisualizationCategories() {
    Set<String> keys = this.visualizationCategories.keySet();
    ArrayList<String> _return = new ArrayList<>(keys.size());
    _return.addAll(keys);
    return _return;
  }
}
