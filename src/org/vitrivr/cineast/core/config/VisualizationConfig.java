package org.vitrivr.cineast.core.config;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.art.modules.*;
import org.vitrivr.cineast.art.modules.visualization.Visualization;
import org.vitrivr.cineast.core.data.DoublePair;
import org.vitrivr.cineast.core.util.ReflectionHelper;

import java.util.*;

public final class VisualizationConfig {

  private final HashMap<String, List<Class<? extends Visualization>>> visualizationCategories;
  private final String cachePath;
  private final boolean cacheEnabled;
  
  public static final String DEFAULT_CACHE_PATH = "cache/art/";
  public static final HashMap<String, List<Class<? extends Visualization>>> DEFAULT_VISUALIZATION_CATEGORIES = new HashMap<>();
  public static final boolean DEFAULT_CACHE_ENABLED = true;
  
  private static Logger LOGGER = LogManager.getLogger();

  static {
    //add all categories with their containing visualizations
    List<Class<? extends Visualization>> list;

    list = new ArrayList();
    list.add(VisualizationAverageColorGrid8.class);
    list.add(VisualizationMedianColorGrid8.class);
    list.add(VisualizationDominantEdgeGrid8.class);
    list.add(VisualizationDominantEdgeGrid16.class);
    list.add(VisualizationDominantEdgeAverageColorGrid8.class);
    list.add(VisualizationDominantEdgeAverageColorGrid16.class);
    DEFAULT_VISUALIZATION_CATEGORIES.put("segments", list);

    list = new ArrayList();
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

    list = new ArrayList();
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

  public VisualizationConfig() {
    this(DEFAULT_VISUALIZATION_CATEGORIES, DEFAULT_CACHE_PATH, DEFAULT_CACHE_ENABLED);
  }

  public VisualizationConfig(HashMap<String, List<Class<? extends Visualization>>> visualizationCategories, String cachePath, boolean cacheEnabled) {
    this.visualizationCategories = visualizationCategories;
    this.cachePath = cachePath;
    this.cacheEnabled = cacheEnabled;
  }

  public List<Class<? extends Visualization>> getVisualizations() {
    List<Class<? extends Visualization>> _return = new ArrayList<Class<? extends Visualization>>();
    for (Map.Entry<String, List<Class<? extends Visualization>>> entry : visualizationCategories.entrySet()) {
      _return.addAll(entry.getValue());
    }
    return _return;
  }

  public String getVisualizationCachePath(){
    return cachePath;
  }

  public boolean getCacheEnabled(){
    return cacheEnabled;
  }

  public boolean isValidVisualization(Class vizClass) {
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

  public static VisualizationConfig parse(JsonObject obj) {
    if (obj == null) {
      throw new NullPointerException("JsonObject was null");
    }

    HashMap<String, List<Class<? extends Visualization>>> _visualizationCategories = DEFAULT_VISUALIZATION_CATEGORIES;
    boolean _cacheEnabled = DEFAULT_CACHE_ENABLED;
    String _cachePath = DEFAULT_CACHE_PATH;

    if(obj.get("visualizations") != null){
      try{
        JsonObject visualizations = obj.get("visualizations").asObject();
        HashMap<String, List<DoublePair<Class<Visualization>>>> map = new HashMap<>();
        for(String category : visualizations.names()){
          try{
            HashSet<Class<Visualization>> set = parseVisualizationCategory(visualizations.get(category).asArray());
            for (Class<Visualization> vizClass : set) {
              LOGGER.info(vizClass.getName()+" | category: "+category);
            }
            map.put(category, new ArrayList(set));
          }catch(UnsupportedOperationException notAnArray){
            throw new IllegalArgumentException("not an array in visualization config > visualizers > " + category);
          }
        }
      }catch(UnsupportedOperationException notAnObject){
        throw new IllegalArgumentException("'visualization' was not an object in retriever configuration");
      }
    }

    if (obj.get("cachePath") != null) {
      try {
        _cachePath = obj.get("cachePath").asString();
      } catch (UnsupportedOperationException e) {
        throw new IllegalArgumentException("'cachePath' was not a String in API configuration");
      }
    }

    if (obj.get("cacheEnabled") != null) {
      try {
        _cacheEnabled= obj.get("cacheEnabled").asBoolean();
      } catch (UnsupportedOperationException e) {
        throw new IllegalArgumentException("'cacheEnabled' was not a String in API configuration");
      }
    }

    return new VisualizationConfig(_visualizationCategories,_cachePath, _cacheEnabled);
  }

  private static HashSet<Class<Visualization>> parseVisualizationCategory(JsonArray jarr){
    if(jarr == null){
      return null;
    }

    HashSet<Class<Visualization>> classes = new HashSet<>(); //for de-duplication

    for(JsonValue jval : jarr){
      try{
        JsonObject jobj = jval.asObject();
        if(jobj.get("visualization") == null){
          continue;
        }
        Class<Visualization> c = null;
        try {
          c = ReflectionHelper.getClassFromJson(jobj.get("visualization").asObject(), Visualization.class, ReflectionHelper.FEATURE_MODULE_PACKAGE);
        } catch (IllegalArgumentException | ClassNotFoundException | InstantiationException | UnsupportedOperationException e) {
          //ignore at this point
        }

        if(c == null || classes.contains(c)){
          continue;
        }

        classes.add(c);

      }catch(UnsupportedOperationException notAnObject){
        org.jcodec.common.logging.Logger.warn("entry in feature list was not an object, ignoring");
      }
    }


    return classes;
  }
}
