package ch.unibas.cs.dbis.cineast.core.runtime;

import java.util.List;
import java.util.concurrent.Callable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ch.unibas.cs.dbis.cineast.core.data.LongDoublePair;
import ch.unibas.cs.dbis.cineast.core.data.Pair;
import ch.unibas.cs.dbis.cineast.core.data.QueryContainer;
import ch.unibas.cs.dbis.cineast.core.features.retriever.Retriever;

public class RetrievalTask implements Callable<Pair<Retriever, List<LongDoublePair>>> {

	private Retriever retriever;
	private QueryContainer query = null;
	private long shotId = -1;
	private String resultCacheName = null;
	private static final Logger LOGGER = LogManager.getLogger();
		
	private RetrievalTask(Retriever retriever){
		this.retriever = retriever;
	}
	
	public RetrievalTask(Retriever retriever, QueryContainer query, String resultCacheName) {
		this(retriever);
		this.query = query;
		this.resultCacheName = resultCacheName;
	}
	
	public RetrievalTask(Retriever retriever, QueryContainer query) {
		this(retriever, query, null);
	}
	
	public RetrievalTask(Retriever retriever, long shotId, String resultCacheName) {
		this(retriever);
		this.shotId = shotId;
		this.resultCacheName = resultCacheName;

	}
	
	public RetrievalTask(Retriever retriever, long shotId){
		this(retriever, shotId, null);
	}
	
	@Override
	public Pair<Retriever, List<LongDoublePair>> call() throws Exception {
		LOGGER.entry();
		LOGGER.debug("starting {}", retriever.getClass().getSimpleName());
		List<LongDoublePair> result;
		if(this.query == null){
			if(this.resultCacheName != null){
				result = this.retriever.getSimilar(this.shotId, this.resultCacheName);
			}else{
				result = this.retriever.getSimilar(this.shotId);
			}
			
		}else{
			if(this.resultCacheName != null){
				result = this.retriever.getSimilar(this.query, this.resultCacheName);

			}else{
				result = this.retriever.getSimilar(this.query);
			}
		}
		return LOGGER.exit(new Pair<Retriever, List<LongDoublePair>>(this.retriever, result));
	}


}
