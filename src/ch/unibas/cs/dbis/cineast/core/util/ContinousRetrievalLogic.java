package ch.unibas.cs.dbis.cineast.core.util;

import gnu.trove.map.hash.TObjectDoubleHashMap;

import java.util.ArrayList;
import java.util.List;

import ch.unibas.cs.dbis.cineast.api.API;
import ch.unibas.cs.dbis.cineast.core.data.LongDoublePair;
import ch.unibas.cs.dbis.cineast.core.data.QueryContainer;
import ch.unibas.cs.dbis.cineast.core.features.retriever.Retriever;
import ch.unibas.cs.dbis.cineast.core.runtime.ContinousQueryDispatcher;

public class ContinousRetrievalLogic {

	public static List<LongDoublePair> retrieve(QueryContainer qc, String category, String resultCacheName) {
		TObjectDoubleHashMap<Retriever> retrievers = API.getRetrieversByCategory(category);
		if(retrievers.isEmpty()){
			return new ArrayList<LongDoublePair>(1);
		}
		return ContinousQueryDispatcher.retirieve(qc, retrievers, API.getInitializer(), resultCacheName);
	}

	public static List<LongDoublePair> retrieve(long id, String category, String resultCacheName) {
		TObjectDoubleHashMap<Retriever> retrievers = API.getRetrieversByCategory(category);
		if(retrievers.isEmpty()){
			return new ArrayList<LongDoublePair>(1);
		}
		return ContinousQueryDispatcher.retirieve(id, retrievers, API.getInitializer(), resultCacheName);
	}
	
	public static void shutdown(){
		ContinousQueryDispatcher.shutdown();
	}

}
