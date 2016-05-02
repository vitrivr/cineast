package ch.unibas.cs.dbis.cineast.core.runtime;

import java.util.List;
import java.util.concurrent.Callable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ch.unibas.cs.dbis.cineast.core.config.QueryConfig;
import ch.unibas.cs.dbis.cineast.core.data.Pair;
import ch.unibas.cs.dbis.cineast.core.data.QueryContainer;
import ch.unibas.cs.dbis.cineast.core.data.StringDoublePair;
import ch.unibas.cs.dbis.cineast.core.features.retriever.Retriever;

public class RetrievalTask implements Callable<Pair<Retriever, List<StringDoublePair>>> {

	private Retriever retriever;
	private QueryContainer query = null;
	private long shotId = -1;
	private String resultCacheName = null;
	private static final Logger LOGGER = LogManager.getLogger();
	private QueryConfig config = null; //TODO
		
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
	public Pair<Retriever, List<StringDoublePair>> call() throws Exception {
		LOGGER.entry();
		LOGGER.debug("starting {}", retriever.getClass().getSimpleName());
		List<StringDoublePair> result;
		if(this.query == null){
			result = this.retriever.getSimilar(this.shotId, this.config);
		}else{
			result = this.retriever.getSimilar(this.query, this.config);
		}
		return LOGGER.exit(new Pair<Retriever, List<StringDoublePair>>(this.retriever, result));
	}


}
