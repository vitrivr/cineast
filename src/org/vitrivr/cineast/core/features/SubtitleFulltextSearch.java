package ch.unibas.cs.dbis.cineast.core.features;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ch.unibas.cs.dbis.cineast.core.config.QueryConfig;
import ch.unibas.cs.dbis.cineast.core.data.SegmentContainer;
import ch.unibas.cs.dbis.cineast.core.data.StringDoublePair;
import ch.unibas.cs.dbis.cineast.core.db.PersistencyWriter;
import ch.unibas.cs.dbis.cineast.core.db.PersistentTuple;
import ch.unibas.cs.dbis.cineast.core.decode.subtitle.SubtitleItem;
import ch.unibas.cs.dbis.cineast.core.features.abstracts.AbstractFeatureModule;

public class SubtitleFulltextSearch extends AbstractFeatureModule {

	private static final Logger LOGGER = LogManager.getLogger();
	
	public SubtitleFulltextSearch(){
		super("features_fulltext", 1);
	}
	
	@Override
	public void processShot(SegmentContainer shot) {
		LOGGER.entry();
		List<SubtitleItem> subiItems = shot.getSubtitleItems();
		if (!subiItems.isEmpty() && !phandler.idExists(shot.getId())) {
			StringBuffer buf = new StringBuffer();
			for(SubtitleItem si : subiItems){
				buf.append(si.getText());
				buf.append('\n');
			}
			persist(shot.getId(), buf.toString());
		}
		LOGGER.exit();
	}

	private void persist(String shotId, String s) {
		PersistentTuple tuple = this.phandler.generateTuple(shotId, s);
		this.phandler.persist(tuple);
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

	@Override
	public void init(PersistencyWriter<?> phandler) {
		super.init(phandler);
		this.phandler.setFieldNames("id", "text");
	}
	
//	@Override
//	public List<LongDoublePair> getSimilar(SegmentContainer qc) {
//		return getSimilar(qc, null);
//	}
//	
//	private List<LongDoublePair> getText(String text, int limit, String resultCacheName){
//		String query;
//		if(resultCacheName == null){
//			query = "WITH q AS ("
//				+ "SELECT plainto_tsquery('" + ADAMTuple.escape(text) + "') AS query), "
//				+ "ranked AS( "
//				+ "SELECT shotid, text, ts_rank_cd(tsv, query) AS rank "
//				+ "FROM features.fulltext, q "
//				+ "WHERE q.query @@ tsv "
//				+ "ORDER BY rank DESC "
//				+ "LIMIT " + limit + ") "
//				+ "SELECT shotid, rank "
//				+ "FROM ranked, q "
//				+ "ORDER BY ranked DESC";
//		}else{
//			query = "WITH c AS (SELECT shotid AS filter FROM cineast.resultcacheelements, cineast.resultcachenames WHERE "
//				+ "resultcacheelements.chacheid = resultcachenames.id AND resultcachenames.name = '"
//				+ resultCacheName 
//				+ "'), q AS ("
//				+ "SELECT plainto_tsquery('" + ADAMTuple.escape(text) + "') AS query), "
//				+ "ranked AS( "
//				+ "SELECT shotid, text, ts_rank_cd(tsv, query) AS rank "
//				+ "FROM features.fulltext, q, c "
//				+ "WHERE q.query @@ tsv "
//				+ "AND shotid = c.filter "
//				+ "ORDER BY rank DESC "
//				+ "LIMIT " + limit + ") "
//				+ "SELECT shotid, rank "
//				+ "FROM ranked, q "
//				+ "ORDER BY ranked DESC";
//		}
//		ResultSet rset = this.selector.select(query);
//		return manageResultSet(rset);
//	}
//
//	@Override
//	public List<LongDoublePair> getSimilar(long shotId) {
//		return getSimilar(shotId, null);		
//	}

//	@Override
//	public List<LongDoublePair> getSimilar(SegmentContainer qc, String resultCacheName) {
//		int limit = Config.getRetrieverConfig().getMaxResultsPerModule();
//		StringBuffer buf = new StringBuffer();
//		
//		List<LongDoublePair> result;
//		if(!qc.getSubtitleItems().isEmpty()){
//			for(SubtitleItem item : qc.getSubtitleItems()){
//				buf.append(item.getText());
//				buf.append('\n');
//			}
//			result = getText(buf.toString(), limit, resultCacheName);
//		}else{
//			result = new ArrayList<LongDoublePair>(1);
//		}
//		
//		return result;
//	}
//
//	@Override
//	public List<LongDoublePair> getSimilar(long shotId, String resultCacheName) {
//		int limit = Config.getRetrieverConfig().getMaxResultsPerModule();
//		ResultSet rset = this.selector.select("select text from features.fulltext where shotid = " + shotId);
//		if(rset != null){
//			try{
//				if(rset.next()){
//					String text = rset.getString(1);
//					List<LongDoublePair> result = getText(text, limit, resultCacheName);
//					return result;
//				}
//			}catch(SQLException e){
//				LOGGER.fatal(LogHelper.SQL_MARKER, LogHelper.getStackTrace(e));
//			}
//		}
//		return new ArrayList<LongDoublePair>(1);
//	}

}
