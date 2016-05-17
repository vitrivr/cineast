package ch.unibas.cs.dbis.cineast.core.util;

import java.util.ArrayList;
import java.util.List;

import ch.unibas.cs.dbis.cineast.api.API;
import ch.unibas.cs.dbis.cineast.core.config.QueryConfig;
import ch.unibas.cs.dbis.cineast.core.data.QueryContainer;
import ch.unibas.cs.dbis.cineast.core.data.StringDoublePair;
import ch.unibas.cs.dbis.cineast.core.features.retriever.Retriever;
import ch.unibas.cs.dbis.cineast.core.runtime.ContinousQueryDispatcher;
import gnu.trove.map.hash.TObjectDoubleHashMap;

public class ContinousRetrievalLogic {

	public static List<StringDoublePair> retrieve(QueryContainer qc, String category, QueryConfig config) {
		TObjectDoubleHashMap<Retriever> retrievers = API.getRetrieversByCategory(category);
		if(retrievers.isEmpty()){
			return new ArrayList<StringDoublePair>(1);
		}
		return ContinousQueryDispatcher.retirieve(qc, retrievers, API.getInitializer(), config);
	}

	public static List<StringDoublePair> retrieve(String id, String category, QueryConfig config) {
		TObjectDoubleHashMap<Retriever> retrievers = API.getRetrieversByCategory(category);
		if(retrievers.isEmpty()){
			return new ArrayList<StringDoublePair>(1);
		}
		return ContinousQueryDispatcher.retirieve(id, retrievers, API.getInitializer(), config);
	}
	
	public static void shutdown(){
		ContinousQueryDispatcher.shutdown();
	}

}
