package org.vitrivr.cineast.core.runtime;

import java.util.List;
import java.util.concurrent.Callable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.config.QueryConfig;
import org.vitrivr.cineast.core.data.Pair;
import org.vitrivr.cineast.core.data.StringDoublePair;
import org.vitrivr.cineast.core.data.query.containers.QueryContainer;
import org.vitrivr.cineast.core.features.retriever.Retriever;

public class RetrievalTask implements Callable<Pair<Retriever, List<StringDoublePair>>> {

	private Retriever retriever;
	private QueryContainer query = null;
	private String shotId = null;
	private static final Logger LOGGER = LogManager.getLogger();
	private QueryConfig config = null; //TODO
		
	private RetrievalTask(Retriever retriever){
		this.retriever = retriever;
	}
	
	public RetrievalTask(Retriever retriever, QueryContainer query, QueryConfig qc) {
		this(retriever);
		this.query = query;
		this.config = qc;
	}
	
	public RetrievalTask(Retriever retriever, QueryContainer query) {
		this(retriever, query, null);
	}

	/**
	 *
	 * @param retriever
	 * @param segmentId
	 * @param qc
	 */
	public RetrievalTask(Retriever retriever, String segmentId, QueryConfig qc) {
		this(retriever);
		this.shotId = segmentId;
		this.config = qc;

	}

	/**
	 *
	 * @param retriever
	 * @param segmentId
	 */
	public RetrievalTask(Retriever retriever, String segmentId){
		this(retriever, segmentId, null);
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
