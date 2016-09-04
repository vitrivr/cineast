package org.vitrivr.cineast.core.config;

import org.vitrivr.cineast.art.modules.*;
import org.vitrivr.cineast.art.modules.visualization.Visualization;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public final class VisualizationConfig {

  private final HashMap<String, List<Class<? extends Visualization>>> visualizationCategories;
  public static List<Class<? extends Visualization>> visualizations = new ArrayList();

  public static final HashMap<String, List<Class<? extends Visualization>>> DEFAULT_VISUALIZATION_CATEGORIES = new HashMap<>();

  static {
    //add all visualizations
    visualizations.add(VisualizationAverageColorGrid8.class);
    visualizations.add(VisualizationMedianColorGrid8.class);
    visualizations.add(VisualizationAverageColorGradient.class);
    visualizations.add(VisualizationAverageColorStripe.class);
    visualizations.add(VisualizationMedianColorGradient.class);
    visualizations.add(VisualizationMedianColorStripe.class);
    visualizations.add(VisualizationDominantEdgeGrid8.class);
    visualizations.add(VisualizationDominantEdgeGrid16.class);
    visualizations.add(VisualizationDominantEdgeAverageColorGrid8.class);
    visualizations.add(VisualizationDominantEdgeAverageColorGrid16.class);
    visualizations.add(VisualizationAverageColorStripeVariable.class);
    visualizations.add(VisualizationMedianColorStripeVariable.class);
    visualizations.add(VisualizationMedianColorGrid8Square.class);
    visualizations.add(VisualizationAverageColorGrid8Square.class);


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
    DEFAULT_VISUALIZATION_CATEGORIES.put("featureRevert", list);
  }

  public VisualizationConfig() {
    this(DEFAULT_VISUALIZATION_CATEGORIES);
  }

  public VisualizationConfig(HashMap<String, List<Class<? extends Visualization>>> visualizationCategories) {
    this.visualizationCategories = visualizationCategories;
  }

  public boolean isValidVisualization(Class className) {
    return visualizations.contains(className);
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
