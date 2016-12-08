package org.vitrivr.cineast.core.config;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import gnu.trove.map.hash.TObjectDoubleHashMap;
import org.jcodec.common.logging.Logger;
import org.vitrivr.cineast.core.data.DoublePair;
import org.vitrivr.cineast.core.features.*;
import org.vitrivr.cineast.core.features.exporter.QueryImageExporter;
import org.vitrivr.cineast.core.features.neuralnet.classification.tf.NeuralNetVGG16Feature;
import org.vitrivr.cineast.core.features.retriever.Retriever;
import org.vitrivr.cineast.core.util.ReflectionHelper;

import java.util.*;

public final class RetrieverConfig {

	private final int threadPoolSize;
	private final int taskQueueSize;
	private final int maxResults;
	private final int resultsPerModule;
	private final HashMap<String, List<DoublePair<Class<? extends Retriever>>>> retrieverCategories;
	
	public static final int DEFAULT_THREAD_POOL_SIZE = 4;
	public static final int DEFAULT_TASK_QUEUE_SIZE = 10;
	public static final int DEFAULT_MAX_RESULTS = 100;
	public static final int DEFAULT_RESULTS_PER_MODULE = 50;
	public static final HashMap<String, List<DoublePair<Class<? extends Retriever>>>> DEFAULT_RETRIEVER_CATEGORIES = new HashMap<>();
	
	static{
		
		List<DoublePair<Class<? extends Retriever>>> list;
		
		list = new ArrayList<>(6);
		list.add(DoublePair.pair(AverageColor.class,				2.3));
		list.add(DoublePair.pair(DominantColors.class,				1.0));
		list.add(DoublePair.pair(MedianColor.class,					1.2));
		list.add(DoublePair.pair(QueryImageExporter.class, 0.0001));
		list.add(DoublePair.pair(AverageFuzzyHist.class,      0.7));
		list.add(DoublePair.pair(MedianFuzzyHist.class,       1.3));
		DEFAULT_RETRIEVER_CATEGORIES.put("globalcolor", list);

		list = new ArrayList<>(13);
		list.add(DoublePair.pair(AverageColorARP44.class, 			0.5));
		list.add(DoublePair.pair(MedianColorARP44.class, 			0.85));
		list.add(DoublePair.pair(SubDivAverageFuzzyColor.class, 	0.5));
		list.add(DoublePair.pair(SubDivMedianFuzzyColor.class, 		0.85));
		list.add(DoublePair.pair(AverageColorGrid8.class, 			1.8));
		list.add(DoublePair.pair(ChromaGrid8.class, 				0.95));
		list.add(DoublePair.pair(SaturationGrid8.class, 			0.65));
		list.add(DoublePair.pair(AverageColorCLD.class, 			1.4));
		list.add(DoublePair.pair(CLD.class, 						1.3));
		list.add(DoublePair.pair(HueValueVarianceGrid8.class, 		0.85));
		list.add(DoublePair.pair(MedianColorGrid8.class, 			1.7));
		list.add(DoublePair.pair(AverageColorRaster.class, 			1.0));
		list.add(DoublePair.pair(MedianColorRaster.class, 			1.0));
		DEFAULT_RETRIEVER_CATEGORIES.put("localcolor", list);
		
		list = new ArrayList<>(7);
		list.add(DoublePair.pair(EdgeARP88.class, 					0.85));
		list.add(DoublePair.pair(EdgeGrid16.class, 					1.15));
		list.add(DoublePair.pair(EdgeARP88Full.class, 				0.85));
		list.add(DoublePair.pair(EdgeGrid16Full.class, 				0.85));
		list.add(DoublePair.pair(EHD.class, 						0.7));
		list.add(DoublePair.pair(DominantEdgeGrid16.class, 			1.4));
		list.add(DoublePair.pair(DominantEdgeGrid8.class, 			1.4));
		DEFAULT_RETRIEVER_CATEGORIES.put("edge", list);
		
		list = new ArrayList<>(9);
		list.add(DoublePair.pair(MotionHistogram.class, 			0.5));
		list.add(DoublePair.pair(SubDivMotionHistogram2.class, 		1.0));
		list.add(DoublePair.pair(SubDivMotionHistogram3.class, 		1.0));
		list.add(DoublePair.pair(SubDivMotionHistogram4.class, 		1.0));
		list.add(DoublePair.pair(SubDivMotionHistogram5.class, 		1.0));
		list.add(DoublePair.pair(SubDivMotionSum2.class, 			0.5));
		list.add(DoublePair.pair(SubDivMotionSum3.class, 			0.5));
		list.add(DoublePair.pair(SubDivMotionSum4.class, 			0.5));
		list.add(DoublePair.pair(SubDivMotionSum5.class, 			0.5));
		DEFAULT_RETRIEVER_CATEGORIES.put("motion", list);

		list = new ArrayList<>(1);
		list.add(DoublePair.pair(NeuralNetVGG16Feature.class, 1.0));
		DEFAULT_RETRIEVER_CATEGORIES.put("neuralnet", list);
		
		list = new ArrayList<>(3);
		list.add(DoublePair.pair(SubtitleFulltextSearch.class,    1.0));
		list.add(DoublePair.pair(DescriptionTextSearch.class,    1.0));
		
//		list.add(DoublePair.pair(QueryImageExporter.class, 			0.001));
		DEFAULT_RETRIEVER_CATEGORIES.put("meta", list);
	}
	
