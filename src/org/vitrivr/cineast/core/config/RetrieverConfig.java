package org.vitrivr.cineast.core.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.vitrivr.cineast.core.config.deserializers.FeatureCategoriesDeserializer;
import org.vitrivr.cineast.core.data.DoublePair;
import org.vitrivr.cineast.core.features.AverageColor;
import org.vitrivr.cineast.core.features.AverageColorARP44;
import org.vitrivr.cineast.core.features.AverageColorCLD;
import org.vitrivr.cineast.core.features.AverageColorGrid8;
import org.vitrivr.cineast.core.features.AverageColorRaster;
import org.vitrivr.cineast.core.features.AverageFuzzyHist;
import org.vitrivr.cineast.core.features.CLD;
import org.vitrivr.cineast.core.features.ChromaGrid8;
import org.vitrivr.cineast.core.features.DescriptionTextSearch;
import org.vitrivr.cineast.core.features.DominantColors;
import org.vitrivr.cineast.core.features.DominantEdgeGrid16;
import org.vitrivr.cineast.core.features.DominantEdgeGrid8;
import org.vitrivr.cineast.core.features.EHD;
import org.vitrivr.cineast.core.features.EdgeARP88;
import org.vitrivr.cineast.core.features.EdgeARP88Full;
import org.vitrivr.cineast.core.features.EdgeGrid16;
import org.vitrivr.cineast.core.features.EdgeGrid16Full;
import org.vitrivr.cineast.core.features.HueValueVarianceGrid8;
import org.vitrivr.cineast.core.features.MedianColor;
import org.vitrivr.cineast.core.features.MedianColorARP44;
import org.vitrivr.cineast.core.features.MedianColorGrid8;
import org.vitrivr.cineast.core.features.MedianColorRaster;
import org.vitrivr.cineast.core.features.MedianFuzzyHist;
import org.vitrivr.cineast.core.features.MotionHistogram;
import org.vitrivr.cineast.core.features.SaturationGrid8;
import org.vitrivr.cineast.core.features.SubDivAverageFuzzyColor;
import org.vitrivr.cineast.core.features.SubDivMedianFuzzyColor;
import org.vitrivr.cineast.core.features.SubDivMotionHistogram2;
import org.vitrivr.cineast.core.features.SubDivMotionHistogram3;
import org.vitrivr.cineast.core.features.SubDivMotionHistogram4;
import org.vitrivr.cineast.core.features.SubDivMotionHistogram5;
import org.vitrivr.cineast.core.features.SubDivMotionSum2;
import org.vitrivr.cineast.core.features.SubDivMotionSum3;
import org.vitrivr.cineast.core.features.SubDivMotionSum4;
import org.vitrivr.cineast.core.features.SubDivMotionSum5;
import org.vitrivr.cineast.core.features.SubtitleFulltextSearch;
import org.vitrivr.cineast.core.features.exporter.QueryImageExporter;
import org.vitrivr.cineast.core.features.neuralnet.classification.tf.NeuralNetVGG16Feature;
import org.vitrivr.cineast.core.features.retriever.Retriever;
import org.vitrivr.cineast.core.util.ReflectionHelper;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import gnu.trove.map.hash.TObjectDoubleHashMap;

public final class RetrieverConfig {
	private static final HashMap<String, List<DoublePair<Class<? extends Retriever>>>> DEFAULT_RETRIEVER_CATEGORIES = new HashMap<>();

	private int threadPoolSize = 4;
	private int taskQueueSize = 10;
	private int maxResults = 100;
	private int resultsPerModule = 50;
	private HashMap<String, List<DoublePair<Class<? extends Retriever>>>> retrieverCategories = DEFAULT_RETRIEVER_CATEGORIES;
	

	
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

	@JsonCreator
	public RetrieverConfig() {

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
	@JsonDeserialize(contentUsing = FeatureCategoriesDeserializer.class)
	public List<String> getRetrieverCategories(){
		Set<String> keys = this.retrieverCategories.keySet();
		ArrayList<String> _return = new ArrayList<>(keys.size());
		_return.addAll(keys);
		return _return;
	}
	public void setRetrieverCategories(HashMap<String, List<DoublePair<Class<? extends Retriever>>>> retrieverCategories) {
		this.retrieverCategories = retrieverCategories;
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

  public Optional<Retriever> getRetrieverByName(String retrieverName) {
    for (List<DoublePair<Class<? extends Retriever>>> pair : this.retrieverCategories
        .values()) {
      for (DoublePair<Class<? extends Retriever>> retrieverPair : pair) {
        if (retrieverPair.key.getSimpleName().equals(retrieverName)) {
          Retriever retriever = ReflectionHelper.instanciate(retrieverPair.key);
          if (retriever != null) {
            return Optional.of(retriever);
          }
        }
      }
    }
    return Optional.empty();
  }
}
