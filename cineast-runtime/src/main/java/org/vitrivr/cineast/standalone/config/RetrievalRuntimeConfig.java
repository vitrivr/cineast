package org.vitrivr.cineast.standalone.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import gnu.trove.map.hash.TObjectDoubleHashMap;
import org.vitrivr.cineast.core.features.*;
import org.vitrivr.cineast.core.features.exporter.QueryImageExporter;
import org.vitrivr.cineast.core.features.retriever.Retriever;
import org.vitrivr.cineast.core.util.ReflectionHelper;

import java.util.*;

public final class RetrievalRuntimeConfig {
	private static final HashMap<String, List<RetrieverConfig>> DEFAULT_RETRIEVER_CATEGORIES = new HashMap<>();

	private int threadPoolSize = 4;
	private int taskQueueSize = 10;
	private int maxResults = 100;
	private int resultsPerModule = 50;
	private HashMap<String, List<RetrieverConfig>> retrieverCategories = DEFAULT_RETRIEVER_CATEGORIES;

	static{
		
		List<RetrieverConfig> list;
		
		list = new ArrayList<>(6);
		list.add(new RetrieverConfig(AverageColor.class,				2.3));
		list.add(new RetrieverConfig(DominantColors.class,				1.0));
		list.add(new RetrieverConfig(MedianColor.class,					1.2));
		list.add(new RetrieverConfig(QueryImageExporter.class, 0.0001));
		list.add(new RetrieverConfig(AverageFuzzyHist.class,      0.7));
		list.add(new RetrieverConfig(MedianFuzzyHist.class,       1.3));
		DEFAULT_RETRIEVER_CATEGORIES.put("globalcolor", list);

		list = new ArrayList<>(13);
		list.add(new RetrieverConfig(AverageColorARP44.class, 			0.5));
		list.add(new RetrieverConfig(MedianColorARP44.class, 			0.85));
		list.add(new RetrieverConfig(SubDivAverageFuzzyColor.class, 	0.5));
		list.add(new RetrieverConfig(SubDivMedianFuzzyColor.class, 		0.85));
		list.add(new RetrieverConfig(AverageColorGrid8.class, 			1.8));
		list.add(new RetrieverConfig(ChromaGrid8.class, 				0.95));
		list.add(new RetrieverConfig(SaturationGrid8.class, 			0.65));
		list.add(new RetrieverConfig(AverageColorCLD.class, 			1.4));
		list.add(new RetrieverConfig(CLD.class, 						1.3));
		list.add(new RetrieverConfig(HueValueVarianceGrid8.class, 		0.85));
		list.add(new RetrieverConfig(MedianColorGrid8.class, 			1.7));
		list.add(new RetrieverConfig(AverageColorRaster.class, 			1.0));
		list.add(new RetrieverConfig(MedianColorRaster.class, 			1.0));
		DEFAULT_RETRIEVER_CATEGORIES.put("localcolor", list);
		
		list = new ArrayList<>(7);
		list.add(new RetrieverConfig(EdgeARP88.class, 					0.85));
		list.add(new RetrieverConfig(EdgeGrid16.class, 					1.15));
		list.add(new RetrieverConfig(EdgeARP88Full.class, 				0.85));
		list.add(new RetrieverConfig(EdgeGrid16Full.class, 				0.85));
		list.add(new RetrieverConfig(EHD.class, 						0.7));
		list.add(new RetrieverConfig(DominantEdgeGrid16.class, 			1.4));
		list.add(new RetrieverConfig(DominantEdgeGrid8.class, 			1.4));
		DEFAULT_RETRIEVER_CATEGORIES.put("edge", list);
		
		list = new ArrayList<>(9);
		list.add(new RetrieverConfig(MotionHistogram.class, 			0.5));
		list.add(new RetrieverConfig(SubDivMotionHistogram2.class, 		1.0));
		list.add(new RetrieverConfig(SubDivMotionHistogram3.class, 		1.0));
		list.add(new RetrieverConfig(SubDivMotionHistogram4.class, 		1.0));
		list.add(new RetrieverConfig(SubDivMotionHistogram5.class, 		1.0));
		list.add(new RetrieverConfig(SubDivMotionSum2.class, 			0.5));
		list.add(new RetrieverConfig(SubDivMotionSum3.class, 			0.5));
		list.add(new RetrieverConfig(SubDivMotionSum4.class, 			0.5));
		list.add(new RetrieverConfig(SubDivMotionSum5.class, 			0.5));
		DEFAULT_RETRIEVER_CATEGORIES.put("motion", list);

		list = new ArrayList<>(3);
		list.add(new RetrieverConfig(SubtitleFulltextSearch.class,    1.0));
		list.add(new RetrieverConfig(DescriptionTextSearch.class,    1.0));
		
//		list.add(new RetrieverConfig(QueryImageExporter.class, 			0.001));
		DEFAULT_RETRIEVER_CATEGORIES.put("meta", list);
	}

	@JsonCreator
	public RetrievalRuntimeConfig() {

	}

	@JsonProperty
	public int getThreadPoolSize(){
		return this.threadPoolSize;
	}
	public void setThreadPoolSize(int threadPoolSize) {
		this.threadPoolSize = threadPoolSize;
	}

	@JsonProperty
	public int getTaskQueueSize() {
		return this.taskQueueSize;
	}
	public void setTaskQueueSize(int taskQueueSize) {
		this.taskQueueSize = taskQueueSize;
	}

	@JsonProperty
	public int getMaxResults(){
		return this.maxResults;
	}
	public void setMaxResults(int maxResults) {
		this.maxResults = maxResults;
	}

	@JsonProperty
	public int getMaxResultsPerModule(){
		return this.resultsPerModule;
	}
	public void setResultsPerModule(int resultsPerModule) {
		this.resultsPerModule = resultsPerModule;
	}

	@JsonProperty("features")
	public List<String> getRetrieverCategories(){
		Set<String> keys = this.retrieverCategories.keySet();
		ArrayList<String> _return = new ArrayList<>(keys.size());
		_return.addAll(keys);
		return _return;
	}


	public TObjectDoubleHashMap<Retriever> getRetrieversByCategory(String category){
		List<RetrieverConfig> list = this.retrieverCategories.get(category);
		if(list == null){
			return new TObjectDoubleHashMap<>(1);
		}

		TObjectDoubleHashMap<Retriever> _return = new TObjectDoubleHashMap<>(list.size());
		for(RetrieverConfig config : list){

			Retriever rev;

			if(config.getProperties() == null){
			    rev = ReflectionHelper.instantiate(config.getRetrieverClass());
            } else {
                rev = ReflectionHelper.instantiate(config.getRetrieverClass(), config.getProperties());
            }

			if(rev != null){
				_return.put(rev, config.getWeight());
			}
		}

		return _return;
	}

  public Optional<Retriever> getRetrieverByName(String retrieverName) {
    for (List<RetrieverConfig> configs : this.retrieverCategories
        .values()) {
      for (RetrieverConfig config : configs) {
        if (config.getRetrieverClass().getSimpleName().equals(retrieverName)) {

            Retriever retriever;

            if(config.getProperties() == null){
                retriever = ReflectionHelper.instantiate(config.getRetrieverClass());
            } else {
                retriever = ReflectionHelper.instantiate(config.getRetrieverClass(), config.getProperties());
            }

          if (retriever != null) {
            return Optional.of(retriever);
          }
        }
      }
    }
    return Optional.empty();
  }
}