	public RetrieverConfig(){
		this(DEFAULT_THREAD_POOL_SIZE, DEFAULT_TASK_QUEUE_SIZE, DEFAULT_MAX_RESULTS, DEFAULT_RESULTS_PER_MODULE, DEFAULT_RETRIEVER_CATEGORIES);
	}
	
	public RetrieverConfig(int threadPoolSize, int taskQueueSize, int maxResults, int resultsPerModule, HashMap<String, List<DoublePair<Class<? extends Retriever>>>> retrieverCategories){
		this.threadPoolSize = threadPoolSize;
		this.taskQueueSize = taskQueueSize;
		this.maxResults = maxResults;
		this.resultsPerModule = resultsPerModule;
		this.retrieverCategories = retrieverCategories;
	}
	
	public int getThreadPoolSize(){
		return this.threadPoolSize;
	}

	public int getTaskQueueSize() {
		return this.taskQueueSize;
	}
	
	public int getMaxResults(){
		return this.maxResults;
	}
	
	public int getMaxResultsPerModule(){
		return this.resultsPerModule;
	}
	
	public TObjectDoubleHashMap<Retriever> getRetrieversByCategory(String category){
		List<DoublePair<Class<? extends Retriever>>> list = this.retrieverCategories.get(category);
		if(list == null){
			return new TObjectDoubleHashMap<>(1);
		}
		
		TObjectDoubleHashMap<Retriever> _return = new TObjectDoubleHashMap<>(list.size());
		for(DoublePair<Class<? extends Retriever>> pair : list){
			Retriever rev = ReflectionHelper.instanciate(pair.key);
			if(rev != null){
				_return.put(rev, pair.value);
			}
		}
		
		return _return;
	}
	
	public List<String> getRetrieverCategories(){
		Set<String> keys = this.retrieverCategories.keySet();
		ArrayList<String> _return = new ArrayList<>(keys.size());
		_return.addAll(keys);
		return _return;
	}
	
