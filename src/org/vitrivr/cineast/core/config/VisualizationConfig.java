package org.vitrivr.cineast.core.config;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import gnu.trove.map.hash.TObjectDoubleHashMap;
import org.jcodec.common.logging.Logger;
import org.vitrivr.cineast.art.modules.VisualizationAverageColorGrid8;
import org.vitrivr.cineast.art.modules.VisualizationMedianColorGrid8;
import org.vitrivr.cineast.art.modules.visualization.Visualization;
import org.vitrivr.cineast.core.data.DoublePair;
import org.vitrivr.cineast.core.features.*;
import org.vitrivr.cineast.core.features.exporter.QueryImageExporter;
import org.vitrivr.cineast.core.features.retriever.Retriever;
import org.vitrivr.cineast.core.util.ReflectionHelper;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.*;

public final class VisualizationConfig {

	private final HashMap<String, List<Class<? extends Visualization>>> visualizationCategories;
	public static List<Class<? extends Visualization>> visualizations = new ArrayList();

	public static final HashMap<String, List<Class<? extends Visualization>>> DEFAULT_VISUALIZATION_CATEGORIES = new HashMap<>();

	static{
		//add all visualizations
		visualizations.add(VisualizationAverageColorGrid8.class);
		visualizations.add(VisualizationMedianColorGrid8.class);


		//add all categories with their containing visualizations
		List<Class<? extends Visualization>> list;

		list = new ArrayList<>(1);
		list.add(VisualizationAverageColorGrid8.class);
		list.add(VisualizationMedianColorGrid8.class);
		DEFAULT_VISUALIZATION_CATEGORIES.put("shots", list);

		list = new ArrayList<>(1);
		list.add(VisualizationAverageColorGrid8.class);
		DEFAULT_VISUALIZATION_CATEGORIES.put("videos", list);

		list = new ArrayList<>(1);
		list.add(VisualizationAverageColorGrid8.class);
		list.add(VisualizationAverageColorGrid8.class);
		DEFAULT_VISUALIZATION_CATEGORIES.put("featureRevert", list);
	}

	public VisualizationConfig(){
		this(DEFAULT_VISUALIZATION_CATEGORIES);
	}

	public VisualizationConfig(HashMap<String, List<Class<? extends Visualization>>> visualizationCategories){
		this.visualizationCategories = visualizationCategories;
	}

	public boolean isValidVisualization(Class className){
		return visualizations.contains(className);
	}
	
	public List<Class<? extends Visualization>> getVisualizationsByCategory(String category){
		return this.visualizationCategories.get(category);
	}
	
	public List<String> getVisualizationCategories(){
		Set<String> keys = this.visualizationCategories.keySet();
		ArrayList<String> _return = new ArrayList<>(keys.size());
		_return.addAll(keys);
		return _return;
	}
}
