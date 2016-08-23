package org.vitrivr.cineast.core.util;

import java.util.ArrayList;
import java.util.List;

import org.vitrivr.cineast.api.API;
import org.vitrivr.cineast.core.config.Config;
import org.vitrivr.cineast.core.config.QueryConfig;
import org.vitrivr.cineast.core.data.QueryContainer;
import org.vitrivr.cineast.core.data.StringDoublePair;
import org.vitrivr.cineast.core.features.retriever.Retriever;
import org.vitrivr.cineast.core.runtime.ContinousQueryDispatcher;

import gnu.trove.map.hash.TObjectDoubleHashMap;

public class ContinousRetrievalLogic {

	public static List<StringDoublePair> retrieve(QueryContainer qc, String category, QueryConfig config) {
		TObjectDoubleHashMap<Retriever> retrievers = Config.getRetrieverConfig().getRetrieversByCategory(category);
		if(retrievers.isEmpty()){
			return new ArrayList<StringDoublePair>(1);
		}
		return ContinousQueryDispatcher.retrieve(qc, retrievers, API.getInitializer(), config);
	}

	public static List<StringDoublePair> retrieve(String id, String category, QueryConfig config) {
		TObjectDoubleHashMap<Retriever> retrievers = Config.getRetrieverConfig().getRetrieversByCategory(category);
		if(retrievers.isEmpty()){
			return new ArrayList<StringDoublePair>(1);
		}
		return ContinousQueryDispatcher.retrieve(id, retrievers, API.getInitializer(), config);
	}
	
	public static void shutdown(){
		ContinousQueryDispatcher.shutdown();
	}

}