	/**
	 * 
	 * expects a json object of the following form:
	 * <pre>
	 * {
	 * 	"threadPoolSize" : (int)
	 * 	"taskQueueSize" : (int)
	 * 	"maxResults" : (int)
	 * 	"resultsPerModule" : (int)
	 *  "features" : {
	 *  	category_name: [ ... ]  
	 *  }
	 * }
	 * </pre>
	 * @throws NullPointerException in case provided JsonObject is null
	 * @throws IllegalArgumentException if any of the specified parameters does not have the expected type or is outside the valid range
	 */
	public static RetrieverConfig parse(JsonObject obj) throws NullPointerException, IllegalArgumentException{
		if(obj == null){
			throw new NullPointerException("JsonObject was null");
		}
		
		int threadPoolSize = DEFAULT_THREAD_POOL_SIZE;
		if(obj.get("threadPoolSize") != null){
			try{
				threadPoolSize = obj.get("threadPoolSize").asInt();
			}catch(UnsupportedOperationException e){
				throw new IllegalArgumentException("'threadPoolSize' was not an integer in retriever configuration");
			}
			
			if(threadPoolSize <= 0){
				throw new IllegalArgumentException("'threadPoolSize' must be > 0");
			}
		}
		
		int taskQueueSize = DEFAULT_TASK_QUEUE_SIZE;
		if(obj.get("taskQueueSize") != null){
			try{
				taskQueueSize = obj.get("taskQueueSize").asInt();
			}catch(UnsupportedOperationException e){
				throw new IllegalArgumentException("'taskQueueSize' was not an integer in retriever configuration");
			}
			
			if(taskQueueSize <= 0){
				throw new IllegalArgumentException("'threadPoolSize' must be > 0");
			}
		}
		
		int maxResults = DEFAULT_MAX_RESULTS;
		if(obj.get("maxResults") != null){
			try{
				maxResults = obj.get("maxResults").asInt();
			}catch(UnsupportedOperationException e){
				throw new IllegalArgumentException("'maxResults' was not an integer in retriever configuration");
			}
			
			if(maxResults <= 0){
				throw new IllegalArgumentException("'maxResults' must be > 0");
			}
		}
		
		int resultsPerModule = DEFAULT_RESULTS_PER_MODULE;
		if(obj.get("resultsPerModule") != null){
			try{
				resultsPerModule = obj.get("resultsPerModule").asInt();
			}catch(UnsupportedOperationException e){
				throw new IllegalArgumentException("'resultsPerModule' was not an integer in retriever configuration");
			}
			
			if(resultsPerModule <= 0){
				throw new IllegalArgumentException("'resultsPerModule' must be > 0");
			}
		}
		
		HashMap<String, List<DoublePair<Class<? extends Retriever>>>> retrieverCategories = DEFAULT_RETRIEVER_CATEGORIES;
		if(obj.get("features") != null){
			try{
				JsonObject features = obj.get("features").asObject();
				HashMap<String, List<DoublePair<Class<? extends Retriever>>>> map = new HashMap<>();
				for(String category : features.names()){
					try{
						ArrayList<DoublePair<Class<? extends Retriever>>> list = parseRetrieverCategory(features.get(category).asArray());
						map.put(category, list);
					}catch(UnsupportedOperationException notAnArray){
						throw new IllegalArgumentException("not an array in retreiver config > features > " + category);
					}
				}
				retrieverCategories = map;
			}catch(UnsupportedOperationException notAnObject){
				throw new IllegalArgumentException("'features' was not an object in retriever configuration");
			}
		}
		
		return new RetrieverConfig(threadPoolSize, taskQueueSize, maxResults, resultsPerModule, retrieverCategories);
	}
	
	private static ArrayList<DoublePair<Class<? extends Retriever>>> parseRetrieverCategory(JsonArray jarr){
		if(jarr == null){
			return null;
		}
		
		ArrayList<DoublePair<Class<? extends Retriever>>> _return = new ArrayList<>(jarr.size());
		
		HashSet<Class<Retriever>> classes = new HashSet<>(); //for de-duplication
		
		for(JsonValue jval : jarr){
			try{
				JsonObject jobj = jval.asObject();
				if(jobj.get("feature") == null){
					continue;
				}
				Class<Retriever> c = null;
				try {
				  if(jobj.get("feature").isString()){
				    c = ReflectionHelper.getClassFromName(jobj.get("feature").asString(), Retriever.class, ReflectionHelper.FEATURE_MODULE_PACKAGE);
				  }else{
				    c = ReflectionHelper.getClassFromJson(jobj.get("feature").asObject(), Retriever.class, ReflectionHelper.FEATURE_MODULE_PACKAGE);
				  }
					
				} catch (IllegalArgumentException | ClassNotFoundException | InstantiationException | UnsupportedOperationException e) {
					//ignore at this point
				}
				
				if(c == null || classes.contains(c)){
					continue;
				}
				
				double weight = 1d;
				if(jobj.get("weight") != null){
					try{
						weight = jobj.get("weight").asDouble();
					}catch(UnsupportedOperationException e){
						//ignore
					}
				}
				
				_return.add(new DoublePair<Class<? extends Retriever>>(c, weight));
				classes.add(c);
				
			}catch(UnsupportedOperationException notAnObject){
				Logger.warn("entry in feature list was not an object, ignoring");
			}
		}
		
		
		return _return;
	}
}
