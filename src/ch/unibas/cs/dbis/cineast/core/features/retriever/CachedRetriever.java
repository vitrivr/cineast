package ch.unibas.cs.dbis.cineast.core.features.retriever;

import java.util.HashMap;
import java.util.List;

import ch.unibas.cs.dbis.cineast.core.config.QueryConfig;
import ch.unibas.cs.dbis.cineast.core.data.LongDoublePair;
import ch.unibas.cs.dbis.cineast.core.data.SegmentContainer;
import ch.unibas.cs.dbis.cineast.core.data.StringDoublePair;
import ch.unibas.cs.dbis.cineast.core.db.DBSelector;
import gnu.trove.map.hash.TLongObjectHashMap;

public class CachedRetriever implements Retriever {

	private Retriever retriever;
	private HashMap<SegmentContainer, List<LongDoublePair>> resultCache = new HashMap<SegmentContainer, List<LongDoublePair>>();
	private TLongObjectHashMap<List<LongDoublePair>> longResultCache = new TLongObjectHashMap<List<LongDoublePair>>();
	private DBSelector selector = null;
	
	public CachedRetriever(Retriever retriever){
		this.retriever = retriever;
	}
	
	@Override
	public void init(DBSelector selector) {
		this.selector = selector;
	}

//	@Override
//	public List<LongDoublePair> getSimilar(SegmentContainer qc) {
//		if(this.resultCache.containsKey(qc)){
//			return this.resultCache.get(qc);
//		}
//		this.retriever.init(this.selector);
//		List<LongDoublePair> result = this.retriever.getSimilar(qc);
//		this.resultCache.put(qc, result);
//		return result;
//	}
//
//	@Override
//	public List<LongDoublePair> getSimilar(long shotId) {
//		if(this.longResultCache.containsKey(shotId)){
//			return this.longResultCache.get(shotId);
//		}
//		this.retriever.init(this.selector);
//		List<LongDoublePair> result = this.retriever.getSimilar(shotId);
//		this.longResultCache.put(shotId, result);
//		return result;
//	}

	@Override
	public void finish() {
		this.retriever.finish();
	}
	
	public Retriever getRetriever(){
		return this.retriever;
	}

	@Override
	public List<StringDoublePair> getSimilar(SegmentContainer sc, QueryConfig qc) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<StringDoublePair> getSimilar(String shotId, QueryConfig qc) {
		// TODO Auto-generated method stub
		return null;
	}

}
